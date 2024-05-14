import java.util.ArrayList;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class Autenticacao {
    private int uid;
    private String senha;

    public Autenticacao (int userID) {
        this.uid = userID;
    }

    // Métodos para autenticação bifator do usuário no Cofre Digital

    public boolean verificaSenha(ArrayList<String[]> input){
        Database database = Database.getInstance();

        String[] senhasPossiveis = getSenhasPossiveis(input);        
        String hash = database.getHashDoUsuario(uid);
        boolean senhaVerificada = false;

        for (String senha: senhasPossiveis){
            senhaVerificada = OpenBSDBCrypt.checkPassword(hash, senha.toCharArray());
            if (senhaVerificada) {
                this.senha = senha;
                break;
            }
        }
        return senhaVerificada;
    }

    private String[] getSenhasPossiveis(ArrayList<String[]> duplas) {
        ArrayList<String> listaSenhas = new ArrayList<>();
        listaSenhas.add("");

        for (String[] dupla : duplas) {
            ArrayList<String> novasSenhas = new ArrayList<>();
            for (String senha : listaSenhas) {
                novasSenhas.add(senha + dupla[0]);
                novasSenhas.add(senha + dupla[1]);
            }
            listaSenhas = novasSenhas;
        }

        String[] arraySenhas = new String[listaSenhas.size()];
        arraySenhas = listaSenhas.toArray(arraySenhas);
        return arraySenhas;
    } 

    public boolean verificaTOTP(String inputTOTP) {
        try {
            Database database = Database.getInstance();
            byte[] chaveSecretaCodificadaBase32Cifrada = database.getChaveSecretaCriptografadaDoUsuario(uid);

            // Gerar Kaes com a senha do usuário verificada na etapa anterior 
            SecretKey chaveAES = GestorDeSeguranca.generateKaes(senha);

            // Decriptar a chave secreta com a Kaes gerada
            byte[] chaveSecretaCodificadaBase32Array = GestorDeSeguranca.decryptChave(chaveSecretaCodificadaBase32Cifrada, chaveAES);
            String chaveSecretaCodificadaBase32 = new String(chaveSecretaCodificadaBase32Array, "UTF8");

            // Verificar se o código inserido pelo usuário está correto
            TOTP totp = new TOTP(chaveSecretaCodificadaBase32, 30);
            boolean totpVerificado= totp.validateCode(inputTOTP);
            
            return totpVerificado;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

 }

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;

public class AutenticacaoTest {

    private Autenticacao autenticacao;
    private int userID;

    @Before
    public void setUp() {
        Database database = Database.getInstance();
        int uid = database.getUsuarioIfExists("user02@inf1416.puc-rio.br");
        if (uid == -1) {
            String diretorioAtual = System.getProperty("user.dir");
            String caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-pkcs8-aes.pem";
            String caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-x509.crt";
        
            Cadastro cadastro = new Cadastro(
                caminhoCertificadoDigital, 
                caminhoChavePrivada, 
                "user02", 
                2, 
                "12345678", 
                "12345678"
            );
            
            cadastro.verificaEntradasDoCadastro();
            cadastro.getDetalhesDoCertificadoDigital();
            cadastro.cadastraUsuario();
            uid = database.getUsuarioIfExists("user02@inf1416.puc-rio.br");
        }
        
        userID = uid;
        autenticacao = new Autenticacao(userID);
    }

    @Test
    public void testVerificaSenha() {
        ArrayList<String[]> inputValido = new ArrayList<>();
        inputValido.add(new String[]{"1", "0"});
        inputValido.add(new String[]{"2", "6"});
        inputValido.add(new String[]{"3", "4"});
        inputValido.add(new String[]{"2", "4"});
        inputValido.add(new String[]{"5", "7"});
        inputValido.add(new String[]{"6", "3"});
        inputValido.add(new String[]{"1", "7"});
        inputValido.add(new String[]{"8", "9"});

        boolean senhaValida = autenticacao.verificaSenha(inputValido);
        assertTrue(senhaValida);

        ArrayList<String[]> inputInvalido = new ArrayList<>();
        inputInvalido.add(new String[]{"1", "0"});
        inputInvalido.add(new String[]{"2", "6"});
        inputInvalido.add(new String[]{"3", "4"});
        inputInvalido.add(new String[]{"4", "2"});
        inputInvalido.add(new String[]{"0", "7"});  // tinha que ter um 5 na dupla
        inputInvalido.add(new String[]{"6", "3"});
        inputInvalido.add(new String[]{"7", "1"});
        inputInvalido.add(new String[]{"8", "9"});

        boolean senhaInvalida = autenticacao.verificaSenha(inputInvalido);
        assertFalse(senhaInvalida);
    }

    @Test
    public void testVerificaTOTP() {
        try {
            ArrayList<String[]> inputValido = new ArrayList<>();
            inputValido.add(new String[]{"1", "0"});
            inputValido.add(new String[]{"2", "6"});
            inputValido.add(new String[]{"3", "4"});
            inputValido.add(new String[]{"4", "2"});
            inputValido.add(new String[]{"5", "7"});
            inputValido.add(new String[]{"6", "3"});
            inputValido.add(new String[]{"7", "1"});
            inputValido.add(new String[]{"8", "9"});

            autenticacao.verificaSenha(inputValido);

            String inputTOTPinvalido = "123456";
            boolean totpInvalido = autenticacao.verificaTOTP(inputTOTPinvalido);
            assertFalse(totpInvalido);

            String inputTOTPvalido = getValidTOTP();
            System.out.println("valid TOTP: " + inputTOTPvalido);
            boolean totpValido = autenticacao.verificaTOTP(inputTOTPvalido);
            assertTrue(totpValido);

        } catch (Exception e) {
            fail("Erro ao verificar TOTP: " + e.getMessage());
        }
    }

    private String getValidTOTP() {
        try {
            Database database = Database.getInstance();
            byte[] chaveSecretaCodificadaBase32Cifrada = database.getChaveSecretaCriptografadaDoUsuario(userID);
            SecretKey chaveAES = GestorDeSeguranca.generateKaes("12345678");
            // Decriptar a chave secreta com a Kaes gerada
            byte[] chaveSecretaCodificadaBase32Array = GestorDeSeguranca.decryptChave(chaveSecretaCodificadaBase32Cifrada, chaveAES);
            String chaveSecretaCodificadaBase32 = new String(chaveSecretaCodificadaBase32Array, "UTF8");
            // Retornar o código TOTP esperado para a validação correta
            TOTP totp = new TOTP(chaveSecretaCodificadaBase32, 30);
            return totp.generateCode();
        } catch(Exception e) {
            fail("Erro ao pegar o TOTP válido: " + e.getMessage());
            return null;
        }
    }

}

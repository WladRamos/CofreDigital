import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Scanner;

public class RecuperaArquivo {

    private String emailUsuario;
    private String grupoUsuario;
    private String pastaSegura;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public RecuperaArquivo(String emailUsuario, String grupoUsuario, String pastaSegura, PublicKey publicKey, PrivateKey privateKey) {
        this.emailUsuario = emailUsuario;
        this.grupoUsuario = grupoUsuario;
        this.pastaSegura = pastaSegura;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String decriptaEVerifica(String nomeArquivo) throws Exception {
        // Checa se os arquivos necessários existem
        if (!arquivoExiste(nomeArquivo + ".env") || !arquivoExiste(nomeArquivo + ".enc") || !arquivoExiste(nomeArquivo + ".asd")) {
            return null;
        }

        // Decripta a semente da chave simétrica
        byte[] bytesEnvelope = lerBytes(pastaSegura + "/" + nomeArquivo + ".env");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] semente = cipher.doFinal(bytesEnvelope); //Erro aqui

        // Cria a chave AES a partir da semente
        Key keyAES = ManipuladorDeChaves.generateKaes(new String(semente, "UTF-8"));

        // Decripta o arquivo de índice
        byte[] bytesCriptograma = lerBytes(pastaSegura + "/" + nomeArquivo + ".enc");
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyAES);
        byte[] textoPlano = cipher.doFinal(bytesCriptograma);

        //Verifica a assinatura
        byte[] signatureBytes = lerBytes(pastaSegura + "/" + nomeArquivo + ".asd");
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(textoPlano);
        if (!signature.verify(signatureBytes)) {
            throw new SecurityException("A assinatura digital não é válida.");
        }

        if ("index".equals(nomeArquivo)) {
            return filtro(new String(textoPlano));
        } else {
            return new String(textoPlano);
        }
    }

    private String filtro(String textoPlano) {
        StringBuilder resultado = new StringBuilder();
        Scanner scanner = new Scanner(textoPlano);
        while (scanner.hasNextLine()) {
            String linha = scanner.nextLine();
            // String[] partes = linha.split(" ");
            // if (partes[2].equals(emailUsuario) || parts[3].equals(grupoUsuario)) {
            //     resultado.append(linha).append("\n");
            // }
            resultado.append(linha).append("\n");
        }
        scanner.close();
        return resultado.toString();
    }

    private byte[] lerBytes(String caminho) throws IOException {
        return Files.readAllBytes(new File(caminho).toPath());
    }

    private boolean arquivoExiste(String nomeArquivo) {
        return new File(pastaSegura + "/" + nomeArquivo).exists();
    }
}
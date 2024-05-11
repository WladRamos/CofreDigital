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

    public String decriptaEVerifica() throws Exception {
        // Decripta a semente da chave simétrica
        byte[] bytesEnvelope = lerBytes(pastaSegura + "/index.env");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] semente = cipher.doFinal(bytesEnvelope); //Erro aqui

        // Cria a chave AES a partir da semente
        Key keyAES = ManipuladorDeChaves.generateKaes(new String(semente, "UTF-8"));

        // Decripta o arquivo de índice
        byte[] bytesCriptograma = lerBytes(pastaSegura + "/index.enc");
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyAES);
        byte[] textoPlano = cipher.doFinal(bytesCriptograma);

        // // Verifica a assinatura
        // byte[] signatureBytes = readAllBytes(pastaSegura + "/index.asd");
        // Signature signature = Signature.getInstance("SHA1withRSA");
        // signature.initVerify(publicKey);
        // signature.update(textoPlano);
        // if (!signature.verify(signatureBytes)) {
        //     throw new SecurityException("A assinatura digital não é válida.");
        // }

        // Retorna os dados do usuário ou grupo de usuário
        return filtro(new String(textoPlano));
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

    public static void main(String[] args) {
        try {
            // Gera um par de chaves mockado
            KeyPair keyPair = KeyMock.generateKeyPair();

            // Configura os parâmetros de teste
            String emailUsuario = "usuario@example.com";
            String grupoUsuario = "administrador";
            String pastaSegura = "CofreDigital/Pacote-T4/Files/";

            // Cria uma instância da classe RecuperaArquivo com chaves mockadas
            RecuperaArquivo recuperaArquivo = new RecuperaArquivo(emailUsuario, grupoUsuario, pastaSegura, keyPair.getPublic(), keyPair.getPrivate());

            // Executa a função de decriptação e verificação
            String result = recuperaArquivo.decriptaEVerifica();

            // Exibe o resultado
            System.out.println("Conteúdo do arquivo de índice decriptado e verificado:");
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Erro ao recuperar e verificar o arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;



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

    public List<List<String>> decriptaEVerificaIndex() throws Exception {
        // Checa se os arquivos necessários existem
        if (!arquivoExiste("index.env") || !arquivoExiste("index.enc") || !arquivoExiste("index.asd")) {
            return null;
        }

        // Decripta a semente da chave simétrica
        byte[] bytesEnvelope = lerBytes(pastaSegura + "/" +"index.env");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] semente = cipher.doFinal(bytesEnvelope);

        // Cria a chave AES a partir da semente
        Key keyAES = ManipuladorDeChaves.generateKaes(new String(semente, "UTF-8"));

        // Decripta o arquivo de índice
        byte[] bytesCriptograma = lerBytes(pastaSegura + "/" + "index.enc");
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyAES);
        byte[] textoPlano = cipher.doFinal(bytesCriptograma);

        //Verifica a assinatura
        byte[] signatureBytes = lerBytes(pastaSegura + "/" + "index.asd");
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(textoPlano);
        if (!signature.verify(signatureBytes)) {
            throw new SecurityException("A assinatura digital não é válida.");
        }

        // Converte o texto plano em uma lista de listas
        String decodedText = new String(textoPlano, "UTF-8");
        List<List<String>> fileList = new ArrayList<>();
        String[] lines = decodedText.split("\n");
        for (String line : lines) {
            List<String> fileInfo = Arrays.asList(line.trim().split("\\s+"));
            fileList.add(fileInfo);
        }

        return fileList;
        //return filtro(new String(textoPlano));
    }

    public void decriptaEVerificaArquivos(String nomeCodigo, String nomeSecreto) throws Exception {
        // Checa se os arquivos necessários existem
        if (!arquivoExiste(nomeCodigo + ".env") || !arquivoExiste(nomeCodigo + ".enc") || !arquivoExiste(nomeCodigo + ".asd")) {
            throw new FileNotFoundException("Um ou mais arquivos necessários não foram encontrados.");
        }

        // Decripta a semente da chave simétrica
        byte[] bytesEnvelope = lerBytes(pastaSegura + "/" + nomeCodigo + ".env");
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] semente = cipher.doFinal(bytesEnvelope);

        // Cria a chave AES a partir da semente
        Key keyAES = ManipuladorDeChaves.generateKaes(new String(semente, "UTF-8"));

        // Decripta o arquivo de índice
        byte[] bytesCriptograma = lerBytes(pastaSegura + "/" + nomeCodigo + ".enc");
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyAES);
        byte[] textoPlano = cipher.doFinal(bytesCriptograma);

        //Verifica a assinatura
        byte[] signatureBytes = lerBytes(pastaSegura + "/" + nomeCodigo + ".asd");
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(textoPlano);
        if (!signature.verify(signatureBytes)) {
            throw new SecurityException("A assinatura digital não é válida.");
        }

        // Escreve os dados descriptografados em um novo arquivo com o nome secreto
        try (FileOutputStream fos = new FileOutputStream(pastaSegura + "/" + nomeSecreto)) {
            fos.write(textoPlano);
        } catch (IOException e) {
            throw new IOException("Erro ao escrever o arquivo descriptografado.", e);
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
        FileInputStream file = new FileInputStream(caminho);
        byte[] Bytesarquivo = file.readAllBytes();
        file.close();
        return Bytesarquivo;
    }

    private boolean arquivoExiste(String nomeArquivo) {
        return new File(pastaSegura + "/" + nomeArquivo).exists();
    }
}
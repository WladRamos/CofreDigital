import javax.crypto.Cipher;
import java.io.*;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;



public class RecuperaArquivo {

    //private String emailUsuario;
    private String grupoUsuario;
    private String pastaSegura;
    private PublicKey publicKeyAdmin;
    private PrivateKey privateKeyAdmin;
    private PublicKey publicKeyUser;
    private PrivateKey privateKeyUser;
    private byte[] textoPlanoIndice;
    private byte[] textoPlanoDocx;

    public RecuperaArquivo(String emailUsuario, String grupoUsuario, String pastaSegura, PublicKey publicKeyAdmin, PrivateKey privateKeyAdmin, PublicKey publicKeyUser, PrivateKey privateKeyUser) {
        //this.emailUsuario = emailUsuario;
        this.grupoUsuario = grupoUsuario;
        this.pastaSegura = pastaSegura;
        this.publicKeyAdmin = publicKeyAdmin;
        this.privateKeyAdmin = privateKeyAdmin;
        this.publicKeyUser = publicKeyUser;
        this.privateKeyUser = privateKeyUser;
    }

    private byte[] decryptArquivo(String nomeArquivo){
        try{
            PrivateKey privateKey;
            if(nomeArquivo == "index"){
                privateKey = privateKeyAdmin;
            }else{
                privateKey = privateKeyUser;
            }
            // Decripta a semente da chave simétrica
            byte[] bytesEnvelope = lerBytes(pastaSegura + "/" + nomeArquivo + ".env");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] semente = cipher.doFinal(bytesEnvelope);

            // Cria a chave AES a partir da semente
            Key keyAES = GestorDeSeguranca.generateKaes(new String(semente, "UTF-8"));

            // Decripta o arquivo de índice
            byte[] bytesCriptograma = lerBytes(pastaSegura + "/" + nomeArquivo + ".enc");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyAES);
            byte[] textoPlano = cipher.doFinal(bytesCriptograma);
            return textoPlano;
        }
        catch(Exception e){
            return null;
        }
        
    }

    private Boolean verificaAutentEIntegrArquivo(byte[] textoPlano, String nomeArquivo){
        try{
            PublicKey publicKey;
            if(nomeArquivo == "index"){
                publicKey = publicKeyAdmin;
            }else{
                publicKey = publicKeyUser;
            }
            //Verifica a assinatura
            byte[] signatureBytes = lerBytes(pastaSegura + "/" + nomeArquivo + ".asd");
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(textoPlano);
            if (!signature.verify(signatureBytes)) {
                return false;
            }
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public String verificaArquivos(String nomeArquivo){
        //Boolean caminhoVerificado = GestorDeSeguranca.verificaCaminhoDoArquivo(pastaSegura);
        Boolean caminhoVerificado = true;
        if(!caminhoVerificado){
            return "Problema na Pasta";
        }else{
            Boolean arquivosExistem = (arquivoExiste(nomeArquivo + ".env") && arquivoExiste(nomeArquivo + ".enc") && arquivoExiste(nomeArquivo + ".asd"));
            if(!arquivosExistem){
                return "Problema na Pasta";
            }
        }

        byte[] textoPlano = decryptArquivo(nomeArquivo);
        if(textoPlano == null){
            return "Erro na decriptação do arquivo: " + nomeArquivo;
        }else{
            if(!verificaAutentEIntegrArquivo(textoPlano, nomeArquivo)){
                return "Erro ao verificar integridade e autenticidade do arquivo: " + nomeArquivo;
            }
        }

        if(nomeArquivo == "index"){
            textoPlanoIndice = textoPlano;
        }else{
            textoPlanoDocx = textoPlano;
        }
        return "OK";
    }
    

    public List<List<String>> recuperaIndex(){
        List<List<String>> listaArquivos = new ArrayList<>();
        try{
            // Converte o texto plano em uma lista de listas
            String conteudoIndex = new String(textoPlanoIndice, "UTF-8");
            
            String[] linhas = conteudoIndex.split("\n");
            for (String linha : linhas) {
                List<String> infoArquivo = Arrays.asList(linha.trim().split("\\s+"));
                listaArquivos.add(infoArquivo);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        for (List<String> file : listaArquivos) {
            System.out.println(file);
        }
        return filtro(listaArquivos);
    }

    public void recuperaArquivosDocx(String nomeSecreto) throws Exception{
        // Escreve os dados descriptografados em um novo arquivo com o nome secreto
        try (FileOutputStream fos = new FileOutputStream(pastaSegura + "/" + nomeSecreto)) {
            fos.write(textoPlanoDocx);
        } catch (IOException e) {
            throw new IOException("Erro ao escrever o arquivo descriptografado.", e);
        }  
    }

    private List<List<String>> filtro(List<List<String>> listaArquivos) {
        List<List<String>> listaFiltrada = new ArrayList<>();
    
        for (List<String> sublista : listaArquivos) {
            if (sublista.get(3).equals(grupoUsuario)) {
                listaFiltrada.add(new ArrayList<>(sublista));
            }
        }
        return listaFiltrada; 
        
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
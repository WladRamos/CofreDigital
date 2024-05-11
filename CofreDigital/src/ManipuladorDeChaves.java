import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.*;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class ManipuladorDeChaves {

    public static String generateChaveSecreta() {
        SecureRandom secureRandom = new SecureRandom();
        // O código TOTP é formada por 20 bytes aleatórios        
        byte[] bytesAleatorios = new byte[20];
        secureRandom.nextBytes(bytesAleatorios);

        // Codificar em Base32
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        String chaveSecretaCodificadaBase32 = base32.toString(bytesAleatorios);

        return chaveSecretaCodificadaBase32;
    } 

    public static void generateQRcodeDaChaveSecreta(String chaveSecretaCodificadaBase32) {
        // todo
    }

    public static SecretKey generateKaes(String segredo) throws Exception {
        try {
            // Configurando o SecureRandom com o segredo
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(segredo.getBytes());

            // Inicializando o KeyGenerator para AES
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, secureRandom); // Tamanho da chave: 256 bits

            SecretKey Kaes = keyGenerator.generateKey();
            return Kaes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] encryptChave(byte[] chave, SecretKey chaveAES) throws Exception {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, chaveAES);
            byte[] chaveCifrada = cipher.doFinal(chave);
            return chaveCifrada;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } 
    }

    public static byte[] decryptChave(byte[] chave, SecretKey chaveAES) throws Exception {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, chaveAES);
            byte[] chaveDescriptografada = cipher.doFinal(chave);
            return chaveDescriptografada;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < array.length; i++) {
            String hex = Integer.toHexString(0x0100 + (array[i] & 0x00FF)).substring(1);
            buf.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return buf.toString();
    }

    public static X509Certificate generateObjetoCertificadoDigitalFromArquivo(String caminhoCertificadoDigital) throws Exception {
        try{
            // Criando um FileInputStream a partir do arquivo do certificado
            FileInputStream file = new FileInputStream(caminhoCertificadoDigital);
            // Gerando o objeto X509Certificate a partir do arquivo do certificado
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificado = (X509Certificate) cf.generateCertificate(file);
            file.close();

            return certificado;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] generateCertificadoDigitalPEM(String caminhoCertificadoDigital) {
        try{
            // Criando um FileInputStream a partir do arquivo do certificado
            FileInputStream file = new FileInputStream(caminhoCertificadoDigital);
            byte[] certificadoBytes = file.readAllBytes();
            file.close();

            return certificadoBytes;            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static X509Certificate generateObjetoCertificadoDigitalFromPEM(String certificadoPEM) throws Exception {
        try{
            // Retirando cabeçalho, rodapé e line breaks
            String certificadoPEMTratado = removerMarcacoesCertificado(certificadoPEM);

            // Decodificando a string tratada do certificado PEM
            byte[] certificadoBytes = Base64.getDecoder().decode(certificadoPEMTratado);

            // Criando um ByteArrayInputStream com os bytes do certificado PEM
            ByteArrayInputStream certificadoInputStream = new ByteArrayInputStream(certificadoBytes);

            // Gerando o objeto X509Certificate a partir do certificado PEM
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificado = (X509Certificate) cf.generateCertificate(certificadoInputStream);
            
            return certificado;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }          
    }

    private static String removerMarcacoesCertificado(String certificadoStr) {
        certificadoStr = certificadoStr.replaceAll("-----BEGIN CERTIFICATE-----", "");
        certificadoStr = certificadoStr.replaceAll("-----END CERTIFICATE-----", "");
        certificadoStr = certificadoStr.replaceAll("\\n", "");
        certificadoStr = certificadoStr.replaceAll("\\r", "");
        
        return certificadoStr.trim();
    }

    public static String extrairCertificadoComMarcacoes(String certificadoCompleto) {
        Pattern pattern = Pattern.compile("(-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(certificadoCompleto);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return null;
        }
    }
    
    public static PrivateKey generateObjetoChavePrivadaFromArquivo(String caminhoChavePrivada, String fraseSecreta) {
        try {
            FileInputStream chavePrivadaInputStream = new FileInputStream(caminhoChavePrivada);
            byte[] chavePrivadaBytes = chavePrivadaInputStream.readAllBytes();
            chavePrivadaInputStream.close();

            // Descriptografando a chave privada utilizando a frase secreta
            SecretKey Kaes = ManipuladorDeChaves.generateKaes(fraseSecreta);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, Kaes);
            byte[] chavePrivadaDescriptografada = cipher.doFinal(chavePrivadaBytes);

            // Retirando cabeçalho, rodapé e line breaks
            String chavePrivadaDescriptografadaStr = new String(chavePrivadaDescriptografada, "UTF-8");
            String chavePrivadaDescriptografadaStrTratada = removerMarcacoesChavePrivada(chavePrivadaDescriptografadaStr);
            
            // Decodificando a chave privada descriptografada e tratada
            byte[] chavePrivadaDescriptografadaDecodificada = Base64.getDecoder().decode(chavePrivadaDescriptografadaStrTratada);

            // Gerando o objeto chave privada correspondente
            PKCS8EncodedKeySpec chavePrivadaSpec = new PKCS8EncodedKeySpec(chavePrivadaDescriptografadaDecodificada);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey chavePrivada = keyFactory.generatePrivate(chavePrivadaSpec);
            
            return chavePrivada;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String removerMarcacoesChavePrivada(String chavePrivadaStr) {
        chavePrivadaStr = chavePrivadaStr.replaceAll("-----BEGIN PRIVATE KEY-----", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("-----END PRIVATE KEY-----", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("\\n", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("\\r", "");
        
        return chavePrivadaStr.trim();
    }

    public static byte[] generateChavePrivadaBin(String caminhoChavePrivada) {
        try {
            FileInputStream chavePrivadaInputStream = new FileInputStream(caminhoChavePrivada);
            byte[] chavePrivadaBytes = chavePrivadaInputStream.readAllBytes();
            chavePrivadaInputStream.close();

            return chavePrivadaBytes;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey generateObjetoChavePrivadaFromBin(byte[] chavePrivadaCriptografada, String fraseSecreta) {
        try {
            // Descriptografando a chave privada utilizando a frase secreta
            SecretKey Kaes = ManipuladorDeChaves.generateKaes(fraseSecreta);
            byte[] chavePrivadaDescriptografada = decryptChave(chavePrivadaCriptografada, Kaes);

            // Retirando cabeçalho, rodapé e line breaks
            String chavePrivadaDescriptografadaStr = new String(chavePrivadaDescriptografada, "UTF-8");
            System.out.println("chave no generate: " + chavePrivadaDescriptografadaStr);
            String chavePrivadaDescriptografadaStrTratada = removerMarcacoesChavePrivada(chavePrivadaDescriptografadaStr);
            
            // Decodificando a chave privada descriptografada e tratada
            byte[] chavePrivadaDescriptografadaDecodificada = Base64.getDecoder().decode(chavePrivadaDescriptografadaStrTratada);

            // Gerando o objeto chave privada correspondente
            PKCS8EncodedKeySpec chavePrivadaSpec = new PKCS8EncodedKeySpec(chavePrivadaDescriptografadaDecodificada);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey chavePrivada = keyFactory.generatePrivate(chavePrivadaSpec);
            
            return chavePrivada;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean verificaChavePrivadaComChavePublica(PrivateKey chavePrivada, PublicKey chavePublica) {
        try {
            // Gerando array aleatório de 4096 bytes
            byte[] arrayAleatorio = new byte[4096];
            new SecureRandom().nextBytes(arrayAleatorio);

            // Assinando o array aleatório usando a chave privada
            Signature assinatura = Signature.getInstance("SHA256withRSA");   
            assinatura.initSign(chavePrivada);
            assinatura.update(arrayAleatorio);
            byte[] assinaturaBytes = assinatura.sign();

            // Verificando a assinatura usando a chave pública
            assinatura.initVerify(chavePublica);
            assinatura.update(arrayAleatorio);
            boolean chavePrivadaVerificada = assinatura.verify(assinaturaBytes);

            return chavePrivadaVerificada;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String generateHashDaSenha(String senha) {
        // Gerando um SALT aleatório
        final byte[] salt = new byte[16];  // 16 bytes 
        new SecureRandom().nextBytes(salt);

        // Gerando o hash para a senha informada
        String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, 12);
        return hash;
    }
    
}

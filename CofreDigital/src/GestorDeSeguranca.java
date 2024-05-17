import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.*;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

public class GestorDeSeguranca {

    // Métodos compartilhados de segurança

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

    public static byte[] encryptChave(byte[] chave, SecretKey kaes) throws Exception {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, kaes);
            byte[] chaveCifrada = cipher.doFinal(chave);
            return chaveCifrada;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } 
    }

    public static byte[] decryptChave(byte[] chave, SecretKey kaes) throws Exception {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, kaes);
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

    // Métodos Chave secreta TOTP

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

    // Métodos Certificado Digital

    public static String generateCertificadoPEM(String caminhoCertificadoDigital) {
        try{
            // Criando um FileInputStream a partir do arquivo do certificado
            FileInputStream file = new FileInputStream(caminhoCertificadoDigital);
            byte[] certificadoBytes = file.readAllBytes();
            file.close();

            String certificadoPEM = extrairCertificadoComLimitadores(new String(certificadoBytes, "UTF-8"));

            return certificadoPEM;            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static X509Certificate generateX509CertificateFromPEM(String certificadoPEM) throws Exception {
        try{
            // Retirando cabeçalho, rodapé e line breaks
            String certificadoPEMTratado = removeLimitadoresDoCertificado(certificadoPEM);

            // Decodificando a string tratada do certificado PEM
            byte[] certificadoBytes = Base64.getDecoder().decode(certificadoPEMTratado);

            // Gerando o objeto X509Certificate a partir do certificado PEM
            ByteArrayInputStream certificadoInputStream = new ByteArrayInputStream(certificadoBytes);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificado = (X509Certificate) cf.generateCertificate(certificadoInputStream);
            
            return certificado;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }          
    }

    public static X509Certificate generateX509CertificateFromFile(String caminhoCertificadoDigital) throws Exception {
        try{
            // Gerando o objeto X509Certificate a partir do arquivo do certificado
            FileInputStream file = new FileInputStream(caminhoCertificadoDigital);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate certificado = (X509Certificate) cf.generateCertificate(file);
            file.close();

            return certificado;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String removeLimitadoresDoCertificado(String certificadoStr) {
        certificadoStr = certificadoStr.replaceAll("-----BEGIN CERTIFICATE-----", "");
        certificadoStr = certificadoStr.replaceAll("-----END CERTIFICATE-----", "");
        certificadoStr = certificadoStr.replaceAll("\\n", "");
        certificadoStr = certificadoStr.replaceAll("\\r", "");
        
        return certificadoStr.trim();
    }

    public static String extrairCertificadoComLimitadores(String certificadoCompleto) {
        Pattern pattern = Pattern.compile("(-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(certificadoCompleto);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return null;
        }
    }

    // Métodos Chave Privada
    
    public static byte[] generateChavePrivadaBIN(String caminhoChavePrivada) {
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
    
    public static PrivateKey generatePrivateKeyFromBIN(byte[] chavePrivadaCriptografada, String fraseSecreta) {
        try {
            // Descriptografando a chave privada utilizando a frase secreta
            SecretKey Kaes = GestorDeSeguranca.generateKaes(fraseSecreta);
            byte[] chavePrivadaDescriptografada = decryptChave(chavePrivadaCriptografada, Kaes);

            // Retirando cabeçalho, rodapé e line breaks
            String chavePrivadaDescriptografadaStr = new String(chavePrivadaDescriptografada, "UTF-8");
            String chavePrivadaDescriptografadaStrTratada = removeLimitadoresDaChavePrivada(chavePrivadaDescriptografadaStr);
            
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

    public static PrivateKey generatePrivateKeyFromFile(String caminhoChavePrivada, String fraseSecreta) {
        try {
            byte[] chavePrivadaBytes = generateChavePrivadaBIN(caminhoChavePrivada);
            PrivateKey chavePrivada = generatePrivateKeyFromBIN(chavePrivadaBytes, fraseSecreta);
            
            return chavePrivada;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String removeLimitadoresDaChavePrivada(String chavePrivadaStr) {
        chavePrivadaStr = chavePrivadaStr.replaceAll("-----BEGIN PRIVATE KEY-----", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("-----END PRIVATE KEY-----", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("\\n", "");
        chavePrivadaStr = chavePrivadaStr.replaceAll("\\r", "");
        
        return chavePrivadaStr.trim();
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
    
    // Métodos Senha pessoal

    public static String generateHashDaSenha(String senha) {
        // Gerando um SALT aleatório
        final byte[] salt = new byte[16];  // 16 bytes 
        new SecureRandom().nextBytes(salt);

        // Gerando o hash para a senha informada
        String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, 12);
        return hash;
    }
    
}

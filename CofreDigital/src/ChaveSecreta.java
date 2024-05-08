import java.security.*;
import javax.crypto.*;

public class ChaveSecreta {

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

    public static String encryptChaveSecreta(String chaveSecretaCodificadaBase32, SecretKey chaveAES) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, chaveAES);
        byte[] chaveSecretaCodificadaBase32Cifrada = cipher.doFinal(chaveSecretaCodificadaBase32.getBytes("UTF-8"));
        String chaveSecretaCodificadaBase32CifradaHex = byteArrayToHexString(chaveSecretaCodificadaBase32Cifrada);
        // duvida: podemos salvar a chave secreta no banco como byte[] ou temos que passar para string em hexadecimal?
        return chaveSecretaCodificadaBase32CifradaHex;
    }

    private static String byteArrayToHexString(byte[] array) {
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < array.length; i++) {
            String hex = Integer.toHexString(0x0100 + (array[i] & 0x00FF)).substring(1);
            buf.append((hex.length() < 2 ? "0" : "") + hex);
        }
        return buf.toString();
    }

    public static String decryptChaveSecreta(String chaveSecretaCodificadaBase32CifradaHex, SecretKey chaveAES) throws Exception {
        byte[] chaveSecretaCodificadaBase32Cifrada = hexStringToByteArray(chaveSecretaCodificadaBase32CifradaHex);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] chaveSecretaCodificadaBase32Array = cipher.doFinal(chaveSecretaCodificadaBase32Cifrada);

        String chaveSecretaCodificadaBase32 = new String(chaveSecretaCodificadaBase32Array, "UTF8");
        return chaveSecretaCodificadaBase32;
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] byteArray = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return byteArray;
    }
    
}

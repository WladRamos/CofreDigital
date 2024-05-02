import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

public class TOTP {
 
    private byte [] key = null;
    private long timeStepInSeconds = 30;

    // Construtor da classe. Recebe a chave secreta em BASE32 e o intervalo
    // de tempo a ser adotado (default = 30 segundos). Deve decodificar a
    // chave secreta e armazenar em key. Em caso de erro, gera Exception.
    public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        this.key = base32.fromString(base32EncodedSecret);
        this.timeStepInSeconds = timeStepInSeconds;
    }

    // Recebe o HASH HMAC-SHA1 e determina o código TOTP de 6 algarismos
    // decimais, prefixado com zeros quando necessário.
    private String getTOTPCodeFromHash(byte[] hash) {
        // Encontrando a posição inicial do deslocamento 
        int offset = hash[hash.length - 1] & 0xF;
        // Lendo um total de 4 bytes a partir do byte indicado pelo offset
        int truncatedHash = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16) | ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
        // Convertendo para um número de 6 dígitos ou menos
        String totp = String.valueOf(truncatedHash % 1000000);

        // Preenche com zeros à esquerda, se necessário
        while (totp.length() < 6) {
            totp = "0" + totp;
        }
        return totp;
    }

    // Recebe o contador e a chave secreta para produzir o hash HMAC-SHA1.
    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, "RAW");
            mac.init(keySpec);
            byte[] hash =  mac.doFinal(counter);
            return hash;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Recebe o intervalo de tempo e executa o algoritmo TOTP para produzir
    // o código TOTP. Usa os métodos auxiliares getTOTPCodeFromHash e HMAC_SHA1.
    private String TOTPCode(long timeInterval) {
        // Convertendo o timeInterval para um array de 8 bytes
        byte[] counter = new byte[Long.BYTES];
        for (int i = 7; i >= 0; i--) {
            counter[i] = (byte) (timeInterval & 0xFF);
            timeInterval >>= 8;
        }

        byte[] hash = HMAC_SHA1(counter, key); 
        return getTOTPCodeFromHash(hash);
    }

    // Método que é utilizado para solicitar a geração do código TOTP.
    public String generateCode() {
        // Calculando a quantidade de intervalos de 30 segundos desde Janeiro 1, 1970, 00:00:00 GMT.
        long secondsSinceEpoch = System.currentTimeMillis() / 1000;
        long timeInterval = secondsSinceEpoch / (timeStepInSeconds);

        return TOTPCode(timeInterval);
    }

    // Método que é utilizado para validar um código TOTP (inputTOTP).
    // Deve considerar um atraso ou adiantamento de 30 segundos no
    // relógio da máquina que gerou o código TOTP.
    public boolean validateCode(String inputTOTP) {
        long secondsSinceEpoch = System.currentTimeMillis() / 1000;

        String menos30segTOTP = TOTPCode((secondsSinceEpoch - 30) / timeStepInSeconds);
        String exatoTOTP = TOTPCode(secondsSinceEpoch / timeStepInSeconds);
        String mais30segTOTP = TOTPCode((secondsSinceEpoch + 30) / timeStepInSeconds);

        boolean codeIsValid = (inputTOTP.equalsIgnoreCase(menos30segTOTP)) || (inputTOTP.equalsIgnoreCase(exatoTOTP)) || (inputTOTP.equalsIgnoreCase(mais30segTOTP));

        return codeIsValid;
    }
 
}

import org.junit.Test;
import static org.junit.Assert.*;

public class TOTPTest {

    @Test
    public void testGenerateCode() {
        try {
            // Cria uma instância da classe TOTP com uma chave secreta e um intervalo de tempo de 30 segundos
            TOTP totp = new TOTP("BQOCDDA4Q6QDAFVOZULR3NQQ4QAG5WPB", 30);
            // Gera um código TOTP
            String code = totp.generateCode();
            // Verifica se o código gerado não é nulo ou vazio ou inválido
            System.out.println("TOTP code: " + code);
            assertNotNull(code);
            assertFalse(code.isEmpty());
            assertTrue(isTOTPValid(code));

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while generating TOTP code");
        }
    }

    private boolean isTOTPValid(String totp) {
        // Verifica se a string tem exatamente 6 caracteres de digitos numéricos
        if (totp.length() != 6) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            if (!Character.isDigit(totp.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testValidateCode() {
        try {
            // Cria uma instância da classe TOTP com uma chave secreta e um intervalo de tempo de 30 segundos
            TOTP totp = new TOTP("BQOCDDA4Q6QDAFVOZULR3NQQ4QAG5WPB", 30);
            // Gera um código TOTP
            String code = totp.generateCode();
            // Verifica se o código gerado é válido
            assertTrue(totp.validateCode(code));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while generating or validating TOTP code");
        }
    }
}

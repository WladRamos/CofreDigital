import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import java.security.cert.X509Certificate;

public class GestorDeSegurancaTest {
    private String caminhoChavePrivada, caminhoCertificadoDigital;

    @Before
    public void setUp() {
        String diretorioAtual = System.getProperty("user.dir");
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-x509.crt";
    }

    // Métodos compartilhados de segurança

    @Test
    public void testGenerateKaes() {
        try {
            String segredo = "segredo";
            SecretKey kaes = GestorDeSeguranca.generateKaes(segredo);
            assertNotNull(kaes);
            assertEquals("AES", kaes.getAlgorithm());
            assertEquals(256, kaes.getEncoded().length * 8);

        } catch (Exception e) {
            fail("Exceção lançada durante a geração da chave AES");
        }
    }

    @Test
    public void testEncryptAndDecryptChave() {
        try {
            String segredo = "segredo";
            SecretKey kaes = GestorDeSeguranca.generateKaes(segredo);

            byte[] chave = "chave".getBytes();
            byte[] chaveCifrada = GestorDeSeguranca.encryptChave(chave, kaes);

            assertNotNull(chaveCifrada);
            assertNotEquals(0, chaveCifrada.length);

            byte[] chaveDescriptografada = GestorDeSeguranca.decryptChave(chaveCifrada, kaes);
            assertArrayEquals(chave, chaveDescriptografada);

        } catch (Exception e) {
            fail("Exceção lançada durante a criptografia/descriptografia da chave");
        }
    }

    @Test
    public void testByteArrayToHexString() {
        byte[] array = {(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF};
        String hexString = GestorDeSeguranca.byteArrayToHexString(array);
        assertEquals("deadbeef", hexString);
    }

    // Métodos Chave secreta TOTP

    @Test
    public void testGenerateChaveSecreta() {
        try {
            String chaveSecreta = GestorDeSeguranca.generateChaveSecreta();
            System.out.println("chave secreta: " + chaveSecreta);
            assertNotNull(chaveSecreta);
            assertEquals(32, chaveSecreta.length()); 
            assertTrue(chaveSecreta.matches("[A-Z2-7]+"));

            String senha = "1304587609";
            SecretKey kaes = GestorDeSeguranca.generateKaes(senha);
            byte[] chaveSecretaCifrada = GestorDeSeguranca.encryptChave(chaveSecreta.getBytes("UTF-8"), kaes);
            System.out.println("chave secreta encriptada (tam: " + chaveSecretaCifrada.length + "): " + chaveSecretaCifrada);
            
        } catch (Exception e) {
            fail("Exception thrown while generating secret key");
        }
    }
    /* 
    @Test
    public void testGenerateQRcodeDaChaveSecreta() {
        try {
           // todo 
        } catch (Exception e) {
            fail("Exception thrown while generating secret key QRcode");
        }
    }
    */
    // Métodos Certificado Digital

    @Test
    public void testGenerateCertificadoPEM() {
        try {
            File file = new File(caminhoCertificadoDigital);
            System.out.println("file tam: " + file.length());

            String certificadoPEMBytes = GestorDeSeguranca.generateCertificadoPEM(caminhoCertificadoDigital);
            assertNotNull(certificadoPEMBytes);
            assertNotEquals(0, certificadoPEMBytes.length());

            System.out.println("certificado PEM String (tam: " + certificadoPEMBytes.length() + "): " + certificadoPEMBytes);
            
        } catch (Exception e) {
            fail("Exceção lançada durante a geração do certificado PEM");
        }
    }

    @Test
    public void testGenerateX509CertificateFromPEM() {
        try {
            String certificadoPEM = GestorDeSeguranca.generateCertificadoPEM(caminhoCertificadoDigital);
            assertNotNull(certificadoPEM);

            X509Certificate certificado = GestorDeSeguranca.generateX509CertificateFromPEM(certificadoPEM);
            assertNotNull(certificado);

        } catch (Exception e) {
            fail("Exceção lançada durante a geração do certificado X.509 a partir do PEM");
        }
    }

    @Test
    public void testGenerateX509CertificateFromFile() {
        try {
            X509Certificate certificado = GestorDeSeguranca.generateX509CertificateFromFile(caminhoCertificadoDigital);
            assertNotNull(certificado);

        } catch (Exception e) {
            fail("Exceção lançada durante a geração do certificado X.509 a partir do arquivo");
        }
    }

    @Test
    public void testExtrairCertificadoComLimitadores() {
        try {
            byte[] certificadoBytes = Files.readAllBytes(Paths.get(caminhoCertificadoDigital));
            String certificadoCompleto = new String(certificadoBytes, "UTF-8");
            String certificadoComLimitadores = GestorDeSeguranca.extrairCertificadoComLimitadores(certificadoCompleto);
            assertNotNull(certificadoComLimitadores);
            System.out.println("certificado com limitadores: " + certificadoComLimitadores);
            
        } catch (IOException e) {
            fail("Exceção lançada durante a extração do certificado com limitadores");
        }
    }

    // Métodos Chave Privada

    @Test
    public void testGenerateChavePrivadaBIN() {
        try {
            byte[] chavePrivadaBytes = GestorDeSeguranca.generateChavePrivadaBIN(caminhoChavePrivada);
            assertNotNull(chavePrivadaBytes);
            assertNotEquals(0, chavePrivadaBytes.length);

            File file = new File(caminhoChavePrivada);
            System.out.println("file tam: " + file.length());
            System.out.println("Chave privada BIN Bytes (tam: " + chavePrivadaBytes.length + "): " + chavePrivadaBytes);
            
        } catch (Exception e) {
            fail("Exceção lançada durante a geração da chave privada BIN");
        }
    }

    @Test
    public void testGeneratePrivateKeyFromBIN() {
        try {
            String fraseSecreta = "user02";
            byte[] chavePrivadaBytes = GestorDeSeguranca.generateChavePrivadaBIN(caminhoChavePrivada);
            assertNotNull(chavePrivadaBytes);

            PrivateKey chavePrivada = GestorDeSeguranca.generatePrivateKeyFromBIN(chavePrivadaBytes, fraseSecreta);
            assertNotNull(chavePrivada);

        } catch (Exception e) {
            fail("Exceção lançada durante a geração da chave privada a partir do BIN");
        }
    }

    @Test
    public void testGeneratePrivateKeyFromFile() {
        try {
            String fraseSecreta = "user02";
            PrivateKey chavePrivada = GestorDeSeguranca.generatePrivateKeyFromFile(caminhoChavePrivada, fraseSecreta);
            assertNotNull(chavePrivada);

        } catch (Exception e) {
            fail("Exceção lançada durante a geração da chave privada a partir do arquivo");
        }
    }

    @Test
    public void testVerificaChavePrivadaComChavePublica() {
        try {
            String fraseSecreta = "user02";
            PrivateKey chavePrivada = GestorDeSeguranca.generatePrivateKeyFromFile(caminhoChavePrivada, fraseSecreta);
            assertNotNull(chavePrivada);
            
            X509Certificate certificado = GestorDeSeguranca.generateX509CertificateFromFile(caminhoCertificadoDigital);
            assertNotNull(certificado);
            PublicKey chavePublica = certificado.getPublicKey();

            boolean chavePrivadaVerificada = GestorDeSeguranca.verificaChavePrivadaComChavePublica(chavePrivada, chavePublica);
            assertTrue(chavePrivadaVerificada);

        } catch (Exception e) {
            fail("Exceção lançada durante a verificação da chave privada com a chave pública");
        }
    }

    // Métodos Senha pessoal

    @Test
    public void testGenerateHashDaSenha() {
        try {
            String hash = GestorDeSeguranca.generateHashDaSenha("13572468");
            System.out.println("Hash da senha: " + hash);
            assertNotNull(hash);
            assertEquals(60, hash.length());

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while generating password hash");
        }
    }

}

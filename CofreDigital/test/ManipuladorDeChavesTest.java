import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.security.PrivateKey;

import javax.crypto.SecretKey;

public class ManipuladorDeChavesTest {
    private String caminhoChavePrivada, caminhoCertificadoDigital;

    @Before
    public void setUp() {
        String diretorioAtual = System.getProperty("user.dir");
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/test/user02-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/test/user02-x509.crt";
    }

    @Test
    public void testGenerateChaveSecreta() {
        try {
            String chaveSecreta = ManipuladorDeChaves.generateChaveSecreta();
            System.out.println("chave secreta: " + chaveSecreta);
            assertNotNull(chaveSecreta);
            assertEquals(32, chaveSecreta.length()); 
            assertTrue(chaveSecreta.matches("[A-Z2-7]+"));
        } catch (Exception e) {
            fail("Exception thrown while generating secret key");
        }
    }

    @Test
    public void testGenerateChavePrivadaBin() {
        try {
            byte[] chavePrivada = ManipuladorDeChaves.generateChavePrivadaBin(caminhoChavePrivada);
            System.out.println("chave privada bin (tam: " + chavePrivada.length + "): " + chavePrivada);
            File file = new File(caminhoChavePrivada);
            System.out.println("file tam: " + file.length());
            assertNotNull(chavePrivada);

            PrivateKey pk = ManipuladorDeChaves.generateObjetoChavePrivadaFromBin(chavePrivada, "user02");
            assertNotNull(pk);
        } catch (Exception e) {
            fail("Exception thrown while generating private key binary");
        }
    }

    @Test
    public void testGenerateCertificadoDigitalPEM() {
        try {
            byte[] cert = ManipuladorDeChaves.generateCertificadoDigitalPEM (caminhoCertificadoDigital);
            System.out.println("certificado PEM (tam: " + cert.length + "): " + cert);
            File file = new File(caminhoCertificadoDigital);
            System.out.println("file tam: " + file.length());
            assertNotNull(cert);

            String certStr = ManipuladorDeChaves.extrairCertificadoComMarcacoes(new String(cert, "UTF-8"));
            System.out.println("certificado PEM string UTF-8 (tam: " + certStr.length() + "):\n" + certStr);

        } catch (Exception e) {
            fail("Exception thrown while generating certificate PEM");
        }
    }

    @Test
    public void testGenerateHashDaSenha() {
        try {
            String hash = ManipuladorDeChaves.generateHashDaSenha("13572468");
            System.out.println("hash: " + hash);
            assertNotNull(hash);
            assertEquals(60, hash.length());

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown while generating password hash");
        }
    }
}

/*import static org.junit.Assert.*;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;

public class CadastroTest {

    private Cadastro cadastro;

    @Before
    public void setUp() {
        cadastro = new Cadastro();
    }

    @Test
    public void testGenerateObjetoCertificadoDigital() {
        String diretorioAtual = System.getProperty("user.dir");
        String caminhoCertificado = diretorioAtual + File.separator + "CofreDigital/test/user02-x509.crt";

        try {
            X509Certificate certificado = cadastro.generateObjetoCertificadoDigital(caminhoCertificado);
            assertNotNull(certificado);
            testExtrairNome(certificado);
            testExtrairEmail(certificado);
        } catch (Exception e) {
            fail("Erro ao gerar o certificado digital: " + e.getMessage());
        }
    }

    public void testExtrairNome(X509Certificate certificado) {
        String nome = cadastro.extrairNomeDoCertificadoDigital(certificado);
        assertNotNull(nome);
        assertEquals("Usuario 02", nome);
    }

    public void testExtrairEmail(X509Certificate certificado) {
        try {
            String email = cadastro.extrairEmailDoCertificadoDigital(certificado);
            assertEquals("user02@inf1416.puc-rio.br", email);
        } catch (Exception e) {
            fail("Erro ao extrair o email do certificado: " + e.getMessage());
        }
    }

    @Test
    public void generateObjetoChavePrivadaFromArquivo() {
        String diretorioAtual = System.getProperty("user.dir");
        String caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/test/user02-pkcs8-aes.pem";

        try {
            PrivateKey certificado = cadastro.generateObjetoChavePrivadaFromArquivo(caminhoChavePrivada, "user02");
            assertNotNull(certificado);
        } catch (Exception e) {
            fail("Erro ao gerar a chave secreta: " + e.getMessage());
        }
    }
 
    @Test
    public void extrairDetalhesDoCertificadoDigital() {
        String diretorioAtual = System.getProperty("user.dir");
        String caminhoCertificado = diretorioAtual + File.separator + "CofreDigital/test/user02-x509.crt";

        try {
            X509Certificate certificado = cadastro.generateObjetoCertificadoDigital(caminhoCertificado);
            assertNotNull(certificado);
            cadastro.extrairDetalhesDoCertificadoDigital(certificado);
        } catch (Exception e) {
            fail("Erro ao extrair detalhes do certificado: " + e.getMessage());
        }
    }

}
*/
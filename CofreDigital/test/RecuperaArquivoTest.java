import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class RecuperaArquivoTest {

    private RecuperaArquivo recuperaArquivo;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String caminhoChavePrivada, caminhoCertificadoDigital;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        String diretorioAtual = System.getProperty("user.dir");
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/test/user02-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/test/user02-x509.crt";

        privateKey = ManipuladorDeChaves.generateObjetoChavePrivadaFromArquivo(caminhoChavePrivada, "user02");
        X509Certificate certificado;
        try{
            certificado = ManipuladorDeChaves.generateObjetoCertificadoDigitalFromArquivo(caminhoCertificadoDigital);
            publicKey = certificado.getPublicKey();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        // Configurando a instância da classe RecuperaArquivo
        recuperaArquivo = new RecuperaArquivo("user@example.com", "usuario", "CofreDigital/Pacote-T4/Files/", publicKey, privateKey);
    }

    @Test
    public void testDecriptaEVerificaIndex() throws Exception {
        // Este teste assume que o arquivo "index.enc" contém dados válidos
        String resultado = recuperaArquivo.decriptaEVerifica("index");
        assertNotNull("O resultado não deveria ser nulo", resultado);
        assertFalse("O resultado não deveria ser vazio", resultado.isEmpty());
    }

    @Test
    public void testDecriptaEVerificaXXYYZZ00() throws Exception {
        // Este teste assume que o arquivo "XXYYZZ00.enc" contém dados válidos
        String resultado = recuperaArquivo.decriptaEVerifica("XXYYZZ00");
        assertNotNull("O resultado não deveria ser nulo", resultado);
        assertFalse("O resultado não deveria ser vazio", resultado.isEmpty());
    }

    @Test
    public void testDecriptaEVerificaABCD() throws Exception {
        // Este teste verifica o comportamento para um nome de arquivo que não corresponde a um arquivo existente
        String resultado = recuperaArquivo.decriptaEVerifica("ABCD");
        assertNull("O resultado deveria ser nulo para um arquivo inexistente", resultado);
    }
}

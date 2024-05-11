import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

public class RecuperaArquivoTest {

    private RecuperaArquivo recuperaArquivo;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private String caminhoChavePrivada, caminhoCertificadoDigital;

    @Before
    public void setUp() throws NoSuchAlgorithmException {
        String diretorioAtual = System.getProperty("user.dir");
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/test/admin-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/test/admin-x509.crt";

        privateKey = ManipuladorDeChaves.generateObjetoChavePrivadaFromArquivo(caminhoChavePrivada, "admin");
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
        List<List<String>> resultado = recuperaArquivo.decriptaEVerificaIndex();
        
        // Verifica se o resultado não é nulo
        assertNotNull("O resultado não deveria ser nulo", resultado);
        
        // Verifica se o resultado não está vazio
        assertFalse("O resultado não deveria ser vazio", resultado.isEmpty());

        // Imprime o resultado para verificação visual (opcional)
        for (List<String> file : resultado) {
            System.out.println(file);
        }

        // Verificar se cada sublista contém o número correto de elementos (por exemplo, se cada arquivo deveria ter 4 informações)
        for (List<String> file : resultado) {
            assertEquals("Cada arquivo deveria ter 4 informações", 4, file.size());
        }
    }

    
}

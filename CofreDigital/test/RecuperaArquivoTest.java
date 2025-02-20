import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
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
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/admin-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/admin-x509.crt";

        privateKey = GestorDeSeguranca.generatePrivateKeyFromFile(caminhoChavePrivada, "admin");
        X509Certificate certificado;
        try{
            certificado = GestorDeSeguranca.generateX509CertificateFromFile(caminhoCertificadoDigital);
            publicKey = certificado.getPublicKey();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        // Configurando a instância da classe RecuperaArquivo
        recuperaArquivo = new RecuperaArquivo("admin@inf1416.puc-rio.br", "administrador", "CofreDigital/Pacote-T4/Files/", publicKey, privateKey, publicKey, privateKey);
    }

    @Test
    public void testDecriptaEVerificaIndex() throws Exception {
        // Este teste assume que o arquivo "index.enc" contém dados válidos
        String resultRecupecacao = recuperaArquivo.verificaArquivos("index");
        assertEquals("Erro na verificação dos arquivos", "OK", resultRecupecacao);

        List<List<String>> resultado = recuperaArquivo.recuperaIndex();

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

    
    @Test
    public void testDecriptaEVerificaXXYYZZ00() throws Exception {
        String nomeSecreto = "teste00.docx";
        String nomeCodigo = "XXYYZZ00";

        // Este teste assume que o arquivo "index.enc" contém dados válidos
        String resultRecupecacao = recuperaArquivo.verificaArquivos(nomeCodigo);
        assertEquals("Erro na verificação dos arquivos", "OK", resultRecupecacao);

        recuperaArquivo.recuperaArquivosDocx(nomeSecreto);

        // Checa se o arquivo com o nome secreto foi criado na pasta
        File f = new File("CofreDigital/Pacote-T4/Files/" + "/" + nomeSecreto);
        assertTrue("O arquivo descriptografado deveria existir", f.exists());

        // Verifica o tamanho do arquivo para garantir que não está vazio
        assertTrue("O arquivo descriptografado não deveria estar vazio", f.length() > 0);
    }
}

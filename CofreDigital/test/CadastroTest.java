import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class CadastroTest {

    private String caminhoChavePrivada, caminhoCertificadoDigital, caminhoDeOutroCertificadoDigital;

    @Before
    public void setUp() {
        String diretorioAtual = System.getProperty("user.dir");
        caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-pkcs8-aes.pem";
        caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user02-x509.crt";
        caminhoDeOutroCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/Pacote-T4/Keys/user01-x509.crt";
    }

    @Test
    public void testVerificaEntradasDoCadastro() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Entradas verificadas", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroCaminhoCertificadoInvalido() {
        Cadastro cadastro = new Cadastro(
            "pasta/caminhoCertificadoDigital", 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Caminho do arquivo do certificado digital inválido.", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroCaminhoChavePrivadaInvalido() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            "pasta/caminhoChavePrivada", 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Caminho do arquivo da chave privada inválido.", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroFraseSecretaIncorreta() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "fraseIncorreta", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Frase secreta inválida para a chave privada fornecida.", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroParDeChavesInvalido() {
        Cadastro cadastro = new Cadastro(
            caminhoDeOutroCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Assinatura digital inválida para a chave privada fornecida.", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroGrupoInvalido() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            0, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Grupo inválido. Escolha 1 para Administrador ou 2 para Usuário.", verified);
    }

    @Test
    public void testVerificaEntradasDoCadastro_erroSenhasDiferentes() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12340678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Senha e confirmação de senha não são iguais.", verified);
    }

    @Test
    public void testGetDetalhesDoCertificadoDigital() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Entradas verificadas", verified);

        try {
            HashMap<String, String> hashmap = cadastro.getDetalhesDoCertificadoDigital();
            assertNotNull(hashmap);
            System.out.println(hashmap);
        } catch (Exception e) {
            fail("Exceção lançada durante getDetalhesDoCertificadoDigital().");
        }
        
    }

    @Test
    public void testCadastraUsuario() {
        Cadastro cadastro = new Cadastro(
            caminhoCertificadoDigital, 
            caminhoChavePrivada, 
            "user02", 
            2, 
            "12345678", 
            "12345678"
        );
        String verified = cadastro.verificaEntradasDoCadastro();
        assertEquals("Entradas verificadas", verified);

        try {
            HashMap<String, String> hashmap = cadastro.getDetalhesDoCertificadoDigital();
            assertNotNull(hashmap);
        } catch (Exception e) {
            fail("Exceção lançada durante getDetalhesDoCertificadoDigital().");
        }

        Database database = Database.getInstance();
        database.deleteUsuarioAndChaveiroIfExists("user02@inf1416.puc-rio.br");
        
        String chaveSecreta = cadastro.cadastraUsuario();
        assertNotNull(chaveSecreta);
        System.out.println("chave secreta TOTP gerada: " + chaveSecreta);
        
        int uid = database.getUsuarioIfExists("user02@inf1416.puc-rio.br");
        assertFalse(uid==-1);
    }  

}

import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.crypto.*;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class Cadastro {
    private X509Certificate certificado;
    private PrivateKey chavePrivada;
    private PublicKey chavePublica;

    private String caminhoCertificadoDigital, caminhoChavePrivada;

    // Informações do usuário para o banco de dados
    String nomeUsuario, emailUsuario, hashUsuario, chaveSecretaTOTP;
    String certificadoDigitalPEM;
    byte[] chavePrivadaBin;


    // Métodos para o cadastro de um novo usuário do Cofre Digital    

    // Métodos de verificação das entradas de cadastro

    public boolean verificaCaminhoCertificadoDigital(String caminhoCertificadoDigitalInput) {
        if (verificaCaminhoDoArquivo(caminhoCertificadoDigitalInput)){
            this.caminhoCertificadoDigital = caminhoCertificadoDigitalInput;
            return true;
        }
        return false;
    }

    public boolean verificaCertificadoDigital() throws Exception {
        try{
            X509Certificate x509certificate = ManipuladorDeChaves.generateObjetoCertificadoDigitalFromArquivo(caminhoCertificadoDigital);
            if (x509certificate != null) {
                PublicKey pubkey = certificado.getPublicKey();
                if (pubkey != null) {
                    this.certificado = x509certificate;
                    this.chavePublica = pubkey;
                    return true;
                }                
            } 
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }        
    }

    public boolean verificaCaminhoChavePrivada(String caminhoChavePrivadaInput) {
        if (verificaCaminhoDoArquivo(caminhoChavePrivadaInput)){
            this.caminhoChavePrivada = caminhoChavePrivadaInput;
            return true;
        }
        return false;
    }

    public boolean verificaFraseSecretaDaChavePrivada(String fraseSecreta) {
        try{
            PrivateKey pkey = ManipuladorDeChaves.generateObjetoChavePrivadaFromArquivo(caminhoChavePrivada, fraseSecreta);
            if (pkey != null) {
                this.chavePrivada = pkey;
                return true;
            } 
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verificaChavePrivadaComChavePublica() {
        try {
            boolean chavePrivadaVerificada = ManipuladorDeChaves.verificaChavePrivadaComChavePublica(chavePrivada, chavePublica);
            return chavePrivadaVerificada;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
       
    public boolean verificaSenhasIguais(String senha, String confirmarSenha) {
        /* Verificação de formato de senha em tempo real será feito pela interface:
        * As senhas pessoais são sempre formadas por oito, nove ou dez números formados por dígitos de 0 a 9. 
        * Não podem ser aceitas sequências de números repetidos.
        * Método verificaSenhasIguais(senha, confirmaSenha) considera o recebimento de senhas com formato já validado.
        */
        if(senha.equals(confirmarSenha)) {
            this.hashUsuario = ManipuladorDeChaves.generateHashDaSenha(senha);
            return true;
        }
        return false;
    }

    // Método de confirmação das informações de cadastro

    public HashMap<String, String> getDetalhesDoCertificadoDigital() throws CertificateParsingException {
        HashMap<String, String> certificadoMap = new HashMap<>();
        try{
            // Extrair a versão do certificado
            String versao = String.valueOf(certificado.getVersion());
            certificadoMap.put("versao", versao);

            // Extrair a série do certificado
            byte[] serieBytes = certificado.getSerialNumber().toByteArray();
            String serie = ManipuladorDeChaves.byteArrayToHexString(serieBytes);
            certificadoMap.put("serie", serie);

            // Extrair a data de validade do certificado
            Date validadeInicio = certificado.getNotBefore(), validadeFim = certificado.getNotAfter();
            String validade = "De " + validadeInicio + " até " + validadeFim;
            certificadoMap.put("validade", validade);

            // Extrair o tipo de assinatura do certificado
            String tipoAssinatura = certificado.getSigAlgName();
            certificadoMap.put("tipo_assinatura", tipoAssinatura);

            // Extrair o emissor do certificado
            String emissor = certificado.getIssuerX500Principal().toString();   // duvida: É para informar o emissor assim: "EMAILADDRESS=ca@grad.inf.puc-rio.br, CN=AC INF1416, OU=INF1416, O=PUC, L=Rio, ST=RJ, C=BR" ou mostrar apenas nome e email para confirmação?
            certificadoMap.put("emissor", emissor);
            
            // Extrair o sujeito (friendly name) do certificado
            String friendlyName = extrairNomeDoCertificadoDigital(certificado);
            certificadoMap.put("sujeito", friendlyName);
            this.nomeUsuario = friendlyName;

            // Extrair o email do sujeito do certificado
            String email = extrairEmailDoCertificadoDigital(certificado);
            certificadoMap.put("email", email);
            this.emailUsuario = email;

            return certificadoMap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }

    // Método de cadastro das informações confirmadas no banco de dados do Cofre Digital

    public String cadastraUsuario(int grupo) {
        this.chavePrivadaBin = ManipuladorDeChaves.generateChavePrivadaBin(caminhoChavePrivada);
        // Checar recebimento correto de todas as informações de cadastro
        if (grupo != 1 && grupo != 2) {
            System.err.println("Grupo inválido. Escolha 1 para Administrador ou 2 para Usuário.");
            return null;
        }
        // todo: checar se há todos os itens neccessários e chamar funções de insert no banco
        return "chave secreta totp";
    }

    // Métodos auxiliares do cadastro de um novo usuário do Cofre Digital

    private boolean verificaCaminhoDoArquivo(String caminhoArquivo) {
        try {
            FileInputStream arquivoInputStream = new FileInputStream(caminhoArquivo);
            arquivoInputStream.close(); 
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String extrairNomeDoCertificadoDigital(X509Certificate certificado) {
        String identificador = certificado.getSubjectX500Principal().getName();
        String[] partes = identificador.split(",");
        for (String parte : partes) {
            if (parte.trim().startsWith("CN=")) {
                return parte.substring(3);
            }
        }
        return null;
    }

    private String extrairEmailDoCertificadoDigital(X509Certificate certificado) {
        String identificador = certificado.getSubjectX500Principal().toString();
        String[] partes = identificador.split(",");
        for (String parte : partes) {
            if (parte.trim().startsWith("EMAILADDRESS=")) {
                return parte.substring("EMAILADDRESS=".length());
            }
        }
        return null;
    }

 }
 
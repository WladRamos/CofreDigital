import java.io.FileInputStream;
import java.security.*;
import java.security.cert.*;
import java.util.*;

import javax.crypto.SecretKey;

public class Cadastro {
    private X509Certificate certificado;
    private PrivateKey chavePrivada;
    private PublicKey chavePublica;

    private String caminhoCertificadoDigital, caminhoChavePrivada, fraseSecreta, senha, confirmaSenha;
    private int grupo;
    private boolean entradasVerificadas = false;

    private String emailUsuario, nomeUsuario;

    public Cadastro(
        String caminhoCertificadoDigitalInput, 
        String caminhoChavePrivadaInput, 
        String fraseSecretaInput, 
        int grupoInput, 
        String senhaInput, 
        String confirmaSenhaInput
    ) {
        this.caminhoCertificadoDigital = caminhoCertificadoDigitalInput;
        this.caminhoChavePrivada = caminhoChavePrivadaInput;
        this.fraseSecreta = fraseSecretaInput;
        this.grupo = grupoInput;
        this.senha = senhaInput;
        this.confirmaSenha = confirmaSenhaInput;
    }

    // Métodos para o cadastro de um novo usuário do Cofre Digital    

    public String verificaEntradasDoCadastro() {
        if (!verificaCaminhoDoArquivo(caminhoCertificadoDigital)) {
            return "Caminho do arquivo do certificado digital inválido.";
        }
        if (!verificaCertificadoDigital()) {
            return "Certificado digital inválido";
        }
        if (!verificaCaminhoDoArquivo(caminhoChavePrivada)) {
            return "Caminho do arquivo da chave privada inválido.";
        }
        if (!verificaFraseSecretaDaChavePrivada()) {
            return "Frase secreta incorreta para a chave privada fornecida.";
        }
        if (!verificaChavePrivadaComChavePublica()) {
            return "Par de chaves inválido. Chave pública presente no certificado não corresponde à chave privada fornecida.";
        }
        if (grupo!=1 && grupo!=2) {
            return "Grupo inválido. Escolha 1 para Administrador ou 2 para Usuário.";
        }
        /* Verificação de formato de senha em tempo real será feito pela interface:
        * As senhas pessoais são sempre formadas por oito, nove ou dez números formados por dígitos de 0 a 9. 
        * Não podem ser aceitas sequências de números repetidos.
        * Nessa verificação considera-se o recebimento de senhas com formato já validado.
        */
        if (!senha.equals(confirmaSenha)) {
            return "Senha e confirmação de senha não são iguais.";
        }
        this.entradasVerificadas = true;
        return "Entradas verificadas";
    }

    public HashMap<String, String> getDetalhesDoCertificadoDigital() {
        HashMap<String, String> certificadoMap = new HashMap<>();
        try{
            // Extrair a versão do certificado
            String versao = String.valueOf(certificado.getVersion());
            certificadoMap.put("versao", versao);

            // Extrair a série do certificado
            byte[] serieBytes = certificado.getSerialNumber().toByteArray();
            String serie = GestorDeSeguranca.byteArrayToHexString(serieBytes);
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

    public String cadastraUsuario() {
        if (!entradasVerificadas) {
            System.err.println("Entradas do cadastro não verificadas.");
            return null;
        }

        if (emailUsuario == null) {
            System.err.println("Erro ao extrair o email do usuário.");
            return null;
        }

        if (nomeUsuario == null) {
            System.err.println("Erro ao extrair o nome do usuário.");
            return null;
        }

        String hashUsuario = GestorDeSeguranca.generateHashDaSenha(senha);
        if (hashUsuario == null) {
            System.err.println("Erro ao gerar o hash da senha do usuário.");
            return null;
        }

        String chaveSecretaTOTP = GestorDeSeguranca.generateChaveSecreta();
        byte[] chaveSecretaTOTPcriptografada = null;
        if(chaveSecretaTOTP != null) {
            try {
                SecretKey kaes = GestorDeSeguranca.generateKaes(senha);
                chaveSecretaTOTPcriptografada = GestorDeSeguranca.encryptChave(chaveSecretaTOTP.getBytes("UTF-8"), kaes);
                if (chaveSecretaTOTPcriptografada == null) {
                    System.err.println("Erro ao gerar a chave secreta TOTP criptografada.");
                    return null;
                }
                System.out.println("chave: " + chaveSecretaTOTPcriptografada);
            } catch (Exception e) {
                System.err.println("Erro ao criptografar a chave secreta TOTP com a senha do usuário.");
                e.printStackTrace();
                return null;
            }
        } else {
            System.err.println("Erro ao gerar uma chave secreta TOTP.");
            return null;
        }  

        byte[] chavePrivadaBIN = GestorDeSeguranca.generateChavePrivadaBIN(caminhoChavePrivada);
        if (chavePrivadaBIN == null) {
            System.err.println("Erro ao gerar a chave privada (.BIN).");
            return null;
        }

        String certificadoDigitalPEM = GestorDeSeguranca.generateCertificadoPEM(caminhoCertificadoDigital);
        if (certificadoDigitalPEM == null) {
            System.err.println("Erro ao gerar o certificado digital (.PEM).");
            return null;
        } 

        // Inserindo o novo usuário no banco de dados do Cofre Digital
        Database database = Database.getInstance();
        boolean usuarioInserido = database.insertIntoUsuarios(
            emailUsuario, 
            nomeUsuario, 
            hashUsuario, 
            chaveSecretaTOTPcriptografada, 
            chavePrivadaBIN, 
            certificadoDigitalPEM, 
            grupo
        );

        return usuarioInserido ? chaveSecretaTOTP : null;   // Se a inserção for bem sucedida, retorna a chave secreta gerada
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

    private boolean verificaCertificadoDigital() {
        try{
            X509Certificate x509certificate = GestorDeSeguranca.generateX509CertificateFromFile(caminhoCertificadoDigital);
            if (x509certificate != null) {
                PublicKey pubkey = x509certificate.getPublicKey();
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

    private boolean verificaFraseSecretaDaChavePrivada() {
        try{
            PrivateKey pkey = GestorDeSeguranca.generatePrivateKeyFromFile(caminhoChavePrivada, fraseSecreta);
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

    private boolean verificaChavePrivadaComChavePublica() {
        try {
            boolean chavePrivadaVerificada = GestorDeSeguranca.verificaChavePrivadaComChavePublica(chavePrivada, chavePublica);
            return chavePrivadaVerificada;

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
 
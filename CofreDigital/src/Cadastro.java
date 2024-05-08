import java.io.FileInputStream;

import javax.crypto.*;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class Cadastro {
    
    // Métodos para realizar o cadastro de um novo usuário do Cofre Digital

    public boolean cadastraUsuario(){
        // todo: chamar funções de insert no banco
        return true;
    }

    // todo: funções publicas que retornam apenas os booleanos para as verificações (wrappers)

    public X509Certificate generateObjetoCertificadoDigital(String caminhoCertificadoDigital) throws Exception {
        FileInputStream file = new FileInputStream(caminhoCertificadoDigital);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate certificado = cf.generateCertificate(file);
        file.close();

        if (certificado instanceof X509Certificate) {
            try{
                ((X509Certificate)certificado).checkValidity();
                return (X509Certificate) certificado;
            } catch (Exception e) {
                return null;
            }
        } 
        return null;
    }

    public String extrairNomeDoCertificadoDigital(X509Certificate certificado) {
        String identificador = certificado.getSubjectX500Principal().getName();
        String[] partes = identificador.split(",");
        for (String parte : partes) {
            if (parte.trim().startsWith("CN=")) {
                return parte.substring(3);
            }
        }
        return null;
    }

    public String extrairEmailDoCertificadoDigital(X509Certificate certificado) throws CertificateParsingException {
        String identificador = certificado.getSubjectX500Principal().toString();
        String[] partes = identificador.split(",");
        for (String parte : partes) {
            if (parte.trim().startsWith("EMAILADDRESS=")) {
                return parte.substring("EMAILADDRESS=".length());
            }
        }
        return null;
    }

    public void extrairVersaoSerieValidadeTipoDeAssinaturaEmissorDoCertificadoDigital(X509Certificate certificado) throws CertificateParsingException {
        // todo
    }

    public PrivateKey generateObjetoChavePrivada(String caminhoChavePrivada, String fraseSecreta) {
        try {
            FileInputStream chavePrivadaInputStream = new FileInputStream(caminhoChavePrivada);
            byte[] chavePrivadaBytes = chavePrivadaInputStream.readAllBytes();
            chavePrivadaInputStream.close();

            // Descriptografando a chave privada utilizando a frase secreta
            SecretKey Kaes = ChaveSecreta.generateKaes(fraseSecreta);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, Kaes);
            byte[] chavePrivadaDescriptografada = cipher.doFinal(chavePrivadaBytes);

            // Decodificando a chave privada descriptografada do formato PEM para o formato binário
            PKCS8EncodedKeySpec chavePrivadaSpec = new PKCS8EncodedKeySpec(chavePrivadaDescriptografada);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");      // duvida: qual o algoritmo da chave?
            PrivateKey chavePrivada = keyFactory.generatePrivate(chavePrivadaSpec);
            
            return chavePrivada;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public boolean verificaChavePrivada(PrivateKey chavePrivada, X509Certificate certificado) {
        try {
            // Gerando array aleatório de 4096 bytes
            byte[] arrayAleatorio = new byte[4096];
            new SecureRandom().nextBytes(arrayAleatorio);

            // Assinando o array aleatório usando a chave privada
            Signature assinatura = Signature.getInstance("SHA256withRSA");      // duvida: qual o algoritmo?    
            assinatura.initSign(chavePrivada);
            assinatura.update(arrayAleatorio);
            byte[] assinaturaBytes = assinatura.sign();

            // Verificando a assinatura usando a chave pública
            PublicKey chavePublica = certificado.getPublicKey();
            assinatura.initVerify(chavePublica);
            assinatura.update(arrayAleatorio);
            boolean chavePrivadaVerificada = assinatura.verify(assinaturaBytes);

            return chavePrivadaVerificada;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
   
    public String generateHashDaSenha(String senha) {
        // Gerando um SALT aleatório
        int tamanhoSalt = 16; // 16 bytes = 128 bits
        byte[] saltBytes = new byte[tamanhoSalt];
        new SecureRandom().nextBytes(saltBytes);

        // Convertendo os bytes em uma string Base64
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        // Gerando o hash para a senha informada
        String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt.getBytes(), 12);
        return hash;
    }
    
 }

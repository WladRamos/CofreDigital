public class CadastraUsuario {
    private String email, nome, hashSenha, chaveSecretaTOTP,    // Tabela Usuarios
    chavePrivadaCriptografada, certificadoDigital, // Tabela Chaveiro
    grupo; // Tabela Grupos
    
    // Construtor da Classe
    public CadastraUsuario (String caminhoCertificadoDigital, String caminhoArquivoChavePrivada, String fraseSecreta) {
        String certificadoDigital = getCertificadoDigital(caminhoCertificadoDigital);
        String chavePrivada = getChavePrivada(caminhoArquivoChavePrivada);
        // Checar se não são nulas e se a chave privada é equivalente ao certificado digital
        // e a frase secreta verirfica a chave privada
        // Se estiver tudo ok, cria a intância e inicializa esses parâmetros privados da classe
        // Caso contrário da uma exception
        if (fraseSecretaConfereChavePrivada()){
            // continua...
        }
        this.certificadoDigital = certificadoDigital;
        this.chavePrivadaCriptografada = criptografaChavePrivada(chavePrivada);
    }

    // Retona a semente do TOTP, para a interface informar ao usuário para que ele a cadastre no gerador do Google
    public String cadastraUsuario(String grupoUsuario, String senhaPessoal){
        // Se certificado e chavePrivada não forem nulos, prossegue com o cadastro
        setEmailENome();
        setHashDaSenhaPessoal(senhaPessoal);
        setChaveSecretaTOTP();
        insereUsuarioNoBancoDeDados();
        return chaveSecretaTOTP;
        // Caso contrário, retorna NULL
    }

    private String getCertificadoDigital (String caminhoCertificadoDigital) {
        return "certificado";
    }

    private String getChavePrivada(String caminhoArquivoChavePrivada){
        return "chave";
    }

    private String criptografaChavePrivada(String chavePrivada){
        // cripitografa chave privada antes de colocar no banco
        return "chaveCriptografada";
    }

    private void setEmailENome(){

    }

    private void setHashDaSenhaPessoal(String senhaPessoal){

    }

    private void setChaveSecretaTOTP(){

    }

    private boolean fraseSecretaConfereChavePrivada(){
        // Usar a frase secreta para alguma coisa!!
        return true;
    }

    private boolean insereUsuarioNoBancoDeDados(){
        return true;
    }

}

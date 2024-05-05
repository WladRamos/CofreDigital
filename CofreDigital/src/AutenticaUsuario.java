import java.util.ArrayList;

public class AutenticaUsuario {

    public static String[] usuarioExiste(String email){
        // Se usuário existe no banco, retorna as informações do usuário
        String[] usuario = {"nome", "grupo", "acessos"};
        return usuario;
        // Se usuário não existir, retorna NULL
    }

    public static boolean verificaSenha(ArrayList<String[]> senha){
        // Verificar a árvore de possíveis senhas
        return true;
    }

    public static boolean verificaTOTP(String totp){
        return true;
    }

 }

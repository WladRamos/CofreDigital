public class Main {
    public static void main(String[] args) {
        Database banco = Database.getInstance();
        int nUsuarios = banco.countUsuariosNoSistema();
        if (nUsuarios == -1){
            //fazer jpanel de erro
            System.exit(1);
        }
        else if(nUsuarios == 0) {
            new InterfaceCofreDigital(0);
        }
        else{
            new InterfaceCofreDigital(1);
        }
    }
}

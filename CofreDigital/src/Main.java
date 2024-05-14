import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        Database database = Database.getInstance();
        int nUsuarios = database.countUsuariosNoSistema(); 
        if (nUsuarios == -1){
            JOptionPane.showMessageDialog(
                null,
                "Falha ao conectar com o banco de dados. \nTente novamente mais tarde.", 
                "Warning", JOptionPane.ERROR_MESSAGE
            );
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

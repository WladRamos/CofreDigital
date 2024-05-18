/*  
INF1416 - Segurança da Informação - 2024.1 - 3WA
T4: Cofre Digital - Prof.: Anderson Oliveira da Silva
Nome: Marina Schuler Martins Matrícula: 2110075
Nome: Wladimir Calazam de Araujo Goes Ramos Matrícula: 2110104
*/

import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        Database database = Database.getInstance();
        int nUsuarios = database.countUsuariosNoSistema(); 
        if (nUsuarios == -1){
            JOptionPane.showMessageDialog(
                null,
                "Falha ao conectar com o banco de dados. \nTente novamente mais tarde.", 
                "Aviso", JOptionPane.ERROR_MESSAGE
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

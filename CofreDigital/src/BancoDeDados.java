import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BancoDeDados {

    private static final String URL = "jdbc:mysql://localhost:3306/meu_banco_de_dados";
    private static final String USUARIO = "seu_usuario";
    private static final String SENHA = "sua_senha";

    private Connection connection;

    public BancoDeDados() {
        try {
            this.connection = DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void fecharConexao() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void criarTabela() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS minha_tabela (id INT PRIMARY KEY AUTO_INCREMENT, nome VARCHAR(255))";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Outros métodos para manipular o banco de dados, como inserir, atualizar, excluir registros, etc.
}

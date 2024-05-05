import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

public class DatabaseTest {

    @Test
    public void testConnectionToDatabase() {
        Database database = Database.getInstance();
        Connection connection = database.getConnection();
        assertNotNull(connection);
    }

    @Test
    public void testInitDatabase() {
        Database database = Database.getInstance();
        database.initDatabase(); // Inicializa o banco de dados

        // Verifica se as tabelas foram criadas corretamente
        try (Connection connection = database.getConnection()) {
            String[] tables = {"Chaveiro", "Grupos", "Mensagens", "Usuarios", "Registros"};
            for (String table : tables) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table);
                     ResultSet resultSet = statement.executeQuery()) {
                    // Verifica se a consulta à tabela não lança exceção
                    // Indica que a tabela existe
                    assertEquals(true, resultSet.next());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

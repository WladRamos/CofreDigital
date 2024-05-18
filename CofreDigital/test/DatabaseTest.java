import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

        // Verifica se as tabelas foram criadas corretamente
        try {
            Connection connection = database.getConnection();
            String[] tables = {"Chaveiro", "Grupos", "Mensagens", "Usuarios", "Registros"};
            int[] expectedCounts = {0, 2, 57, 0, 0}; // Chaveiro, Grupos, Mensagens, Usuarios, Registros
            
            for (int i = 0; i < tables.length; i++) {
                String table = tables[i];
                // Verifica se a tabela existe no banco de dados
                boolean tableExists = false;
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT EXISTS (" +
                    "    SELECT 1 " +
                    "    FROM information_schema.tables " +
                    "    WHERE table_schema = ? " +
                    "    AND table_name = ?" +
                    ")"
                );
                statement.setString(1, "CofreDigital");
                statement.setString(2, table);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    tableExists = resultSet.getInt(1) == 1;
                }
                assertTrue("Tabela " + table + " não encontrada.", tableExists);

                // Verifica se quantidade de entradas na tabela está correta
                if (tableExists) {
                    if (table.equals("Mensagens") || table.equals("Grupos")) {
                        statement = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
                        ResultSet countResult = statement.executeQuery();
                        int count = 0;
                        if (countResult.next()) {
                            count = countResult.getInt(1);
                        }
                        assertEquals("Quantidade incorreta de entradas na tabela " + table, expectedCounts[i], count);
                    }
                }
            }        
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testInsertIntoRegistros() {
        Database database = Database.getInstance();
        try {
            boolean inserted = database.insertIntoRegistros(1001, -1, null);
            assertTrue(inserted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static Database database;
    private Connection connection;

    private Database() {
        try {
            String url = "jdbc:mysql://localhost:3306/CofreDigital";
            String usuario = "dbuser";
            String senha = "mysqlinf1416";
            this.connection = DriverManager.getConnection(url, usuario, senha);
            createDatabaseIfNotExists();
            this.connection.setCatalog("CofreDigital");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Database getInstance() {
        if (database == null) {
            database = new Database();
        }
        return database;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE DATABASE IF NOT EXISTS CofreDigital";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initDatabase() {
        /* Cria as tabelas do banco de dados CofreDigital, se já não existirem */
        createTableChaveiro(); 
        createTableGrupos(); 
        createTableMensagens();
        createTableUsuarios(); 
        createTableRegistros();

        /* Popula a tabela Mensagens com as mensagens de registro pré-definidas */
        populateTableMensagens();
        try {
            int countMsg = countTableEntries("Mensagens");
            if (countMsg != 57) {
                System.out.println("Err: Há 57 mensagens de registro pré-definidas e" + countMsg + " entradas na tabela Mensagens.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Popula a tabela Grupos com os grupos pré-definidos */
        populateTableGrupos();
        try {
            int countGrp = countTableEntries("Grupos");
            if (countGrp != 57) {
                System.out.println("Err: Há 2 grupos pré-definidos e" + countGrp + " entradas na tabela Grupos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTableUsuarios() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Usuarios (" +
                         "UID INT PRIMARY KEY AUTO_INCREMENT," +
                         "email VARCHAR(300) NOT NULL UNIQUE," +
                         "nome VARCHAR(200) NOT NULL," +
                         "hash VARCHAR(60) NOT NULL," +
                         "chave_secreta VARCHAR(255) NOT NULL," +    // Não tenho certeza desse tipo para a chave_secreta
                         "chaveiro_fk INT FOREIGN KEY REFERENCES Chaveiro(KID)," +
                         "grupo_fk INT FOREIGN KEY REFERENCES Grupos(GID)" +
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableChaveiro() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Chaveiro (" +
                         "KID INT PRIMARY KEY AUTO_INCREMENT," +
                         "chave_privada_criptografada BLOB NOT NULL," +
                         "certificado_digital TEXT NOT NULL," +
                         "CONSTRAINT unique_chave_certificado UNIQUE (chave_privada, certificado_digital)" +
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableGrupos() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Grupos (" +
                         "GID INT PRIMARY KEY AUTO_INCREMENT," +
                         "nome_grupo VARCHAR(30) NOT NULL UNIQUE" +
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }

    private void createTableMensagens() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Mensagens (" +
                         "MID INT PRIMARY KEY AUTO_INCREMENT," +
                         "mensagem VARCHAR(255) NOT NULL UNIQUE" +
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTableRegistros() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Registros (" +
                         "RID BIGINT PRIMARY KEY AUTO_INCREMENT," +
                         "mensagem_fk INT FOREIGN KEY REFERENCES Mensagens(MID)," +
                         "Data DATETIME," +
                         "usuario_fk INT FOREIGN KEY REFERENCES Usuarios(UID)," +
                         "arquivo_selecionado_decriptacao VARCHAR(255)" +   // Não tenho certeza desse tipo
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTableMensagens(){
        try {
            int count = countTableEntries("Mensagens");
            if (count == 0) {
                String sql = "INSERT INTO Mensagens (MID, mensagem) VALUES (1001, 'Sistema iniciado.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (1002, 'Sistema encerrado.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (1003, 'Sessão iniciada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (1004, 'Sessão encerrada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (2001, 'Autenticação etapa 1 iniciada.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (2002, 'Autenticação etapa 1 encerrada.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (2003, 'Login name <login_name> identificado com acesso liberado.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (2004, 'Login name <login_name> identificado com acesso bloqueado.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (2005, 'Login name <login_name> não identificado.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3001, 'Autenticação etapa 2 iniciada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3002, 'Autenticação etapa 2 encerrada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3003, 'Senha pessoal verificada positivamente para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3004, 'Primeiro erro da senha pessoal contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3005, 'Segundo erro da senha pessoal contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3006, 'Terceiro erro da senha pessoal contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (3007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4001, 'Autenticação etapa 3 iniciada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4002, 'Autenticação etapa 3 encerrada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4003, 'Token verificado positivamente para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4004, 'Primeiro erro de token contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4005, 'Segundo erro de token contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4006, 'Terceiro erro de token contabilizado para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (4007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (5001, 'Tela principal apresentada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (5002, 'Opção 1 do menu principal selecionada por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (5003, 'Opção 2 do menu principal selecionada por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (5004, 'Opção 3 do menu principal selecionada por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6001, 'Tela de cadastro apresentada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6002, 'Botão cadastrar pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6003, 'Senha pessoal inválida fornecida por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6004, 'Caminho do certificado digital inválido fornecido por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6005, 'Chave privada verificada negativamente para <login_name> (caminho inválido).');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6006, 'Chave privada verificada negativamente para <login_name> (frase secreta inválida).');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6007, 'Chave privada verificada negativamente para <login_name> (assinatura digital inválida).');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6008, 'Confirmação de dados aceita por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6009, 'Confirmação de dados rejeitada por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (6010, 'Botão voltar de cadastro para o menu principal pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7001, 'Tela de consulta de arquivos secretos apresentada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7002, 'Botão voltar de consulta para o menu principal pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7003, 'Botão Listar de consulta pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7004, 'Caminho de pasta inválido fornecido por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7005, 'Arquivo de índice decriptado com sucesso para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7006, 'Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7007, 'Falha na decriptação do arquivo de índice para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7008, 'Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7009, 'Lista de arquivos presentes no índice apresentada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7010, 'Arquivo <arq_name> selecionado por <login_name> para decriptação.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7011, 'Acesso permitido ao arquivo <arq_name> para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7012, 'Acesso negado ao arquivo <arq_name> para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7013, 'Arquivo <arq_name> decriptado com sucesso para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7014, 'Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7015, 'Falha na decriptação do arquivo <arq_name> para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (7016, 'Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (8001, 'Tela de saída apresentada para <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (8002, 'Botão encerrar sessão pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (8003, 'Botão encerrar sistema pressionado por <login_name>.');" +
                             "INSERT INTO Mensagens (MID, mensagem) VALUES (8004, 'Botão voltar de sair para o menu principal pressionado por <login_name>.');";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                System.out.println("populateTableMensagens() abortada. Tabela Mensagens não está vazia.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTableGrupos(){
        try {
            int count = countTableEntries("Grupos");
            if (count == 0) {
                String sql = "INSERT INTO Grupos (GID, nome_grupo) VALUES (1, 'Administrador');" +
                             "INSERT INTO Grupos (GID, nome_grupo) VALUES (2, 'Usuário comum');";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
                System.out.println("populateTableGrupos() abortada. Tabela Grupos não está vazia.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int countTableEntries(String table){
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table);
            resultSet.next();
            int count = resultSet.getInt(1);
            resultSet.close();
            return count;
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }
    
}

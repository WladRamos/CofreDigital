import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Database {

    private static Database database;
    private Connection connection;

    private Database() {
        try {
            String url = "jdbc:mysql://localhost:3306";
            String usuario = "root";
            String senha = "mysql";
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
                System.out.println("Erro: Há 57 mensagens de registro pré-definidas e " + countMsg + " entradas na tabela Mensagens.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Popula a tabela Grupos com os grupos pré-definidos */
        populateTableGrupos();
        try {
            int countGrp = countTableEntries("Grupos");
            if (countGrp != 2) {
                System.out.println("Erro: Há 2 grupos pré-definidos e " + countGrp + " entradas na tabela Grupos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Criação das Tabelas do Banco de Dados CofreDigital

    private void createTableUsuarios() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Usuarios (" +
                         "UID INT PRIMARY KEY AUTO_INCREMENT," +
                         "email VARCHAR(300) NOT NULL UNIQUE," +
                         "nome VARCHAR(200) NOT NULL," +
                         "hash VARCHAR(60) NOT NULL," +
                         "chave_secreta VARCHAR(255) NOT NULL," +    // Não tenho certeza desse tipo para a chave_secreta
                         "chaveiro_fk INT," +
                         "grupo_fk INT," +
                         "FOREIGN KEY (chaveiro_fk) REFERENCES Chaveiro(KID)," +
                         "FOREIGN KEY (grupo_fk) REFERENCES Grupos(GID)" +
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
                         "chave_privada_criptografada BLOB(512) NOT NULL," +    // Checar esse tamanhos depois
                         "certificado_digital TEXT(512) NOT NULL," +
                         "CONSTRAINT unique_chave_certificado UNIQUE (chave_privada_criptografada(512), certificado_digital(512))" +
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
                         "mensagem_fk INT," +
                         "timestamp DATETIME," +
                         "usuario_fk INT," +
                         "arquivo_selecionado_decriptacao VARCHAR(255)," +      // Não tenho certeza desse tipo 
                         "FOREIGN KEY (mensagem_fk) REFERENCES Mensagens(MID)," +
                         "FOREIGN KEY (usuario_fk) REFERENCES Usuarios(UID)" +
                         ")";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Populando as Tabelas Mensagens e Grupos com as entradas pré-definidas

    private void populateTableMensagens(){
        try {
            int count = countTableEntries("Mensagens");
            if (count == 0) {
                Statement statement = connection.createStatement();
        
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (1001, 'Sistema iniciado.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (1002, 'Sistema encerrado.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (1003, 'Sessão iniciada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (1004, 'Sessão encerrada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (2001, 'Autenticação etapa 1 iniciada.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (2002, 'Autenticação etapa 1 encerrada.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (2003, 'Login name <login_name> identificado com acesso liberado.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (2004, 'Login name <login_name> identificado com acesso bloqueado.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (2005, 'Login name <login_name> não identificado.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3001, 'Autenticação etapa 2 iniciada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3002, 'Autenticação etapa 2 encerrada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3003, 'Senha pessoal verificada positivamente para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3004, 'Primeiro erro da senha pessoal contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3005, 'Segundo erro da senha pessoal contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3006, 'Terceiro erro da senha pessoal contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (3007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4001, 'Autenticação etapa 3 iniciada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4002, 'Autenticação etapa 3 encerrada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4003, 'Token verificado positivamente para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4004, 'Primeiro erro de token contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4005, 'Segundo erro de token contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4006, 'Terceiro erro de token contabilizado para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (4007, 'Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (5001, 'Tela principal apresentada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (5002, 'Opção 1 do menu principal selecionada por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (5003, 'Opção 2 do menu principal selecionada por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (5004, 'Opção 3 do menu principal selecionada por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6001, 'Tela de cadastro apresentada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6002, 'Botão cadastrar pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6003, 'Senha pessoal inválida fornecida por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6004, 'Caminho do certificado digital inválido fornecido por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6005, 'Chave privada verificada negativamente para <login_name> (caminho inválido).')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6006, 'Chave privada verificada negativamente para <login_name> (frase secreta inválida).')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6007, 'Chave privada verificada negativamente para <login_name> (assinatura digital inválida).')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6008, 'Confirmação de dados aceita por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6009, 'Confirmação de dados rejeitada por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (6010, 'Botão voltar de cadastro para o menu principal pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7001, 'Tela de consulta de arquivos secretos apresentada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7002, 'Botão voltar de consulta para o menu principal pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7003, 'Botão Listar de consulta pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7004, 'Caminho de pasta inválido fornecido por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7005, 'Arquivo de índice decriptado com sucesso para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7006, 'Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7007, 'Falha na decriptação do arquivo de índice para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7008, 'Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7009, 'Lista de arquivos presentes no índice apresentada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7010, 'Arquivo <arq_name> selecionado por <login_name> para decriptação.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7011, 'Acesso permitido ao arquivo <arq_name> para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7012, 'Acesso negado ao arquivo <arq_name> para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7013, 'Arquivo <arq_name> decriptado com sucesso para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7014, 'Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7015, 'Falha na decriptação do arquivo <arq_name> para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (7016, 'Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (8001, 'Tela de saída apresentada para <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (8002, 'Botão encerrar sessão pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (8003, 'Botão encerrar sistema pressionado por <login_name>.')");
                statement.executeUpdate("INSERT INTO Mensagens (MID, mensagem) VALUES (8004, 'Botão voltar de sair para o menu principal pressionado por <login_name>.')");

                statement.close();
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
                Statement statement = connection.createStatement();
        
                statement.executeUpdate("INSERT INTO Grupos (GID, nome_grupo) VALUES (1, 'Administrador')");
                statement.executeUpdate("INSERT INTO Grupos (GID, nome_grupo) VALUES (2, 'Usuário')");
                
                statement.close();
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
 
    // Métodos de Busca e Manipulação no Banco de Dados CofreDigital

    public int getUIDdoUsuarioIfExists(String email) {
        int uid = -1;
        String sql = "SELECT UID " +
                     "FROM Usuarios u " +
                     "WHERE u.email = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                uid = resultSet.getInt("UID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return uid;     // Se usuário não encontrado, retorna -1
    }

    public HashMap<String, String> getinformacoesDoUsuario(int uid){
        HashMap<String, String> info = null;
        String sql = "SELECT UID, nome, grupo_nome " +
                     "FROM Usuarios u " +
                     "JOIN Grupos g ON u.grupo_fk = g.GID " +
                     "WHERE u.UID = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String grupo = resultSet.getString("grupo_nome");
                String numero_de_acessos = countAcessosDoUsuario(uid);
                // Inserindo as informações do usuário no HashMap
                info = new HashMap<>();
                info.put("nome", nome);
                info.put("grupo", grupo);
                info.put("numero_de_acessos", numero_de_acessos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return info;     // Se usuário não encontrado, retorna null
    }

    private String countAcessosDoUsuario(int uid){
        String n_acessos = null;
        String sql = "SELECT COUNT(*) AS n_acessos " +
                     "FROM Registros " +
                     "WHERE usuario_fk = ? AND mensagem_fk = 2003";     // duvida: conferir se a mensagem 2003 é o que configura um acesso (acho que sim)
        
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                n_acessos = resultSet.getString("n_acessos");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return n_acessos;     // Se usuário não encontrado, retorna null
    }

    public String getHashDoUsuario(int uid){
        String hash = null;
        String sql = "SELECT hash " +
                     "FROM Usuarios u " +
                     "WHERE u.UID = ?";
        
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                hash = resultSet.getString("hash");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return hash;     // Se usuário não encontrado, retorna null
    }

    public String getChaveSecretaDoUsuario(int uid) {
        String chaveSecreta = null;
        String sql = "SELECT chave_secreta " +
                     "FROM Usuarios u " +
                     "WHERE u.UID = ?";
        
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                chaveSecreta = resultSet.getString("chave_secreta");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return chaveSecreta;     // Se usuário não encontrado, retorna null
    }

    public int countUsuariosNoSistema() {
        int countUsuarios = -1;
        String sql = "SELECT COUNT(*) AS total_usuarios FROM Usuarios";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                countUsuarios = resultSet.getInt("total_usuarios");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return countUsuarios;   // Se houver algum erro na consulta, retorna -1
    }

    public int countAberturasDeArquivosUsuario(int uid) {
        int countAberturas = -1;
        String sql = "SELECT COUNT(*) AS n_aberturas_arquivos " +
                     "FROM Registros " +
                     "WHERE usuario_fk = ? AND mensagem_fk = 7014";     // duvida: conferir se a mensagem 7014 é o que configura um acesso ao arquivo (acho que sim)
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                countAberturas = resultSet.getInt("n_aberturas_arquivos");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return countAberturas;   // Se houver algum erro na consulta, retorna -1
    }

}

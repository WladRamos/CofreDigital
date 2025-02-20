/*  
INF1416 - Segurança da Informação - 2024.1 - 3WA
T4: Cofre Digital - Prof.: Anderson Oliveira da Silva
Nome: Marina Schuler Martins Matrícula: 2110075
Nome: Wladimir Calazam de Araujo Goes Ramos Matrícula: 2110104
*/

import java.time.*;
import java.sql.*;
import java.util.*;

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
            initDatabaseIfNotInitialized();
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

    private void initDatabaseIfNotInitialized() {
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
                         "email VARCHAR(250) NOT NULL UNIQUE," +
                         "nome VARCHAR(250) NOT NULL," +
                         "hash VARCHAR(60) NOT NULL," +
                         "chave_secreta BLOB(48) NOT NULL," +
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
                         "chave_privada_criptografada BLOB(512) NOT NULL," +  
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
                         "arquivo VARCHAR(255)," +
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

    private void populateTableMensagens() {
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
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTableGrupos() {
        try {
            int count = countTableEntries("Grupos");
            if (count == 0) {
                Statement statement = connection.createStatement();
        
                statement.executeUpdate("INSERT INTO Grupos (GID, nome_grupo) VALUES (1, 'Administrador')");
                statement.executeUpdate("INSERT INTO Grupos (GID, nome_grupo) VALUES (2, 'Usuário')");
                
                statement.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Métodos auxiliares para consultas no banco de dados

    private int countTableEntries(String table) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table);
            if (resultSet.next()) {
               int count = resultSet.getInt(1);
                resultSet.close();
                return count; 
            }
        } catch (SQLException e){
            System.out.println("Exception: countTableEntries(String table)");
        }
        return -1;
    }

    private int countMessagesForUser(int uid, int mid) {
        String sql = "SELECT COUNT(*) AS count FROM Registros " +
                     "WHERE usuario_fk = ? AND mensagem_fk = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            statement.setInt(2, mid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
               int count = resultSet.getInt("count");
                return count; 
            }
        } catch (SQLException e) {
            System.out.println("Exception: countMessagesForUser(int uid, int mid)");
        }
        return -1;
    }

    private String replacePlaceholdersDaMensagem(String msg, String login_name, String arq_name) {
        if (msg.contains("<login_name>")) {
            if (login_name != null) {
               msg = msg.replace("<login_name>", login_name); 
            } else {
                msg = msg.replace("<login_name>", "usuário corrente não logado");
            }
        }
        if (msg.contains("<arq_name>")) {
            if (arq_name != null) {
                msg = msg.replace("<arq_name>", arq_name);
            } else {
                msg = msg.replace("<arq_name>", "arquivo não identificado");
            }
        }
        return msg;
    }

    // Métodos públicos para consultas no banco da dados

    public int getUsuarioIfExists(String email) {
        String sql = "SELECT UID FROM Usuarios u " +
                     "WHERE u.email = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int uid = resultSet.getInt("UID");
                return uid;
            }
        } catch (SQLException e) {
            System.out.println("Exception: getUsuarioIfExists(String email)");
        }
        return -1;
    }

    public HashMap<String, String> getInfoDoUsuario(int uid) {
        if (uid == -1)
            return null;
        
        String sql = "SELECT UID, nome, nome_grupo FROM Usuarios u " +
                     "JOIN Grupos g ON u.grupo_fk = g.GID " +
                     "WHERE u.UID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Inserindo as informações do usuário no HashMap    
                HashMap<String, String> info = new HashMap<>();
                info.put("nome", resultSet.getString("nome"));
                info.put("grupo", resultSet.getString("nome_grupo"));
                info.put("numero_de_acessos", Integer.toString(countMessagesForUser(uid, 2003)));   // duvida: conferir se a mensagem 2003 é o que configura um acesso
                return info;
            }           
        } catch (SQLException e) {
            System.out.println("Exception: getInfoDoUsuario(int uid)");
        }
        return null;
    }

    public String getHashDoUsuario(int uid) {
        String sql = "SELECT hash FROM Usuarios u WHERE u.UID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
               String hash = resultSet.getString("hash");
                return hash; 
            }
        } catch (SQLException e) {
            System.out.println("Exception: getHashDoUsuario(int uid)");
        }
        return null;
    }

    public byte[] getChaveSecretaCriptografadaDoUsuario(int uid) {
        String sql = "SELECT chave_secreta FROM Usuarios u WHERE u.UID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
              byte[] chaveSecreta = resultSet.getBytes("chave_secreta");
                return chaveSecreta;  
            }
        } catch (SQLException e) {
            System.out.println("Exception: getChaveSecretaCriptografadaDoUsuario(int uid)");
        }
        return null;
    }

    public byte[] getChavePrivadaCriptografadaDoUsuario(int uid) {
        String sql = "SELECT chave_privada_criptografada FROM Chaveiro c " +
                     "JOIN Usuarios u ON u.chaveiro_fk = c.KID WHERE u.UID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
               byte[] chave_privada_criptografada = resultSet.getBytes("chave_privada_criptografada");
                return chave_privada_criptografada; 
            }
        } catch (SQLException e) {
            System.out.println("Exception: getChavePrivadaCriptografadaDoUsuario(int uid)");            
        }
        return null;
    }

    public String getCertificadoDigitalDoUsuario(int uid) {
        String sql = "SELECT certificado_digital FROM Chaveiro c " +
                     "JOIN Usuarios u ON u.chaveiro_fk = c.KID WHERE u.UID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
              String certificado_digital = resultSet.getString("certificado_digital");
                return certificado_digital;  
            }
        } catch (SQLException e) {
            System.out.println("Exception: getCertificadoDigitalDoUsuario(int uid)");
        }
        return null;
    }
    
    public int countConsultasDoUsuario(int uid) {
        return countMessagesForUser(uid, 7014);     
        // duvida: conferir se a mensagem 7014 é o que configura uma consulta a arquivo
    }

    public int countUsuariosNoSistema() {
        return countTableEntries("Usuarios");
    }

    public boolean usuarioIsBlocked(int uid) {
        String sql = "SELECT timestamp FROM Registros " +
                     "WHERE usuario_fk = ? AND (mensagem_fk = 3007 OR mensagem_fk = 4007)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();

            Timestamp latestTimestamp = null;
            while (resultSet.next()) {
                Timestamp currentTimestamp = resultSet.getTimestamp("timestamp");
                if (latestTimestamp == null || currentTimestamp.after(latestTimestamp)) {
                    latestTimestamp = currentTimestamp;
                }
            }

            if (latestTimestamp != null) {
                Instant now = Instant.now();
                Instant latestInstant = latestTimestamp.toInstant();
                Duration duration = Duration.between(latestInstant, now);
                return duration.toMinutes() < 2;
            } else {
                return false; 
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public List<String> getLogsEmOrdemCronologica() {
        String sql = "SELECT r.RID, m.mensagem, r.timestamp, u.email, r.arquivo " +
                     "FROM Registros r " +
                     "JOIN Mensagens m ON r.mensagem_fk = m.MID " +
                     "LEFT JOIN Usuarios u ON r.usuario_fk = u.UID " +
                     "ORDER BY r.timestamp ASC";
        try {
            List<String> listaDeLogs = new ArrayList<>();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String mensagem = resultSet.getString("mensagem");   
                Timestamp timestamp = resultSet.getTimestamp("timestamp");
                String login_name = resultSet.getString("email");
                String arq_name = resultSet.getString("arquivo");

                String mensagem_formatada = replacePlaceholdersDaMensagem(mensagem, login_name, arq_name);
                String log = String.format("%s      %s", timestamp.toString(), mensagem_formatada);
                listaDeLogs.add(log);
            }
            return listaDeLogs;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Métodos auxiliares para manipulações no banco da dados
    
    private int getChaveiroDoUsuarioIfExists(int uid) {
        try {
            String sql = "SELECT chaveiro_fk FROM Usuarios WHERE UID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, uid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
               int chaveiro_fk = resultSet.getInt("chaveiro_fk");
                return chaveiro_fk; 
            }
        } catch (Exception e) {
            System.out.println("Exception: getChaveiroDoUsuarioIfExists(int uid)");
        }  
        return -1;      
    }
    
    private int insertIntoChaveiro(byte[] chave_privada_criptografada_bin, String certificado_digital_pem) {
        try {
            String sql = "INSERT INTO Chaveiro (chave_privada_criptografada, certificado_digital) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setBytes(1, chave_privada_criptografada_bin);
            statement.setString(2, certificado_digital_pem);
            int rowsInserted = statement.executeUpdate();
 
            if (rowsInserted > 0) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                   int kid = resultSet.getInt(1);
                    return kid; 
                }                                
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.out.println("Exception: insertIntoChaveiro(byte[] chave_privada_criptografada_bin, String certificado_digital_pem)");
        }
        return -1;
    }

    private boolean usuarioExists(int uid) {
        String sql = "SELECT COUNT(*) AS count FROM Usuarios u WHERE u.uid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, uid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }    

   // Métodos públicos para manipulações no banco da dados

    public boolean insertIntoUsuarios(
        String email, String nome, String hash, byte[] chave_secreta_criptografada, 
        byte[] chave_privada_criptografada_bin, String certificado_digital_pem, int grupo
    ) {
        try{
            int kid = insertIntoChaveiro(chave_privada_criptografada_bin, certificado_digital_pem);
            if (kid != -1) {
                String sql = "INSERT INTO Usuarios (email, nome, hash, chave_secreta, chaveiro_fk, grupo_fk) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, email);
                statement.setString(2, nome);
                statement.setString(3, hash);
                statement.setBytes(4, chave_secreta_criptografada);
                statement.setInt(5, kid);
                statement.setInt(6, grupo);

                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUsuarioAndChaveiroIfExists(String email) {
        try {
            boolean status = false;
            int uid = getUsuarioIfExists(email);
            if (uid != -1) {
                int kid = getChaveiroDoUsuarioIfExists(uid);
                if (kid != -1) {
                    // Excluindo o usuário da tabela Usuarios
                    String deleteUsuarioSQL = "DELETE FROM Usuarios WHERE UID = ?";
                    PreparedStatement deleteUsuarioStatement = connection.prepareStatement(deleteUsuarioSQL);
                    deleteUsuarioStatement.setInt(1, uid);
                    deleteUsuarioStatement.executeUpdate();
        
                    // Exclua o registro correspondente na tabela Chaveiro
                    String deleteChaveiroSQL = "DELETE FROM Chaveiro WHERE KID = ?";
                    PreparedStatement deleteChaveiroStatement = connection.prepareStatement(deleteChaveiroSQL);
                    deleteChaveiroStatement.setInt(1, kid);
                    deleteChaveiroStatement.executeUpdate(); 

                    status = true;
                } 
            } 
            return status;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    } 
    
    public boolean insertIntoRegistros(int mid, int uid, String arquivo) {
        try {
            LocalTime horaAtual = LocalTime.now();
            Timestamp timestamp = Timestamp.valueOf(horaAtual.atDate(java.time.LocalDate.now()));
    
            if (usuarioExists(uid)) {
                String sql = "INSERT INTO Registros (mensagem_fk, timestamp, usuario_fk, arquivo) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, mid);
                statement.setTimestamp(2, timestamp);
                statement.setInt(3, uid);
                statement.setString(4, arquivo);
                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0; 
            } else {
                String sql = "INSERT INTO Registros (mensagem_fk, timestamp, arquivo) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, mid);
                statement.setTimestamp(2, timestamp);
                statement.setString(3, arquivo);
                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }
    
}

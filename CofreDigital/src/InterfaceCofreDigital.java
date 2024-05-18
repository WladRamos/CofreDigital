import javax.crypto.SecretKey;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import static org.junit.Assert.fail;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.*;
import java.security.*;
import java.util.List;

public class InterfaceCofreDigital {
    private Database database;
    private Autenticacao autenticacao;
    private RecuperaArquivo recuperaArquivo;
    private JFrame janelaPrincipal;
    private JTextField campoTextoTOTP;
    private JPasswordField campoSenha;
    private int tentativasErradas = 0;
    private JPanel painelTeclas;
    private JButton botaoOK;
    private ArrayList<String[]> possibilidadesSenha = new ArrayList<>();
    private String grupoUsuario, nomeUsuario, qtdAcessosUsuario, emailUsuario;
    private int idUsuario = -1, idAdministrador = -1;
    private String fraseSecretaAdmin;
    private final String emailAdmin = "admin@inf1416.puc-rio.br";


    public InterfaceCofreDigital(int status) {
        this.database = Database.getInstance();
        database.insertIntoRegistros(1001, -1, null);   // Sistema iniciado.

        janelaPrincipal = new JFrame("Cofre Digital");
        janelaPrincipal.setSize(500, 200);
        janelaPrincipal.setLayout(new BorderLayout());
        janelaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (status == 0){
            mostrarTelaCadastro(0);
        }else{
            mostrarTelaFraseSecretaAdmin();
        }
    }

    private void mostrarTelaFraseSecretaAdmin() {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Criação de um painel intermediário para o cabeçalho e campo de frase secreta
        JPanel painelSuperior = new JPanel(new BorderLayout());    
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        painelSuperior.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelFraseSecreta = new JPanel(); // Usar FlowLayout por padrão, ou escolher outro se necessário
        JTextField campoFraseSecretaAdmin = new JTextField(20);
        painelFraseSecreta.add(new JLabel("Frase Secreta do Administrador:"));
        painelFraseSecreta.add(campoFraseSecretaAdmin);
        painelSuperior.add(painelFraseSecreta, BorderLayout.CENTER);
    
        // Adiciona o painel superior ao BorderLayout.NORTH da janela principal
        janelaPrincipal.add(painelSuperior, BorderLayout.NORTH);

        JPanel painelBotoes = new JPanel(new FlowLayout());

        JButton btnConfirma = new JButton("Confirmar");
        painelBotoes.add(btnConfirma);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        btnConfirma.addActionListener(e ->{
            String textoCampoFraseSecretaAdmin = campoFraseSecretaAdmin.getText();
            idAdministrador = database.getUsuarioIfExists(emailAdmin);
            byte[] chavePrivCript =  database.getChavePrivadaCriptografadaDoUsuario(idAdministrador);
            PrivateKey objPrivateKey = GestorDeSeguranca.generatePrivateKeyFromBIN(chavePrivCript, textoCampoFraseSecretaAdmin);
            if(objPrivateKey != null){
                fraseSecretaAdmin = textoCampoFraseSecretaAdmin;
                mostrarTelaNomeLogin();
            }
            else{
                JOptionPane.showMessageDialog(janelaPrincipal, "Chave Secreta do admin incorreta", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }

    private void mostrarTelaNomeLogin() {
        database.insertIntoRegistros(2001, -1, null);    // Autenticação etapa 1 iniciada.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());

        JPanel painelCabecalho = new JPanel();
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        painelCabecalho.add(rotuloCabecalho);

        JPanel painelEmail = new JPanel(new FlowLayout());
        JLabel rotuloEmail = new JLabel("Login name:");
        JTextField campoTextoEmail = new JTextField(20);
        
        painelEmail.add(rotuloEmail);
        painelEmail.add(campoTextoEmail);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton botaoLogin = new JButton("OK");

        campoTextoEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                boolean valido = campoTextoEmail.getText().matches("^[^@]+@[^@]+\\.(com|br)$");
                botaoLogin.setEnabled(valido);
            }
        });

        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoLogin);
        painelBotoes.add(botaoLimpar);
        botaoLogin.setEnabled(false);

        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        janelaPrincipal.add(painelEmail, BorderLayout.CENTER);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.setVisible(true);

        botaoLogin.addActionListener(e -> {
            String campoTextoEmailInput = campoTextoEmail.getText();
            int uid = database.getUsuarioIfExists(campoTextoEmailInput);
            HashMap<String, String> u = database.getInfoDoUsuario(uid);
            if (uid != -1 && u != null) {
                if (database.usuarioIsBlocked(uid)) {
                    database.insertIntoRegistros(2004, uid, null);    // Login name <login_name> identificado com acesso bloqueado.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Usuário identificado com acesso bloqueado. Aguarde 2 minutos para tentar novamente.", "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                } else { 
                    idUsuario = uid;
                    emailUsuario = campoTextoEmailInput;
                    nomeUsuario = u.get("nome");
                    grupoUsuario = u.get("grupo");
                    qtdAcessosUsuario = u.get("numero_de_acessos");
                    database.insertIntoRegistros(2003, uid, null);  // Login name <login_name> identificado com acesso liberado.
                    database.insertIntoRegistros(2002, -1, null);    // Autenticação etapa 1 encerrada.
                    mostrarTelaSenha();
                }
            } else {
                database.insertIntoRegistros(2005, -1, campoTextoEmailInput);  // Login name <login_name> não identificado.
                JOptionPane.showMessageDialog(janelaPrincipal, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        botaoLimpar.addActionListener(e -> campoTextoEmail.setText(""));
        janelaPrincipal.pack();
    }

    private void mostrarTelaSenha() {
        possibilidadesSenha.clear();
        database.insertIntoRegistros(3001, idUsuario, null);    // Autenticação etapa 2 iniciada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        janelaPrincipal.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelSenha = new JPanel();
        campoSenha = new JPasswordField(20);
        campoSenha.setEditable(true);
        painelSenha.add(new JLabel("Senha pessoal:"));
        painelSenha.add(campoSenha);
        janelaPrincipal.add(painelSenha, BorderLayout.NORTH);
    
        painelTeclas = new JPanel(new GridLayout(2, 5));
        redistribuirNumeros();
    
        JPanel painelControle = new JPanel(new FlowLayout());
        botaoOK = new JButton("OK");
        botaoOK.setEnabled(false);
        JButton botaoLimpar = new JButton("LIMPAR");
        painelControle.add(botaoOK);
        painelControle.add(botaoLimpar);
    
        botaoLimpar.addActionListener(e -> {
            possibilidadesSenha.clear();
            campoSenha.setText("");
            botaoOK.setEnabled(false);
        });

        botaoOK.addActionListener(e -> {
            autenticacao = new Autenticacao(idUsuario);
            Boolean senhaValidada = autenticacao.verificaSenha(possibilidadesSenha);
            if (senhaValidada) {
                tentativasErradas = 0;
                database.insertIntoRegistros(3003, idUsuario, null);    // Senha pessoal verificada positivamente para <login_name>.
                database.insertIntoRegistros(3002, idUsuario, null);    // Autenticação etapa 2 encerrada para <login_name>.
                mostrarTelaTOTP();
            } else {
                tentativasErradas++;
                if (tentativasErradas == 1) {
                    database.insertIntoRegistros(3004, idUsuario, null);    // Primeiro erro da senha pessoal contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Senha Incorreta (x1).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradas == 2) {
                    database.insertIntoRegistros(3005, idUsuario, null);    // Segundo erro da senha pessoal contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Senha Incorreta (x2).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradas >= 3) {
                    database.insertIntoRegistros(3006, idUsuario, null);    // Terceiro erro da senha pessoal contabilizado para <login_name>.
                    database.insertIntoRegistros(3007, idUsuario, null);    // Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Acesso bloqueado por 2 minutos após três tentativas incorretas.", "Bloqueio de Acesso", JOptionPane.ERROR_MESSAGE);
                    database.insertIntoRegistros(3002, idUsuario, null);    // Autenticação etapa 2 encerrada para <login_name>.
                    mostrarTelaNomeLogin();
                }
            }
        });
    
        janelaPrincipal.add(painelTeclas, BorderLayout.CENTER);
        janelaPrincipal.add(painelControle, BorderLayout.SOUTH);
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }
    
    private void validarComprimentoSenha() {
        int comprimento = campoSenha.getPassword().length;
        botaoOK.setEnabled(comprimento >= 8 && comprimento <= 10);
    }
    
    private void redistribuirNumeros() {
        painelTeclas.removeAll();
        ArrayList<Integer> numeros = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numeros.add(i);
        }
        Collections.shuffle(numeros);
    
        for (int i = 0; i < numeros.size(); i += 2) {
            int num1 = numeros.get(i);
            int num2 = numeros.get(i + 1);
            JButton botao = new JButton(num1 + " " + num2);
            painelTeclas.add(botao);
            botao.addActionListener(e -> {
                String[] nums = botao.getText().split(" ");
                possibilidadesSenha.add(nums);
                campoSenha.setText(campoSenha.getText() + "*");
                validarComprimentoSenha();
                redistribuirNumeros();
                janelaPrincipal.revalidate();
                janelaPrincipal.repaint();
            });
        }
    }

    private void mostrarTelaTOTP() {
        database.insertIntoRegistros(4001, idUsuario, null);    // Autenticação etapa 3 iniciada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        janelaPrincipal.add(rotuloCabecalho, BorderLayout.NORTH);

        JPanel painelTOTP = new JPanel(new FlowLayout());
        JLabel rotuloTOTP = new JLabel("TOTP:");
        campoTextoTOTP = new JTextField(20);
    
        painelTOTP.add(rotuloTOTP);
        painelTOTP.add(campoTextoTOTP);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton botaoOkTOTP = new JButton("OK");

        campoTextoTOTP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                botaoOkTOTP.setEnabled(campoTextoTOTP.getText().length() == 6 && campoTextoTOTP.getText().matches("\\d{6}"));
            }
        });

        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoOkTOTP);
        painelBotoes.add(botaoLimpar);
        botaoOkTOTP.setEnabled(false);

        janelaPrincipal.add(painelTOTP, BorderLayout.CENTER);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.setVisible(true);

        String inputTOTPvalido = getValidTOTP();
        System.out.println("valid TOTP: " + inputTOTPvalido);

        botaoOkTOTP.addActionListener(e -> {
            Boolean TOTPValidado = autenticacao.verificaTOTP(campoTextoTOTP.getText());
            if (TOTPValidado) {
                tentativasErradas = 0;
                database.insertIntoRegistros(4003, idUsuario, null);    // Token verificado positivamente para <login_name>.
                database.insertIntoRegistros(4002, idUsuario, null);    // Autenticação etapa 3 encerrada para <login_name>.
                database.insertIntoRegistros(1003, idUsuario, null);    // Sessão iniciada para <login_name>.
                mostrarTelaMenu();
            } else {
                tentativasErradas++;
                if (tentativasErradas == 1) {
                    database.insertIntoRegistros(4004, idUsuario, null);    // Primeiro erro de token contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto (x1).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradas == 2) {
                    database.insertIntoRegistros(4005, idUsuario, null);    // Segundo erro de token contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto (x2).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradas >= 3) {
                    database.insertIntoRegistros(4006, idUsuario, null);    // Terceiro erro de token contabilizado para <login_name>.
                    database.insertIntoRegistros(4007, idUsuario, null);    // Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Acesso bloqueado por 2 minutos após três tentativas incorretas.", "Bloqueio de Acesso", JOptionPane.ERROR_MESSAGE);
                    database.insertIntoRegistros(4002, idUsuario, null);    // Autenticação etapa 3 encerrada para <login_name>.
                    mostrarTelaNomeLogin();
                }
            }
        });
        botaoLimpar.addActionListener(e -> campoTextoTOTP.setText(""));
    }

    private String getValidTOTP() {
        try {
            int userID = database.getUsuarioIfExists(emailUsuario);
            byte[] chaveSecretaCodificadaBase32Cifrada = database.getChaveSecretaCriptografadaDoUsuario(userID);
            SecretKey chaveAES = GestorDeSeguranca.generateKaes("12345678");
            // Decriptar a chave secreta com a Kaes gerada
            byte[] chaveSecretaCodificadaBase32Array = GestorDeSeguranca.decryptChave(chaveSecretaCodificadaBase32Cifrada, chaveAES);
            String chaveSecretaCodificadaBase32 = new String(chaveSecretaCodificadaBase32Array, "UTF8");
            // Retornar o código TOTP esperado para a validação correta
            TOTP totp = new TOTP(chaveSecretaCodificadaBase32, 30);
            return totp.generateCode();
        } catch(Exception e) {
            fail("Erro ao pegar o TOTP válido: " + e.getMessage());
            return null;
        }
    }

    private void mostrarTelaMenu() {
        database.insertIntoRegistros(5001, idUsuario, null);    // Tela principal apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());

        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        painelCorpo1.add(new JLabel("Total de acessos do usuário: " + qtdAcessosUsuario));
        janelaPrincipal.add(painelCorpo1, BorderLayout.CENTER);
    
        JPanel painelCorpo2 = new JPanel(new GridLayout(3, 1));

        if ("Administrador".equals(grupoUsuario)) {
            JButton botaoCadastrar = new JButton("Cadastrar um novo usuário");
            painelCorpo2.add(botaoCadastrar);
            botaoCadastrar.addActionListener(e -> {
                database.insertIntoRegistros(5002, idUsuario, null);    // Opção 1 do menu principal selecionada por <login_name>.
                mostrarTelaCadastro(1);
            });
        }
        JButton botaoConsultar = new JButton("Consultar pasta de arquivos secretos do usuário");
        JButton botaoSair = new JButton("Sair do Sistema");
    
        painelCorpo2.add(botaoConsultar);
        painelCorpo2.add(botaoSair);
        janelaPrincipal.add(painelCorpo2, BorderLayout.SOUTH);
    
        // Definindo ações para os botões
        botaoConsultar.addActionListener(e -> {
            database.insertIntoRegistros(5003, idUsuario, null);    // Opção 2 do menu principal selecionada por <login_name>.
            mostrarTelaConsulta();
        });
        botaoSair.addActionListener(e -> {
            database.insertIntoRegistros(5004, idUsuario, null);    // Opção 3 do menu principal selecionada por <login_name>.
            mostrarTelaSaida();
        });
    
        janelaPrincipal.pack();
        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
        janelaPrincipal.setVisible(true);
    }

    private void mostrarTelaCadastro(int status) {
        database.insertIntoRegistros(6001, idUsuario, null);    // Tela de cadastro apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());

        // Define o título da tela baseado no status
        String tituloTela = (status == 0) ? "Cadastro do administrador" : "Novo cadastro";
        JLabel labelTituloTela = new JLabel(tituloTela, JLabel.CENTER);
        janelaPrincipal.add(labelTituloTela, BorderLayout.NORTH);
    
        if(status != 0){
            JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
            painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
            painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
            painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
            janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        } else {
            janelaPrincipal.add(labelTituloTela, BorderLayout.NORTH);
        }
        
        Box verticalBox = Box.createVerticalBox();
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        Database banco = Database.getInstance();
        int nUsuarios = banco.countUsuariosNoSistema();
        painelCorpo1.add(new JLabel("Total de usuários do sistema: " + nUsuarios));
        verticalBox.add(painelCorpo1);
    
        JPanel painelCorpo2 = new JPanel(new GridLayout(6, 2, 5, 5)); 
        painelCorpo2.add(new JLabel("Caminho do arquivo do certificado digital:"));
        JTextField campoCertificado = new JTextField(255);
        painelCorpo2.add(campoCertificado);
    
        painelCorpo2.add(new JLabel("Caminho do arquivo da chave privada:"));
        JTextField campoChavePrivada = new JTextField(255);
        painelCorpo2.add(campoChavePrivada);
    
        painelCorpo2.add(new JLabel("Frase secreta:"));
        JTextField campoFraseSecreta = new JTextField(255);
        painelCorpo2.add(campoFraseSecreta);
    
        painelCorpo2.add(new JLabel("Grupo:"));
        JComboBox<String> comboBoxGrupo;

        if (status == 0) {
            comboBoxGrupo = new JComboBox<>(new String[]{"Administrador"});
        } else {
            comboBoxGrupo = new JComboBox<>(new String[]{"Usuário", "Administrador"});
        }
        painelCorpo2.add(comboBoxGrupo);
    
        painelCorpo2.add(new JLabel("Senha pessoal:"));
        JPasswordField campoSenha = new JPasswordField(10);
        painelCorpo2.add(campoSenha);
    
        painelCorpo2.add(new JLabel("Confirmação da senha pessoal:"));
        JPasswordField campoConfirmacaoSenha = new JPasswordField(10);
        painelCorpo2.add(campoConfirmacaoSenha);

        verticalBox.add(painelCorpo2);
        janelaPrincipal.add(verticalBox, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton botaoCadastrar = new JButton("Cadastrar");
        tratamentosBotaoCadastrar(botaoCadastrar, campoSenha);
        if(status != 0){
            JButton botaoVoltar = new JButton("Voltar");
            painelBotoes.add(botaoVoltar);
            botaoVoltar.addActionListener(e -> {
                database.insertIntoRegistros(6010, idUsuario, null);    // Botão voltar de cadastro para o menu principal pressionado por <login_name>.
                mostrarTelaMenu();
            });
        }

        painelBotoes.add(botaoCadastrar);
    
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);

        botaoCadastrar.addActionListener(e -> {
            database.insertIntoRegistros(6002, idUsuario, null);    // Botão cadastrar pressionado por <login_name>.
            Object grupoSelecionado = comboBoxGrupo.getSelectedItem();
            int codigoGrupo = 0;
            if ("Administrador".equals(grupoSelecionado)) {
                codigoGrupo = 1;
            } else if ("Usuário".equals(grupoSelecionado)) {
                codigoGrupo = 2;
            }

            Cadastro cadastro = new Cadastro(
                campoCertificado.getText(), 
                campoChavePrivada.getText(), 
                campoFraseSecreta.getText(), 
                codigoGrupo, 
                new String (campoSenha.getPassword()), 
                new String (campoConfirmacaoSenha.getPassword())
            );

            String msg = cadastro.verificaEntradasDoCadastro();
            if (msg.equals("Entradas verificadas")) {
                try {
                    HashMap<String,String> info = cadastro.getDetalhesDoCertificadoDigital();
                    mostrarPopUpConfirmacao(cadastro, info, status);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Falha ao extrair as informações do certificado digital.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    x.printStackTrace();
                }
            } else {
                if (msg.equals("Caminho do arquivo do certificado digital inválido.")) {
                    database.insertIntoRegistros(6004, idUsuario, null);   // Caminho do certificado digital inválido fornecido por <login_name>.
                } else if (msg.equals("Caminho do arquivo da chave privada inválido.")) {
                    database.insertIntoRegistros(6005, idUsuario, null);   // Chave privada verificada negativamente para <login_name> (caminho inválido).
                } else if (msg.equals("Frase secreta inválida para a chave privada fornecida.")) {
                    database.insertIntoRegistros(6006, idUsuario, null);   // Chave privada verificada negativamente para <login_name> (frase secreta inválida).
                } else if (msg.equals("Assinatura digital inválida para a chave privada fornecida.")) {
                    database.insertIntoRegistros(6007, idUsuario, null);   // Chave privada verificada negativamente para <login_name> (assinatura digital inválida).
                } else if (msg.equals("Senha e confirmação de senha não são iguais.")) {
                    database.insertIntoRegistros(6003, idUsuario, null);    // Senha pessoal inválida fornecida por <login_name>.
                }
                JOptionPane.showMessageDialog(janelaPrincipal, msg, "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }                                       
        });

        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }

    private boolean validarSenhaCadastro(char[] senha) {
        String senhaStr = new String(senha);
        if (senhaStr.length() < 8 || senhaStr.length() > 10) {
            return false;
        }
        for (int i = 0; i < senhaStr.length(); i++) {
            if (!Character.isDigit(senhaStr.charAt(i))) {
                return false;
            } 
            if (i > 0 && senhaStr.charAt(i) == senhaStr.charAt(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private void tratamentosBotaoCadastrar(JButton botaoCadastrar, JPasswordField campoSenha) {
        botaoCadastrar.addActionListener(e -> {
            char[] senha = campoSenha.getPassword();
            if (!validarSenhaCadastro(senha)) {
                database.insertIntoRegistros(6003, idUsuario, null);    // Senha pessoal inválida fornecida por <login_name>.
                JOptionPane.showMessageDialog(janelaPrincipal, "A senha deve ter de 8 a 10 dígitos.\nA senha não pode conter sequências de números repetidos.\nA senha deve ser formada apenas por digitos de 0 a 9.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(janelaPrincipal, "Usuário cadastrado com sucesso!", "Cadastro", JOptionPane.INFORMATION_MESSAGE);
            mostrarTelaMenu();
        });
    }

    private void mostrarPopUpConfirmacao(Cadastro cadastro, HashMap<String,String> info, int statusTipoCadastro) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        // Criar o JPanel que conterá todos os componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Versão: " + info.get("versao")));
        panel.add(new JLabel("Série: " + info.get("serie")));
        panel.add(new JLabel("Validade: " + info.get("validade")));
        panel.add(new JLabel("Tipo de Assinatura: " + info.get("tipo_assinatura")));
        panel.add(new JLabel("Emissor: " + info.get("emissor")));
        panel.add(new JLabel("Sujeito: " + info.get("sujeito")));
        panel.add(new JLabel("Email: " + info.get("email")));

        // Criar os botões
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            database.insertIntoRegistros(6008, idUsuario, null);    // Confirmação de dados aceita por <login_name>.
            String codigoTOTP = cadastro.cadastraUsuario();
            mostrarPopUpCodigoTOTP(codigoTOTP,statusTipoCadastro);
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            database.insertIntoRegistros(6009, idUsuario, null);    // Confirmação de dados rejeitada por <login_name>.
            frame.dispose();
        });

        // Adicionar botões ao painel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        // Adicionar o painel ao frame
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void mostrarPopUpCodigoTOTP(String codigoTOTP, int statusTipoCadastro) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        // Criar o JPanel que conterá todos os componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Insira o código a seguir como chave de configuração no seu google Authenticator: "));
        panel.add(new JLabel(""));
        panel.add(new JLabel(codigoTOTP));

        // Criar os botões
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            if(statusTipoCadastro == 0){
                mostrarTelaNomeLogin();
            }else{
                mostrarTelaCadastro(statusTipoCadastro);
            }
        });

        // Adicionar botões ao painel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        panel.add(buttonPanel);

        // Adicionar o painel ao frame
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void mostrarTelaConsulta() {
        database.insertIntoRegistros(7001, idUsuario, null);   // Confirmação de dados rejeitada por <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        Box verticalBox = Box.createVerticalBox();

        JPanel painelCorpo2 = new JPanel(new GridLayout(6, 2, 5, 5));
        painelCorpo2.add(new JLabel("Caminho da Pasta Segura:"));
        JTextField caminhoPasta = new JTextField(255);
        painelCorpo2.add(caminhoPasta);

        painelCorpo2.add(new JLabel("Frase secreta:"));
        JTextField fraseSecretaUsuario = new JTextField(255);
        painelCorpo2.add(fraseSecretaUsuario);

        JButton btnListar = new JButton("Listar");
        painelCorpo2.add(new JLabel(""));
        painelCorpo2.add(btnListar);

        JButton btnVoltar = new JButton("Voltar");
        painelCorpo2.add(new JLabel(""));
        painelCorpo2.add(btnVoltar);

        verticalBox.add(painelCorpo2);
        janelaPrincipal.add(verticalBox, BorderLayout.CENTER);

        // Configuração da tabela
        DefaultTableModel model = new DefaultTableModel(null, new String[]{"Nome Código", "Nome", "Dono", "Grupo"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabelaArquivos = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setPreferredSize(new Dimension(600, 100));

        // Painel para a tabela e o botão "Selecionar"
        JPanel painelTabelaEBotoes = new JPanel();
        painelTabelaEBotoes.setLayout(new BorderLayout());
        painelTabelaEBotoes.add(scrollPane, BorderLayout.CENTER);

        // Painel para o botão "Selecionar"
        JPanel painelBotoes = new JPanel();
        JButton btnSelecionar = new JButton("Selecionar");
        btnSelecionar.setEnabled(false); // Inicialmente desabilitado
        painelBotoes.add(btnSelecionar);
        painelTabelaEBotoes.add(painelBotoes, BorderLayout.SOUTH);

        janelaPrincipal.add(painelTabelaEBotoes, BorderLayout.SOUTH);

        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);

        btnVoltar.addActionListener(e -> {
            database.insertIntoRegistros(7002, idUsuario, null);   // Botão voltar de consulta para o menu principal pressionado por <login_name>.
            mostrarTelaMenu();
        });

        btnListar.addActionListener(e -> {
            database.insertIntoRegistros(7003, idUsuario, null);   // Botão Listar de consulta pressionado por <login_name>.
            try {
                byte[] chaveSecredaAdminCript = database.getChavePrivadaCriptografadaDoUsuario(idAdministrador);
                PrivateKey objPrivateKeyAdmin = GestorDeSeguranca.generatePrivateKeyFromBIN(chaveSecredaAdminCript, fraseSecretaAdmin);
                if(objPrivateKeyAdmin != null){
                    String certificadoAdminPem = database.getCertificadoDigitalDoUsuario(idAdministrador);
                    X509Certificate objCertificadoAdmin = GestorDeSeguranca.generateX509CertificateFromPEM(certificadoAdminPem);
                    PublicKey objPublicKeyAdmin = objCertificadoAdmin.getPublicKey();

                    byte[] chaveSecredaUserCript = database.getChavePrivadaCriptografadaDoUsuario(idUsuario);
                    PrivateKey objPrivateKeyUser = GestorDeSeguranca.generatePrivateKeyFromBIN(chaveSecredaUserCript, fraseSecretaUsuario.getText());
                    if(objPrivateKeyUser != null){
                        String certificadoUserPem = database.getCertificadoDigitalDoUsuario(idUsuario);
                        X509Certificate objCertificadoUser = GestorDeSeguranca.generateX509CertificateFromPEM(certificadoUserPem);
                        PublicKey objPublicKeyUser = objCertificadoUser.getPublicKey();

                        if (grupoUsuario.equals("Usuário")) {
                            grupoUsuario = "usuario";
                        }
                        else{
                            grupoUsuario = "administrador";
                        }

                        recuperaArquivo = new RecuperaArquivo(emailUsuario, grupoUsuario, caminhoPasta.getText(), objPublicKeyAdmin, objPrivateKeyAdmin, objPublicKeyUser, objPrivateKeyUser);
                        String resultRecupecacao = recuperaArquivo.verificaArquivos("index");
                        if(resultRecupecacao.equals("OK")){
                
                            List<List<String>> resultado = recuperaArquivo.recuperaIndex();
                
                            // Preenche a tabela com os dados retornados
                            model.setRowCount(0); // Limpa linhas antigas
                            for (List<String> rowData : resultado) {
                                model.addRow(rowData.toArray());
                            }
                            tabelaArquivos.revalidate();
                            tabelaArquivos.repaint();
                        }else{
                            JOptionPane.showMessageDialog(janelaPrincipal, resultRecupecacao, "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }else{
                        JOptionPane.showMessageDialog(janelaPrincipal, "Chave Secreta do usuário incorreta", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(janelaPrincipal, "Chave Secreta do admin incorreta", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Adiciona um ListSelectionListener para rastrear a linha selecionada
        tabelaArquivos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tabelaArquivos.getSelectedRow();
                btnSelecionar.setEnabled(selectedRow != -1); // Habilita o botão se uma linha estiver selecionada
            }
        });

        // Adiciona um ActionListener ao botão "Selecionar"
        btnSelecionar.addActionListener(e -> {
            int selectedRow = tabelaArquivos.getSelectedRow();
            if (selectedRow != -1) {
                String nomeCodigo = (String) model.getValueAt(selectedRow, 0);
                String nome = (String) model.getValueAt(selectedRow, 1);
                String dono = (String) model.getValueAt(selectedRow, 2);

                try{
                    if(dono.equals(emailUsuario)){
                        String resultRecupecacao2 = recuperaArquivo.verificaArquivos(nomeCodigo);
                        if(resultRecupecacao2.equals("OK")){
                            recuperaArquivo.recuperaArquivosDocx(nome);
                            File f = new File(caminhoPasta.getText() + "/" + nome);
                            if(f.exists() && f.length() > 0){
                                JOptionPane.showMessageDialog(janelaPrincipal, "Arquivo decriptado com sucesso", "Confirmação", JOptionPane.INFORMATION_MESSAGE);

                            }else{
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao decriptar o arquivo", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }else{
                            JOptionPane.showMessageDialog(janelaPrincipal, resultRecupecacao2, "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else{
                        JOptionPane.showMessageDialog(janelaPrincipal, "Somente o dono pode decriptar o arquivo", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    
                }
                catch (Exception x){
                    x.printStackTrace();
                }
                
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Nenhum arquivo selecionado", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void mostrarTelaSaida() {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Cabeçalho
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        // Corpo1
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        painelCorpo1.add(new JLabel("Total de acessos do usuário: " + qtdAcessosUsuario));
        janelaPrincipal.add(painelCorpo1, BorderLayout.CENTER);
    
        // Corpo2
        JPanel painelCorpo2 = new JPanel(new GridLayout(2, 1));
        painelCorpo2.add(new JLabel("Saída do sistema:"));
        painelCorpo2.add(new JLabel("Pressione o botão Encerrar Sessão ou o botão Encerrar Sistema para confirmar.", JLabel.CENTER));
    
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton botaoEncerrarSessao = new JButton("Encerrar Sessão");
        JButton botaoEncerrarSistema = new JButton("Encerrar Sistema");
        JButton botaoVoltar = new JButton("Voltar de Sair para o Menu Principal");
    
        painelBotoes.add(botaoEncerrarSessao);
        painelBotoes.add(botaoEncerrarSistema);
        painelBotoes.add(botaoVoltar);
    
        // Adiciona painelBotoes ao painelCorpo2
        painelCorpo2.add(painelBotoes);
        janelaPrincipal.add(painelCorpo2, BorderLayout.SOUTH);
    
        // Definindo ações para os botões
        botaoEncerrarSessao.addActionListener(e -> {
            database.insertIntoRegistros(1004, idUsuario, null);    // Sessão encerrada para <login_name>.
            
            // Reseta todas as variáveis do usuário
            autenticacao = null;
            campoTextoTOTP = null;
            campoSenha = null;
            tentativasErradas = 0;
            painelTeclas = null;
            botaoOK = null;
            possibilidadesSenha.clear();
            grupoUsuario = null;
            nomeUsuario = null;
            qtdAcessosUsuario = null;
            emailUsuario = null;
            idUsuario = -1;

            // Redireciona para a tela de login
            mostrarTelaNomeLogin();        
        });
        botaoEncerrarSistema.addActionListener(e -> {
            database.insertIntoRegistros(1002, -1, null);   // Sistema encerrado.
            System.exit(0);  
        });
        botaoVoltar.addActionListener(e -> mostrarTelaMenu());
    
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }

}

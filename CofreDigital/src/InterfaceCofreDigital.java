/*  
INF1416 - Segurança da Informação - 2024.1 - 3WA
T4: Cofre Digital - Prof.: Anderson Oliveira da Silva
Nome: Marina Schuler Martins Matrícula: 2110075
Nome: Wladimir Calazam de Araujo Goes Ramos Matrícula: 2110104
*/

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
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
    private int tentativasErradasSenha = 0;
    private int tentativasErradasTOTP = 0;
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
        rotuloCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        painelSuperior.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelFraseSecreta = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Centraliza os componentes com menos margens
        JLabel labelFraseSecreta = new JLabel("Frase Secreta do Administrador:");
        JTextField campoFraseSecretaAdmin = new JTextField(20);
        painelFraseSecreta.add(labelFraseSecreta);
        painelFraseSecreta.add(campoFraseSecretaAdmin);
        painelFraseSecreta.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        painelSuperior.add(painelFraseSecreta, BorderLayout.CENTER);
    
        // Adiciona o painel superior ao BorderLayout.CENTER da janela principal
        janelaPrincipal.add(painelSuperior, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnConfirma = new JButton("Confirmar");
        painelBotoes.add(btnConfirma);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        btnConfirma.addActionListener(e -> {
            String textoCampoFraseSecretaAdmin = campoFraseSecretaAdmin.getText();
            idAdministrador = database.getUsuarioIfExists(emailAdmin);
            byte[] chavePrivCript = database.getChavePrivadaCriptografadaDoUsuario(idAdministrador);
            PrivateKey objPrivateKey = GestorDeSeguranca.generatePrivateKeyFromBIN(chavePrivCript, textoCampoFraseSecretaAdmin);
            if (objPrivateKey != null) {
                fraseSecretaAdmin = textoCampoFraseSecretaAdmin;
                mostrarTelaNomeLogin();
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Frase Secreta do administrador incorreta", "Erro", JOptionPane.OK_OPTION);
                database.insertIntoRegistros(1002, -1, null); //log encerrar sistema
                System.exit(1);
            }
        });
    
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // centraliza a janela na tela
        janelaPrincipal.setVisible(true);
    }   

    private void mostrarTelaNomeLogin() {
        database.insertIntoRegistros(2001, -1, null);    // Autenticação etapa 1 iniciada.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JPanel painelSuperior = new JPanel(new BorderLayout());
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        rotuloCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        painelSuperior.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelLogin = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel rotuloEmail = new JLabel("Login name:");
        JTextField campoTextoEmail = new JTextField(20);
        painelLogin.add(rotuloEmail);
        painelLogin.add(campoTextoEmail);
        painelLogin.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        painelSuperior.add(painelLogin, BorderLayout.CENTER);
    
        janelaPrincipal.add(painelSuperior, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoLogin = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoLogin);
        painelBotoes.add(botaoLimpar);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        campoTextoEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                boolean valido = campoTextoEmail.getText().matches("^[^@]+@[^@]+\\.(com|br)$");
                botaoLogin.setEnabled(valido);
            }
        });
        botaoLogin.setEnabled(false);
    
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
    
        // Definindo um tamanho fixo para a janela
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // centraliza a janela na tela
        janelaPrincipal.setVisible(true);
    }    

    private void mostrarTelaSenha() {
        possibilidadesSenha.clear();
        database.insertIntoRegistros(3001, idUsuario, null);    // Autenticação etapa 2 iniciada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        rotuloCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        janelaPrincipal.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelSenha = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel labelSenha = new JLabel("Senha pessoal:");
        campoSenha = new JPasswordField(20);
        campoSenha.setEditable(false);
        painelSenha.add(labelSenha);
        painelSenha.add(campoSenha);
        painelSenha.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        janelaPrincipal.add(painelSenha, BorderLayout.NORTH);
    
        painelTeclas = new JPanel(new GridLayout(2, 5, 10, 10));
        redistribuirNumeros();
        painelTeclas.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        janelaPrincipal.add(painelTeclas, BorderLayout.CENTER);
    
        JPanel painelControle = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botaoOK = new JButton("OK");
        botaoOK.setEnabled(false);
        JButton botaoLimpar = new JButton("LIMPAR");
        painelControle.add(botaoOK);
        painelControle.add(botaoLimpar);
        painelControle.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        janelaPrincipal.add(painelControle, BorderLayout.SOUTH);
    
        botaoLimpar.addActionListener(e -> {
            possibilidadesSenha.clear();
            campoSenha.setText("");
            botaoOK.setEnabled(false);
        });
    
        botaoOK.addActionListener(e -> {
            autenticacao = new Autenticacao(idUsuario);
            Boolean senhaValidada = autenticacao.verificaSenha(possibilidadesSenha);
            if (senhaValidada) {
                tentativasErradasSenha = 0;
                database.insertIntoRegistros(3003, idUsuario, null);    // Senha pessoal verificada positivamente para <login_name>.
                database.insertIntoRegistros(3002, idUsuario, null);    // Autenticação etapa 2 encerrada para <login_name>.
                mostrarTelaTOTP();
            } else {
                tentativasErradasSenha++;
                if (tentativasErradasSenha == 1) {
                    database.insertIntoRegistros(3004, idUsuario, null);    // Primeiro erro da senha pessoal contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Senha Incorreta (x1).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradasSenha == 2) {
                    database.insertIntoRegistros(3005, idUsuario, null);    // Segundo erro da senha pessoal contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Senha Incorreta (x2).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradasSenha >= 3) {
                    database.insertIntoRegistros(3006, idUsuario, null);    // Terceiro erro da senha pessoal contabilizado para <login_name>.
                    database.insertIntoRegistros(3007, idUsuario, null);    // Acesso do usuario <login_name> bloqueado pela autenticação etapa 2.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Acesso bloqueado por 2 minutos após três tentativas incorretas.", "Bloqueio de Acesso", JOptionPane.ERROR_MESSAGE);
                    database.insertIntoRegistros(3002, idUsuario, null);    // Autenticação etapa 2 encerrada para <login_name>.
                    tentativasErradasSenha = 0;
                    mostrarTelaNomeLogin();
                }
            }
        });
        
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // centraliza a janela na tela
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
        rotuloCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        janelaPrincipal.add(rotuloCabecalho, BorderLayout.NORTH);
    
        JPanel painelTOTP = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JLabel rotuloTOTP = new JLabel("TOTP:");
        campoTextoTOTP = new JTextField(20);
        painelTOTP.add(rotuloTOTP);
        painelTOTP.add(campoTextoTOTP);
        painelTOTP.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        janelaPrincipal.add(painelTOTP, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoOkTOTP = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoOkTOTP);
        painelBotoes.add(botaoLimpar);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        botaoOkTOTP.setEnabled(false);
    
        campoTextoTOTP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                botaoOkTOTP.setEnabled(campoTextoTOTP.getText().length() == 6 && campoTextoTOTP.getText().matches("\\d{6}"));
            }
        });

        botaoLimpar.addActionListener(e -> campoTextoTOTP.setText(""));
    
        botaoOkTOTP.addActionListener(e -> {
            Boolean TOTPValidado = autenticacao.verificaTOTP(campoTextoTOTP.getText());
            if (TOTPValidado) {
                tentativasErradasTOTP = 0;
                database.insertIntoRegistros(4003, idUsuario, null);    // Token verificado positivamente para <login_name>.
                database.insertIntoRegistros(4002, idUsuario, null);    // Autenticação etapa 3 encerrada para <login_name>.
                database.insertIntoRegistros(1003, idUsuario, null);    // Sessão iniciada para <login_name>.
                mostrarTelaMenu();
            } else {
                tentativasErradasTOTP++;
                if (tentativasErradasTOTP == 1) {
                    database.insertIntoRegistros(4004, idUsuario, null);    // Primeiro erro de token contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto (x1).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradasTOTP == 2) {
                    database.insertIntoRegistros(4005, idUsuario, null);    // Segundo erro de token contabilizado para <login_name>.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto (x2).", "Erro", JOptionPane.ERROR_MESSAGE);
                } else if (tentativasErradasTOTP >= 3) {
                    database.insertIntoRegistros(4006, idUsuario, null);    // Terceiro erro de token contabilizado para <login_name>.
                    database.insertIntoRegistros(4007, idUsuario, null);    // Acesso do usuario <login_name> bloqueado pela autenticação etapa 3.
                    JOptionPane.showMessageDialog(janelaPrincipal, "Acesso bloqueado por 2 minutos após três tentativas incorretas.", "Bloqueio de Acesso", JOptionPane.ERROR_MESSAGE);
                    database.insertIntoRegistros(4002, idUsuario, null);    // Autenticação etapa 3 encerrada para <login_name>.
                    tentativasErradasTOTP = 0;
                    mostrarTelaNomeLogin();
                }
            }
        });
    
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }

    private void mostrarTelaMenu() {
        database.insertIntoRegistros(5001, idUsuario, null);    // Tela principal apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Painel de cabeçalho
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        painelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        // Painel de corpo 1
        JPanel painelCorpo1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        painelCorpo1.add(new JLabel("Total de acessos do usuário: " + qtdAcessosUsuario));
        painelCorpo1.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        janelaPrincipal.add(painelCorpo1, BorderLayout.CENTER);
    
        // Painel de corpo 2
        JPanel painelCorpo2 = new JPanel(new GridLayout(3, 1, 10, 10));
        painelCorpo2.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
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
        janelaPrincipal.setLocationRelativeTo(null); // centraliza a janela na tela
        janelaPrincipal.setVisible(true);
    }
    

    private void mostrarTelaCadastro(int status) {
        database.insertIntoRegistros(6001, idUsuario, null); // Tela de cadastro apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Define o título da tela baseado no status
        String tituloTela = (status == 0) ? "Cadastro do administrador" : "Novo cadastro";
        JLabel labelTituloTela = new JLabel(tituloTela, JLabel.CENTER);
        labelTituloTela.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        janelaPrincipal.add(labelTituloTela, BorderLayout.NORTH);
    
        if (status != 0) {
            // Criação do painel de cabeçalho com GridBagLayout para centralização
            JPanel painelCabecalho = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
        
            JLabel labelLogin = new JLabel("Login: " + emailUsuario);
            JLabel labelGrupo = new JLabel("Grupo: " + grupoUsuario);
            JLabel labelNome = new JLabel("Nome: " + nomeUsuario);

            painelCabecalho.add(labelLogin, gbc);
            gbc.gridy++;
            painelCabecalho.add(labelGrupo, gbc);
            gbc.gridy++;
            painelCabecalho.add(labelNome, gbc);
        
            painelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        }
    
        Box verticalBox = Box.createVerticalBox();
        JPanel painelCorpo1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        int nUsuarios = database.countUsuariosNoSistema();
        painelCorpo1.add(new JLabel("Total de usuários do sistema: " + nUsuarios));
        verticalBox.add(painelCorpo1);
    
        JPanel painelCorpo2 = new JPanel();
        GroupLayout layoutCorpo = new GroupLayout(painelCorpo2);
        painelCorpo2.setLayout(layoutCorpo);
    
        layoutCorpo.setAutoCreateGaps(true);
        layoutCorpo.setAutoCreateContainerGaps(true);
    
        JLabel labelCertificado = new JLabel("Caminho do arquivo do certificado digital:");
        JTextField campoCertificado = new JTextField(255);
        campoCertificado.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JLabel labelChavePrivada = new JLabel("Caminho do arquivo da chave privada:");
        JTextField campoChavePrivada = new JTextField(255);
        campoChavePrivada.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JLabel labelFraseSecreta = new JLabel("Frase secreta:");
        JTextField campoFraseSecreta = new JTextField(255);
        campoFraseSecreta.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JLabel labelGrupo = new JLabel("Grupo:");
        JComboBox<String> comboBoxGrupo;
        if (status == 0) {
            comboBoxGrupo = new JComboBox<>(new String[]{"Administrador"});
        } else {
            comboBoxGrupo = new JComboBox<>(new String[]{"Usuário", "Administrador"});
        }
        comboBoxGrupo.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do comboBox
    
        JLabel labelSenha = new JLabel("Senha pessoal:");
        JPasswordField campoSenha = new JPasswordField(10);
        campoSenha.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JLabel labelConfirmacaoSenha = new JLabel("Confirmação da senha pessoal:");
        JPasswordField campoConfirmacaoSenha = new JPasswordField(10);
        campoConfirmacaoSenha.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        layoutCorpo.setHorizontalGroup(
            layoutCorpo.createSequentialGroup()
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(labelCertificado)
                    .addComponent(labelChavePrivada)
                    .addComponent(labelFraseSecreta)
                    .addComponent(labelGrupo)
                    .addComponent(labelSenha)
                    .addComponent(labelConfirmacaoSenha))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(campoCertificado)
                    .addComponent(campoChavePrivada)
                    .addComponent(campoFraseSecreta)
                    .addComponent(comboBoxGrupo)
                    .addComponent(campoSenha)
                    .addComponent(campoConfirmacaoSenha))
        );
    
        layoutCorpo.setVerticalGroup(
            layoutCorpo.createSequentialGroup()
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCertificado)
                    .addComponent(campoCertificado))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelChavePrivada)
                    .addComponent(campoChavePrivada))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFraseSecreta)
                    .addComponent(campoFraseSecreta))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGrupo)
                    .addComponent(comboBoxGrupo))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSenha)
                    .addComponent(campoSenha))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelConfirmacaoSenha)
                    .addComponent(campoConfirmacaoSenha))
        );
    
        verticalBox.add(painelCorpo2);
        janelaPrincipal.add(verticalBox, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoCadastrar = new JButton("Cadastrar");
        tratamentosBotaoCadastrar(botaoCadastrar, campoSenha);
    
        if (status != 0) {
            JButton botaoVoltar = new JButton("Voltar");
            painelBotoes.add(botaoVoltar);
            botaoVoltar.addActionListener(e -> {
                database.insertIntoRegistros(6010, idUsuario, null); // Botão voltar de cadastro para o menu principal pressionado por <login_name>.
                mostrarTelaMenu();
            });
        }
    
        painelBotoes.add(botaoCadastrar);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        botaoCadastrar.addActionListener(e -> {
            database.insertIntoRegistros(6002, idUsuario, null); // Botão cadastrar pressionado por <login_name>.
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
                new String(campoSenha.getPassword()), 
                new String(campoConfirmacaoSenha.getPassword())
            );
    
            String msg = cadastro.verificaEntradasDoCadastro();
            if (msg.equals("Entradas verificadas")) {
                if(validarSenhaCadastro(campoSenha.getPassword())){
                    try {
                        HashMap<String, String> info = cadastro.getDetalhesDoCertificadoDigital();
                        boolean loginNameLivre = (database.getUsuarioIfExists(info.get("email")) == -1);
                        if (loginNameLivre) {
                            mostrarPopUpConfirmacao(cadastro, info, status, campoFraseSecreta.getText());
                        } else {
                            JOptionPane.showMessageDialog(janelaPrincipal, "O email presente no certificado digital fornecido já pertence a um usuário cadastrado.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception x) {
                        JOptionPane.showMessageDialog(janelaPrincipal, "Falha ao extrair as informações do certificado digital.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                        x.printStackTrace();
                    }
                }
            } else {
                if (msg.equals("Caminho do arquivo do certificado digital inválido.")) {
                    database.insertIntoRegistros(6004, idUsuario, null); // Caminho do certificado digital inválido fornecido por <login_name>.
                } else if (msg.equals("Caminho do arquivo da chave privada inválido.")) {
                    database.insertIntoRegistros(6005, idUsuario, null); // Chave privada verificada negativamente para <login_name> (caminho inválido).
                } else if (msg.equals("Frase secreta inválida para a chave privada fornecida.")) {
                    database.insertIntoRegistros(6006, idUsuario, null); // Chave privada verificada negativamente para <login_name> (frase secreta inválida).
                } else if (msg.equals("Assinatura digital inválida para a chave privada fornecida.")) {
                    database.insertIntoRegistros(6007, idUsuario, null); // Chave privada verificada negativamente para <login_name> (assinatura digital inválida).
                } else if (msg.equals("Senha e confirmação de senha não são iguais.")) {
                    database.insertIntoRegistros(6003, idUsuario, null); // Senha pessoal inválida fornecida por <login_name>.
                }
                JOptionPane.showMessageDialog(janelaPrincipal, msg, "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // centraliza a janela na tela
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
        });
    }

    private void mostrarPopUpConfirmacao(Cadastro cadastro, HashMap<String, String> info, int statusTipoCadastro, String fraseSecreta) {
        JFrame frame = new JFrame("Confirmação de Cadastro");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Permite que a janela pop-up seja fechada sem encerrar o programa
    
        // Criar o JPanel que conterá todos os componentes
        JPanel panel = new JPanel(new BorderLayout());
    
        // Criar um JPanel intermediário para alinhar à esquerda
        JPanel leftAlignedPanel = new JPanel();
        leftAlignedPanel.setLayout(new BoxLayout(leftAlignedPanel, BoxLayout.Y_AXIS));
        leftAlignedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        leftAlignedPanel.add(new JLabel("  Versão: " + info.get("versao")));
        leftAlignedPanel.add(new JLabel("  Série: " + info.get("serie")));
        leftAlignedPanel.add(new JLabel("  Validade: " + info.get("validade")));
        leftAlignedPanel.add(new JLabel("  Tipo de Assinatura: " + info.get("tipo_assinatura")));
        leftAlignedPanel.add(new JLabel("  Emissor: " + info.get("emissor")));
        leftAlignedPanel.add(new JLabel("  Sujeito: " + info.get("sujeito")));
        leftAlignedPanel.add(new JLabel("  Email: " + info.get("email")));
    
        panel.add(leftAlignedPanel, BorderLayout.CENTER);
    
        // Criar os botões
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            database.insertIntoRegistros(6008, idUsuario, null);    // Confirmação de dados aceita por <login_name>.
            String codigoTOTP = cadastro.cadastraUsuario();
            if(codigoTOTP == null){
                JOptionPane.showMessageDialog(janelaPrincipal, "Falha ao cadastrar novo usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
            }
            BufferedImage QRcode = GestorDeSeguranca.generateQRcodeDaChaveSecreta(codigoTOTP, info.get("email"));
            if(statusTipoCadastro == 0){
                fraseSecretaAdmin = fraseSecreta;
                idAdministrador = database.getUsuarioIfExists(info.get("email"));
            }
            mostrarPopUpCodigoTOTP(codigoTOTP, QRcode, statusTipoCadastro);
            frame.dispose(); // Fecha a janela pop-up
        });
    
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
            database.insertIntoRegistros(6009, idUsuario, null);    // Confirmação de dados rejeitada por <login_name>.
            frame.dispose(); // Fecha a janela pop-up
        });
    
        // Adicionar botões ao painel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        // Adicionar o painel ao frame
        frame.add(panel);
        frame.setSize(660, 200); // Define o tamanho fixo
        frame.setLocationRelativeTo(null); // Centraliza a janela na tela
        frame.setVisible(true);
    }        

    private void mostrarPopUpCodigoTOTP(String codigoTOTP, BufferedImage QRcode, int statusTipoCadastro) {
        JFrame frame = new JFrame("Código TOTP");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Permite que a janela pop-up seja fechada sem encerrar o programa
    
        // Criar o JPanel que conterá todos os componentes
        JPanel panel = new JPanel(new BorderLayout());
    
        // Criar um JPanel intermediário para alinhar os textos à esquerda
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        textPanel.add(new JLabel("  Insira o código a seguir como chave de configuração no seu Google Authenticator: "));
        textPanel.add(Box.createVerticalStrut(10)); // Adicionar espaço vertical
        textPanel.add(new JLabel("  " + codigoTOTP));
        textPanel.add(Box.createVerticalStrut(10)); // Adicionar espaço vertical
        textPanel.add(new JLabel("  Ou utilize a opção Ler QR code: "));
        textPanel.add(Box.createVerticalStrut(10)); // Adicionar espaço vertical
    
        panel.add(textPanel, BorderLayout.NORTH);
    
        // Adicionar a imagem do QR code centralizada
        JLabel qrCodeLabel = new JLabel(new ImageIcon(QRcode));
        qrCodeLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(qrCodeLabel, BorderLayout.CENTER);
    
        // Criar o botão
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            if (statusTipoCadastro == 0) {
                mostrarTelaNomeLogin();
            } else {
                mostrarTelaCadastro(statusTipoCadastro);
            }
            frame.dispose(); // Fecha a janela pop-up
        });
    
        // Adicionar o botão centralizado
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        // Adicionar o painel ao frame
        frame.add(panel);
        frame.setSize(510, 400); // Define o tamanho fixo
        frame.setLocationRelativeTo(null); // Centraliza a janela na tela
        frame.setVisible(true);
    }    

    private void mostrarTelaConsulta() {
        database.insertIntoRegistros(7001, idUsuario, null);   // Tela de consulta de arquivos secretos apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Criação do painel de cabeçalho com GridBagLayout para centralização
        JPanel painelCabecalho = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
    
        JLabel labelLogin = new JLabel("Login: " + emailUsuario);
        JLabel labelGrupo = new JLabel("Grupo: " + grupoUsuario);
        JLabel labelNome = new JLabel("Nome: " + nomeUsuario);

        painelCabecalho.add(labelLogin, gbc);
        gbc.gridy++;
        painelCabecalho.add(labelGrupo, gbc);
        gbc.gridy++;
        painelCabecalho.add(labelNome, gbc);
    
        painelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        Box verticalBox = Box.createVerticalBox();

        JPanel painelCorpo1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        int nConsultas = database.countConsultasDoUsuario(idUsuario);
        painelCorpo1.add(new JLabel("Total de consultas do usuário: " + nConsultas));
        verticalBox.add(painelCorpo1);

        JPanel painelCorpo2 = new JPanel();
        GroupLayout layoutCorpo = new GroupLayout(painelCorpo2);
        painelCorpo2.setLayout(layoutCorpo);
    
        layoutCorpo.setAutoCreateGaps(true);
        layoutCorpo.setAutoCreateContainerGaps(true);
    
        JLabel labelCaminhoPasta = new JLabel("Caminho da Pasta Segura:");
        JTextField caminhoPasta = new JTextField(255);
        caminhoPasta.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JLabel labelFraseSecreta = new JLabel("Frase secreta:");
        JTextField fraseSecretaUsuario = new JTextField(255);
        fraseSecretaUsuario.setMaximumSize(new Dimension(300, 30)); // Define o tamanho máximo do campo
    
        JButton btnListar = new JButton("Listar");
        JButton btnVoltar = new JButton("Voltar");
    
        layoutCorpo.setHorizontalGroup(
            layoutCorpo.createSequentialGroup()
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(labelCaminhoPasta)
                    .addComponent(labelFraseSecreta))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(caminhoPasta)
                    .addComponent(fraseSecretaUsuario)
                    .addGroup(layoutCorpo.createSequentialGroup()
                        .addComponent(btnListar)
                        .addComponent(btnVoltar)))
        );
    
        layoutCorpo.setVerticalGroup(
            layoutCorpo.createSequentialGroup()
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCaminhoPasta)
                    .addComponent(caminhoPasta))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFraseSecreta)
                    .addComponent(fraseSecretaUsuario))
                .addGroup(layoutCorpo.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnListar)
                    .addComponent(btnVoltar))
        );
    
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
        scrollPane.setPreferredSize(new Dimension(600, 200));
    
        // Painel para a tabela e o botão "Selecionar"
        JPanel painelTabelaEBotoes = new JPanel(new BorderLayout());
        painelTabelaEBotoes.add(scrollPane, BorderLayout.CENTER);
    
        // Painel para o botão "Selecionar"
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSelecionar = new JButton("Decriptar");
        btnSelecionar.setEnabled(false); // Inicialmente desabilitado
        painelBotoes.add(btnSelecionar);
        painelTabelaEBotoes.add(painelBotoes, BorderLayout.SOUTH);
    
        janelaPrincipal.add(painelTabelaEBotoes, BorderLayout.SOUTH);
    
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // Centraliza a janela na tela
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
                if (objPrivateKeyAdmin != null) {
                    String certificadoAdminPem = database.getCertificadoDigitalDoUsuario(idAdministrador);
                    X509Certificate objCertificadoAdmin = GestorDeSeguranca.generateX509CertificateFromPEM(certificadoAdminPem);
                    PublicKey objPublicKeyAdmin = objCertificadoAdmin.getPublicKey();
    
                    byte[] chaveSecredaUserCript = database.getChavePrivadaCriptografadaDoUsuario(idUsuario);
                    PrivateKey objPrivateKeyUser = GestorDeSeguranca.generatePrivateKeyFromBIN(chaveSecredaUserCript, fraseSecretaUsuario.getText());
                    if (objPrivateKeyUser != null) {
                        String certificadoUserPem = database.getCertificadoDigitalDoUsuario(idUsuario);
                        X509Certificate objCertificadoUser = GestorDeSeguranca.generateX509CertificateFromPEM(certificadoUserPem);
                        PublicKey objPublicKeyUser = objCertificadoUser.getPublicKey();
                        
                        String auxGrupoUsuario;
                        if (grupoUsuario.equals("Usuário")) {
                            auxGrupoUsuario = "usuario";
                        } else {
                            auxGrupoUsuario = "administrador";
                        }
    
                        recuperaArquivo = new RecuperaArquivo(emailUsuario, auxGrupoUsuario, caminhoPasta.getText(), objPublicKeyAdmin, objPrivateKeyAdmin, objPublicKeyUser, objPrivateKeyUser);
                        String resultRecupecacao = recuperaArquivo.verificaArquivos("index");
                        if (resultRecupecacao.equals("OK")) {
                            database.insertIntoRegistros(7005, idUsuario, null);   // Arquivo de índice decriptado com sucesso para <login_name>.
                            database.insertIntoRegistros(7006, idUsuario, null);   // Arquivo de índice verificado (integridade e autenticidade) com sucesso para <login_name>.
                                    
                            List<List<String>> resultado = recuperaArquivo.recuperaIndex();
    
                            // Preenche a tabela com os dados retornados
                            model.setRowCount(0); // Limpa linhas antigas
                            for (List<String> rowData : resultado) {
                                model.addRow(rowData.toArray());
                            }
    
                            database.insertIntoRegistros(7009, idUsuario, null);   // Lista de arquivos presentes no índice apresentada para <login_name>.
                            tabelaArquivos.revalidate();
                            tabelaArquivos.repaint();
                            
                        } else {
                            if (resultRecupecacao.equals("Caminho de pasta inválido.")) {
                                database.insertIntoRegistros(7004, idUsuario, null);   // Caminho de pasta inválido fornecido por <login_name>.
                                JOptionPane.showMessageDialog(janelaPrincipal, resultRecupecacao, "Erro", JOptionPane.ERROR_MESSAGE);
                            } else if (resultRecupecacao.equals("Erro na decriptação do arquivo.")) {
                                database.insertIntoRegistros(7007, idUsuario, null);   // Falha na decriptação do arquivo de índice para <login_name>.
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro na decriptação do arquivo de índice.", "Erro", JOptionPane.ERROR_MESSAGE);
                            } else if (resultRecupecacao.equals("Erro ao verificar integridade e autenticidade do arquivo.")) {
                                database.insertIntoRegistros(7005, idUsuario, null);   // Arquivo de índice decriptado com sucesso para <login_name>.
                                database.insertIntoRegistros(7008, idUsuario, null);   // Falha na verificação (integridade e autenticidade) do arquivo de índice para <login_name>.
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao verificar integridade e autenticidade do arquivo de índice.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(janelaPrincipal, "Frase Secreta do usuário incorreta", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Frase Secreta do administrador incorreta", "Erro", JOptionPane.ERROR_MESSAGE);
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
    
                database.insertIntoRegistros(7010, idUsuario, nome);    // Arquivo <arq_name> selecionado por <login_name> para decriptação.
    
                try {
                    if (dono.equals(emailUsuario)) {
                        database.insertIntoRegistros(7011, idUsuario, nome);    // Acesso permitido ao arquivo <arq_name> para <login_name>.
                        String resultRecupecacao2 = recuperaArquivo.verificaArquivos(nomeCodigo);
                        if (resultRecupecacao2.equals("OK")) {
                            database.insertIntoRegistros(7013, idUsuario, nome);   // Arquivo <arq_name> decriptado com sucesso para <login_name>.
                            database.insertIntoRegistros(7014, idUsuario, nome);   // Arquivo <arq_name> verificado (integridade e autenticidade) com sucesso para <login_name>.
                        
                            recuperaArquivo.recuperaArquivosDocx(nome);
                            File f = new File(caminhoPasta.getText() + "/" + nome);
                            if(f.exists() && f.length() > 0){
                                JOptionPane.showMessageDialog(janelaPrincipal, "Arquivo decriptado gerado com sucesso.", "Confirmação", JOptionPane.INFORMATION_MESSAGE);
                            } else{
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao gerar o arquivo decriptado.", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            if (resultRecupecacao2.equals("Caminho de pasta inválido.") || resultRecupecacao2.equals("Erro na decriptação do arquivo.")) {
                                database.insertIntoRegistros(7015, idUsuario, nome);   // Falha na decriptação do arquivo <arq_name> para <login_name>.
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro na decriptação do arquivo: " + nome, "Erro", JOptionPane.ERROR_MESSAGE);
                            } else if (resultRecupecacao2.equals("Erro ao verificar integridade e autenticidade do arquivo.")) {
                                database.insertIntoRegistros(7013, idUsuario, nome);   // Arquivo <arq_name> decriptado com sucesso para <login_name>.
                                database.insertIntoRegistros(7016, idUsuario, nome);   // Falha na verificação (integridade e autenticidade) do arquivo <arq_name> para <login_name>.
                                JOptionPane.showMessageDialog(janelaPrincipal, "Erro ao verificar integridade e autenticidade do arquivo: " + nome, "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    else {
                        database.insertIntoRegistros(7012, idUsuario, nome);    // Acesso negado ao arquivo <arq_name> para <login_name>.
                        JOptionPane.showMessageDialog(janelaPrincipal, "Somente o dono pode decriptar o arquivo selecionado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    
                }
                catch (Exception x) {
                    x.printStackTrace();
                }
                
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Nenhum arquivo selecionado", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }            
   
    private void mostrarTelaSaida() {
        database.insertIntoRegistros(8001, idUsuario, null);    // Tela de saída apresentada para <login_name>.
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // Cabeçalho centralizado com bordas
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + emailUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        painelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        // Corpo principal
        JPanel painelCorpoPrincipal = new JPanel();
        painelCorpoPrincipal.setLayout(new BoxLayout(painelCorpoPrincipal, BoxLayout.Y_AXIS));
        painelCorpoPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        // Corpo1
        JPanel painelCorpo1 = new JPanel();
        painelCorpo1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelCorpo1.add(new JLabel("Total de acessos do usuário: " + qtdAcessosUsuario));
        painelCorpo1.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCorpoPrincipal.add(painelCorpo1);
    
        // Corpo2
        JPanel painelCorpo2 = new JPanel();
        painelCorpo2.setLayout(new BoxLayout(painelCorpo2, BoxLayout.Y_AXIS));
        JLabel labelSaida = new JLabel("   Saída do sistema:");
        labelSaida.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel labelInstrucoes = new JLabel("   Pressione o botão Encerrar Sessão ou o botão Encerrar Sistema para confirmar.", JLabel.LEFT);
        labelInstrucoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCorpo2.add(labelSaida);
        painelCorpo2.add(labelInstrucoes);
        painelCorpo2.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelCorpo2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        painelCorpoPrincipal.add(painelCorpo2);
    
        janelaPrincipal.add(painelCorpoPrincipal, BorderLayout.CENTER);
    
        // Painel de botões na parte inferior
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton botaoEncerrarSessao = new JButton("Encerrar Sessão");
        JButton botaoEncerrarSistema = new JButton("Encerrar Sistema");
        JButton botaoVoltar = new JButton("Voltar ao Menu Principal");
    
        painelBotoes.add(botaoEncerrarSessao);
        painelBotoes.add(botaoEncerrarSistema);
        painelBotoes.add(botaoVoltar);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        // Definindo ações para os botões
        botaoEncerrarSessao.addActionListener(e -> {
            database.insertIntoRegistros(8002, idUsuario, null);    // Botão encerrar sessão pressionado por <login_name>.
    
            // Reseta todas as variáveis do usuário
            autenticacao = null;
            campoTextoTOTP = null;
            campoSenha = null;
            tentativasErradasSenha = 0;
            tentativasErradasTOTP = 0;
            painelTeclas = null;
            botaoOK = null;
            possibilidadesSenha.clear();
            grupoUsuario = null;
            nomeUsuario = null;
            qtdAcessosUsuario = null;
            emailUsuario = null;
    
            database.insertIntoRegistros(1004, idUsuario, null);    // Sessão encerrada para <login_name>.
    
            idUsuario = -1;
    
            // Redireciona para a tela de login
            mostrarTelaNomeLogin();
        });
    
        botaoEncerrarSistema.addActionListener(e -> {
            database.insertIntoRegistros(8003, idUsuario, null);    // Botão encerrar sistema pressionado por <login_name>.
            database.insertIntoRegistros(1002, -1, null);   // Sistema encerrado.
            System.exit(0);
        });
    
        botaoVoltar.addActionListener(e -> {
            database.insertIntoRegistros(8004, idUsuario, null);    // Botão voltar de sair para o menu principal pressionado por <login_name>.
            mostrarTelaMenu();
        });
    
        janelaPrincipal.pack();
        janelaPrincipal.setLocationRelativeTo(null); // Centraliza a janela na tela
        janelaPrincipal.setVisible(true);
    }
    
               
}

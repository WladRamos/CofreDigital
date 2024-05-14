import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.security.*;
import java.util.List;

public class InterfaceCofreDigital {
    private Database database;
    private JFrame janelaPrincipal;
    private JTextField campoTextoEmail, campoTextoTOTP;
    private JPasswordField campoSenha;
    private JPanel painelTeclas;
    private JButton botaoOK;
    private ArrayList<String[]> possibilidadesSenha = new ArrayList<>();
    private String grupoUsuario, nomeUsuario, qtdAcessosUsuario;
    private int idUsuario;
    private String fraseSecretaAdmin;


    public InterfaceCofreDigital(int status) {
        this.database = Database.getInstance();
        janelaPrincipal = new JFrame("Cofre Digital");
        janelaPrincipal.setSize(500, 200);
        janelaPrincipal.setLayout(new BorderLayout());
        janelaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (status == 0){
            mostrarTelaCadastro(0);
            // mostrarTelaConsulta();   // mock
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
            fraseSecretaAdmin = campoFraseSecretaAdmin.getText();
            mostrarTelaNomeLogin();
        });

        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);
    }

    private void mostrarTelaNomeLogin() {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());

        JPanel painelCabecalho = new JPanel();
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        painelCabecalho.add(rotuloCabecalho);

        JPanel painelEmail = new JPanel(new FlowLayout());
        JLabel rotuloEmail = new JLabel("Login name:");
        campoTextoEmail = new JTextField(20);
        
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
            String email = campoTextoEmail.getText();
            int uid = database.getUsuarioIfExists(email);
            HashMap<String, String> u = null;
            if (uid != -1){
                idUsuario = uid;
                u = database.getInfoDoUsuario(uid);
                if (u != null) {
                    nomeUsuario = u.get("nome");
                    grupoUsuario = u.get("grupo");
                    qtdAcessosUsuario = u.get("numero_de_acessos");
                    mostrarTelaSenha();
                }
            }
            JOptionPane.showMessageDialog(janelaPrincipal, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        });
        botaoLimpar.addActionListener(e -> campoTextoEmail.setText(""));
        janelaPrincipal.pack();
    }

    private void mostrarTelaSenha() {
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
            campoSenha.setText("");
            botaoOK.setEnabled(false);
        });

        botaoOK.addActionListener(e -> {
            Autenticacao aut = new Autenticacao(idUsuario);
            Boolean senhaValidada = aut.verificaSenha(possibilidadesSenha);
            if (senhaValidada) {
                mostrarTelaTOTP();
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Senha Incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        janelaPrincipal.add(painelTeclas, BorderLayout.CENTER);
        janelaPrincipal.add(painelControle, BorderLayout.SOUTH);
    
        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
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
                botaoOkTOTP.setEnabled(campoTextoTOTP.getText().length() == 6);
            }
        });

        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoOkTOTP);
        painelBotoes.add(botaoLimpar);
        botaoOkTOTP.setEnabled(false);

        janelaPrincipal.add(painelTOTP, BorderLayout.CENTER);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.setVisible(true);

        botaoOkTOTP.addActionListener(e -> {
            Autenticacao autenticacao = new Autenticacao(idUsuario);
            Boolean TOTPValidado = autenticacao.verificaTOTP(campoTextoTOTP.getText());
            if (TOTPValidado) {
                mostrarTelaMenu();
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        botaoLimpar.addActionListener(e -> campoTextoEmail.setText(""));
    }

    private void mostrarTelaMenu() {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + campoTextoEmail.getText(), JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        painelCorpo1.add(new JLabel("Total de acessos do usuário: " + qtdAcessosUsuario));
        janelaPrincipal.add(painelCorpo1, BorderLayout.CENTER);
    
        JPanel painelCorpo2 = new JPanel(new GridLayout(3, 1));
        JButton botaoCadastrar = new JButton("1 – Cadastrar um novo usuário");
        JButton botaoConsultar = new JButton("2 – Consultar pasta de arquivos secretos do usuário");
        JButton botaoSair = new JButton("3 – Sair do Sistema");
    
        painelCorpo2.add(botaoCadastrar);
        painelCorpo2.add(botaoConsultar);
        painelCorpo2.add(botaoSair);
        janelaPrincipal.add(painelCorpo2, BorderLayout.SOUTH);
    
        // Definindo ações para os botões
        botaoCadastrar.addActionListener(e -> mostrarTelaCadastro(1));
        botaoConsultar.addActionListener(e -> mostrarTelaConsulta());
        botaoSair.addActionListener(e -> System.exit(0));
    
        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
    }

    private void mostrarTelaCadastro(int status) {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());

        // Define o título da tela baseado no status
        String tituloTela = (status == 0) ? "Cadastro do administrador" : "Novo cadastro";
        JLabel labelTituloTela = new JLabel(tituloTela, JLabel.CENTER);
        janelaPrincipal.add(labelTituloTela, BorderLayout.NORTH);
    
        if(status != 0){
            JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
            painelCabecalho.add(new JLabel("Login: " + campoTextoEmail.getText(), JLabel.CENTER));
            painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
            painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
            janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
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
            botaoVoltar.addActionListener(e -> mostrarTelaMenu());
        }

        painelBotoes.add(botaoCadastrar);
    
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);

        botaoCadastrar.addActionListener(e -> {
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
                    mostrarPopUpConfirmacao(cadastro, info);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(janelaPrincipal, "Falha ao extrair as informações do certificado digital.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    x.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, msg, "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }                                       
        });
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
                JOptionPane.showMessageDialog(janelaPrincipal, "A senha deve ter de 8 a 10 dígitos.\nA senha não pode conter sequências de números repetidos.\nA senha deve ser formada apenas por digitos de 0 a 9.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(janelaPrincipal, "Usuário cadastrado com sucesso!", "Cadastro", JOptionPane.INFORMATION_MESSAGE);
            mostrarTelaMenu();
        });
    }

    private void mostrarPopUpConfirmacao(Cadastro cadastro, HashMap<String,String> info) {
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
            String codigoTOTP = cadastro.cadastraUsuario();
            mostrarPopUpCodigoTOTP(codigoTOTP);
        });

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> {
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

    private void mostrarPopUpCodigoTOTP(String codigoTOTP) {
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
            mostrarTelaNomeLogin();
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
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        // JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        // painelCabecalho.add(new JLabel("Login: " + campoTextoEmail.getText(), JLabel.CENTER));
        // painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        // painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        // janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        Box verticalBox = Box.createVerticalBox();

        // Adicionando componentes ao painel
        JPanel painelCorpo2 = new JPanel(new GridLayout(6, 2, 5, 5));
        painelCorpo2.add(new JLabel("Caminho da pasta:"));
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
        String[] colunas = {"Nome Código", "Nome", "Dono", "Grupo"};
        DefaultTableModel model = new DefaultTableModel(null, colunas);
        JTable tabelaArquivos = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        janelaPrincipal.add(scrollPane, BorderLayout.SOUTH);

        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);

        btnVoltar.addActionListener(e -> mostrarTelaMenu());
        btnListar.addActionListener(e -> {
            try {
                // Assume que os métodos de geração de chave e certificado são previamente definidos
                String diretorioAtual = System.getProperty("user.dir");
                String caminhoChavePrivada = diretorioAtual + File.separator + "CofreDigital/test/admin-pkcs8-aes.pem";
                String caminhoCertificadoDigital = diretorioAtual + File.separator + "CofreDigital/test/admin-x509.crt";
                PrivateKey privateKey = GestorDeSeguranca.generatePrivateKeyFromFile(caminhoChavePrivada, "admin");
                X509Certificate certificado = GestorDeSeguranca.generateX509CertificateFromFile(caminhoCertificadoDigital);
                PublicKey publicKey = certificado.getPublicKey();
                RecuperaArquivo recuperaArquivo = new RecuperaArquivo("user@example.com", "usuario", caminhoPasta.getText(), publicKey, privateKey);
                List<List<String>> resultado = recuperaArquivo.decriptaEVerificaIndex();

                // Preenche a tabela com os dados retornados
                model.setRowCount(0); // Limpa linhas antigas
                for (List<String> rowData : resultado) {
                    model.addRow(rowData.toArray());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}

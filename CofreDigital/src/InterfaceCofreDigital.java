import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

public class InterfaceCofreDigital {
    private JFrame janelaPrincipal;
    private JTextField campoTextoEmail;
    private JTextField campoTextoTOTP;
    private JButton botaoLogin;
    private JPasswordField campoSenha;
    private JPanel painelTeclas;
    private JButton botaoOK;
    private JButton botaoOkTOTP;
    private ArrayList<String[]> possibilidadesSenha = new ArrayList<>();
    private String grupoUsuario;
    private String nomeUsuario;
    private String qtdAcessosUsuario;


    public InterfaceCofreDigital() {
        mostrarTelaNomeLogin();
    }

    private void mostrarTelaNomeLogin() {
        janelaPrincipal = new JFrame("Cofre Digital");
        janelaPrincipal.setSize(500, 200);
        janelaPrincipal.setLayout(new BorderLayout());
        janelaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel painelCabecalho = new JPanel();
        JLabel rotuloCabecalho = new JLabel("Cofre Digital - Autenticação", JLabel.CENTER);
        painelCabecalho.add(rotuloCabecalho);

        JPanel painelEmail = new JPanel(new FlowLayout());
        JLabel rotuloEmail = new JLabel("Login name:");
        campoTextoEmail = new JTextField(20);
        campoTextoEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validarEmail();
            }
        });
        painelEmail.add(rotuloEmail);
        painelEmail.add(campoTextoEmail);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        botaoLogin = new JButton("OK");
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
            String[] usuario = AutenticaUsuario.usuarioExiste(email);
            if (usuario != null) {
                nomeUsuario = usuario[0];
                grupoUsuario = usuario[1];
                qtdAcessosUsuario = usuario[2];
                mostrarTelaSenha();

                /* 
                 * mudança de interface:
                 * 
                 * int uid = database.getUIDdoUsuarioIfExists(email)
                 * HashMap<String, String> u = null
                 * if (uid != -1)
                 *      u = database.getInformacoesDoUsuario(uid);
                 * if (u != null) {
                 *     String nome = u.get("nome");
                 *     String grupo = u.get("grupo");
                 *     String acessos = u.get("numero_de_acessos");
                 * }
                 * 
                 * Para usar os métodos de autenticação, passar o uid do usuário que 
                 * deseja autanticar no construtor de Autenticação e aplicar os métodos
                 * nessa instância.
                 */

            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "E-mail não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        botaoLimpar.addActionListener(e -> campoTextoEmail.setText(""));
    }

    private void validarEmail() {
        String email = campoTextoEmail.getText();
        boolean valido = email.matches("^[^@]+@[^@]+\\.(com|br)$");
        botaoLogin.setEnabled(valido);
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

            Boolean senhaValidada = AutenticaUsuario.verificaSenha(possibilidadesSenha);
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
        campoTextoTOTP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validarComprimentoTOTP();
            }
        });
    
        painelTOTP.add(rotuloTOTP);
        painelTOTP.add(campoTextoTOTP);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        botaoOkTOTP = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");
        painelBotoes.add(botaoOkTOTP);
        painelBotoes.add(botaoLimpar);
        botaoOkTOTP.setEnabled(false);

        janelaPrincipal.add(painelTOTP, BorderLayout.CENTER);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.setVisible(true);

        botaoOkTOTP.addActionListener(e -> {
            Boolean TOTPValidado = AutenticaUsuario.verificaTOTP(campoTextoTOTP.getText());
            if (TOTPValidado) {
                System.out.println("TESTE");
                mostrarTelaMenu();
            } else {
                JOptionPane.showMessageDialog(janelaPrincipal, "Código Incorreto.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        botaoLimpar.addActionListener(e -> campoTextoEmail.setText(""));
    }
    
    private void validarComprimentoTOTP() {
        int comprimento = campoTextoTOTP.getText().length();
        botaoOkTOTP.setEnabled(comprimento == 6);
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
        botaoCadastrar.addActionListener(e -> mostrarTelaCadastro());
        //botaoConsultar.addActionListener(e -> consultarArquivos());
        botaoSair.addActionListener(e -> System.exit(0));
    
        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
    }

    private void mostrarTelaCadastro() {

        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + campoTextoEmail.getText(), JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        

        Box verticalBox = Box.createVerticalBox();
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        painelCorpo1.add(new JLabel("Total de usuários do sistema: " + "totalUsuarios"));
        verticalBox.add(painelCorpo1);
    
        JPanel painelCorpo2 = new JPanel(new GridLayout(6, 2, 5, 5)); 
        painelCorpo2.add(new JLabel("Caminho do arquivo do certificado digital:"));
        JTextField campoCertificado = new JTextField(20);
        painelCorpo2.add(campoCertificado);
    
        painelCorpo2.add(new JLabel("Caminho do arquivo da chave privada:"));
        JTextField campoChavePrivada = new JTextField(20);
        painelCorpo2.add(campoChavePrivada);
    
        painelCorpo2.add(new JLabel("Frase secreta:"));
        JTextField campoFraseSecreta = new JTextField(20);
        painelCorpo2.add(campoFraseSecreta);
    
        painelCorpo2.add(new JLabel("Grupo:"));
        JComboBox<String> comboBoxGrupo = new JComboBox<>(new String[]{"Administrador", "Usuário"});
        painelCorpo2.add(comboBoxGrupo);
    
        painelCorpo2.add(new JLabel("Senha pessoal:"));
        JPasswordField campoSenha = new JPasswordField(10);
        painelCorpo2.add(campoSenha);
    
        painelCorpo2.add(new JLabel("Confirmação senha pessoal:"));
        JPasswordField campoConfirmacaoSenha = new JPasswordField(10);
        painelCorpo2.add(campoConfirmacaoSenha);

        verticalBox.add(painelCorpo2);
        janelaPrincipal.add(verticalBox, BorderLayout.CENTER);
    
        JPanel painelBotoes = new JPanel(new FlowLayout());
        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");
        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoVoltar);
    
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);

        //botaoCadastrar.addActionListener(e -> mostrarTelaCadastro());
        botaoVoltar.addActionListener(e -> mostrarTelaMenu());
    }
    

    public static void main(String[] args) {
        new InterfaceCofreDigital();
    }
}

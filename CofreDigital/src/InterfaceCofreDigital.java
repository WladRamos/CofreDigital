import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;

public class InterfaceCofreDigital {
    private JFrame janelaPrincipal;
    private JTextField campoTextoEmail;
    private JButton botaoLogin;
    private JPasswordField campoSenha;
    private JPanel painelTeclas;

    public InterfaceCofreDigital() {
        prepararGUI();
    }

    private void prepararGUI() {
        janelaPrincipal = new JFrame("Cofre Digital - Autenticação");
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
        botaoLogin.setEnabled(false); // inicialmente desativado

        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
        janelaPrincipal.add(painelEmail, BorderLayout.CENTER);
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
        janelaPrincipal.setVisible(true);

        botaoLogin.addActionListener(e -> mostrarTelaSenha());
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
        campoSenha.setEditable(false);
        painelSenha.add(new JLabel("Senha pessoal:"));
        painelSenha.add(campoSenha);
        janelaPrincipal.add(painelSenha, BorderLayout.NORTH);

        painelTeclas = new JPanel(new GridLayout(2, 5));
        redistribuirNumeros();

        JPanel painelControle = new JPanel(new FlowLayout());
        JButton botaoOK = new JButton("OK");
        JButton botaoLimpar = new JButton("LIMPAR");
        painelControle.add(botaoOK);
        painelControle.add(botaoLimpar);

        botaoLimpar.addActionListener(e -> campoSenha.setText(""));

        janelaPrincipal.add(painelTeclas, BorderLayout.CENTER);
        janelaPrincipal.add(painelControle, BorderLayout.SOUTH);

        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
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
                campoSenha.setText(campoSenha.getText() + "*");
                redistribuirNumeros();
                janelaPrincipal.revalidate();
                janelaPrincipal.repaint();
            });
        }
    }

    public static void main(String[] args) {
        new InterfaceCofreDigital();
    }
}

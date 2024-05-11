import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

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
    private int idUsuario;


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
            Database database = Database.getInstance();
            String email = campoTextoEmail.getText();
            int uid = database.getUIDdoUsuarioIfExists(email);
            HashMap<String, String> u = null;
            if (uid != -1){
                idUsuario = uid;
                u = database.getinformacoesDoUsuario(uid);
                if (u != null) {
                    String nome = u.get("nome");
                    String grupo = u.get("grupo");
                    String acessos = u.get("numero_de_acessos");

                    nomeUsuario = nome;
                    grupoUsuario = grupo;
                    qtdAcessosUsuario = acessos;
                    mostrarTelaSenha();
                }else{
                    JOptionPane.showMessageDialog(janelaPrincipal, "E-mail não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
            else {
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
            Autenticacao aut = new Autenticacao(idUsuario);
            Boolean TOTPValidado = false;
            try{
                TOTPValidado = aut.verificaTOTP(campoTextoTOTP.getText());
            }
            catch (Exception x){
                x.printStackTrace();
            }
         
            if (TOTPValidado) {
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
        botaoConsultar.addActionListener(e -> mostrarTelaConsulta());
        botaoSair.addActionListener(e -> System.exit(0));
    
        janelaPrincipal.revalidate();
        janelaPrincipal.repaint();
    }

    private void mostrarTelaCadastro() {

        //VALIDAR SE O GRUPO FOI SELECIONADO

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
        JTextField campoCertificado = new JTextField(255);
        painelCorpo2.add(campoCertificado);
    
        painelCorpo2.add(new JLabel("Caminho do arquivo da chave privada:"));
        JTextField campoChavePrivada = new JTextField(255);
        painelCorpo2.add(campoChavePrivada);
    
        painelCorpo2.add(new JLabel("Frase secreta:"));
        JTextField campoFraseSecreta = new JTextField(255);
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
        TratamentosBotaoCadastrar(botaoCadastrar, campoSenha);
        JButton botaoVoltar = new JButton("Voltar");
        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoVoltar);
    
        janelaPrincipal.add(painelBotoes, BorderLayout.SOUTH);
    
        janelaPrincipal.pack();
        janelaPrincipal.setVisible(true);

        botaoCadastrar.addActionListener(e -> {
            Cadastro cadastro = new Cadastro();
            Boolean caminhoCertificadoVerificado = cadastro.verificaCaminhoCertificadoDigital(campoCertificado.getText());
            if (caminhoCertificadoVerificado){
                Boolean certificadoVerificado = false;
                try{
                    certificadoVerificado = cadastro.verificaCertificadoDigital();
                }catch(Exception x){
                    x.printStackTrace();
                }
                if(certificadoVerificado){
                    Boolean caminhoChaveVerificado = cadastro.verificaCaminhoChavePrivada(campoChavePrivada.getText());
                    if(caminhoChaveVerificado){
                        Boolean fraseSecretaVerificada = cadastro.verificaFraseSecretaDaChavePrivada(campoFraseSecreta.getText());
                        if(fraseSecretaVerificada){
                            Boolean parDeChavesVerificado = cadastro.verificaChavePrivadaComChavePublica();
                            if(parDeChavesVerificado){
                                Boolean senhasIguaisVerificadas = cadastro.verificaSenhasIguais(campoSenha.getText(), campoConfirmacaoSenha.getText());
                                if (senhasIguaisVerificadas) {
                                    try{
                                        Object grupoSelecionado = comboBoxGrupo.getSelectedItem();
                                        int codigoGrupo = 0;
                                        if ("Administrador".equals(grupoSelecionado)) {
                                            codigoGrupo = 1;
                                        } else if ("Usuário".equals(grupoSelecionado)) {
                                            codigoGrupo = 2;
                                        }
                                        HashMap<String,String> info = cadastro.getDetalhesDoCertificadoDigital();
                                        mostrarPopUpConfirmacao(cadastro, info, codigoGrupo);
                                    }catch(Exception x){
                                        JOptionPane.showMessageDialog(janelaPrincipal, "Problema ao extrair informações do Certificado", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                                        x.printStackTrace();
                                    }
                                }else{
                                    JOptionPane.showMessageDialog(janelaPrincipal, "Senha e confirmação de senha diferentes.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                                }
                            }else{
                                JOptionPane.showMessageDialog(janelaPrincipal, "Par de chaves inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                            }
                        }else{
                            JOptionPane.showMessageDialog(janelaPrincipal, "Frase secreta inválida.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                        }
                    }else{
                        JOptionPane.showMessageDialog(janelaPrincipal, "Caminho da chave privada inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                    }
                }else{
                    JOptionPane.showMessageDialog(janelaPrincipal, "Certificado inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(janelaPrincipal, "Caminho do certificado inválido.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            }
            
        });
        botaoVoltar.addActionListener(e -> mostrarTelaMenu());
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

    private void TratamentosBotaoCadastrar(JButton botaoCadastrar, JPasswordField campoSenha) {
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

    private void mostrarTelaConsulta() {
        janelaPrincipal.getContentPane().removeAll();
        janelaPrincipal.setLayout(new BorderLayout());
    
        JPanel painelCabecalho = new JPanel(new GridLayout(3, 1));
        painelCabecalho.add(new JLabel("Login: " + campoTextoEmail.getText(), JLabel.CENTER));
        painelCabecalho.add(new JLabel("Grupo: " + grupoUsuario, JLabel.CENTER));
        painelCabecalho.add(new JLabel("Nome: " + nomeUsuario, JLabel.CENTER));
        janelaPrincipal.add(painelCabecalho, BorderLayout.NORTH);
    
        Box verticalBox = Box.createVerticalBox();
    
        JPanel painelCorpo1 = new JPanel(new FlowLayout());
        painelCorpo1.add(new JLabel("Total de consultas do usuário: " + "qtdConsultas"));
        verticalBox.add(painelCorpo1);
    
        JPanel painelCorpo2 = new JPanel(new GridLayout(6, 2, 5, 5));
        painelCorpo2.add(new JLabel("Caminho da pasta:"));
        painelCorpo2.add(new JTextField(20));
        painelCorpo2.add(new JLabel("Frase secreta:"));
        painelCorpo2.add(new JTextField(20));
        JButton btnListar = new JButton("Listar");
        painelCorpo2.add(new JLabel(""));
        painelCorpo2.add(btnListar);
        JButton btnVoltar = new JButton("Voltar");
        painelCorpo2.add(new JLabel(""));
        painelCorpo2.add(btnVoltar);
        verticalBox.add(painelCorpo2);
    
        janelaPrincipal.add(verticalBox, BorderLayout.CENTER);
    
        // Tabela para listar arquivos secretos
        String[] colunas = {"Nome do Arquivo", "Dono", "Grupo"};
        Object[][] dados = {}; // Dados serão preenchidos posteriormente
        JTable tabelaArquivos = new JTable(dados, colunas);
        JScrollPane scrollPane = new JScrollPane(tabelaArquivos);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        janelaPrincipal.add(scrollPane, BorderLayout.SOUTH);
    
        janelaPrincipal.pack(); // Ajusta o tamanho da janela aos componentes
        janelaPrincipal.setVisible(true);
    
        btnVoltar.addActionListener(e -> mostrarTelaMenu());
        btnListar.addActionListener(e -> {
            //VALIDAR CHAVE SECRETA
            //CHAMAR RECUPERAARQUIVO(PASTA FORNECIDA, USUARIO E GRUPO)
        });
    }

    private void mostrarPopUpConfirmacao(Cadastro cadastro, HashMap<String,String> info, int codigoGrupo) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        // Criar o JPanel que conterá todos os componentes
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Versão:" + info.get("versao")));
        panel.add(new JLabel("Série:" + info.get("serie")));
        panel.add(new JLabel("Validade:" + info.get("validade")));
        panel.add(new JLabel("Tipo de Assinatura:" + info.get("tipo_assinatura")));
        panel.add(new JLabel("Emissor:" + info.get("emissor")));
        panel.add(new JLabel("Sujeito:" + info.get("sujeito")));
        panel.add(new JLabel("Email:" + info.get("email")));

        // Criar os botões
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            String codigoTOTP = cadastro.cadastraUsuario(codigoGrupo);
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

        panel.add(new JLabel("Insira o código a seguir como chave de configuração no seu google Authenticator:"));
        panel.add(new JLabel(""));

        panel.add(new JLabel(codigoTOTP));

        // Criar os botões
        JButton confirmButton = new JButton("Confirmar");
        confirmButton.addActionListener(e -> {
            mostrarTelaMenu();
        });

        // Adicionar botões ao painel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        panel.add(buttonPanel);

        // Adicionar o painel ao frame
        frame.add(panel);
        frame.pack(); // Ajusta o tamanho do frame ao conteúdo
        frame.setVisible(true); // Mostra o frame
    }

    public static void main(String[] args) {
        new InterfaceCofreDigital();
    }
}

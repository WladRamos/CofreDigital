import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class LogView {
    public static void main(String[] args) {
        Database database = Database.getInstance();
        List<String> listaDeLogs = database.getLogsEmOrdemCronologica();

        JFrame janelaPrincipal = new JFrame("Visualização de Registros do Cofre Digital");
        janelaPrincipal.setSize(1000, 500); 
        janelaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(10, 20, 30, 20));

        JLabel titulo = new JLabel("Listagem de Registros (LogView)");
        titulo.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        titulo.setHorizontalAlignment(JLabel.CENTER);

        JTextPane textPaneLogs = new JTextPane();
        textPaneLogs.setEditable(false);

        StyledDocument doc = textPaneLogs.getStyledDocument();

        SimpleAttributeSet logStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(logStyle, "Monospaced");
        StyleConstants.setFontSize(logStyle, 12);

        SimpleAttributeSet separatorStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(separatorStyle, Color.GRAY);
        StyleConstants.setAlignment(separatorStyle, StyleConstants.ALIGN_CENTER);

        for (String log : listaDeLogs) {
            try {
                doc.insertString(doc.getLength(), "  " + log + "\n", logStyle);
                String separator = new String(new char[250]).replace('\0', '-'); // Linha de comprimento fixo
                doc.insertString(doc.getLength(), separator + "\n", separatorStyle);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        JScrollPane scrollPane = new JScrollPane(textPaneLogs);
        painelPrincipal.add(titulo, BorderLayout.NORTH);
        painelPrincipal.add(scrollPane, BorderLayout.CENTER);
        janelaPrincipal.add(painelPrincipal);
        janelaPrincipal.setVisible(true);
    }

}

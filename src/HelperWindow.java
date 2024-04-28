import javax.swing.*;
import java.awt.*;

public class HelperWindow extends JFrame {

    private final JScrollPane scrollPane;
    private JTextArea textArea = new JTextArea();

    public HelperWindow() {
        setLocation(200, -1070);
        setVisible(true);
        setSize(600, 1010);
        scrollPane = new JScrollPane(textArea);
        textArea.setFont(new Font("Courier", Font.PLAIN, 16));
        textArea.setBackground(new Color(41,41,41));
        textArea.setForeground(Color.WHITE);
        add(scrollPane);
    }

    protected void println(String s) {
        textArea.append(s + "\n");
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());    }
}

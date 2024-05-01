import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

public class DebugWindow extends JFrame {

    private final JScrollPane scrollPane;
    private JTextArea textArea = new JTextArea();

    public DebugWindow() {

        Rectangle2D.Double rect = new Rectangle2D.Double();

        setLocation(200, -1070);
        setVisible(true);
        setSize(600, 1010);
        scrollPane = new JScrollPane(textArea);
        textArea.setFont(new Font("Courier", Font.PLAIN, 16));
        textArea.setBackground(new Color(41, 41, 41));
        textArea.setForeground(Color.WHITE);
        add(scrollPane);
    }

    protected void println(String s) {
        textArea.append(s + "\n");
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum() + 10);
    }

    public void clear() {
        textArea.setText("");
    }
}

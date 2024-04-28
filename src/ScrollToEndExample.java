import javax.swing.*;

public class ScrollToEndExample {
    public static void main(String[] args) {
        // Create a JTextArea with some text
        JTextArea textArea = new JTextArea();
        for (int i = 0; i < 100; i++) {
            textArea.append("Line " + i + "\n");
        }

        // Create a JScrollPane and add the JTextArea to it
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Set up your JFrame and add the scroll pane
        JFrame frame = new JFrame("Scroll To End Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scrollPane);
        frame.setSize(400, 300);
        frame.setVisible(true);

        // Scroll the JScrollPane to the end
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }
}

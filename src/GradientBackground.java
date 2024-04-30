import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GradientBackground extends JFrame implements KeyListener {
    private int rectWidth = 200;
    private int rectHeight = 200;
    private int lineWidth = 1;

    public GradientBackground() {
        setTitle("Color Gradient and Adjustable Rectangle");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        addKeyListener(this);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background color gradient from blue to red
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 0, getHeight(), Color.RED);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw green rectangle with adjustable line width
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(lineWidth));
        int x = (getWidth() - rectWidth) / 2;
        int y = (getHeight() - rectHeight) / 2;
        g2d.drawRect(x, y, rectWidth, rectHeight);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            lineWidth += 1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (lineWidth > 1) {
                lineWidth -= 1;
            }
        }
        repaint();
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GradientBackground program = new GradientBackground();
            program.setVisible(true);
        });
    }
}

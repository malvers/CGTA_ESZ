import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ZoomExample extends JFrame {
    private double zoomFactor = 1.0; // Initial zoom factor
    private int posX = 100; // Initial X position of the rectangle
    private int posY = 200; // Initial Y position of the rectangle
    private int initialWidth = 100; // Initial width of the rectangle
    private int initialHeight = 50; // Initial height of the rectangle
    private int mousePosX;
    private int mousePosY;
    private boolean isDragging = false;

    public ZoomExample() {
        super("Zoom Example");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // Calculate zoom factor based on vertical movement
                    double deltaY = e.getY() - mousePosY;
                    zoomFactor += deltaY * 0.01;

                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Start dragging
                    isDragging = true;
                    mousePosY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Stop dragging
                    isDragging = false;
                }
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLUE);

        // Calculate adjusted width and height based on the zoom factor
        int adjustedWidth = (int) (initialWidth * zoomFactor);
        int adjustedHeight = (int) (initialHeight * zoomFactor);

        // Calculate position to draw the rectangle
        int x = posX - adjustedWidth / 2;
        int y = posY - adjustedHeight / 2;

        // Draw the rectangle
        g2d.fillRect(x, y, adjustedWidth, adjustedHeight);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ZoomExample example = new ZoomExample();
            example.setVisible(true);
        });
    }
}

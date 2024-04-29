import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class RotateRectangleExample extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Create a Rectangle2D.Double
        double x = 100;
        double y = 100;
        double width = 200;
        double height = 100;
        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, width, height);

        // Create an AffineTransform for rotation
        double rotationAngle = Math.toRadians(45); // Rotate by 45 degrees
        AffineTransform transform = new AffineTransform();
        transform.rotate(rotationAngle, x + width / 2, y + height / 2);

        // Apply the transformation to the rectangle
        Shape rotatedRect = transform.createTransformedShape(rect);

        g2d.setColor(Color.GREEN);
        g2d.draw(rect);

        // Draw the rotated rectangle
        g2d.setColor(Color.RED);
        g2d.draw(rotatedRect);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Rotate Rectangle Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new RotateRectangleExample());
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

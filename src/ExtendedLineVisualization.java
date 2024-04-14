import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class ExtendedLineVisualization extends JFrame {
    private Point2D.Double A, B, C; // Vertices of the triangle
    private Point2D.Double extendedPoint; // Extended point from C

    public ExtendedLineVisualization() {
        super("Extended Line Visualization");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        A = new Point2D.Double(300, 500); // Adjusted coordinates for a smaller triangle
        B = new Point2D.Double(400, 400);
        C = new Point2D.Double(200, 400);

        // Find the vector representing AC
        double vectorAC_x = C.getX() - A.getX();
        double vectorAC_y = C.getY() - A.getY();

        // Find the length of AB (the same as AC)
        double lengthAB = Math.sqrt(Math.pow(vectorAC_x, 2) + Math.pow(vectorAC_y, 2));

        // Normalize the vector AC to get the direction
        double direction_x = vectorAC_x / lengthAB;
        double direction_y = vectorAC_y / lengthAB;

        // Scale the direction vector by the length of AB to get the extended point
        double scaled_x = direction_x * lengthAB;
        double scaled_y = direction_y * lengthAB;

        // Add the scaled vector to point C to get the extended point
        extendedPoint = new Point2D.Double(C.getX() + scaled_x, C.getY() + scaled_y);
    }



    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw the original triangle
        g2d.drawLine((int) A.getX(), (int) A.getY(), (int) B.getX(), (int) B.getY());
        g2d.drawLine((int) B.getX(), (int) B.getY(), (int) C.getX(), (int) C.getY());
        g2d.drawLine((int) C.getX(), (int) C.getY(), (int) A.getX(), (int) A.getY());

        // Draw the extended line
        g2d.setColor(Color.RED);
        g2d.drawLine((int) C.getX(), (int) C.getY(), (int) extendedPoint.getX(), (int) extendedPoint.getY());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExtendedLineVisualization visualization = new ExtendedLineVisualization();
            visualization.setVisible(true);
        });
    }
}

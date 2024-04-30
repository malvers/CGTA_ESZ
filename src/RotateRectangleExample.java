import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

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

        ArrayList<Point2D.Double> points = getShapePoints(rotatedRect);

        // Print the points
        for (Point2D.Double point : points) {
            System.out.println("X: " + point.x + ", Y: " + point.y);
        }
        g2d.setColor(Color.GREEN);
        g2d.draw(rect);

        System.out.println(rect);

        System.out.println(rotatedRect);

        // Draw the rotated rectangle
        g2d.setColor(Color.RED);
        g2d.draw(rotatedRect);
    }

    public static ArrayList<Point2D.Double> getShapePoints(Shape shape) {
        ArrayList<Point2D.Double> points = new ArrayList<>();
        PathIterator iterator = shape.getPathIterator(null);
        double[] coords = new double[6];

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    points.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                case PathIterator.SEG_CUBICTO:
                    points.add(new Point2D.Double(coords[0], coords[1]));
                    points.add(new Point2D.Double(coords[2], coords[3]));
                    if (type == PathIterator.SEG_CUBICTO) {
                        points.add(new Point2D.Double(coords[4], coords[5]));
                    }
                    break;
                case PathIterator.SEG_CLOSE:
                    // No need to add a point for SEG_CLOSE
                    break;
                default:
                    // Handle other segment types if necessary
                    break;
            }
            iterator.next();
        }

        return points;
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

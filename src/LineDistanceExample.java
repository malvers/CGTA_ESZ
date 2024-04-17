import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LineDistanceExample extends JFrame {

    private MyVector point = new MyVector(100, 100);
    private MyVector lineStart = new MyVector(500, 200);
    private MyVector lineEnd = new MyVector(300, 300);
    private boolean mouseDragging = false;

    public LineDistanceExample() {
        setTitle("Line Distance Example");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                mouseDragging = true;
                point.x = e.getX();
                point.y = e.getY();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (!mouseDragging) {
                    point.x = e.getX();
                    point.y = e.getY();
                    repaint();
                }
            }
        });

        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.RED);
        g2d.drawLine((int) lineStart.x, (int) lineStart.y, (int) lineEnd.x, (int) lineEnd.y);

        g2d.setColor(Color.BLUE);
        g2d.fillOval((int) point.x - 5, (int) point.y - 5, 10, 10);

        double distance = distanceToPointFromLine(point, lineStart, lineEnd);
        System.out.println("Distance: " + distance);
    }

    public static double distanceToPointFromLine(MyVector point, MyVector lineStart, MyVector lineEnd) {
        double numerator = Math.abs((lineEnd.x - lineStart.x) * (lineStart.y - point.y) - (lineStart.x - point.x) * (lineEnd.y - lineStart.y));
        double denominator = Math.sqrt(Math.pow(lineEnd.x - lineStart.x, 2) + Math.pow(lineEnd.y - lineStart.y, 2));
        return numerator / denominator;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LineDistanceExample::new);
    }

    static class MyVector {
        double x, y;

        MyVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}

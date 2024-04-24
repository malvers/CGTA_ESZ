import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class MyDoublePolygon {
    private ArrayList<Point2D.Double> points;

    private String name = "";

    public MyDoublePolygon(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyDoublePolygon() {
        points = new ArrayList<>();
    }

    public ArrayList<Point2D.Double> getPoints() {
        return points;
    }

    public void addPoint(double x, double y) {
        points.add(new Point2D.Double(x, y));
    }

    public boolean contains(double x, double y) {

        int numPoints = points.size();
        boolean contains = false;
        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(j);
            if (((p1.y > y) != (p2.y > y)) &&
                    (x < (p2.x - p1.x) * (y - p1.y) / (p2.y - p1.y) + p1.x)) {
                contains = !contains;
            }
        }
        return contains;
    }

    public int getNumPoints() {
        return points.size();
    }

    public void addCurve(MyDoublePolygon curve) {

        for (int i = 0; i < curve.getNumPoints(); i++) {
            this.addPoint(curve.getPoint(i));
        }
    }

    private void addPoint(Point2D.Double point) {
        points.add(point);
    }

    protected Point2D.Double getPoint(int i) {

        return points.get(i);
    }

    public void setPoints(ArrayList<Point2D.Double> pointsIn) {
        points = new ArrayList<Point2D.Double>(pointsIn);
    }

    protected double getWidth() {

        double dx = points.get(1).x - points.get(0).x;
        double dy = points.get(1).y - points.get(0).y;
        return Math.sqrt(dx*dx + dy*dy);
    }

    public void draw(Graphics2D g2d) {

        int i;
        for (i = 0; i < points.size() - 1; i++) {
            g2d.draw(new Line2D.Double(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y));
        }
        g2d.draw(new Line2D.Double(points.get(0).x, points.get(0).y, points.get(i).x, points.get(i).y));
    }

    public void print() {

        System.out.print("\n" + name + " -> ");
        for (int i = 0; i < points.size(); i++) {
            DecimalFormat formatter = new DecimalFormat("#000.00");
            String xs = formatter.format(points.get(i).x);
            String ys = formatter.format(points.get(i).y);
            System.out.print((" | " + i + " x: " + xs + " " + i + " y: " + ys + " "));
        }
        System.out.print(" | - width: " + getWidth());
    }
}

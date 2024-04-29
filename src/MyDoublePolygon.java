import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

    public boolean contains(Point2D.Double p) {
        return contains(p.x, p.y);
    }

    public boolean contains(double x, double y) {

        int numPoints = points.size();
        boolean inside = false;
        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get(j);
            if ((p1.y < y && p2.y >= y || p2.y < y && p1.y >= y) && (p1.x <= x || p2.x <= x)) {
                if (p1.x + (y - p1.y) / (p2.y - p1.y) * (p2.x - p1.x) < x) {
                    inside = !inside;
                }
            }
        }
        return inside;
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

        points = new ArrayList<>();
        for (int i = 0; i < pointsIn.size(); i++) {
            Point2D.Double pNew = new Point2D.Double();
            pNew.setLocation(pointsIn.get(i));
            points.add(pNew);
        }
    }

    protected double getWidth() {

        double dx = points.get(1).x - points.get(0).x;
        double dy = points.get(1).y - points.get(0).y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void draw(Graphics2D g2d) {

        int i;
        for (i = 0; i < points.size() - 1; i++) {
            g2d.draw(new Line2D.Double(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y));
        }
        g2d.draw(new Line2D.Double(points.get(0).x, points.get(0).y, points.get(i).x, points.get(i).y));
    }

    public void fill(Graphics2D g2d) {

        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];

        // Convert the points to integer coordinates
        for (int i = 0; i < points.size(); i++) {
            xPoints[i] = (int) points.get(i).x;
            yPoints[i] = (int) points.get(i).y;
        }

        g2d.fillPolygon(xPoints, yPoints, points.size());
    }

    public void print(String s) {

        name = s;
        System.out.print("\n" + name + " -> ");
        for (int i = 0; i < points.size(); i++) {
            DecimalFormat formatter = new DecimalFormat("#000.00");
            String xs = formatter.format(points.get(i).x);
            String ys = formatter.format(points.get(i).y);
            System.out.print((" | " + i + " x: " + xs + " " + i + " y: " + ys + " "));
        }
//        System.out.print(" | - width: " + getWidth());
    }

    public List<MyVector> getIntersectionPoints(MyDoublePolygon otherPolygon) {

        List<MyVector> intersectionPoints = new ArrayList<>();

        // Iterate over each edge of the current polygon
        for (int index = 0; index < points.size(); index++) {

            MyVector p1 = new MyVector(points.get(index));
            MyVector p2 = new MyVector(points.get((index + 1) % points.size()));

            // Check if the edge intersects with any edge of the other polygon
            for (int j = 0; j < otherPolygon.points.size(); j++) {

                MyVector p3 = new MyVector(otherPolygon.points.get(j));
                MyVector p4 = new MyVector(otherPolygon.points.get((j + 1) % otherPolygon.points.size()));

                // Create line segments for the current edge and the edge of the other polygon
                Line2D.Double line1 = new Line2D.Double(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                Line2D.Double line2 = new Line2D.Double(p3.getX(), p3.getY(), p4.getX(), p4.getY());

                // Check if the two line segments intersect
                if (line1.intersectsLine(line2)) {

                    // If they do, calculate the intersection point and add it to the list
                    Point2D.Double intersection = getIntersectionPoint(line1, line2);
                    if (intersection != null) {
                        MyVector v = new MyVector(intersection);
                        v.setName("index: " + index);
                        v.setId(index);
                        intersectionPoints.add(v);
                    }
                }
            }
        }
        return intersectionPoints;
    }

    private Point2D.Double getIntersectionPoint(Line2D line1, Line2D line2) {

        double x1 = line1.getX1(), y1 = line1.getY1();
        double x2 = line1.getX2(), y2 = line1.getY2();
        double x3 = line2.getX1(), y3 = line2.getY1();
        double x4 = line2.getX2(), y4 = line2.getY2();

        double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (det == 0) {
            return null; // Parallel lines, no intersection
        }

        double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det;

        return new Point2D.Double(px, py);
    }

    public double calculateArea() {

        double area = 0.0;
        int numPoints = points.size();

        for (int i = 0; i < points.size(); i++) {
            Point2D.Double currentPoint = points.get(i);
            Point2D.Double nextPoint = points.get((i + 1) % numPoints);
            area += currentPoint.x * nextPoint.y - nextPoint.x * currentPoint.y;
        }
        return Math.abs(area) / 2.0;
    }

    public MyDoublePolygon getSubPolygon(int from, int to) {

        ArrayList<Point2D.Double> poly = new ArrayList<>();
        /// TODO: + 1 is critical
        for (int i = from; i < to + 1; i++) {
            Point2D.Double point = points.get(i);
            poly.add(point);
        }
        MyDoublePolygon polygon = new MyDoublePolygon();
        polygon.setPoints(poly);
        return polygon;
    }

    public MyVector getCenter() {

        double width = points.get(1).x - points.get(0).x;
        double x = points.get(0).x + (width / 2.0);
        double y = points.get(0).y + (width / 2.0);

        return new MyVector(x, y, "");
    }
}

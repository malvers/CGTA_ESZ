import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

class MyVector extends Ellipse2D.Double {
    private String name;
    public boolean selected = false;
    private boolean visible = true;

    public MyVector(double p1, double p2, String str) {

        name = str;
        this.x = p1;
        this.y = p2;
        this.width = 8;
        this.height = 8;
    }

    public MyVector(Point2D.Double p) {
        x = p.x;
        y = p.y;
    }

    public static void printArrayList(ArrayList<MyVector> list) {
        for (MyVector vector : list) {
            vector.print("al: ");
        }
    }

    public static MyVector circleFromPoints(MyVector p1, MyVector p2, MyVector p3, double radius) {

        double p2SquaredMag = p2.x * p2.x + p2.y * p2.y;
        MyVector offset = new MyVector(p2SquaredMag, p2SquaredMag, "");

        double bcX = (p1.x * p1.x + p1.y * p1.y - offset.x) / 2.0;
        double bcY = (p1.x * p1.x + p1.y * p1.y - offset.y) / 2.0;
        MyVector bc = new MyVector(bcX, bcY, "");

        double cdX = (offset.x - p3.x * p3.x - p3.y * p3.y) / 2.0;
        double cdY = (offset.y - p3.x * p3.x - p3.y * p3.y) / 2.0;
        MyVector cd = new MyVector(cdX, cdY, "");

        double det = (p1.x - p2.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p2.y);

        // Use TOL instead of hardcoding the tolerance value
        double TOL = 1e-8;
        if (Math.abs(det) < TOL) {
            throw new IllegalArgumentException("Points are collinear, cannot form a circle.");
        }

        double iDet = 1.0 / det;

        double centerX = (bc.x * (p2.y - p3.y) - cd.x * (p1.y - p2.y)) * iDet;
        double centerY = (cd.y * (p1.x - p2.x) - bc.y * (p2.x - p3.x)) * iDet;
        radius = Math.sqrt((p2.x - centerX) * (p2.x - centerX) + (p2.y - centerY) * (p2.y - centerY));

        return new MyVector(centerX, centerY, "");
    }

    @Override
    public boolean contains(double x, double y) {
        double xx = x + width / 2;
        double yy = y + width / 2;
        return super.contains(xx, yy);
    }

    public void fill(Graphics2D g2d, boolean drawAnnotation) {

        if( !visible ) return;
        g2d.fill(new Ellipse2D.Double(x - width / 2, y - width / 2, width, width));
        if (drawAnnotation) {
            g2d.drawString(name, (int) x + 6, (int) y + 6);
        }
    }

    public void print(String s) {

        System.out.println("name: " + s + " x: " + x + " y: " + y);
    }

    public String getName() {
        return name;
    }

    public MyVector flip() {
        return new MyVector(y, x, name);
    }

    public MyVector rotate(double ang) {

        double newX = this.x * Math.cos(ang) - this.y * Math.sin(ang);
        double newY = this.x * Math.sin(ang) + this.y * Math.cos(ang);
        return new MyVector(newX, newY, this.name);
    }

    public static Point2D.Double rotate(Point2D.Double point, double ang) {

        double newX = point.x * Math.cos(ang) - point.y * Math.sin(ang);
        double newY = point.x * Math.sin(ang) + point.y * Math.cos(ang);
        return new Point2D.Double(newX, newY);
    }

    public MyVector add(MyVector in) {

        return new MyVector(x + in.x, y + in.y, name);
    }

    public MyVector subtract(MyVector in) {
        return new MyVector(x - in.x, y - in.y, name);
    }

    public MyVector multiply(double v) {
        return new MyVector(x * v, y * v, name);
    }

    protected static MyVector getVector(MyVector h1, MyVector h2) {

        return new MyVector(h1.x - h2.x, h1.y - h2.y, h1.name);
    }

    public static double angleBetweenHandles(MyVector handle1, MyVector handle2) {

        // Calculate the dot product of the two Handles
        double dotProduct = handle1.x * handle2.x + handle1.y * handle2.y;

        // Calculate the magnitudes of each Handle
        double magnitude1 = Math.sqrt(handle1.x * handle1.x + handle1.y * handle1.y);
        double magnitude2 = Math.sqrt(handle2.x * handle2.x + handle2.y * handle2.y);

        // Calculate the cosine of the angle between the Handles
        double cosAngle = dotProduct / (magnitude1 * magnitude2);

        // Use the arc cos function to get the angle in radians
        return Math.acos(cosAngle);
    }

    public MyVector makeItThatLong(double l) {

        double len = getLength();

        x = (x / len) * l;
        y = (y / len) * l;

        return new MyVector(x, y, name);
    }

    protected double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public static double distanceToPointFromLine(Point point, Point2D.Double sceneShift, MyVector handle1, MyVector handle2) {

        // Adjust the point coordinates based on the scene shift
        double adjustedX = point.x - sceneShift.x;
        double adjustedY = point.y - sceneShift.y;

        // Check if the adjusted point is within the bounding box defined by the two vectors
        double minX = Math.min(handle1.x, handle2.x);
        double minY = Math.min(handle1.y, handle2.y);
        double maxX = Math.max(handle1.x, handle2.x);
        double maxY = Math.max(handle1.y, handle2.y);

        if (adjustedX < minX || adjustedX > maxX || adjustedY < minY || adjustedY > maxY) {
            // Point is outside the bounding box, return some default value (e.g., -1)
            return 1000;
        }

        // Calculate the distance to the line
        double x1 = handle1.x;
        double y1 = handle1.y;
        double x2 = handle2.x;
        double y2 = handle2.y;

        double numerator = Math.abs((x2 - x1) * (y1 - adjustedY) - (x1 - adjustedX) * (y2 - y1));
        double denominator = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        return numerator / denominator;
    }

    protected static double getLength(MyVector o, MyVector t) {

        double dx = o.x - t.x;
        double dy = o.y - t.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected MyVector copy() {
        return new MyVector(this.x, this.y, name);
    }

    public static ArrayList<MyVector> scatterPointsAround(MyVector center, double radius, int numPoints) {

        ArrayList<MyVector> points = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numPoints; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            double x = center.x + distance * Math.cos(angle);
            double y = center.y + distance * Math.sin(angle);
            points.add(new MyVector(x, y, ""));
        }

        return points;
    }
    public void setName(String ca) {
        name = ca;
    }

    public void setSize(int s) {
        width = s;
        height = s;
    }

    public void setVisible(boolean b) {
        visible = b;
    }

    public boolean getVisible() {
        return visible;
    }
}
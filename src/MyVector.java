import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

class MyVector extends Ellipse2D.Double {
    private String name;
    protected boolean selected = false;
    private boolean visible = true;
    private int id = -1;

    protected MyVector(double p1, double p2, String str) {

        name = str;
        this.x = p1;
        this.y = p2;
        this.width = 8;
        this.height = 8;
    }

    protected MyVector(Point2D.Double p) {
        this.x = p.x;
        this.y = p.y;
        this.width = 8;
        this.height = 8;
    }

    protected static void printArrayList(ArrayList<MyVector> list) {
        for (MyVector vector : list) {
            vector.print("al: ");
        }
    }

    protected static MyVector circleFromPoints(MyVector p1, MyVector p2, MyVector p3, double radius) {

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

    protected void fill(Graphics2D g2d, boolean drawAnnotation) {

        if( !visible ) return;
        g2d.fill(new Ellipse2D.Double(x - width / 2, y - width / 2, width, width));
        if (drawAnnotation) {
            g2d.drawString(name, (int) x + 6, (int) y + 6);
        }
    }
    protected void fill(Graphics2D g2d) {
        fill(g2d, false);
    }

    protected void print(String s) {

        IRISVisualization.println("name: " + s + " x: " + x + " y: " + y);
    }

    protected void print() {

        print("");
    }

    protected String getName() {
        return name;
    }

    protected MyVector flip() {
        return new MyVector(y, x, name);
    }

    protected MyVector rotate(double ang) {

        double newX = this.x * Math.cos(ang) - this.y * Math.sin(ang);
        double newY = this.x * Math.sin(ang) + this.y * Math.cos(ang);
        return new MyVector(newX, newY, this.name);
    }

    protected static Point2D.Double rotate(Point2D.Double point, double ang) {

        double newX = point.x * Math.cos(ang) - point.y * Math.sin(ang);
        double newY = point.x * Math.sin(ang) + point.y * Math.cos(ang);
        return new Point2D.Double(newX, newY);
    }

    protected MyVector add(MyVector in) {

        return new MyVector(x + in.x, y + in.y, name);
    }

    protected MyVector subtract(MyVector in) {
        return new MyVector(x - in.x, y - in.y, name);
    }

    protected MyVector multiply(double v) {
        return new MyVector(x * v, y * v, name);
    }

    protected static MyVector getVector(MyVector h1, MyVector h2) {

        return new MyVector(h1.x - h2.x, h1.y - h2.y, h1.name);
    }

    protected static double angleBetweenHandles(MyVector handle1, MyVector handle2) {

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

    protected MyVector makeItThatLong(double l) {

        double len = getLength();

        x = (x / len) * l;
        y = (y / len) * l;

        return new MyVector(x, y, name);
    }

    protected double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    protected static double distanceToPointFromLine(Point2D.Double p1, Point2D.Double p2, MyVector v1, MyVector v2) {

        // Adjust the point coordinates based on the scene shift
        double adjustedX = p1.x - p2.x;
        double adjustedY = p1.y - p2.y;

        // Check if the adjusted point is within the bounding box defined by the two vectors
        double minX = Math.min(v1.x, v2.x);
        double minY = Math.min(v1.y, v2.y);
        double maxX = Math.max(v1.x, v2.x);
        double maxY = Math.max(v1.y, v2.y);

        /// TODO: check again for whipers etc.
        if (adjustedX < minX || adjustedX > maxX || adjustedY < minY || adjustedY > maxY) {
            // Point is outside the bounding box OF THE VECTOR, return some default value (e.g., -1)
            return 1000;
        }

        // Calculate the distance to the line
        double x1 = v1.x;
        double y1 = v1.y;
        double x2 = v2.x;
        double y2 = v2.y;

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

    protected static ArrayList<MyVector> scatterPointsAround(MyVector center, double radius, int numPoints) {

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

    protected void setName(String ca) {
        name = ca;
    }

    protected void setSize(int s) {
        width = s;
        height = s;
    }

    protected void setVisible(boolean b) {
        visible = b;
    }

    protected boolean getVisible() {
        return visible;
    }

    protected void setNameToPosition() {

        DecimalFormat formatter = new DecimalFormat("#000.00");
        String xs = formatter.format(x);
        String ys = formatter.format(y);

        name = "x: " + xs + " y: " + ys;
    }

    public int getId() {
        return id;
    }

    public void setId(int index) {
        this.id = index;
    }
}
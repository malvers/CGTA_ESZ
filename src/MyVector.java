import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

class MyVector extends Ellipse2D.Double {
    private String name;
    private double d = 10;
    public boolean selected = false;

    public MyVector(double p1, double p2, String str) {

        name = str;
        this.x = p1;
        this.y = p2;
        this.width = d;
        this.height = d;
    }

    @Override
    public boolean contains(double x, double y) {
        double xx = x + width / 2;
        double yy = y + width / 2;
        return super.contains(xx, yy);
    }

    public void fill(Graphics2D g2d, boolean drawAnnotation) {

        g2d.fill(new Ellipse2D.Double(x - width / 2, y - width / 2, width, width));
        if (drawAnnotation) {
            g2d.drawString(name, (int) x + 6, (int) y + 6);
        }
    }

    public void print(String s) {

        System.out.println(s + " name: " + " x: " + x + " y: " + y);
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
        double x = adjustedX;
        double y = adjustedY;
        double x1 = handle1.x;
        double y1 = handle1.y;
        double x2 = handle2.x;
        double y2 = handle2.y;

        double numerator = Math.abs((x2 - x1) * (y1 - y) - (x1 - x) * (y2 - y1));
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

    public void setName(String ca) {
        name = ca;
    }
}
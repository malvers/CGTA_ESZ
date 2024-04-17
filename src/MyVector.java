import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

class MyVector extends Ellipse2D.Double {
    private final String name;
    public boolean selected = false;

    public MyVector(double p1, double p2, double d, String str) {

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

    public MyVector add(MyVector in) {

        return new MyVector(x + in.x, y + in.y, 6, "");
    }

    public MyVector flip() {
        return new MyVector(y, x, 6, "");
    }

    public MyVector multiply(double v) {
        return new MyVector(x * v, y * v, 10, "");
    }

    public MyVector rotate(double ang) {

        double newX = this.x * Math.cos(ang) - this.y * Math.sin(ang);
        double newY = this.x * Math.sin(ang) + this.y * Math.cos(ang);
        return new MyVector(newX, newY, 6, this.name);
    }

    public MyVector subtract(MyVector in) {

        return new MyVector(x - in.x, y - in.y, 6, "");
    }

    protected static MyVector getVector(MyVector h1, MyVector h2) {

        return new MyVector(h1.x - h2.x, h1.y - h2.y, 2, "");
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

        return new MyVector(x, y , 10, "");
    }

    protected double getLength() {
        return Math.sqrt(x*x + y*y);
    }

    public static double distanceToPointFromLine(Point point, Point2D.Double sceneShift, MyVector handle1, MyVector handle2) {

        double x = point.x - sceneShift.x;
        double y = point.y - sceneShift.y;
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
        return Math.sqrt(dx*dx + dy*dy);
    }
}
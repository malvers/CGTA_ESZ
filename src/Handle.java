import java.awt.*;
import java.awt.geom.Ellipse2D;

class Handle extends Ellipse2D.Double {
    private final String name;
    public boolean selected = false;

    public Handle(double p1, double p2, double d, String str) {

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

    public Handle add(Handle in) {

        return new Handle(x + in.x, y + in.y, 6, "");
    }

    public Handle flip() {
        return new Handle(y, x, 6, "");
    }

    public Handle multiply(double v) {
        return new Handle(x * v, y * v, 10, "");
    }

    public Handle subtract(Handle in) {

        return new Handle(x - in.x, y - in.y, 6, "");
    }

    public Handle makeItThatLong(double l) {

        double len = getLength();

        x = (x / len) * l;
        y = (y / len) * l;

        return new Handle(x, y , 10, "");
    }

    protected double getLength() {
        return Math.sqrt(x*x + y*y);
    }

    protected static double getLength(Handle o, Handle t) {

        double dx = o.x - t.x;
        double dy = o.y - t.y;
        return Math.sqrt(dx*dx + dy*dy);
    }
}
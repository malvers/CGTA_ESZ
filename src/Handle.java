import java.awt.*;
import java.awt.geom.Ellipse2D;

class Handle extends Ellipse2D.Double {
    private String name = "";
    public boolean selected = false;

    public Handle(double p1, double p2, double d, String str) {

        name = str;
        this.x = p1;
        this.y = p2;
        this.width = d;
        this.height = d;
    }

    public void fill(Graphics2D g2d) {

        g2d.fill(new Ellipse2D.Double(x - width / 2, y - width / 2, width, width));
        g2d.drawString(name, (int) x, (int) y);
    }

    public void print() {

        System.out.println(name + " x: " + x + " y: " + y);
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
}
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class MyDoublePolygon {
    private final List<Point2D.Double> points;

    public MyDoublePolygon() {
        points = new ArrayList<>();
    }

    public void addPoint(double x, double y) {
        points.add(new Point2D.Double(x, y));
    }

    public List<Point2D.Double> getPoints() {
        return points;
    }

    public int getNumPoints() {
        return points.size();
    }

    // Other methods such as area, perimeter, etc. could be added here
}

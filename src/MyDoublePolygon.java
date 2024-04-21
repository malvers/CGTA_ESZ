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

    public void addCurve(MyDoublePolygon curve) {

        for (int i = 0; i < curve.getNumPoints(); i++) {
            this.addPoint(curve.getPoint(i));
        }
    }

    private void addPoint(Point2D.Double point) {
        points.add(point);
    }

    private Point2D.Double getPoint(int i) {

        return points.get(i);
    }

    // Other methods such as area, perimeter, etc. could be added here
}

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

final class Painter {

    protected Point2D.Double paintShiftStart = new Point2D.Double();
    protected Point2D.Double paintShift = new Point2D.Double();
    protected Point2D.Double zoomPoint = new Point2D.Double();
    protected double zoomFactorStart = 1.0;
    protected double zoomFactor = zoomFactorStart;

    private AffineTransform transform = new AffineTransform();

    protected void zoomToPoint(double zoom, double x, double y) {

        zoomFactor = zoomFactorStart - zoom;
        zoomPoint.x = x;
        zoomPoint.y = y;

        IrisVis.println("zoom: " + zoom + " zoom factor: " + zoomFactor);
    }

    @SafeVarargs
    protected final void paintAll(Graphics2D g2d, Consumer<Graphics2D>... painters) {

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));

        setZoomAndPan(g2d);

        for (Consumer<Graphics2D> p : painters) {
            p.accept(g2d);
        }

        g2d.dispose();
    }

    private void setZoomAndPan(Graphics2D g2d) {

        transform = AffineTransform.getTranslateInstance(paintShift.x, paintShift.y);
        transform.translate(+zoomPoint.x, +zoomPoint.y);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(-zoomPoint.x, -zoomPoint.y);
        g2d.setTransform(transform);
    }

    protected void storeAtStart() {

        zoomFactorStart = zoomFactor;
        paintShiftStart.x = paintShift.x;
        paintShiftStart.y = paintShift.y;
    }

    protected void setPaintShift(double dx, double dy) {

        paintShift.x = paintShiftStart.x - dx;
        paintShift.y = paintShiftStart.y - dy;

    }
}
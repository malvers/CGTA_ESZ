import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

class Painter {

    protected Point2D.Double paintShiftStart = new Point2D.Double();
    protected Point2D.Double paintShift = new Point2D.Double();
    protected Point2D.Double zoomPoint = new Point2D.Double();
    protected double zoomFactorStart = 1.0;
    protected double zoomFactor = zoomFactorStart;

    private AffineTransform transform = new AffineTransform();

    protected void zoomToPoint(double zoom) {

        zoomFactor = zoomFactorStart - zoom;
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

    protected Point transform(Point p) {

        int x = (int) (zoomFactor * p.x + paintShift.x);
        int y = (int) (zoomFactor * p.y + paintShift.y);
        return new Point(x, y);
    }

    private void setZoomAndPan(Graphics2D g2d) {

        transform = AffineTransform.getTranslateInstance(paintShift.x * zoomFactor, paintShift.y * zoomFactor);

        transform.translate(+zoomPoint.x, +zoomPoint.y);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(-zoomPoint.x, -zoomPoint.y);

        g2d.setTransform(transform);
    }

    protected void storeAtStart(Point2D p) {

        zoomFactorStart = zoomFactor;
        zoomPoint.x = p.getX();
        zoomPoint.y = p.getY();
        paintShiftStart.x = paintShift.x;
        paintShiftStart.y = paintShift.y;
    }

    protected void setPaintShift(double dx, double dy) {

        IrisVis.println();
        IrisVis.print("delta dx: " + dx + " dy: " + dy);
        IrisVis.print(" start x:  " + paintShiftStart.x + " y: " + paintShiftStart.y);

        paintShift.x = paintShiftStart.x - dx / zoomFactor;
        paintShift.y = paintShiftStart.y - dy / zoomFactor;

        IrisVis.print(" after x:  " + paintShift.x + " y: " + paintShift.y);
        IrisVis.println();
    }
}
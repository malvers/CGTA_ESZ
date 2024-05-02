import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

final class Painter {

    public Point2D.Double sceneShift = new Point2D.Double();
    public Point2D.Double dragShift = new Point2D.Double();
    public double zoomFactor = 1.0;

    @SafeVarargs
    public final void paintAll(Graphics2D g2d, double width, double height, Consumer<Graphics2D>... painters) {

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));

        double xs = (sceneShift.x + dragShift.x);
        double ys = (sceneShift.y + dragShift.y);

        AffineTransform transform = AffineTransform.getTranslateInstance(xs, ys);

        double dx = width / 2.0;
        double dy = height / 2.0;
        transform.translate(-dx, -dy);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(+dx, +dy);

        g2d.setTransform(transform);

        for (Consumer<Graphics2D> p : painters) {
            p.accept(g2d);
        }
    }
}
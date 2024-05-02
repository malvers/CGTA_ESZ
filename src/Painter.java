import java.awt.*;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

final class Painter {

    public Point2D.Double sceneShift = new Point2D.Double();
    public double zoomFactor = 1.0;

    @SafeVarargs
    public final void paintAll(Graphics2D g2d, Consumer<Graphics2D>... painters) {

        for (Consumer<Graphics2D> p : painters) {
            p.accept(g2d);
        }
    }
}
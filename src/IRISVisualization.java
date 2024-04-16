import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;

public class IRISVisualization extends JButton {

    private final JFrame frame;
    private Handle exBC;
    private Handle exAB;
    private Handle exCB;
    private Handle exAC;
    private Handle exCA;
    private Handle exBA;
    private final Color color1 = new Color(180, 0, 0);
    private Color color2 = new Color(0, 0, 80);
    private final Color color3 = new Color(80, 140, 0);
    private Handle handleA;
    private Handle handleB;
    private Handle handleC;
    private Handle midCA_BA;
    private Handle midCB_AB;
    private Handle midBC_AC;
    private final Point2D.Double onMousePressed = new Point2D.Double();
    private final Point2D.Double dragShift = new Point2D.Double();
    private Point2D.Double sceneShift = new Point2D.Double();
    private boolean drawAnnotation = true;
    private Handle towCA_BA;
    private Handle towBC_AC;
    private Handle towCB_AB;
    private Handle centerCircle;
    private double radiusCircle;
    private boolean drawCircle = false;
    private boolean drawLines = false;
    private boolean drawIris = false;
    private boolean blackMode = false;
    private boolean drawWhiskers = false;
    private boolean drawTriangle = false;

    public IRISVisualization(JFrame f) {

        frame = f;

        mouseInit();
        keyInit();

        double handleSize = 12;
        sceneShift.x = 80;
        sceneShift.y = 20;

        handleA = new Handle(360, 280, handleSize, "A");
        handleB = new Handle(220, 480, handleSize, "B");
        handleC = new Handle(390, 540, handleSize, "C");

        readSettings();

        adjustColorBlackMode();

        doCalculations();
    }

    private void writeSettings() {

        try {
            String uh = System.getProperty("user.home");
            FileOutputStream f = new FileOutputStream(uh + "/IRISVisualization.bin");
            ObjectOutputStream os = new ObjectOutputStream(f);

            os.writeObject(frame.getSize());
            os.writeObject(frame.getLocation());

            os.writeObject(sceneShift);

            os.writeObject(handleA);
            os.writeObject(handleB);
            os.writeObject(handleC);

            os.writeBoolean(drawCircle);
            os.writeBoolean(drawLines);
            os.writeBoolean(drawAnnotation);
            os.writeBoolean(drawIris);
            os.writeBoolean(blackMode);
            os.writeBoolean(drawWhiskers);
            os.writeBoolean(drawTriangle);

            os.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSettings() {

        try {
            String uh = System.getProperty("user.home");
            FileInputStream f = new FileInputStream(uh + "/IRISVisualization.bin");
            ObjectInputStream os = new ObjectInputStream(f);

            frame.setSize((Dimension) os.readObject());
            frame.setLocation((Point) os.readObject());

            sceneShift = (Point2D.Double) os.readObject();

            handleA = (Handle) os.readObject();
            handleB = (Handle) os.readObject();
            handleC = (Handle) os.readObject();

            drawCircle = os.readBoolean();
            drawLines = os.readBoolean();
            drawAnnotation = os.readBoolean();
            drawIris = os.readBoolean();
            blackMode = os.readBoolean();
            drawWhiskers = os.readBoolean();
            drawTriangle = os.readBoolean();

            os.close();
            f.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void mouseInit() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                onMousePressed.x = e.getX();
                onMousePressed.y = e.getY();

                Point2D.Double shiftMouse = new Point2D.Double();

                shiftMouse.x = e.getX() - sceneShift.x;
                shiftMouse.y = e.getY() - sceneShift.y;

                deselectAllHandles();

                if (handleA.contains(shiftMouse.x, shiftMouse.y)) {
                    handleA.selected = true;
                } else if (handleB.contains(shiftMouse.x, shiftMouse.y)) {
                    handleB.selected = true;
                } else if (handleC.contains(shiftMouse.x, shiftMouse.y)) {
                    handleC.selected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                sceneShift.x += dragShift.x;
                sceneShift.y += dragShift.y;
                dragShift.x = 0;
                dragShift.y = 0;

                deselectAllHandles();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (handleA.selected) {
                    handleA.x = e.getX() - sceneShift.x;
                    handleA.y = e.getY() - sceneShift.y;
                } else if (handleB.selected) {
                    handleB.x = e.getX() - sceneShift.x;
                    handleB.y = e.getY() - sceneShift.y;
                } else if (handleC.selected) {
                    handleC.x = e.getX() - sceneShift.x;
                    handleC.y = e.getY() - sceneShift.y;
                } else {
                    dragShift.x = -(onMousePressed.x - e.getX());
                    dragShift.y = -(onMousePressed.y - e.getY());
                }
                doCalculations();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                frame.setTitle("Conway's IRIS - x: " + e.getX() + " y: " + e.getY());
            }
        });
    }

    private void keyInit() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_W && e.isMetaDown()) {
                    writeSettings();
                    System.exit(0);
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    drawAnnotation = !drawAnnotation;
                } else if (e.getKeyCode() == KeyEvent.VK_B) {
                    blackMode = !blackMode;
                    adjustColorBlackMode();
                } else if (e.getKeyCode() == KeyEvent.VK_C) {
                    drawCircle = !drawCircle;
                } else if (e.getKeyCode() == KeyEvent.VK_I) {
                    drawIris = !drawIris;
                } else if (e.getKeyCode() == KeyEvent.VK_L) {
                    drawLines = !drawLines;
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    sceneShift.x = 0;
                    sceneShift.y = 0;
                } else if (e.getKeyCode() == KeyEvent.VK_T) {
                    drawTriangle = !drawTriangle;
                } else if (e.getKeyCode() == KeyEvent.VK_W) {
                    drawWhiskers = !drawWhiskers;
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    moveHandles(e.getKeyCode());
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    moveHandles(e.getKeyCode());
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    moveHandles(e.getKeyCode());
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    moveHandles(e.getKeyCode());
                }
                repaint();
            }
        });
    }

    private void moveHandles(int vkUp) {

        switch (vkUp) {
            case KeyEvent.VK_UP:
                handleA.y += 20;
                handleB.y += 20;
                handleC.y += 20;
                break;
            case KeyEvent.VK_DOWN:
                handleA.y -= 20;
                handleB.y -= 20;
                handleC.y -= 20;
                break;
            case KeyEvent.VK_LEFT:
                handleA.x -= 20;
                handleB.x -= 20;
                handleC.x -= 20;
                break;
            case KeyEvent.VK_RIGHT:
                handleA.x += 20;
                handleB.x += 20;
                handleC.x += 20;
                break;
        }

    }

    private void adjustColorBlackMode() {
        if (blackMode) {
            color2 = new Color(0, 180, 180);
        } else {
            color2 = new Color(0, 0, 80);
        }
    }

    private void deselectAllHandles() {
        handleA.selected = false;
        handleB.selected = false;
        handleC.selected = false;
    }

    public static double calculateInnerRadiusTriangle(Handle A, Handle B, Handle C) {

        // Calculate side lengths
        double a = Handle.getLength(B, C);
        double b = Handle.getLength(C, A);
        double c = Handle.getLength(A, B);

        // Calculate the perimeter
        double perimeter = a + b + c;

        // Calculate the area using Heron's formula
        double s = perimeter / 2.0; // Semi-perimeter
        double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));

        // Calculate the radius of the inscribed circle
        return 2.0 * area / perimeter;
    }

    private void doCalculations() {

        exBA = calculateExtendedPoint(handleB, handleC, handleA);
        exCA = calculateExtendedPoint(handleC, handleB, handleA);

        exBC = calculateExtendedPoint(handleB, handleA, handleC);
        exAC = calculateExtendedPoint(handleA, handleB, handleC);

        exAB = calculateExtendedPoint(handleA, handleC, handleB);
        exCB = calculateExtendedPoint(handleC, handleA, handleB);

        midCA_BA = calculateMidpoint(exCA, exBA);
        midBC_AC = calculateMidpoint(exAC, exBC);
        midCB_AB = calculateMidpoint(exCB, exAB);

        Handle vecCA_BA = getVector(exBA, exCA);
        Handle recCA_BA = vecCA_BA.flip();
        recCA_BA.x = -recCA_BA.x;
        recCA_BA = recCA_BA.makeItThatLong(400);
        towCA_BA = midCA_BA.add(recCA_BA);

        Handle vecBC_AC = getVector(exBC, exAC);
        Handle recBC_AC = vecBC_AC.flip();
        recBC_AC.x = -recBC_AC.x;
        recBC_AC.makeItThatLong(400);
        towBC_AC = midBC_AC.subtract(recBC_AC);

        Handle vecCB_AB = getVector(exCB, exAB);
        Handle recCB_AB = vecCB_AB.flip();
        recCB_AB.x = -recCB_AB.x;
        recCB_AB.makeItThatLong(400);
        towCB_AB = midCB_AB.add(recCB_AB);

        centerCircle = getIntersectionPointGPT(midCB_AB, towCB_AB, midBC_AC, towBC_AC);

        if (centerCircle != null) {
            radiusCircle = getVector(centerCircle, exAB).getLength();
        }
    }

    private Handle calculateExtendedPoint(Handle one, Handle two, Handle three) {

        double vector12_x = one.getX() - two.getX();
        double vector12_y = one.getY() - two.getY();
        double length12 = Math.sqrt(Math.pow(vector12_x, 2) + Math.pow(vector12_y, 2));

        double vector13_x = one.getX() - three.getX();
        double vector13_y = one.getY() - three.getY();
        double length13 = Math.sqrt(Math.pow(vector13_x, 2) + Math.pow(vector13_y, 2));

        double vector13Scaled_x = vector13_x / length13;
        double vector13Scaled_y = vector13_y / length13;

        vector13Scaled_x *= length12;
        vector13Scaled_y *= length12;

        String name = one.getName() + three.getName();
        return new Handle(three.getX() - vector13Scaled_x, three.getY() - vector13Scaled_y, 6, name);
    }

    private Handle getVector(Handle h1, Handle h2) {

        return new Handle(h1.x - h2.x, h1.y - h2.y, 2, "");

    }

    private Handle calculateMidpoint(Handle point1, Handle point2) {

        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new Handle(midX, midY, 6, "");
    }

    private Handle getIntersectionPointGPT(Handle handle1Start, Handle handle1End, Handle handle2Start, Handle handle2End) {

        // Get the coordinates of the handles
        double x1 = handle1Start.x, y1 = handle1Start.y;
        double x2 = handle1End.x, y2 = handle1End.y;
        double x3 = handle2Start.x, y3 = handle2Start.y;
        double x4 = handle2End.x, y4 = handle2End.y;

        // Calculate the determinant to check for parallel lines
        double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (det == 0) {
            return null; // Parallel lines, no intersection
        }

        // Calculate the intersection point
        double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det;

        return new Handle(px, py, 10, "center");
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));

        if (blackMode) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        AffineTransform shiftTransform = AffineTransform.getTranslateInstance(sceneShift.x + dragShift.x, sceneShift.y + dragShift.y);
        g2d.setTransform(shiftTransform);

        if (drawCircle) {
            drawCircle(g2d);
        }

        if (drawIris) {
            drawIris(g2d);
        }

        if (drawWhiskers) {
            drawWhiskers(g2d);
        }

        if (drawTriangle) {
            drawTriangle(g2d);
        }

        if (drawLines) {
            drawLines(g2d);
        }
    }

    private void drawTriangle(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(3));

        g2d.setColor(color2);
        drawHandleConnector(g2d, handleA, handleB);
        g2d.setColor(color3);
        drawHandleConnector(g2d, handleB, handleC);
        g2d.setColor(color1);
        drawHandleConnector(g2d, handleC, handleA);

        g2d.setColor(Color.darkGray);
        handleA.fill(g2d, drawAnnotation);
        handleB.fill(g2d, drawAnnotation);
        handleC.fill(g2d, drawAnnotation);
    }

    private void drawWhiskers(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(color3);
        exBA.fill(g2d, drawAnnotation);
        exCA.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, exBA.x, exBA.y));
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, exCA.x, exCA.y));
        g2d.setColor(color2);
        exBC.fill(g2d, drawAnnotation);
        exAC.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, exBC.x, exBC.y));
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, exAC.x, exAC.y));
        g2d.setColor(color1);
        exAB.fill(g2d, drawAnnotation);
        exCB.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, exAB.x, exAB.y));
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, exCB.x, exCB.y));
    }

    private void drawLines(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.lightGray);
        drawHandleConnector(g2d, exCA, exBA);
        drawHandleConnector(g2d, exBC, exAC);
        drawHandleConnector(g2d, exCB, exAB);

        midCA_BA.fill(g2d, drawAnnotation);
        midBC_AC.fill(g2d, drawAnnotation);
        midCB_AB.fill(g2d, drawAnnotation);

        g2d.setColor(Color.LIGHT_GRAY);
        drawHandleConnector(g2d, midCA_BA, towCA_BA);
        drawHandleConnector(g2d, midBC_AC, towBC_AC);
        drawHandleConnector(g2d, midCB_AB, towCB_AB);
    }

    private void drawIris(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA.darker().darker());
        double ir = calculateInnerRadiusTriangle(handleA, handleB, handleC);
        g2d.draw(new Ellipse2D.Double(centerCircle.x - ir, centerCircle.y - ir, 2 * ir, 2 * ir));
    }

    private void drawCircle(Graphics2D g2d) {
        g2d.setColor(Color.MAGENTA.darker().darker());
        centerCircle.fill(g2d, drawAnnotation);
        g2d.draw(new Ellipse2D.Double(centerCircle.x - radiusCircle, centerCircle.y - radiusCircle, 2 * radiusCircle, 2 * radiusCircle));
    }

    private void drawHandleConnector(Graphics2D g2d, Handle h1, Handle h2) {

        g2d.draw(new Line2D.Double(h1.x, h1.y, h2.x, h2.y));
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame f = new JFrame();
            f.setSize(760, 760);
            f.add(new IRISVisualization(f));
            f.setVisible(true);
        });
    }
}

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IRISVisualization extends JButton {

    /*

      IDEAS:
      - box arround whiper curve

    */

    private final static String defaulTitle = "Conway's IRIS - Press H for Help";
    private final JFrame frame;
    private final BufferedImage irisPicture;
    private MyVector extendBC;
    private MyVector extendAB;
    private MyVector extendCB;
    private MyVector extendAC;
    private MyVector extendCA;
    private MyVector extendBA;
    private final Color color1 = new Color(180, 0, 0);
    private Color color2 = new Color(0, 0, 80);
    private final Color color3 = new Color(80, 140, 0);
    private final MyVector handleA;
    private final MyVector handleB;
    private final MyVector handleC;
    private MyVector midCA_BA;
    private MyVector midCB_AB;
    private MyVector midBC_AC;
    private final Point2D.Double onMousePressed = new Point2D.Double();
    private final Point2D.Double dragShift = new Point2D.Double();
    private Point2D.Double sceneShift = new Point2D.Double();
    private boolean drawAnnotation = true;
    private MyVector towCA_BA;
    private MyVector towBC_AC;
    private MyVector towCB_AB;
    private MyVector centerCircle;
    private double radiusCircle;
    private boolean drawCircle = false;
    private boolean drawLines = false;
    private boolean drawIris = false;
    private boolean blackMode = false;
    private boolean drawWhiskers = false;
    private boolean drawTriangle = false;
    private boolean drawHelp = false;
    private boolean drawIrisPicture = true;
    private boolean debugMode = true;
    private boolean drawWhiperStuff = false;
    private int irisPicSize = 688;
    private int zoomedSize;
    private int irisPicShiftX = 163;
    private int irisPicShiftY = 186;
    private MyVector whiperCB;
    private MyVector whiperAC;
    private MyVector whiperBA;
    private MyVector whiperCA;
    private MyVector whiperBC;
    private MyVector whiperAB;
    private MyDoublePolygon bigWhiperCurve1 = new MyDoublePolygon();
    private MyDoublePolygon bigWhiperCurve2 = new MyDoublePolygon();
    private MyDoublePolygon bigWhiperCurve3 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve1 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve2 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve3 = new MyDoublePolygon();
    private double whiperFactor = 1.0;
    private Ellipse2D.Double irisCircle;
    private Ellipse2D.Double irisCircleStore;
    private double irisZoom = 1.0;
    private Rectangle boundingBoxWC;
    private double rotationAngle = 0;
    private Polygon testPolygon;
    private MyDoublePolygon hugeCurve;
    private ArrayList<MyVector> boundingBoxCircle;

    public IRISVisualization(JFrame f) {

        frame = f;

        mouseInit();
        keyInit();

        double handleSize = 12;
        sceneShift.x = 80;
        sceneShift.y = 20;

        handleA = new MyVector(535, 395, "A");
        handleB = new MyVector(358, 573, "B");
        handleC = new MyVector(615, 635, "C");

        readSettings();

        adjustColorBlackMode();

        irisPicture = loadImage("/Users/malvers/IdeaProjects/CGTA_ESZ/src/Iris Mi free.png");

        doCalculations();
        calculateWhipers();
        boundingBoxCircle = new ArrayList<>();
    }

    private void writeSettings() {

        try {
            String uh = System.getProperty("user.home");
            FileOutputStream f = new FileOutputStream(uh + "/IRISVisualization.bin");
            ObjectOutputStream os = new ObjectOutputStream(f);

            os.writeObject(frame.getSize());
            os.writeObject(frame.getLocation());

            os.writeObject(sceneShift);

            os.writeBoolean(drawCircle);
            os.writeBoolean(drawLines);
            os.writeBoolean(drawAnnotation);
            os.writeBoolean(drawIris);
            os.writeBoolean(blackMode);
            os.writeBoolean(drawWhiskers);
            os.writeBoolean(drawTriangle);
            os.writeBoolean(drawIrisPicture);

            os.writeInt(irisPicShiftX);
            os.writeInt(irisPicShiftY);
            os.writeInt(irisPicSize);

            os.writeBoolean(drawWhiperStuff);
            os.writeBoolean(debugMode);

//            os.writeObject(handleA);
//            os.writeObject(handleB);
//            os.writeObject(handleC);


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

            drawCircle = os.readBoolean();
            drawLines = os.readBoolean();
            drawAnnotation = os.readBoolean();
            drawIris = os.readBoolean();
            blackMode = os.readBoolean();
            drawWhiskers = os.readBoolean();
            drawTriangle = os.readBoolean();
            drawIrisPicture = os.readBoolean();

            os.readInt();
            os.readInt();
            os.readInt();

            drawWhiperStuff = os.readBoolean();
            debugMode = os.readBoolean();

//            handleA = (Handle) os.readObject();
//            handleB = (Handle) os.readObject();
//            handleC = (Handle) os.readObject();

            os.close();
            f.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /// mouse handling /////////////////////////////////////////////////////////////////////////////////////////////////
    private void mouseInit() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                onMousePressed.x = e.getX();
                onMousePressed.y = e.getY();

                Point2D.Double shiftMouse = new Point2D.Double();

                shiftMouse.x = e.getX() - sceneShift.x;
                shiftMouse.y = e.getY() - sceneShift.y;

                double delta = 4.0;

                deselectAllHandles();

                irisCircleStore = irisCircle;

                if (handleA.contains(shiftMouse.x, shiftMouse.y)) {
                    handleA.selected = true;
                } else if (handleB.contains(shiftMouse.x, shiftMouse.y)) {
                    handleB.selected = true;
                } else if (handleC.contains(shiftMouse.x, shiftMouse.y)) {
                    handleC.selected = true;

                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleA, whiperAC) < delta) {
                    whiperAC.selected = true;
                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleB, whiperBA) < delta) {
                    whiperBA.selected = true;
                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleC, whiperCB) < delta) {
                    whiperCB.selected = true;

                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleA, whiperCA) < delta) {
                    whiperCA.selected = true;
                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleC, whiperBC) < delta) {
                    whiperBC.selected = true;
                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleB, whiperAB) < delta) {
                    whiperAB.selected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                irisPicSize = zoomedSize;
                irisZoom = 1.0;

                sceneShift.x += dragShift.x;
                sceneShift.y += dragShift.y;
                dragShift.x = 0;
                dragShift.y = 0;

                calculateWhipers();
                setWhipersVisibility(true);
                deselectAllHandles();
                deselectAllWhipers();
            }
        });

        handleMouseMotion();
    }

    private void handleMouseMotion() {

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                irisZoom = irisCircle.getWidth() / irisCircleStore.getWidth();

                if (handleA.selected) {
                    handleA.x = e.getX() - sceneShift.x;
                    handleA.y = e.getY() - sceneShift.y;
                    setWhipersVisibility(false);
                } else if (handleB.selected) {
                    handleB.x = e.getX() - sceneShift.x;
                    handleB.y = e.getY() - sceneShift.y;
                    setWhipersVisibility(false);
                } else if (handleC.selected) {
                    handleC.x = e.getX() - sceneShift.x;
                    handleC.y = e.getY() - sceneShift.y;
                    setWhipersVisibility(false);
                } else if (whiperAC.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperAC, handleA, bigWhiperCurve3);
                    if (tmp != null) {
                        whiperAC.x = tmp.x;
                        whiperAC.y = tmp.y;
                    }
                } else if (whiperBA.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperBA, handleB, bigWhiperCurve2);
                    if (tmp != null) {
                        whiperBA.x = tmp.x;
                        whiperBA.y = tmp.y;
                    }
                } else if (whiperCB.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperCB, handleC, bigWhiperCurve1);
                    if (tmp != null) {
                        whiperCB.x = tmp.x;
                        whiperCB.y = tmp.y;
                    }
                } else if (whiperCA.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperCA, handleA, smallWhiperCurve2);
                    if (tmp != null) {
                        whiperCA.x = tmp.x;
                        whiperCA.y = tmp.y;
                    }
                } else if (whiperBC.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperBC, handleB, smallWhiperCurve3);
                    if (tmp != null) {
                        whiperBC.x = tmp.x;
                        whiperBC.y = tmp.y;
                    }
                } else if (whiperAB.selected) {
                    MyVector tmp = calculateTemporaryWhiper(e, whiperAB, handleB, smallWhiperCurve1);
                    if (tmp != null) {
                        whiperAB.x = tmp.x;
                        whiperAB.y = tmp.y;
                    }
                } else {
                    dragShift.x = -(onMousePressed.x - e.getX());
                    dragShift.y = -(onMousePressed.y - e.getY());
                }
                doCalculations();
                createWhiperCurve();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

                if (!drawHelp) {
                    String title = "Conway's IRIS - Press H for Help";
                    if (debugMode) {
                        title += " - x: " + e.getX() + " y: " + e.getY();
                    }
                    frame.setTitle(title);
                }
            }
        });
    }

    /// key handling ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void keyInit() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

//                System.out.println("key: " + e.getKeyCode());
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_SPACE:
                        handleSpaceBar();
                        break;
                    case KeyEvent.VK_A:
                        drawAnnotation = !drawAnnotation;
                        break;
                    case KeyEvent.VK_B:
                        blackMode = !blackMode;
                        adjustColorBlackMode();
                        break;
                    case KeyEvent.VK_C:
                        drawCircle = !drawCircle;
                        break;
                    case KeyEvent.VK_D:
                        debugMode = !debugMode;
                        printHandles();
                        doCalculations();
                        calculateWhipers();
                        break;
                    case KeyEvent.VK_E:
                        drawWhiskers = !drawWhiskers;
                        break;
                    case KeyEvent.VK_H:
                        drawHelp = !drawHelp;
                        break;
                    case KeyEvent.VK_I:
                        drawIris = !drawIris;
                        break;
                    case KeyEvent.VK_L:
                        drawLines = !drawLines;
                        break;
                    case KeyEvent.VK_P:
                        boolean bm = blackMode;
                        drawIrisPicture = !drawIrisPicture;
                        if (drawIrisPicture) {
                            blackMode = true;
                        } else {
                            blackMode = bm;
                        }
                        break;
                    case KeyEvent.VK_R:
                        rotationAngle = 0.0;
                        whiperFactor = 1.0;
                        sceneShift.x = 0;
                        sceneShift.y = 0;
                        doCalculations();
                        calculateWhipers();
                        break;
                    case KeyEvent.VK_S:
                        screenshotCapture();
                        break;
                    case KeyEvent.VK_T:
                        drawTriangle = !drawTriangle;
                        break;
                    case KeyEvent.VK_W:
                        if (e.isMetaDown()) {
                            writeSettings();
                            System.exit(0);
                        } else {
                            drawWhiperStuff = !drawWhiperStuff;
                        }
                        break;
                    case KeyEvent.VK_Z:
                        if (e.isMetaDown()) {
                            irisZoom += 0.1;
                        } else {
                            irisZoom -= 0.1;
                        }
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        if (e.isMetaDown()) {
                            moveIrisPic(e.getKeyCode(), 1);
                        } else {
                            shiftTestPolygon(e.getKeyCode());
                        }
                        break;
                    case 93: /// +
                        if (e.isShiftDown()) {

                        } else if (e.isMetaDown()) {

                        } else {
                            whiperFactor += 0.01;
                            doCalculations();
                            calculateWhipers();
                        }
                        break;
                    case 47: /// -
                        if (e.isShiftDown()) {

                        } else if (e.isMetaDown()) {

                        } else {
                            whiperFactor -= 0.01;
                            doCalculations();
                            calculateWhipers();
                        }
                        break;
                }

                repaint();
            }
        });
    }

    private void shiftTestPolygon(int vKup) {

        int[] xPoints = testPolygon.xpoints;
        int[] yPoints = testPolygon.ypoints;

        int inc = 1;
        for (int i = 0; i < testPolygon.npoints; i++) {

            switch (vKup) {
                case KeyEvent.VK_UP:
                    yPoints[i] -= inc;
                    break;
                case KeyEvent.VK_DOWN:
                    yPoints[i] += inc;
                    break;
                case KeyEvent.VK_LEFT:
                    xPoints[i] -= inc;
                    break;
                case KeyEvent.VK_RIGHT:
                    xPoints[i] += inc;
                    break;
            }
        }
        testPolygon.xpoints = xPoints;
        testPolygon.ypoints = yPoints;

        howManyPointsOutside(testPolygon);
    }

    private void shiftTestPolygonInt(int direction) {

        int[] xPoints = testPolygon.xpoints;
        int[] yPoints = testPolygon.ypoints;

        int inc = 1;
        for (int i = 0; i < testPolygon.npoints; i++) {

            switch (direction) {
                case 0:
                    yPoints[i] -= inc;
                    break;
                case 1:
//                    System.out.println("dir: 1");
                    yPoints[i] += inc;
                    break;
                case 2:
                    xPoints[i] -= inc;
                    break;
                case 3:
                    xPoints[i] += inc;
                    break;
            }
        }
        testPolygon.xpoints = xPoints;
        testPolygon.ypoints = yPoints;
    }

    private MyVector getCenterPolygon() {

        int[] xPoints = testPolygon.xpoints;
        int[] yPoints = testPolygon.ypoints;

        MyVector lUpper = new MyVector(xPoints[0], yPoints[0], "");
        MyVector rLower = new MyVector(xPoints[2], yPoints[2], "");

        MyVector rUpper = new MyVector(xPoints[1], yPoints[1], "");
        MyVector lLower = new MyVector(xPoints[3], yPoints[3], "");

        MyVector v1 = MyVector.getVector(lUpper, rLower);
        MyVector v2 = MyVector.getVector(rUpper, lUpper);

        return getIntersectionPoint(lUpper, rLower, rUpper, lLower);
    }

    private void setWhipersVisibility(boolean b) {
        whiperAB.setVisible(b);
        whiperAC.setVisible(b);
        whiperBA.setVisible(b);
        whiperBC.setVisible(b);
        whiperCA.setVisible(b);
        whiperCB.setVisible(b);
    }

    private void printHandles() {
        handleA.print("A");
        handleB.print("B");
        handleC.print("C");
    }

    private void moveIrisPic(int vKup, int inc) {

        switch (vKup) {
            case KeyEvent.VK_UP:
                irisPicShiftY -= inc;
                break;
            case KeyEvent.VK_DOWN:
                irisPicShiftY += inc;
                break;
            case KeyEvent.VK_LEFT:
                irisPicShiftX -= inc;
                break;
            case KeyEvent.VK_RIGHT:
                irisPicShiftX += inc;
                break;
        }
        System.out.println("psx: " + irisPicShiftX + " psy: " + irisPicShiftY + " ps: " + irisPicSize);
    }

    public static BufferedImage loadImage(String filePath) {

        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void moveHandles(int theCase, int inc) {

        switch (theCase) {
            case KeyEvent.VK_UP:
                handleA.y -= inc;
                handleB.y -= inc;
                handleC.y -= inc;
                break;
            case KeyEvent.VK_DOWN:
                handleA.y += inc;
                handleB.y += inc;
                handleC.y += inc;
                break;
            case KeyEvent.VK_LEFT:
                handleA.x -= inc;
                handleB.x -= inc;
                handleC.x -= inc;
                break;
            case KeyEvent.VK_RIGHT:
                handleA.x += inc;
                handleB.x += inc;
                handleC.x += inc;
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

    private void deselectAllWhipers() {
        whiperAC.selected = false;
        whiperBA.selected = false;
        whiperCB.selected = false;
    }

    private MyVector calculateTemporaryWhiper(MouseEvent e, MyVector whiper, MyVector handle, MyDoublePolygon curve) {

        MyVector tmp = whiper.subtract(handle);

        tmp.x = e.getX() - sceneShift.x;
        tmp.y = e.getY() - sceneShift.y;

        tmp = MyVector.getVector(tmp, handle).makeItThatLong(1000).add(handle);

        return calculateCrossPointOnWhiperCurve(handle, tmp, curve);
    }

    private MyVector calculateCrossPointOnWhiperCurve(MyVector from, MyVector to, MyDoublePolygon curve) {

        List<Point2D.Double> points = curve.getPoints();

        for (int i = 0; i < points.size() - 1; i++) {

            MyVector p1 = new MyVector(points.get(i));
            MyVector p2 = new MyVector(points.get(i + 1));
            MyVector ip = getIntersectionPoint(from, to, p1, p2);
            if (ip != null) {
                return ip;
            }
        }
        return null;
    }

    private MyVector getIntersectionPoint(MyVector vec1Start, MyVector vec1End, MyVector vec2Start, MyVector vec2End) {

        // Create Line2D objects for the input vectors
        Line2D line1 = new Line2D.Double(vec1Start.x, vec1Start.y, vec1End.x, vec1End.y);
        Line2D line2 = new Line2D.Double(vec2Start.getX(), vec2Start.getY(), vec2End.getX(), vec2End.getY());

        // Check if the lines intersect
        if (!line1.intersectsLine(line2)) {
            return null; // No intersection
        }

        // Extract coordinates for each point
        double x1 = vec1Start.x, y1 = vec1Start.y;
        double x2 = vec1End.x, y2 = vec1End.y;
        double x3 = vec2Start.getX(), y3 = vec2Start.getY();
        double x4 = vec2End.getX(), y4 = vec2End.getY();

        // Calculate determinant to check if lines are parallel
        double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (det == 0) {
            return null; // Parallel lines, no intersection
        }

        // Calculate intersection point coordinates
        double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det;

        // Return the intersection point as a new MyVector
        return new MyVector(px, py, "");
    }

    public static double calculateInnerRadiusTriangle(MyVector A, MyVector B, MyVector C) {

        // Calculate side lengths
        double a = MyVector.getLength(B, C);
        double b = MyVector.getLength(C, A);
        double c = MyVector.getLength(A, B);

        // Calculate the perimeter
        double perimeter = a + b + c;

        // Calculate the area using Heron's formula
        double s = perimeter / 2.0; // Semi-perimeter
        double area = Math.sqrt(s * (s - a) * (s - b) * (s - c));

        // Calculate the radius of the inscribed circle
        return 2.0 * area / perimeter;
    }

    private void doCalculations() {

        extendAB = calculateExtendedPoint(handleA, handleC, handleB);
        extendAB = extendAB.subtract(handleB).multiply(whiperFactor).add(handleB);

        /// calculate CB first
        extendCB = calculateExtendedPoint(handleC, handleA, handleB);

        /// get a vector from B to CB. Alternatively one could have used B to AB. "Without loss of generality (WLOG)"
        MyVector tmp = MyVector.getVector(extendCB, handleB);

        /// measure lenth before scaling
        double beforeLen = tmp.getLength();

        tmp = tmp.multiply(whiperFactor);

        /// measure lenth before scaling. If whiperFactor = 1 thei must be identical
        double afterLen = tmp.getLength();

        double diffLen = beforeLen - afterLen;

        /// now add the scaled vector to B
        extendCB = tmp.add(handleB);

        /// now adjust all other length
        extendCA = calculateExtendedPoint(handleC, handleB, handleA);
        extendCA = adjustLength(diffLen, extendCA, handleA);
        extendCA.setName("CA");

        extendBA = calculateExtendedPoint(handleB, handleC, handleA);
        extendBA = adjustLength(diffLen, extendBA, handleA);
        extendBA.setName("BA");

        extendBC = calculateExtendedPoint(handleB, handleA, handleC);
        extendBC = adjustLength(diffLen, extendBC, handleC);
        extendBC.setName("BC");

        extendAC = calculateExtendedPoint(handleA, handleB, handleC);
        extendAC = adjustLength(diffLen, extendAC, handleC);
        extendAC.setName("AC");

        /// calculate the middpoints between the extends
        midCA_BA = calculateMidpoint(extendCA, extendBA);
        midBC_AC = calculateMidpoint(extendAC, extendBC);
        midCB_AB = calculateMidpoint(extendCB, extendAB);

        /// add perpendicular vetors to the midd vector to calculat the center of the circle
        MyVector vecCA_BA = MyVector.getVector(extendBA, extendCA);
        MyVector recCA_BA = vecCA_BA.flip();
        recCA_BA.x = -recCA_BA.x;
        recCA_BA = recCA_BA.makeItThatLong(400);
        towCA_BA = midCA_BA.add(recCA_BA);

        MyVector vecBC_AC = MyVector.getVector(extendBC, extendAC);
        MyVector recBC_AC = vecBC_AC.flip();
        recBC_AC.x = -recBC_AC.x;
        recBC_AC.makeItThatLong(400);
        towBC_AC = midBC_AC.subtract(recBC_AC);

        MyVector vecCB_AB = MyVector.getVector(extendCB, extendAB);
        MyVector recCB_AB = vecCB_AB.flip();
        recCB_AB.x = -recCB_AB.x;
        recCB_AB.makeItThatLong(400);
        towCB_AB = midCB_AB.add(recCB_AB);

        centerCircle = getIntersectionPointGPT(midCB_AB, towCB_AB, midBC_AC, towBC_AC);

        if (centerCircle != null) {
            radiusCircle = MyVector.getVector(centerCircle, extendAB).getLength();
        }
    }

    private MyVector adjustLength(double deltaLength, MyVector extend, MyVector handle) {

        MyVector tmp = MyVector.getVector(extend, handle);
        double newLen = Math.abs(tmp.getLength() - deltaLength);
        tmp.makeItThatLong(newLen);
        return handle.add(tmp);
    }

    private void calculateWhipers() {

        whiperAC = MyVector.getVector(extendAC, handleA).add(handleA);
        whiperBA = MyVector.getVector(extendBA, handleB).add(handleB);
        whiperCB = MyVector.getVector(extendCB, handleC).add(handleC);

        whiperCA = MyVector.getVector(extendCA, handleA).add(handleA);
        whiperBC = MyVector.getVector(extendBC, handleB).add(handleB);
        whiperAB = MyVector.getVector(extendAB, handleB).add(handleB);

        createWhiperCurve();
    }

    private void createWhiperCurve() {

        bigWhiperCurve1 = new MyDoublePolygon();
        bigWhiperCurve2 = new MyDoublePolygon();
        bigWhiperCurve3 = new MyDoublePolygon();

        smallWhiperCurve1 = new MyDoublePolygon();
        smallWhiperCurve2 = new MyDoublePolygon();
        smallWhiperCurve3 = new MyDoublePolygon();

        createBigWhiperCurveSegment(bigWhiperCurve1, extendCB, handleC, handleB, handleA);

        createSmallWhiperCurveSegment(smallWhiperCurve1, handleB, extendAB, extendCB);

        createBigWhiperCurveSegment(bigWhiperCurve2, extendBA, handleB, handleA, handleC);

        createSmallWhiperCurveSegment(smallWhiperCurve2, handleA, extendCA, extendBA);

        createBigWhiperCurveSegment(bigWhiperCurve3, extendAC, handleA, handleC, handleB);

        createSmallWhiperCurveSegment(smallWhiperCurve3, handleC, extendBC, extendAC);

        Point2D.Double extendX = getExtendsWhiperCurveX();
        double distX = (extendX.y - extendX.x);
        Point2D.Double extendY = getExtendsWhiperCurveY();
        double distY = (extendY.y - extendY.x);

        boundingBoxWC = new Rectangle((int) extendX.x, (int) extendY.x, (int) distX, (int) distY);

        getCenter(boundingBoxWC);
        rotateRectangle(boundingBoxWC, 0);
    }

    private void createSmallWhiperCurveSegment(MyDoublePolygon whiperCurve, MyVector v1, MyVector v2, MyVector v3) {

        MyVector vec12 = MyVector.getVector(v1, v2);
        MyVector vec13 = MyVector.getVector(v1, v3);
        double angleVec = MyVector.angleBetweenHandles(vec12, vec13);
        int angleSteps = (int) Math.toDegrees(angleVec);
        double angleInc = angleVec / angleSteps;

        double rotationAngle = 0.0;
        for (int i = 0; i <= angleSteps; i++) {

            MyVector pointOnCurve = MyVector.getVector(v2, v1).rotate(rotationAngle).add(v1);
            rotationAngle += angleInc;

            whiperCurve.addPoint(pointOnCurve.x, pointOnCurve.y);
        }
    }

    private void createBigWhiperCurveSegment(MyDoublePolygon whiperCurve, MyVector whisker, MyVector v1, MyVector v2, MyVector v3) {

        MyVector vecCB = MyVector.getVector(v1, v2);
        MyVector vecCA = MyVector.getVector(v1, v3);
        double angleCB_CA = MyVector.angleBetweenHandles(vecCB, vecCA);
        int angleSteps = (int) Math.toDegrees(angleCB_CA);
        double angleInc = angleCB_CA / angleSteps;

        double rotationAngle = 0.0;
        for (int i = 0; i <= angleSteps; i++) {

            MyVector pointOnCurve = MyVector.getVector(whisker, v1).rotate(rotationAngle).add(v1);
            rotationAngle += angleInc;

            whiperCurve.addPoint(pointOnCurve.x, pointOnCurve.y);
        }
    }

    private MyVector calculateExtendedPoint(MyVector one, MyVector two, MyVector three) {

        double vector12_x = one.getX() - two.getX();
        double vector12_y = one.getY() - two.getY();
        double length12 = Math.sqrt(Math.pow(vector12_x, 2) + Math.pow(vector12_y, 2));

        double vector13_x = (one.getX() - three.getX());
        double vector13_y = (one.getY() - three.getY());
        double length13 = Math.sqrt(Math.pow(vector13_x, 2) + Math.pow(vector13_y, 2));

        double vector13Scaled_x = vector13_x / length13;
        double vector13Scaled_y = vector13_y / length13;

        vector13Scaled_x *= length12;
        vector13Scaled_y *= length12;

        String name = one.getName() + three.getName();
        return new MyVector(three.getX() - vector13Scaled_x, three.getY() - vector13Scaled_y, name);
    }

    private MyVector calculateMidpoint(MyVector point1, MyVector point2) {

        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new MyVector(midX, midY, "");
    }

    private MyVector getIntersectionPointGPT(MyVector handle1Start, MyVector handle1End, MyVector handle2Start, MyVector handle2End) {

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

        MyVector tmp = new MyVector(px, py, "center");
        tmp.setSize(1);
        return tmp;
    }

    /// painting section ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));

        if (drawHelp) {
            Helper.drawHelpPage(g2d);
            return;
        }

        clearBackground(g2d);

        AffineTransform shiftTransform = AffineTransform.getTranslateInstance(sceneShift.x + dragShift.x, sceneShift.y + dragShift.y);
        g2d.setTransform(shiftTransform);

        if (drawIrisPicture) {
            drawIrisPicture(g2d);
        }

        if (drawLines) {
            drawLines(g2d);
        }

        if (drawCircle) {
            drawCircle(g2d);
        }

        if (drawIris) {
            drawIris(g2d);
        }

        if (drawWhiskers) {
            drawExtendWhiskers(g2d);
        }

        if (drawTriangle) {
            drawTriangle(g2d);
        }

        if (drawWhiperStuff) {
            drawWiperCurves(g2d);
            drawWipers(g2d);
        }

        drawBoundingBoyWC(g2d);

        for (MyVector vector : boundingBoxCircle) {
            vector.fill(g2d, false);
        }
    }

    private void drawBoundingBoyWC(Graphics2D g2d) {

        g2d.setColor(Color.RED);

        AffineTransform oldTransform = g2d.getTransform();
        AffineTransform newTransform = new AffineTransform();

        double shiftX = boundingBoxWC.getCenterX();
        double shiftY = boundingBoxWC.getCenterY();

        MyVector centerBoundingBox = new MyVector(shiftX, shiftY, "cbb");
        centerBoundingBox.fill(g2d, false);

        g2d.setColor(Color.MAGENTA);
        centerCircle.fill(g2d, false);

//        System.out.println("angle: " + Math.toDegrees(rotationAngle) + " shiftX: " + shiftX + " shiftY: " + shiftY + " bw: " + boundingBoxWC.width + " bw: " + boundingBoxWC.height);

//        newTransform.translate(-shiftX, -shiftY);
//        newTransform.rotate(rotationAngle);
//        newTransform.translate(+shiftX, +shiftY);
//        g2d.setTransform(newTransform);

        //g2d.draw(boundingBoxWC);

        g2d.setColor(Color.green);
        getCenterPolygon().fill(g2d, true);
        g2d.draw(testPolygon);

        g2d.setTransform(oldTransform);
    }

    private void handleSpaceBar() {
        getCenterPolygon();
        rotationAngle += Math.toRadians(5);
        rotateRectangle(boundingBoxWC, rotationAngle);
        shakePolygon(testPolygon);
    }

    private void shakePolygon(Polygon polygon) {

        createHugeCurve();

        Polygon testPolygonOptimal = copyPolygons(testPolygon);

        int start = howManyPointsOutside(testPolygon);
        int optimal = Integer.MAX_VALUE;
        Random random = new Random();

        int count = 0;
        while (optimal > 20) {

//        for (int i = 0; i < 200000; i++) {

            count++;
            if (count % 1000000 == 0) {
                break;
            }
            shiftTestPolygonInt(random.nextInt(4));

            optimal = howManyPointsOutside(testPolygon);

            if (optimal < start) {
                testPolygonOptimal = copyPolygons(testPolygon);
                start = optimal;
            }
        }
        testPolygon = copyPolygons(testPolygonOptimal);

        int np = howManyPointsOutside(testPolygon);

        if (np < 25) {
            boundingBoxCircle.add(getCenterPolygon());
        }

        //MyVector.printArrayList(boundingBoxCircle);

        if (boundingBoxCircle.size() > 2) {
            double radius = -1;
            //MyVector.circleFromPoints(boundingBoxCircle.get(0), boundingBoxCircle.get(0), boundingBoxCircle.get(0), radius);
            System.out.println("radius: " + radius);
        }

        System.out.println("sp: " + (int) (Math.toDegrees(rotationAngle)) + " outside: " + np + " iter: " + count);
    }

    private void createHugeCurve() {

        hugeCurve = new MyDoublePolygon();

        hugeCurve.addCurve(bigWhiperCurve1);
        hugeCurve.addCurve(bigWhiperCurve2);
        hugeCurve.addCurve(bigWhiperCurve3);
        hugeCurve.addCurve(smallWhiperCurve1);
        hugeCurve.addCurve(smallWhiperCurve2);
        hugeCurve.addCurve(smallWhiperCurve3);
    }

    private Polygon copyPolygons(Polygon from) {

        Polygon to = new Polygon();
        for (int i = 0; i < from.npoints; i++) {
            to.addPoint(from.xpoints[i], from.ypoints[i]);
        }
        return to;
    }

    private void printPolygon(Polygon p, String text) {

        System.out.println("printPolygon: " + text);
        for (int i = 0; i < p.npoints; i++) {
            System.out.println("x: " + p.xpoints[i] + " y: " + p.ypoints[i]);

        }
    }

    private int howManyPointsOutside(Polygon outer) {

        if (hugeCurve == null) {
            return -1;
        }
        int count = 0;
        for (int i = 0; i < hugeCurve.getNumPoints(); i += 1) {

            if (!outer.contains(hugeCurve.getPoints().get(i).x, hugeCurve.getPoints().get(i).y)) {
                count++;
            }
        }
        return count;
    }

    public void screenshotCapture() {

        // Create a BufferedImage to hold the screenshot
        BufferedImage screenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        // Create a graphics context from the BufferedImage
        Graphics2D g2d = screenshot.createGraphics();

        this.paint(g2d);

        g2d.dispose();

        // Save the screenshot to a file
        File outputFile = new File("/Users/malvers/Desktop/IRIS/screenshot_" + ((int) Math.toDegrees(rotationAngle)) + ".png");
        try {
            ImageIO.write(screenshot, "png", outputFile);
            System.out.println("Screenshot saved as: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rotateRectangle(Rectangle rectangle, double angle) {

        double centerX = rectangle.getCenterX();
        double centerY = rectangle.getCenterY();

        MyVector center = new MyVector(centerX, centerY, "");
        MyVector c1 = new MyVector(rectangle.x, rectangle.y, "");
        MyVector c2 = new MyVector(rectangle.x + rectangle.width, rectangle.y, "");
        MyVector c3 = new MyVector(rectangle.x, rectangle.y + rectangle.height, "");
        MyVector c4 = new MyVector(rectangle.x + rectangle.width, rectangle.y + rectangle.width, "");

        MyVector c1r = MyVector.getVector(c1, center).rotate(angle).add(center);
        MyVector c2r = MyVector.getVector(c2, center).rotate(angle).add(center);
        MyVector c4r = MyVector.getVector(c3, center).rotate(angle).add(center);
        MyVector c3r = MyVector.getVector(c4, center).rotate(angle).add(center);

        testPolygon = new Polygon();
        testPolygon.addPoint((int) c1r.x, (int) c1r.y);
        testPolygon.addPoint((int) c2r.x, (int) c2r.y);
        testPolygon.addPoint((int) c3r.x, (int) c3r.y);
        testPolygon.addPoint((int) c4r.x, (int) c4r.y);

        howManyPointsOutside(testPolygon);
    }

    public static Rectangle constructRectangle(MyVector p1, MyVector p2, MyVector p3, MyVector p4) {
        // Find extreme coordinates
        double minX = Math.min(Math.min(Math.min(p1.x, p2.x), p3.x), p4.x);
        double maxX = Math.max(Math.max(Math.max(p1.x, p2.x), p3.x), p4.x);
        double minY = Math.min(Math.min(Math.min(p1.y, p2.y), p3.y), p4.y);
        double maxY = Math.max(Math.max(Math.max(p1.y, p2.y), p3.y), p4.y);

        // Calculate width and height
        double width = maxX - minX;
        double height = maxY - minY;

        // Create and return the rectangle
        return new Rectangle((int) minX, (int) minY, (int) width, (int) height);
    }

    private Point2D.Double getCenter(Rectangle rect) {

        double centerX = rect.getX() + rect.getWidth() / 2;
        double centerY = rect.getY() + rect.getHeight() / 2;
        return new Point2D.Double(centerX, centerY);
    }

    private Point2D.Double getExtendsWhiperCurveX() {

        double min = Double.MAX_VALUE;
        double max = 0;

        List<Point2D.Double> points = bigWhiperCurve1.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        points = bigWhiperCurve2.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        points = bigWhiperCurve3.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        points = smallWhiperCurve1.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        points = smallWhiperCurve2.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        points = smallWhiperCurve3.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).x < min) {
                min = points.get(i).x;
            }
            if (points.get(i).x > max) {
                max = points.get(i).x;
            }
        }
        return new Point2D.Double(min, max);
    }

    private Point2D.Double getExtendsWhiperCurveY() {

        double min = Double.MAX_VALUE;
        double max = 0;

        List<Point2D.Double> points = bigWhiperCurve1.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        points = bigWhiperCurve2.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        points = bigWhiperCurve3.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        points = smallWhiperCurve1.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        points = smallWhiperCurve2.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        points = smallWhiperCurve3.getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).y < min) {
                min = points.get(i).y;
            }
            if (points.get(i).y > max) {
                max = points.get(i).y;
            }
        }
        return new Point2D.Double(min, max);
    }

    private void drawIrisPicture(Graphics2D g2d) {

        zoomedSize = (int) (irisPicSize * irisZoom);
        int delta = (int) (((zoomedSize) / 2.0));
        g2d.drawImage((Image) irisPicture, (int) (centerCircle.x - delta), (int) (centerCircle.y - delta), zoomedSize, zoomedSize, null);
    }

    private void clearBackground(Graphics2D g2d) {
        if (blackMode) {
            g2d.setColor(Color.BLACK);

        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawWiperCurves(Graphics2D g2d) {

        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(1));
        //drawHandleConnector(g2d, handleC, whiperC_CB);

        drawOneSegmentWhiperCurve(smallWhiperCurve1, g2d);
        drawOneSegmentWhiperCurve(smallWhiperCurve2, g2d);
        drawOneSegmentWhiperCurve(smallWhiperCurve3, g2d);

        drawOneSegmentWhiperCurve(bigWhiperCurve1, g2d);
        drawOneSegmentWhiperCurve(bigWhiperCurve2, g2d);
        drawOneSegmentWhiperCurve(bigWhiperCurve3, g2d);
    }

    private void drawOneSegmentWhiperCurve(MyDoublePolygon whiperCurve, Graphics2D g2d) {

        Path2D.Double path = new Path2D.Double();
        List<Point2D.Double> points = whiperCurve.getPoints();

        path.moveTo(points.get(0).getX(), points.get(0).getY());

        for (int i = 1; i < points.size(); i++) {
            /// System.out.println("x: " + points.get(i).getX() + " y: " + points.get(i).getY());
            path.lineTo(points.get(i).getX(), points.get(i).getY());
        }

        g2d.draw(path);
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

    private void drawExtendWhiskers(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(color3);
        extendBA.fill(g2d, drawAnnotation);
        extendCA.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, extendBA.x, extendBA.y));
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, extendCA.x, extendCA.y));
        g2d.setColor(color2);
        extendBC.fill(g2d, drawAnnotation);
        extendAC.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, extendBC.x, extendBC.y));
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, extendAC.x, extendAC.y));
        g2d.setColor(color1);
        extendAB.fill(g2d, drawAnnotation);
        extendCB.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, extendAB.x, extendAB.y));
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, extendCB.x, extendCB.y));
    }

    private void drawLines(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.lightGray);
        drawHandleConnector(g2d, extendCA, extendBA);
        drawHandleConnector(g2d, extendBC, extendAC);
        drawHandleConnector(g2d, extendCB, extendAB);

        midCA_BA.fill(g2d, drawAnnotation);
        midBC_AC.fill(g2d, drawAnnotation);
        midCB_AB.fill(g2d, drawAnnotation);

        g2d.setColor(Color.LIGHT_GRAY);
        drawHandleConnector(g2d, midCA_BA, towCA_BA);
        drawHandleConnector(g2d, midBC_AC, towBC_AC);
        drawHandleConnector(g2d, midCB_AB, towCB_AB);
    }

    private void drawIris(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.MAGENTA.darker().darker());
        double ir = calculateInnerRadiusTriangle(handleA, handleB, handleC);
        irisCircle = new Ellipse2D.Double(centerCircle.x - ir, centerCircle.y - ir, 2 * ir, 2 * ir);
        g2d.draw(irisCircle);

//        g2d.setColor(Color.ORANGE);
//        //irisCircle = new Ellipse2D.Double(centerCircle.x - ir, centerCircle.y - ir, 2 * ir, 2 * ir);
//        if (irisCircleStore != null) {
//            g2d.draw(irisCircleStore);
//        }
    }

    private void drawCircle(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.MAGENTA.darker().darker());
        centerCircle.fill(g2d, drawAnnotation);
        g2d.draw(new Ellipse2D.Double(centerCircle.x - radiusCircle, centerCircle.y - radiusCircle, 2 * radiusCircle, 2 * radiusCircle));
    }

    private void drawHandleConnector(Graphics2D g2d, MyVector h1, MyVector h2) {

        g2d.draw(new Line2D.Double(h1.x, h1.y, h2.x, h2.y));
    }

    private void drawWipers(Graphics2D g2d) {

        int r = Color.ORANGE.getRed();
        int g = Color.ORANGE.getGreen();
        int b = Color.ORANGE.getBlue();

        g2d.setStroke(new BasicStroke(2));

        whiperAC.fill(g2d, false);
        whiperBA.fill(g2d, false);
        whiperCB.fill(g2d, false);
        whiperCA.fill(g2d, false);
        whiperBC.fill(g2d, false);
        whiperAB.fill(g2d, false);

        if (!whiperAC.getVisible()) {
            return;
        }
        setTranslucent(g2d, whiperAC, r, g, b);
        drawHandleConnector(g2d, handleA, whiperAC);
        setTranslucent(g2d, whiperBA, r, g, b);
        drawHandleConnector(g2d, handleB, whiperBA);
        setTranslucent(g2d, whiperCB, r, g, b);
        drawHandleConnector(g2d, handleC, whiperCB);
        setTranslucent(g2d, whiperCA, r, g, b);
        drawHandleConnector(g2d, handleA, whiperCA);
        setTranslucent(g2d, whiperBC, r, g, b);
        drawHandleConnector(g2d, handleC, whiperBC);
        setTranslucent(g2d, whiperAB, r, g, b);
        drawHandleConnector(g2d, handleB, whiperAB);
    }

    private void setTranslucent(Graphics2D g2d, MyVector whiper, int r, int g, int b) {
        if (whiper.selected) {
            g2d.setColor(Color.ORANGE);
        } else {
            g2d.setColor(new Color(r, g, b, 66));
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame f = new JFrame();
            f.setSize(760, 760);
            f.add(new IRISVisualization(f));
            f.setTitle(defaulTitle);
            f.setVisible(true);
        });
    }
}

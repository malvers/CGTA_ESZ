import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IRISVisualization extends JButton implements IObjectiveFunction, Runnable {

    /*

      BUGS:
      - last curve segment
      - flipped triangle

      IDEAS:
      - rotate box around whiper curve

      IMPROVEMENTS:
      -

    */

    private final static String defaultTitle = "Conway's IRIS - Press H for Help";
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
    private MyVector handleA;
    private MyVector handleB;
    private MyVector handleC;
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
    private boolean drawBoundingBox = true;
    private boolean runIt = false;
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
    private MyDoublePolygon boundingBox;
    MyDoublePolygon boundingBoxTest = null;
    private MyDoublePolygon hugeCurve;
    private ArrayList<MyVector> boundingBoxInnerRotationPath;
    private ArrayList<MyDoublePolygon> debugList;
    private List<MyVector> intersectionPoints;
    private ArrayList<MyDoublePolygon> qualityPolygons;

    /// optimization stuff

    private double[] fitness;
    private IRISVisualization fitFun;
    private CMAEvolutionStrategy cmaes;
    private long runningDelay = 0;

    /// constructor ////////////////////////////////////////////////////////////////////////////////////////////////////

    public IRISVisualization(JFrame f) {

        frame = f;

        mouseInit();
        keyInit();

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
        boundingBoxInnerRotationPath = new ArrayList<>();
        debugList = new ArrayList<>();
        qualityPolygons = new ArrayList<>();

        initCMAES();
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
            os.writeBoolean(drawBoundingBox);

            os.writeObject(handleA);
            os.writeObject(handleB);
            os.writeObject(handleC);

            os.writeObject(hw);

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

            irisPicShiftX = os.readInt();
            irisPicShiftY = os.readInt();
            irisPicSize = os.readInt();

            drawWhiperStuff = os.readBoolean();
            debugMode = os.readBoolean();
            drawBoundingBox = os.readBoolean();

            handleA = (MyVector) os.readObject();
            handleB = (MyVector) os.readObject();
            handleC = (MyVector) os.readObject();

            hw = (HelperWindow) os.readObject();
            hw.setVisible(true);

            os.close();
            f.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /// helper functions /////////////////////////////////////////////////////////////////////////////////////////////////

    HelperWindow hw = null;//new HelperWindow();

    public void println() {
        if (hw == null) {
            System.out.println();
        } else {
            hw.println("");
        }
    }

    public void println(String s) {
        if (hw == null) {
            System.out.println(s);
        } else {
            hw.println(s);
        }
    }

    /// mouse handling /////////////////////////////////////////////////////////////////////////////////////////////////

    private void mouseInit() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

                println("ex q: " + experimentalQuality(boundingBox));

                Point2D.Double pd = new Point2D.Double(e.getX(), e.getY());

                if (drawHelp) {
                    return;
                }

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

                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleA, whiperAC) < delta) {
                    whiperAC.selected = true;
                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleB, whiperBA) < delta) {
                    whiperBA.selected = true;
                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleC, whiperCB) < delta) {
                    whiperCB.selected = true;

                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleA, whiperCA) < delta) {
                    whiperCA.selected = true;
                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleC, whiperBC) < delta) {
                    whiperBC.selected = true;
                } else if (MyVector.distanceToPointFromLine(pd, sceneShift, handleB, whiperAB) < delta) {
                    whiperAB.selected = true;
                }

                initBoundingBoxPolygonTest();
                createHugeCurve();
//                println("\nq:  " + qualityFunction());
                println("points outside: " + numberPointsOutside());
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if (drawHelp) {
                    return;
                }

                /// TODO: for debugging the bounding box only
                if (e.isShiftDown()) {
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
                } else {
//                    qualityFunction();
                    numberPointsOutside();
                }
            }
        });

        handleMouseMotion();
    }

    private void handleMouseMotion() {

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (drawHelp) {
                    return;
                }
                if (e.isShiftDown()) {
                    moveSceneHandlesWhipers(e);
                    doCalculations();
                    createWhiperCurves();
                } else {
                    moveBoundingBox(e);
                }
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

    private void moveSceneHandlesWhipers(MouseEvent e) {

        if (irisCircleStore != null && irisCircle != null) {
            irisZoom = irisCircle.getWidth() / irisCircleStore.getWidth();
        }
        if (handleA.selected) {
            boundingBoxInnerRotationPath = new ArrayList<>();
            handleA.x = e.getX() - sceneShift.x;
            handleA.y = e.getY() - sceneShift.y;
            setWhipersVisibility(false);
        } else if (handleB.selected) {
            boundingBoxInnerRotationPath = new ArrayList<>();
            handleB.x = e.getX() - sceneShift.x;
            handleB.y = e.getY() - sceneShift.y;
            setWhipersVisibility(false);
        } else if (handleC.selected) {
            boundingBoxInnerRotationPath = new ArrayList<>();
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
        } else if (e.isShiftDown()) {

        } else {
            dragShift.x = -(onMousePressed.x - e.getX());
            dragShift.y = -(onMousePressed.y - e.getY());
        }
    }

    private void moveBoundingBox(MouseEvent e) {

        double x = -(onMousePressed.x - e.getX());
        double y = -(onMousePressed.y - e.getY());
        setPositionBoundingBox(x, y);
        println("Moving exp q: " + experimentalQuality(boundingBox));
    }

    /// key handling ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void keyInit() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

//                println("key: " + e.getKeyCode());
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_0:
                        if (e.isShiftDown()) {
                            rotationAngle = 0.0;
                            whiperFactor = 1.0;
                            sceneShift.x = 0;
                            sceneShift.y = 0;
                            doCalculations();
                            calculateWhipers();
                        } else {
                            runningDelay = 0;
                        }
                        break;
                    case KeyEvent.VK_1:
                        runningDelay = 100;
                        break;
                    case KeyEvent.VK_2:
                        runningDelay = 200;
                        break;
                    case KeyEvent.VK_3:
                        runningDelay = 300;
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        if (e.isMetaDown()) {
                            moveIrisPic(e.getKeyCode(), 1);
                        } else {
                            double inc = 0.1;
                            if (e.isShiftDown()) {
                                inc = 1.0;
                            } else if (e.isAltDown()) {
                                inc = 5.0;
                            }
                            shiftBoundingBoxPolygon(e.getKeyCode(), inc);
                        }
//                        qualityFunction();
                        break;
                    case KeyEvent.VK_SPACE:
                        handleSpaceBar(e);
                        break;
                    case KeyEvent.VK_A:
                        drawAnnotation = !drawAnnotation;
                        break;
                    case KeyEvent.VK_B:
                        if (e.isMetaDown()) {
                            blackMode = !blackMode;
                            adjustColorBlackMode();
                        } else {
                            drawBoundingBox = !drawBoundingBox;
                        }
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
                        drawIrisPicture = !drawIrisPicture;
                        if (drawIrisPicture) {
                            blackMode = true;
                        }
                        break;
                    case KeyEvent.VK_R:
                        rotateBoundingBox(e);
                        break;
                    case KeyEvent.VK_Q:
                        println("Q - exp q: " + experimentalQuality(boundingBox));
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
                    case KeyEvent.VK_X:
                        startOptimization();
                        break;
                    case KeyEvent.VK_Z:
                        if (e.isMetaDown()) {
                            irisZoom += 0.1;
                        } else {
                            irisZoom -= 0.1;
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

    private void shiftBoundingBoxDouble(int direction) {

        ArrayList<Point2D.Double> points = boundingBox.getPoints();

        double inc = 0.6;
        for (int i = 0; i < boundingBox.getNumPoints(); i++) {

            switch (direction) {
                case 0:
                    points.get(i).y -= inc;
                    break;
                case 1:
                    points.get(i).y += inc;
                    break;
                case 2:
                    points.get(i).x -= inc;
                    break;
                case 3:
                    points.get(i).x += inc;
                    break;
            }
        }
        boundingBox.setPoints(points);
    }

    private void shiftTestPolygonExperimental(double x, double y) {

        ArrayList<Point2D.Double> points = boundingBox.getPoints();

        for (int i = 0; i < boundingBox.getNumPoints(); i++) {

            points.get(i).x = x;
            points.get(i).y = y;
        }
        boundingBox.setPoints(points);
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

        /// measure length before scaling
        double beforeLen = tmp.getLength();

        tmp = tmp.multiply(whiperFactor);

        /// measure length before scaling. If whiperFactor = 1 they must be identical
        double afterLen = tmp.getLength();

        double diffLen = beforeLen - afterLen;

        /// now add the scaled vector to B
        extendCB = tmp.add(handleB);

        /// now adjust all others length
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
        Objects.requireNonNull(centerCircle).setSize(4);

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

        createWhiperCurves();
    }

    private void createWhiperCurves() {

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

        rotateRectangle(boundingBoxWC, 0);

        createHugeCurve();
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

    /// TODO: why three functions (also MyDoublePolygon.getIntersectionPoint)
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

    private MyVector getIntersectionPointGPT(MyVector v1Start, MyVector v1End, MyVector v2Start, MyVector v2End) {

        // Get the coordinates of the handles
        double x1 = v1Start.x, y1 = v1Start.y;
        double x2 = v1End.x, y2 = v1End.y;
        double x3 = v2Start.x, y3 = v2Start.y;
        double x4 = v2End.x, y4 = v2End.y;

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

    private ArrayList<Point2D.Double> createTestPolygon() {
        ArrayList<Point2D.Double> testPolygon = new ArrayList<>();
        testPolygon.add(new Point2D.Double());
        testPolygon.add(new Point2D.Double());
        testPolygon.add(new Point2D.Double());
        testPolygon.add(new Point2D.Double());
        return testPolygon;
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

        drawDebugList(g2d);

        /// under development
        if (drawBoundingBox) {
            drawBoundingBoxWhiperCurve(g2d);
        }

        /// under development
        drawBoundingBoxRotationPath(g2d);

        /// under development
        g2d.setColor(Color.BLUE);
        for (MyDoublePolygon p : qualityPolygons) p.fill(g2d);

        if (intersectionPoints != null && drawBoundingBox) {
            g2d.setColor(Color.CYAN);
            for (MyVector v : intersectionPoints) {
                //v.setNameToPosition();
                v.setSize(6);
                v.fill(g2d, debugMode);
            }
        }

        if (drawWhiperStuff) {
            drawWiperCurves(g2d);
            drawWipers(g2d);
        }
    }

    private void drawBoundingBoxRotationPath(Graphics2D g2d) {

        if (boundingBoxInnerRotationPath.size() > 0) {

            g2d.setColor(Color.GREEN);
            Path2D.Double path = new Path2D.Double();
            double x = boundingBoxInnerRotationPath.get(0).x;
            double y = boundingBoxInnerRotationPath.get(0).y;
            path.moveTo(x, y);
            for (int i = 1; i < boundingBoxInnerRotationPath.size(); i++) {
                x = boundingBoxInnerRotationPath.get(i).x;
                y = boundingBoxInnerRotationPath.get(i).y;
                path.lineTo(x, y);
            }
            g2d.draw(path);
        }
//        for (MyVector vector : boundingBoxInnerRotationPath) {
//            vector.setSize(1);
//            vector.fill(g2d, false);
//        }
    }

    private void drawDebugList(Graphics2D g2d) {

        if (debugList.size() <= 0) {
            return;
        }
        Stroke stroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(1));
        if (debugMode) {
            g2d.setColor(new Color(128, 128, 128, 10));
            for (MyDoublePolygon polygon : debugList) {
                polygon.draw(g2d);
            }
        }
        g2d.setStroke(stroke);
    }

    private void printDebugList() {
        println("\n\nprint debug list - size: " + debugList.size());
        for (MyDoublePolygon polygon : debugList) {
            polygon.print("x23");
        }
    }

    private void createHugeCurve() {

        hugeCurve = new MyDoublePolygon();

        hugeCurve.addCurve(smallWhiperCurve1);
        hugeCurve.addCurve(bigWhiperCurve1);
        hugeCurve.addCurve(smallWhiperCurve2);
        hugeCurve.addCurve(bigWhiperCurve2);
        hugeCurve.addCurve(smallWhiperCurve3);
        hugeCurve.addCurve(bigWhiperCurve3);
    }

    private MyDoublePolygon copyPolygons(MyDoublePolygon from) {

        MyDoublePolygon to = new MyDoublePolygon();
        List<Point2D.Double> pointsFrom = from.getPoints();
        for (int i = 0; i < from.getNumPoints(); i++) {
            to.addPoint(pointsFrom.get(i).x, pointsFrom.get(i).y);
        }
        return to;
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
            println("Screenshot saved as: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rotateRectangle(Rectangle rectangle, double angle) {

        double centerX = rectangle.getCenterX();
        double centerY = rectangle.getCenterY();

        double d = 0.001;
        double d2 = d * 2.0;
        MyVector center = new MyVector(centerX, centerY, "");
        /// .width can be used everywhere since it is a square
        MyVector c1 = new MyVector(rectangle.x - d, rectangle.y - d, "");
        MyVector c2 = new MyVector(rectangle.x - d + rectangle.width + d2, rectangle.y - d, "");
        MyVector c3 = new MyVector(rectangle.x - d, rectangle.y - d + rectangle.width + d2, "");
        MyVector c4 = new MyVector(rectangle.x - d + rectangle.width + d2 - d, rectangle.y - d + rectangle.width + d2, "");

        MyVector c1r = MyVector.getVector(c1, center).rotate(angle).add(center);
        MyVector c2r = MyVector.getVector(c2, center).rotate(angle).add(center);
        MyVector c4r = MyVector.getVector(c3, center).rotate(angle).add(center);
        MyVector c3r = MyVector.getVector(c4, center).rotate(angle).add(center);

        boundingBox = new MyDoublePolygon();
        boundingBox.addPoint((int) c1r.x, (int) c1r.y);
        boundingBox.addPoint((int) c2r.x, (int) c2r.y);
        boundingBox.addPoint((int) c3r.x, (int) c3r.y);
        boundingBox.addPoint((int) c4r.x, (int) c4r.y);

        //howManyPointsOutside(boundingBoxPolygon);
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
        for (Point2D.Double point1 : points) {
            if (point1.x < min) {
                min = point1.x;
            }
            if (point1.x > max) {
                max = point1.x;
            }
        }
        points = bigWhiperCurve2.getPoints();
        for (Point2D.Double element : points) {
            if (element.x < min) {
                min = element.x;
            }
            if (element.x > max) {
                max = element.x;
            }
        }
        points = bigWhiperCurve3.getPoints();
        for (Point2D.Double item : points) {
            if (item.x < min) {
                min = item.x;
            }
            if (item.x > max) {
                max = item.x;
            }
        }
        points = smallWhiperCurve1.getPoints();
        for (Point2D.Double value : points) {
            if (value.x < min) {
                min = value.x;
            }
            if (value.x > max) {
                max = value.x;
            }
        }
        points = smallWhiperCurve2.getPoints();
        for (Point2D.Double aDouble : points) {
            if (aDouble.x < min) {
                min = aDouble.x;
            }
            if (aDouble.x > max) {
                max = aDouble.x;
            }
        }
        points = smallWhiperCurve3.getPoints();
        for (Point2D.Double point : points) {
            if (point.x < min) {
                min = point.x;
            }
            if (point.x > max) {
                max = point.x;
            }
        }
        return new Point2D.Double(min, max);
    }

    private Point2D.Double getExtendsWhiperCurveY() {

        double min = Double.MAX_VALUE;
        double max = 0;

        List<Point2D.Double> points = bigWhiperCurve1.getPoints();
        for (Point2D.Double point1 : points) {
            if (point1.y < min) {
                min = point1.y;
            }
            if (point1.y > max) {
                max = point1.y;
            }
        }
        points = bigWhiperCurve2.getPoints();
        for (Point2D.Double element : points) {
            if (element.y < min) {
                min = element.y;
            }
            if (element.y > max) {
                max = element.y;
            }
        }
        points = bigWhiperCurve3.getPoints();
        for (Point2D.Double item : points) {
            if (item.y < min) {
                min = item.y;
            }
            if (item.y > max) {
                max = item.y;
            }
        }
        points = smallWhiperCurve1.getPoints();
        for (Point2D.Double value : points) {
            if (value.y < min) {
                min = value.y;
            }
            if (value.y > max) {
                max = value.y;
            }
        }
        points = smallWhiperCurve2.getPoints();
        for (Point2D.Double aDouble : points) {
            if (aDouble.y < min) {
                min = aDouble.y;
            }
            if (aDouble.y > max) {
                max = aDouble.y;
            }
        }
        points = smallWhiperCurve3.getPoints();
        for (Point2D.Double point : points) {
            if (point.y < min) {
                min = point.y;
            }
            if (point.y > max) {
                max = point.y;
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

    private void drawBoundingBoxWhiperCurve(Graphics2D g2d) {

        double shiftX = boundingBox.getCenter().x;
        double shiftY = boundingBox.getCenter().y;

        //boundingBox.print("draw bb");
        //println("\nx: " + shiftX + "y: " + shiftY);

        MyVector centerBoundingBox = new MyVector(shiftX, shiftY, "center bounding box");
        centerBoundingBox.setSize(12);

        g2d.setColor(Color.RED);
        centerBoundingBox.fill(g2d, false);

        g2d.setColor(Color.MAGENTA);
        centerCircle.fill(g2d, false);

        g2d.setColor(Color.RED);
        ///g2d.draw(boundingBoxWC);

        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.GREEN);
        boundingBox.getCenter().fill(g2d, true);

        boundingBox.draw(g2d);
    }

    private void drawWiperCurves(Graphics2D g2d) {

        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(1));
        //drawHandleConnector(g2d, handleC, whiperC_CB);

        hugeCurve.draw(g2d);
    }

    private void drawOneSegmentWhiperCurve(MyDoublePolygon whiperCurve, Graphics2D g2d) {

        Path2D.Double path = new Path2D.Double();
        List<Point2D.Double> points = whiperCurve.getPoints();

        path.moveTo(points.get(0).getX(), points.get(0).getY());
        for (int i = 1; i < points.size(); i++) {
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

    /// optimizing section /////////////////////////////////////////////////////////////////////////////////////////////

    private void rotateBoundingBox(KeyEvent e) {

        double inc = 5.0;
        if (e.isShiftDown()) {
            rotationAngle -= Math.toRadians(inc);
        } else {
            rotationAngle += Math.toRadians(inc);
        }
        rotateRectangle(boundingBoxWC, rotationAngle);
        //qualityFunction();
    }

    private void handleSpaceBar(KeyEvent e) {
        ///bruteForce360(e);
        hw.println("Hello World!");
    }

    private void bruteForce360(KeyEvent e) {
        for (int i = 0; i <= 360; i++) {
            rotateBoundingBox(e);
            optimizePolygonPositionBruteForce(i);
        }
    }

    private void optimizePolygonPositionBruteForce(int count) {

        /// puzzle the single 6 curves together to one huge one

        double qStart = numberPointsOutside();
        double qOptimal = Double.MAX_VALUE;

        double searchSize = 16;

        int numPoints = 10000;

        MyVector center = boundingBox.getCenter();
        ArrayList<MyVector> scatter = MyVector.scatterPointsAround(center, searchSize, numPoints);

        debugList = new ArrayList<>();

        MyDoublePolygon testPolygonOptimal = new MyDoublePolygon();

        for (MyVector scatterPoint : scatter) {

            testPolygonOptimal = copyPolygons(boundingBox);

            double displaceX = center.x - scatterPoint.x;
            double displaceY = center.y - scatterPoint.y;

            ArrayList<Point2D.Double> testPolygon = createTestPolygon();

            for (int i = 0; i < 4; i++) {
                testPolygon.get(i).x = boundingBox.getPoint(i).x + displaceX;
                testPolygon.get(i).y = boundingBox.getPoint(i).y + displaceY;
            }

            MyDoublePolygon debug = new MyDoublePolygon("debug " + count);
            debug.setPoints(testPolygon);
            debugList.add(debug);

            qOptimal = numberPointsOutside(debug);

            if (qOptimal < qStart) {
                //println("BINGO optimal: " + qOptimal + " start: " + qStart);
                boundingBox.setPoints(testPolygon);
                testPolygonOptimal = copyPolygons(debug);
                qStart = qOptimal;
            }
        }
        boundingBox = copyPolygons(testPolygonOptimal);
        MyVector c = boundingBox.getCenter();
        c.setSize(4);
        boundingBoxInnerRotationPath.add(c);

        println(count + " optimizePolygonPosition: " + qOptimal);
    }

    /// implementations for CMAES //////////////////////////////////////////////////////////////////////////////////////

    private void shiftBoundingBoxPolygon(int vKup, double inc) {

        //boundingBox.print("shiftBoundingBoxPolygon");

        for (int i = 0; i < boundingBox.getNumPoints(); i++) {

            switch (vKup) {
                case KeyEvent.VK_UP:
                    boundingBox.getPoint(i).y -= inc;
                    break;
                case KeyEvent.VK_DOWN:
                    boundingBox.getPoint(i).y += inc;
                    break;
                case KeyEvent.VK_LEFT:
                    boundingBox.getPoint(i).x -= inc;
                    break;
                case KeyEvent.VK_RIGHT:
                    boundingBox.getPoint(i).x += inc;
                    break;
            }
        }
        println("experimental quality: " + experimentalQuality(boundingBox));
    }

    private double numberPointsOutside() {

        return numberPointsOutside(boundingBox);
    }

    private double numberPointsOutside(MyDoublePolygon outer) {

        if (hugeCurve == null) {
            return -1;
        }
        int count = 0;
        for (int i = 0; i < hugeCurve.getNumPoints(); i += 1) {

            if (outer.contains(hugeCurve.getPoints().get(i).x, hugeCurve.getPoints().get(i).y)) {
                count++;
            }
        }
        return hugeCurve.getNumPoints() - count;
    }

    private void initBoundingBoxPolygonTest() {

        boundingBoxTest = new MyDoublePolygon();
        boundingBoxTest.setPoints(boundingBox.getPoints());
    }

    private double closestDistanceToBoundingBox(Point2D.Double pIn) {

        Point2D.Double pNull = new Point2D.Double(0.0, 0.0);

        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < boundingBox.getNumPoints(); i++) {

            Point2D.Double p1 = boundingBox.getPoint(i);
            Point2D.Double p2 = boundingBox.getPoint((i + 1) % boundingBox.getNumPoints());

            MyVector v1 = new MyVector(p1);
            MyVector v2 = new MyVector(p2);

            double dist = MyVector.distanceToPointFromLine(pIn, pNull, v1, v2);
            if (dist < minDist) {
                minDist = dist;
            }
        }
//        println("min to bb: " + minDist);
        return minDist;
    }

    private void setPositionBoundingBox(double x, double y) {

        for (int i = 0; i < boundingBoxTest.getNumPoints(); i++) {
            boundingBox.getPoint(i).x = boundingBoxTest.getPoint(i).x + x;
            boundingBox.getPoint(i).y = boundingBoxTest.getPoint(i).y + y;
        }
        //println("points outside: " + numberPointsOutside());
    }

    private void initCMAES() {

        // we are the optimizer our self
        fitFun = this;

        // new a CMA-ES and set some initial values
        cmaes = new CMAEvolutionStrategy();
        cmaes.readProperties();
        cmaes.setDimension(2);
        cmaes.setInitialX(1);
        cmaes.setInitialStandardDeviation(0.1);
        cmaes.options.stopFitness = 1e-9;

        double[] par = new double[2];
        cmaes.setInitialX(par);

        // initialize cma and get fitness array to fill in later
        fitness = cmaes.init();                  // new double[cma.parameters.getPopulationSize()];
    }

    private double experimentalQuality(MyDoublePolygon theBox) {

        if (boundingBoxTest == null) {
            initBoundingBoxPolygonTest();
        }

        if (hugeCurve == null) {
            createHugeCurve();
        }

        ArrayList<Point2D.Double> points = hugeCurve.getPoints();

        double sumDist = 0.0;

        for (int i = 0; i < hugeCurve.getNumPoints(); i++) {

            Point2D.Double hcp = points.get(i);

            if (!theBox.contains(hcp.x, hcp.y)) {
                sumDist += closestDistanceToBoundingBox(hcp);
            }
        }
        return sumDist * sumDist;
    }

    @Override
    public double valueOf(double[] values) {

        for (int i = 0; i < 4; i++) {

            boundingBoxTest.getPoint(i).x += values[0];
            boundingBoxTest.getPoint(i).y += values[1];
        }
        return experimentalQuality(boundingBoxTest);
    }

    @Override
    public boolean isFeasible(double[] values) {
        return true;
    }

    private void startOptimization() {

        initCMAES();
        initBoundingBoxPolygonTest();

        new Thread(this).start();
        runIt = !runIt;
    }

    @Override
    public void run() {

        while (runIt) {

            try {
                Thread.sleep(runningDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (cmaes.stopConditions.getNumber() != 0) {
                runIt = false;
            }

            /// core iteration step
            double[][] population = cmaes.samplePopulation();  // get a new population of solutions


            for (int i = 0; i < population.length; ++i) {      // for each candidate solution i

                while (!fitFun.isFeasible(population[i])) {    // test whether solution is feasible,
                    population[i] = cmaes.resampleSingle(i);   // re-sample solution until it is feasible
                }
                fitness[i] = fitFun.valueOf(population[i]);    // compute fitness value, where fitFun
            }                                                  // is the function to be minimized
            cmaes.updateDistribution(fitness);                 // pass fitness array to update search distribution

            if (debugMode) {
                cmaes.writeToDefaultFiles();

                int outModulo = 150000;
                if (cmaes.getCountIter() % (15 * outModulo) == 1) {
                    cmaes.printlnAnnotation();              // might write file as well
                }
                if (cmaes.getCountIter() % outModulo == 1) {
                    cmaes.println();
                }
            }

            repaint();
        }

        double[] best = cmaes.getBestSolution().getX();

        println("best bx: " + best[0] + " best by: " + best[1]);

        for (int i = 0; i < boundingBox.getNumPoints(); i++) {
            boundingBox.getPoint(i).x += best[0];
            boundingBox.getPoint(i).y += best[1];
        }

        println("experimental bb: " + experimentalQuality(boundingBox) + " best cmaes: " + cmaes.getBestFunctionValue());
    }

    /// last but not least main ////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame f = new JFrame();
            f.setSize(760, 760);
            f.add(new IRISVisualization(f));
            f.setTitle(defaultTitle);
            f.setVisible(true);
        });
    }

    /// deprecated ////////////////////////////////////////////////////////////////////////////////////////

    private double distanceToClosestIndex(double x, double y) {

        int numPoints = hugeCurve.getNumPoints();

        double minDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < numPoints; i++) {
            Point2D.Double vertex = hugeCurve.getPoints().get(i);
            double distance = vertex.distance(x, y);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private double qualityFunctionOld() {

        qualityPolygons = new ArrayList<>();
        intersectionPoints = hugeCurve.getIntersectionPoints(boundingBox); /// TODO: reset to boundingBoxTest ?

        //for (MyVector v : intersectionPoints) v.print();

        double quality = 0.0;
        int count = 0;

        for (int i = 0; i < intersectionPoints.size() - 1; i += 2) {


            MyVector isp1 = intersectionPoints.get(i);
            MyVector isp2 = intersectionPoints.get(i + 1);
            isp1.setName(isp1.getName() + " i: " + count);
            isp2.setName(isp2.getName() + " i: " + (count + 1));
            count++;
            int from = isp1.getId();
            int to = isp2.getId();
            MyDoublePolygon polygon = hugeCurve.getSubPolygon(from, to);
            double area = polygon.calculateArea();
            qualityPolygons.add(polygon);
            quality += area;
        }
        return quality;
    }
}

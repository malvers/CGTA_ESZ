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
    private final Color myRed = new Color(180, 0, 0);
    private Color myBlue = new Color(0, 0, 80);
    private final Color myGreen = new Color(80, 140, 0);
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
    private double rotationAngle = 0;
    private Rectangle2D.Double boundingBox = new Rectangle2D.Double();
    private MyDoublePolygon hugeCurve;
    private ArrayList<MyVector> boundingBoxInnerRotationPath;

    /// optimization stuff
    private double[] fitness;
    private IRISVisualization fitFun;
    private CMAEvolutionStrategy cmaes;
    private BufferedImage heatMap;

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("hooked");
            writeSettings();
        }));
    }

    /// read/write settings ////////////////////////////////////////////////////////////////////////////////////////////

    private void writeSettings() {

        try {
            FileOutputStream f = new FileOutputStream("./IRISVisualization.bin");
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

            os.writeDouble(whiperFactor);

            os.writeObject(debugWindow);

            os.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSettings() {

        try {
            FileInputStream f = new FileInputStream("./IRISVisualization.bin");
            ObjectInputStream os = new ObjectInputStream(f);

            frame.setSize((Dimension) os.readObject());
            frame.setLocation((Point) os.readObject());

            //os.readObject();
            sceneShift = (Point2D.Double) os.readObject();

            drawCircle = os.readBoolean();
            drawLines = os.readBoolean();
            drawAnnotation = os.readBoolean();
            drawIris = os.readBoolean();
            blackMode = os.readBoolean();
            drawWhiskers = os.readBoolean();
            drawTriangle = os.readBoolean();
            drawIrisPicture = os.readBoolean();

//            irisPicShiftX = os.readInt();
//            irisPicShiftY = os.readInt();
//            irisPicSize = os.readInt();

            os.readInt();
            os.readInt();
            os.readInt();

            drawWhiperStuff = os.readBoolean();
            debugMode = os.readBoolean();
            drawBoundingBox = os.readBoolean();

            handleA = (MyVector) os.readObject();
            handleB = (MyVector) os.readObject();
            handleC = (MyVector) os.readObject();

            whiperFactor = os.readDouble();

            debugWindow = (DebugWindow) os.readObject();
            if (debugWindow == null) {
                debugWindow = new DebugWindow();
            } else {
                debugWindow.setVisible(true);
                debugWindow.clear();
            }

            os.close();
            f.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /// helper functions /////////////////////////////////////////////////////////////////////////////////////////////////

    static DebugWindow debugWindow = null;//new DebugWindow();

    public static void println() {
        if (debugWindow == null) {
            System.out.println();
        } else {
            debugWindow.println("");
        }
    }

    public static void println(String s) {
        if (debugWindow == null) {
            System.out.println(s);
        } else {
            debugWindow.println(s);
        }
    }

    public static void print(String s) {
        if (debugWindow == null) {
            System.out.println(s);
        } else {
            debugWindow.println(s);
        }
    }

    private void createHeatMap() {

        println("create heat map: " + (getWidth() * getHeight()));

        initRotatedBoundingBox();

        double min = Double.MAX_VALUE;
        double max = 0.0;
        double size = boundingBox.getWidth();

        double[][] values = new double[getWidth()][getHeight()];

        int count = 0;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {

                double q = qualityFunction(shiftShape(boundingBox, x, y));

                values[x][y] = q;

                if (q < min) {
                    min = q;
                }
                if (q > max) {
                    max = q;
                }
                if (count % 100000 == 0) {
                    println("count: " + count);
                }
                count++;
            }
        }

        heatMap = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        println(count + " min: " + min + " max: " + max);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Color color = ColorSpectrum.getColorSpectrum(values[x][y], min, max);
                heatMap.setRGB(x, y, color.getRGB());

                if (values[x][y] == 0.0) {
                    heatMap.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
    }

    /// mouse handling /////////////////////////////////////////////////////////////////////////////////////////////////

    private void mouseInit() {

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {

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
                } else if (whiperAC.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperAC.selected = true;
                } else if (whiperBA.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperBA.selected = true;
                } else if (whiperCB.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperCB.selected = true;
                } else if (whiperBC.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperBC.selected = true;
                } else if (whiperCA.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperCA.selected = true;
                } else if (whiperAB.contains(shiftMouse.x, shiftMouse.y)) {
                    whiperAB.selected = true;
                }

                initRotatedBoundingBox();
                createHugeCurve();
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                if (drawHelp) {
                    return;
                }

                irisPicSize = zoomedSize;
                irisZoom = 1.0;

                sceneShift.x += dragShift.x;
                sceneShift.y += dragShift.y;
                dragShift.x = 0;
                dragShift.y = 0;

                calculateWhipers();
                createHugeCurve();
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

                if (drawHelp) {
                    return;
                }

                moveSceneHandlesWhipers(e);

                doCalculations();
                createWhiperCurves();
                calculateBoundingBoxRotationCurve360();

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

    private boolean areHandlesSelected() {
        return handleA.selected || handleB.selected || handleC.selected;
    }

    private boolean areWhipersSelected() {
        return whiperAC.selected || whiperBA.selected || whiperCB.selected || whiperCA.selected || whiperBC.selected || whiperAB.selected;
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
        } else {
            dragShift.x = -(onMousePressed.x - e.getX());
            dragShift.y = -(onMousePressed.y - e.getY());
        }
    }

    /// key handling ///////////////////////////////////////////////////////////////////////////////////////////////////

    private void keyInit() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                boolean doRepaint = true;

//                println("key: " + e.getKeyCode());
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_0:
                        if (e.isShiftDown()) {
                            sceneShift.x = 0;
                            sceneShift.y = 0;
                        } else {
                            zoomFactor = 1.0;
                            rotationAngle = 0.0;
                            whiperFactor = 1.0;
                            doCalculations();
                            calculateWhipers();
                        }
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
                            shiftBoundingBox(e.getKeyCode(), inc);
                        }
                        break;
                    case KeyEvent.VK_SPACE:

                        doRepaint = false;
                        rotationAngle += Math.toRadians(1.0);
                        initRotatedBoundingBox();
                        initCMAES();
                        runCMAES();

                        break;
                    case KeyEvent.VK_BACK_SPACE:

                        if (debugWindow != null) {
                            debugWindow.clear();
                        }

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

                        break;
                    case KeyEvent.VK_Q:
                        println("Q - exp q: " + qualityFunction(boundingBox));
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
                        if (e.isShiftDown()) {
                            calculateBoundingBoxRotationCurve360();
                        } else {
                            long innerStart = System.currentTimeMillis();
                            startCMAES();
                            println("startCMAES: " + (System.currentTimeMillis() - innerStart) + " ms");
                        }
                        break;
                    case KeyEvent.VK_Y:
                        createHeatMap();
                        break;
                    case KeyEvent.VK_Z:
                        handleZKey(e);
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
                if (doRepaint) {
                    repaint();
                }
            }
        });
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
            myBlue = new Color(0, 180, 180);
        } else {
            myBlue = new Color(0, 0, 80);
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

        double delta = 0.05;
        boundingBox = new Rectangle2D.Double(extendX.x - delta, extendY.x - delta, distX + 2 * delta, distY + 2 * delta);

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
    protected static MyVector getIntersectionPoint(MyVector vec1Start, MyVector vec1End, MyVector vec2Start, MyVector vec2End) {

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

        MyVector tmp = new MyVector(px, py, "");
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

    double zoomFactor = 1.0;

    private void handleZKey(KeyEvent e) {
        if (e.isMetaDown()) {
            irisZoom += 0.1;
        } else if (e.isMetaDown() && e.isShiftDown()) {
            irisZoom -= 0.1;
        } else if (e.isAltDown()) {
            zoomFactor += 0.2;
        } else if (e.isControlDown()) {
            zoomFactor -= 0.2;
        }
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));

        if (drawHelp) {
            HelpPage.drawHelpPage(g2d);
            return;
        }

        clearBackground(g2d);

        double xs = (sceneShift.x + dragShift.x);
        double ys = (sceneShift.y + dragShift.y);

        AffineTransform transform = AffineTransform.getTranslateInstance(xs, ys);

        double dx = getWidth() / 2.0;
        double dy = getWidth() / 2.0;
        transform.translate(-dx, -dy);
        transform.scale(zoomFactor, zoomFactor);
        transform.translate(+dx, +dy);

        g2d.setTransform(transform);

        if (heatMap != null) {
            g2d.drawImage(heatMap, 0, 0, null);
        }

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

        if (drawBoundingBox) {
            drawBoundingBox(g2d);
            drawBoundingBoxRotationPath(g2d);
        }

        if (drawWhiperStuff) {
            drawWiperCurves(g2d);
            drawWipers(g2d);
        }
    }

    private void drawBoundingBoxRotationPath(Graphics2D g2d) {

        if (boundingBoxInnerRotationPath.size() > 0) {

            g2d.setColor(myRed);
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
        g2d.drawImage(irisPicture, (int) (centerCircle.x - delta), (int) (centerCircle.y - delta), zoomedSize, zoomedSize, null);
    }

    private void clearBackground(Graphics2D g2d) {
        if (blackMode) {
            g2d.setColor(Color.BLACK);

        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawBoundingBox(Graphics2D g2d) {

        MyVector centerBoundingBox = getCenterBoundingBox();
        g2d.setColor(myGreen);
        centerBoundingBox.setSize(4);
        centerBoundingBox.fill(g2d, false);

        g2d.setStroke(new BasicStroke(1));
        g2d.draw(rotateRectangle2D(boundingBox));
    }

    private MyVector getCenterBoundingBox() {

        double cx = boundingBox.getCenterX();
        double cy = boundingBox.getCenterY();

        MyVector centerBoundingBox = new MyVector(cx, cy, "center bounding box");
        centerBoundingBox.setSize(4);
        return centerBoundingBox;
    }

    private void drawWiperCurves(Graphics2D g2d) {

        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(1));
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

        g2d.setColor(myBlue);
        drawHandleConnector(g2d, handleA, handleB);
        g2d.setColor(myGreen);
        drawHandleConnector(g2d, handleB, handleC);
        g2d.setColor(myRed);
        drawHandleConnector(g2d, handleC, handleA);

        g2d.setColor(Color.darkGray);
        handleA.fill(g2d, drawAnnotation);
        handleB.fill(g2d, drawAnnotation);
        handleC.fill(g2d, drawAnnotation);
    }

    private void drawExtendWhiskers(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(myGreen);
        extendBA.fill(g2d, drawAnnotation);
        extendCA.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, extendBA.x, extendBA.y));
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, extendCA.x, extendCA.y));
        g2d.setColor(myBlue);
        extendBC.fill(g2d, drawAnnotation);
        extendAC.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, extendBC.x, extendBC.y));
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, extendAC.x, extendAC.y));
        g2d.setColor(myRed);
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
        g2d.setColor(Color.MAGENTA.darker());
        centerCircle.fill(g2d, drawAnnotation);
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
        g2d.setColor(Color.MAGENTA.darker());
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

    /// optimizing related section /////////////////////////////////////////////////////////////////////////////////////

    private void shiftBoundingBox(int vKup, double inc) {

        switch (vKup) {
            case KeyEvent.VK_UP:
                boundingBox.y -= inc;
                break;
            case KeyEvent.VK_DOWN:
                boundingBox.y += inc;
                break;
            case KeyEvent.VK_LEFT:
                boundingBox.x -= inc;
                break;
            case KeyEvent.VK_RIGHT:
                boundingBox.x += inc;
                break;
        }
        println("quality: " + qualityFunction(boundingBox));
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

    private Shape rotateRectangle2D(Rectangle2D.Double box) {

        AffineTransform transform = new AffineTransform();
        double wHalf = boundingBox.width / 2;
        transform.rotate(rotationAngle, boundingBox.x + wHalf, boundingBox.y + wHalf);
        return transform.createTransformedShape(box);
    }

    private double getClosestDistanceToPoint(Point2D point, Shape shape) {

        double minDistance = Double.MAX_VALUE;

        PathIterator pathIterator = shape.getPathIterator(null);
        double[] coordinates = new double[6];

        while (!pathIterator.isDone()) {
            int segmentType = pathIterator.currentSegment(coordinates);
            switch (segmentType) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    double x1 = coordinates[0];
                    double y1 = coordinates[1];
                    double distance = point.distance(x1, y1);
                    minDistance = Math.min(minDistance, distance);
                    break;
            }
            pathIterator.next();
        }

        return minDistance;
    }

    /// implementations for CMAES //////////////////////////////////////////////////////////////////////////////////////

    Shape rotatedBoundingBox;

    private void initRotatedBoundingBox() {
        rotatedBoundingBox = rotateRectangle2D(boundingBox);
    }

    ArrayList<Point2D.Double> pointsHugeCurve = null;

    private double qualityFunction(Shape box) {

        if (pointsHugeCurve == null) {
            pointsHugeCurve = new ArrayList<>(hugeCurve.getNumPoints());
            pointsHugeCurve = hugeCurve.getPoints();
        }

        double sumDist = 0.0;

        for (int i = 0; i < hugeCurve.getNumPoints(); i++) {

            if (!box.contains(pointsHugeCurve.get(i))) {
                sumDist += getClosestDistanceToPoint(pointsHugeCurve.get(i), box);
            }
        }
        return sumDist;
    }

    @Override
    public double valueOf(double[] values) {

        return qualityFunction(shiftShape(rotatedBoundingBox, values[0], values[1]));
    }

    public Shape shiftShape(Shape shape, double deltaX, double deltaY) {

        return AffineTransform.getTranslateInstance(deltaX, deltaY).createTransformedShape(shape);
    }

    @Override
    public boolean isFeasible(double[] values) {
        return true;
    }

    private void startCMAES() {

        initRotatedBoundingBox();
        initCMAES();

        new Thread(this).start();
        runIt = !runIt;
    }

    private void initCMAES() {

        // we are the optimizer our self
        fitFun = this;

        cmaes = new CMAEvolutionStrategy();
        cmaes.setDimension(2);
        cmaes.setInitialX(1);
        cmaes.setInitialStandardDeviation(5);
        cmaes.options.stopFitness = 0;
        cmaes.options.verbosity = -1;

        fitness = cmaes.init();
    }

    private void calculateBoundingBoxRotationCurve360() {

        long start = 0;

        if (debugMode) {
            println("createBoundingBoxRotationCurve360");
            start = System.currentTimeMillis();
        }

        pointsHugeCurve = new ArrayList<>(hugeCurve.getNumPoints());
        pointsHugeCurve = hugeCurve.getPoints();

        initRotatedBoundingBox();
        boundingBoxInnerRotationPath = new ArrayList<>();

        double incAngle = Math.toRadians(4.0);
        double to = Math.PI - incAngle;
        long innerStart = 0;
        for (rotationAngle = 0; rotationAngle < to; rotationAngle += incAngle) {

            if (debugMode) {
                println("rotation: " + Math.toDegrees(rotationAngle));
            }

            rotatedBoundingBox = rotateRectangle2D(boundingBox);

            initCMAES();

            if (debugMode) {
                innerStart = System.currentTimeMillis();
            }

            runCMAES();

            if (debugMode) {
                println("runCMAES: " + (System.currentTimeMillis() - innerStart) + " ms");
            }

            boundingBoxInnerRotationPath.add(getCenterBoundingBox());
        }

        if (debugMode) {
            double delta = System.currentTimeMillis() - start;
            println("done - time needed: " + delta + " [ms] " + " size path: " + boundingBoxInnerRotationPath.size());
        }
    }

    private void runCMAES() {

        while (cmaes.stopConditions.getNumber() == 0) {

            coreIterationStepCMAES();
        }

        setOptimizedBoundingBox();

        SwingUtilities.invokeLater(() -> repaint());
    }

    @Override
    public void run() {

        while (runIt) {

            if (cmaes.stopConditions.getNumber() != 0) {
                runIt = false;
            }

            coreIterationStepCMAES();

            if (debugMode) {
                cmaes.writeToDefaultFiles();

                int outModulo = 150;
                if (cmaes.getCountIter() % (15 * outModulo) == 1) {
                    cmaes.printlnAnnotation();              // might write file as well
                }
                if (cmaes.getCountIter() % outModulo == 1) {
                    cmaes.println();
                }
            }
        }

        setOptimizedBoundingBox();

        SwingUtilities.invokeLater(() -> repaint());
    }

    private void coreIterationStepCMAES() {

        double[][] population = cmaes.samplePopulation();  // get a new population of solutions
        for (int i = 0; i < population.length; ++i) {      // for each candidate solution i

            while (!fitFun.isFeasible(population[i])) {    // test whether solution is feasible,
                population[i] = cmaes.resampleSingle(i);   // re-sample solution until it is feasible
            }
            fitness[i] = fitFun.valueOf(population[i]);    // compute fitness value, where fitFun
        }  // is the function to be minimized
        cmaes.updateDistribution(fitness);                 // pass fitness array to update search distribution
    }

    private void setOptimizedBoundingBox() {

        double[] best = cmaes.getBestSolution().getX();

        double nx = boundingBox.getX() + best[0];
        double ny = boundingBox.getY() + best[1];
        boundingBox.setRect(nx, ny, boundingBox.width, boundingBox.width);
    }

    /// last but not least main ////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame f = new JFrame();
            f.setSize(760, 760);
            f.add(new IRISVisualization(f));
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle(defaultTitle);
            f.setVisible(true);
        });
    }

/*   deprecated deprecated deprecated deprecated deprecated deprecated deprecated deprecated deprecated deprecated    */

//    private void shiftTestPolygonExperimental(double x, double y) {
//
//        ArrayList<Point2D.Double> points = boundingBox.getPoints();
//
//        for (int i = 0; i < boundingBox.getNumPoints(); i++) {
//
//            points.get(i).x = x;
//            points.get(i).y = y;
//        }
//        boundingBox.setPoints(points);
//    }
//    private void shiftBoundingBoxDouble(int direction) {
//
//        ArrayList<Point2D.Double> points = boundingBox.getPoints();
//
//        double inc = 0.6;
//        for (int i = 0; i < boundingBox.getNumPoints(); i++) {
//
//            switch (direction) {
//                case 0:
//                    points.get(i).y -= inc;
//                    break;
//                case 1:
//                    points.get(i).y += inc;
//                    break;
//                case 2:
//                    points.get(i).x -= inc;
//                    break;
//                case 3:
//                    points.get(i).x += inc;
//                    break;
//            }
//        }
//        boundingBox.setPoints(points);
//    }
//    public static Rectangle constructRectangle(MyVector p1, MyVector p2, MyVector p3, MyVector p4) {
//
//        // Find extreme coordinates
//        double minX = Math.min(Math.min(Math.min(p1.x, p2.x), p3.x), p4.x);
//        double maxX = Math.max(Math.max(Math.max(p1.x, p2.x), p3.x), p4.x);
//        double minY = Math.min(Math.min(Math.min(p1.y, p2.y), p3.y), p4.y);
//        double maxY = Math.max(Math.max(Math.max(p1.y, p2.y), p3.y), p4.y);
//
//        // Calculate width and height
//        double width = maxX - minX;
//        double height = maxY - minY;
//
//        // Create and return the rectangle
//        return new Rectangle((int) minX, (int) minY, (int) width, (int) height);
//    }
//
//    private Point2D.Double getCenter(Rectangle rect) {
//
//        double centerX = rect.getX() + rect.getWidth() / 2;
//        double centerY = rect.getY() + rect.getHeight() / 2;
//        return new Point2D.Double(centerX, centerY);
//    }
//
//    private double distanceToClosestIndex(double x, double y) {
//
//        int numPoints = hugeCurve.getNumPoints();
//
//        double minDistance = Double.POSITIVE_INFINITY;
//        for (int i = 0; i < numPoints; i++) {
//            Point2D.Double vertex = hugeCurve.getPoints().get(i);
//            double distance = vertex.distance(x, y);
//            if (distance < minDistance) {
//                minDistance = distance;
//            }
//        }
//        return minDistance;
//    }
//
//    private double qualityFunctionOld() {
//
//        qualityPolygons = new ArrayList<>();
//        intersectionPoints = hugeCurve.getIntersectionPoints(boundingBox);
//
//        //for (MyVector v : intersectionPoints) v.print();
//
//        double quality = 0.0;
//        int count = 0;
//
//        for (int i = 0; i < intersectionPoints.size() - 1; i += 2) {
//
//
//            MyVector isp1 = intersectionPoints.get(i);
//            MyVector isp2 = intersectionPoints.get(i + 1);
//            isp1.setName(isp1.getName() + " i: " + count);
//            isp2.setName(isp2.getName() + " i: " + (count + 1));
//            count++;
//            int from = isp1.getId();
//            int to = isp2.getId();
//            MyDoublePolygon polygon = hugeCurve.getSubPolygon(from, to);
//            double area = polygon.calculateArea();
//            qualityPolygons.add(polygon);
//            quality += area;
//        }
//        return quality;
//    }
//
//    private void optimizePolygonPositionBruteForce(int count) {
//
//        /// puzzle the single 6 curves together to one huge one
//
//        double qStart = numberPointsOutside();
//        double qOptimal = Double.MAX_VALUE;
//
//        double searchSize = 16;
//
//        int numPoints = 10000;
//
//        MyVector center = boundingBox.getCenter();
//        ArrayList<MyVector> scatter = MyVector.scatterPointsAround(center, searchSize, numPoints);
//
//        debugList = new ArrayList<>();
//
//        MyDoublePolygon testPolygonOptimal = new MyDoublePolygon();
//
//        for (MyVector scatterPoint : scatter) {
//
//            testPolygonOptimal = copyPolygons(boundingBox);
//
//            double displaceX = center.x - scatterPoint.x;
//            double displaceY = center.y - scatterPoint.y;
//
//            ArrayList<Point2D.Double> testPolygon = createTestPolygon();
//
//            for (int i = 0; i < 4; i++) {
//                testPolygon.get(i).x = boundingBox.getPoint(i).x + displaceX;
//                testPolygon.get(i).y = boundingBox.getPoint(i).y + displaceY;
//            }
//
//            MyDoublePolygon debug = new MyDoublePolygon("debug " + count);
//            debug.setPoints(testPolygon);
//            debugList.add(debug);
//
//            qOptimal = numberPointsOutside(debug);
//
//            if (qOptimal < qStart) {
//                //println("BINGO optimal: " + qOptimal + " start: " + qStart);
//                boundingBox.setPoints(testPolygon);
//                testPolygonOptimal = copyPolygons(debug);
//                qStart = qOptimal;
//            }
//        }
//        boundingBox = copyPolygons(testPolygonOptimal);
//        MyVector c = boundingBox.getCenter();
//        c.setSize(4);
//        boundingBoxInnerRotationPath.add(c);
//
//        println(count + " optimizePolygonPosition: " + qOptimal);
//    }
}

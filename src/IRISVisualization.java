import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class IRISVisualization extends JButton {

    private final JFrame frame;
    private final BufferedImage irisPic;
    private MyVector whiskerBC;
    private MyVector whiskerAB;
    private MyVector whiskerCB;
    private MyVector whiskerAC;
    private MyVector whiskerCA;
    private MyVector whiskerBA;
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
    private boolean drawIrisPic = true;
    private boolean debugMode = true;
    private boolean drawWhiperCurve = false;
    private int irisPicSize = 688;
    private int irisPicShiftX = 163;
    private int irisPicShifty = 186;
    private MyVector whiperC_CB;
    private MyVector whiperA_AC;
    private MyVector whiperC_CA;
    private MyDoublePolygon bigWhiperCurve1 = new MyDoublePolygon();
    private MyDoublePolygon bigWhiperCurve2 = new MyDoublePolygon();
    private MyDoublePolygon bigWhiperCurve3 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve1 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve2 = new MyDoublePolygon();
    private MyDoublePolygon smallWhiperCurve3 = new MyDoublePolygon();

    public IRISVisualization(JFrame f) {

        frame = f;

        mouseInit();
        keyInit();

        double handleSize = 12;
        sceneShift.x = 80;
        sceneShift.y = 20;

        handleA = new MyVector(535, 395, handleSize, "A");
        handleB = new MyVector(358, 573, handleSize, "B");
        handleC = new MyVector(615, 635, handleSize, "C");

        readSettings();

        adjustColorBlackMode();

        irisPic = loadImage("/Users/malvers/IdeaProjects/CGTA_ESZ/src/Iris Mi free.png");

        doCalculations();
        calculateWhipers();
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
            os.writeBoolean(drawIrisPic);

            os.writeInt(irisPicShiftX);
            os.writeInt(irisPicShifty);
            os.writeInt(irisPicSize);

            os.writeBoolean(drawWhiperCurve);

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
            drawIrisPic = os.readBoolean();

            irisPicShiftX = os.readInt();
            irisPicShifty = os.readInt();
            irisPicSize = os.readInt();

            drawWhiperCurve = os.readBoolean();

//            handleA = (Handle) os.readObject();
//            handleB = (Handle) os.readObject();
//            handleC = (Handle) os.readObject();

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
                } else if (MyVector.distanceToPointFromLine(e.getPoint(), sceneShift, handleC, whiperC_CB) < 4) {
                    whiperC_CB.selected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

                sceneShift.x += dragShift.x;
                sceneShift.y += dragShift.y;
                dragShift.x = 0;
                dragShift.y = 0;

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

                if (handleA.selected) {
                    handleA.x = e.getX() - sceneShift.x;
                    handleA.y = e.getY() - sceneShift.y;
                } else if (handleB.selected) {
                    handleB.x = e.getX() - sceneShift.x;
                    handleB.y = e.getY() - sceneShift.y;
                } else if (handleC.selected) {
                    handleC.x = e.getX() - sceneShift.x;
                    handleC.y = e.getY() - sceneShift.y;
                } else if (whiperC_CB.selected) {

                    MyVector tmp = whiperC_CB.subtract(handleC);

                    double lenB = tmp.getLength();

                    System.out.println("lenB: " + lenB );

                    tmp.y = e.getX() - sceneShift.x;
                    tmp.x = e.getY() - sceneShift.y;

                    tmp = tmp.makeItThatLong(lenB);

                    double lenA = tmp.getLength();

                    whiperC_CB.x = -tmp.x + handleC.x;
                    whiperC_CB.y = -tmp.y + handleC.y;

                    System.out.println("lenA: " + lenA);

                } else {
                    dragShift.x = -(onMousePressed.x - e.getX());
                    dragShift.y = -(onMousePressed.y - e.getY());
                }
                doCalculations();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

                if (!drawHelp) {
                    String title = "Conway's IRIS";
                    if (debugMode) {
                        title += " - x: " + e.getX() + " y: " + e.getY();
                    }
                    frame.setTitle(title);
                }
            }
        });
    }

    private void keyInit() {

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

//                System.out.println("key: " + e.getKeyCode());
                switch (e.getKeyCode()) {

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
                        printHandles();
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
                        drawIrisPic = !drawIrisPic;
                        if (drawIrisPic) {
                            blackMode = true;
                        } else {
                            blackMode = bm;
                        }
                        break;
                    case KeyEvent.VK_R:
                        sceneShift.x = 0;
                        sceneShift.y = 0;
                        break;
                    case KeyEvent.VK_T:
                        drawTriangle = !drawTriangle;
                        break;
                    case KeyEvent.VK_W:
                        if (e.isMetaDown()) {
                            writeSettings();
                            System.exit(0);
                        } else {
                            drawWhiperCurve = !drawWhiperCurve;
                        }
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        if (e.isMetaDown()) {
                            moveIrisPic(e.getKeyCode(), 1);
                        } else {
                            moveHandles(e.getKeyCode(), 20);
                            moveIrisPic(e.getKeyCode(), 20);
                            doCalculations();
                        }
                        break;
                    case 93: /// +
                        irisPicSize++;
                        break;
                    case 47: /// -
                        irisPicSize--;
                        break;
                }

                repaint();
            }
        });
    }

    private void printHandles() {
        handleA.print("A");
        handleB.print("B");
        handleC.print("C");
    }

    private void moveIrisPic(int vKup, int inc) {

        switch (vKup) {
            case KeyEvent.VK_UP:
                irisPicShifty -= inc;
                break;
            case KeyEvent.VK_DOWN:
                irisPicShifty += inc;
                break;
            case KeyEvent.VK_LEFT:
                irisPicShiftX -= inc;
                break;
            case KeyEvent.VK_RIGHT:
                irisPicShiftX += inc;
                break;
        }
        System.out.println("psx: " + irisPicShiftX + " psy: " + irisPicShifty + " ps: " + irisPicSize);
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
        whiperA_AC.selected = false;
        whiperC_CA.selected = false;
        whiperC_CB.selected = false;
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

        whiskerBA = calculateExtendedPoint(handleB, handleC, handleA);
        whiskerCA = calculateExtendedPoint(handleC, handleB, handleA);

        whiskerBC = calculateExtendedPoint(handleB, handleA, handleC);
        whiskerAC = calculateExtendedPoint(handleA, handleB, handleC);

        whiskerAB = calculateExtendedPoint(handleA, handleC, handleB);
        whiskerCB = calculateExtendedPoint(handleC, handleA, handleB);

        midCA_BA = calculateMidpoint(whiskerCA, whiskerBA);
        midBC_AC = calculateMidpoint(whiskerAC, whiskerBC);
        midCB_AB = calculateMidpoint(whiskerCB, whiskerAB);

        MyVector vecCA_BA = MyVector.getVector(whiskerBA, whiskerCA);
        MyVector recCA_BA = vecCA_BA.flip();
        recCA_BA.x = -recCA_BA.x;
        recCA_BA = recCA_BA.makeItThatLong(400);
        towCA_BA = midCA_BA.add(recCA_BA);

        MyVector vecBC_AC = MyVector.getVector(whiskerBC, whiskerAC);
        MyVector recBC_AC = vecBC_AC.flip();
        recBC_AC.x = -recBC_AC.x;
        recBC_AC.makeItThatLong(400);
        towBC_AC = midBC_AC.subtract(recBC_AC);

        MyVector vecCB_AB = MyVector.getVector(whiskerCB, whiskerAB);
        MyVector recCB_AB = vecCB_AB.flip();
        recCB_AB.x = -recCB_AB.x;
        recCB_AB.makeItThatLong(400);
        towCB_AB = midCB_AB.add(recCB_AB);

        centerCircle = getIntersectionPointGPT(midCB_AB, towCB_AB, midBC_AC, towBC_AC);

        if (centerCircle != null) {
            radiusCircle = MyVector.getVector(centerCircle, whiskerAB).getLength();
        }
    }

    private void calculateWhipers() {

        whiperC_CB = MyVector.getVector(whiskerCB, handleC).add(handleC);
        whiperC_CA = MyVector.getVector(whiskerCA, handleC).add(handleC);
        whiperA_AC = MyVector.getVector(whiskerAC, handleA).add(handleA);
        createWhiperCurve();
    }

    private void createWhiperCurve() {

        bigWhiperCurve1 = new MyDoublePolygon();
        bigWhiperCurve2 = new MyDoublePolygon();
        bigWhiperCurve3 = new MyDoublePolygon();

        smallWhiperCurve1 = new MyDoublePolygon();
        smallWhiperCurve2 = new MyDoublePolygon();
        smallWhiperCurve3 = new MyDoublePolygon();

        createBigWhiperCurveSegment(bigWhiperCurve1, whiskerCB, handleC, handleB, handleA);

        createSmallWhiperCurveSegment(smallWhiperCurve1, handleB, whiskerAB, whiskerCB);

        createBigWhiperCurveSegment(bigWhiperCurve2, whiskerBA, handleB, handleA, handleC);

        createSmallWhiperCurveSegment(smallWhiperCurve2, handleA, whiskerCA, whiskerBA);

        createBigWhiperCurveSegment(bigWhiperCurve3, whiskerAC, handleA, handleC, handleB);

        createSmallWhiperCurveSegment(smallWhiperCurve3, handleC, whiskerBC, whiskerAC);
    }

    private void createSmallWhiperCurveSegment(MyDoublePolygon whiperCurve, MyVector v1, MyVector v2, MyVector v3) {

        MyVector vec12 = MyVector.getVector(v1, v2);
        MyVector vec13 = MyVector.getVector(v1, v3);
        double angleVec = MyVector.angleBetweenHandles(vec12, vec13);
        int angleSteps = (int) radiansToDegrees(angleVec);
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
        int angleSteps = (int) radiansToDegrees(angleCB_CA);
        double angleInc = angleCB_CA / angleSteps;

        double rotationAngle = 0.0;
        for (int i = 0; i <= angleSteps; i++) {

            MyVector pointOnCurve = MyVector.getVector(whisker, v1).rotate(rotationAngle).add(v1);
            rotationAngle += angleInc;

            whiperCurve.addPoint(pointOnCurve.x, pointOnCurve.y);
        }
    }

    private double radiansToDegrees(double angleRadians) {
        return angleRadians * (180.0 / Math.PI);
    }

    private MyVector calculateExtendedPoint(MyVector one, MyVector two, MyVector three) {

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
        return new MyVector(three.getX() - vector13Scaled_x, three.getY() - vector13Scaled_y, 6, name);
    }

    private MyVector calculateMidpoint(MyVector point1, MyVector point2) {

        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new MyVector(midX, midY, 6, "");
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

        return new MyVector(px, py, 10, "center");
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

        if (blackMode) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        AffineTransform shiftTransform = AffineTransform.getTranslateInstance(sceneShift.x + dragShift.x, sceneShift.y + dragShift.y);
        g2d.setTransform(shiftTransform);

        if (drawIrisPic) {
            g2d.drawImage(irisPic, irisPicShiftX, irisPicShifty, irisPicSize, irisPicSize, null);
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

        if (drawWhiperCurve) {
            drawWiperCurves(g2d);
            drawWipers(g2d);
        }
    }

    private void drawWiperCurves(Graphics2D g2d) {

        g2d.setColor(Color.BLUE.darker());
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
        whiskerBA.fill(g2d, drawAnnotation);
        whiskerCA.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, whiskerBA.x, whiskerBA.y));
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, whiskerCA.x, whiskerCA.y));
        g2d.setColor(color2);
        whiskerBC.fill(g2d, drawAnnotation);
        whiskerAC.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, whiskerBC.x, whiskerBC.y));
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, whiskerAC.x, whiskerAC.y));
        g2d.setColor(color1);
        whiskerAB.fill(g2d, drawAnnotation);
        whiskerCB.fill(g2d, drawAnnotation);
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, whiskerAB.x, whiskerAB.y));
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, whiskerCB.x, whiskerCB.y));
    }

    private void drawLines(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.lightGray);
        drawHandleConnector(g2d, whiskerCA, whiskerBA);
        drawHandleConnector(g2d, whiskerBC, whiskerAC);
        drawHandleConnector(g2d, whiskerCB, whiskerAB);

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

    private void drawHandleConnector(Graphics2D g2d, MyVector h1, MyVector h2) {

        g2d.draw(new Line2D.Double(h1.x, h1.y, h2.x, h2.y));
    }

    private void drawWipers(Graphics2D g2d) {

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));

        whiperC_CB.fill(g2d, false);
        drawHandleConnector(g2d, handleC, whiperC_CB);

        whiperC_CA.fill(g2d, false);
        drawHandleConnector(g2d, handleC, whiperC_CA);

        whiperA_AC.fill(g2d, false);
        drawHandleConnector(g2d, handleA, whiperA_AC);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame f = new JFrame();
            f.setSize(760, 760);
            f.add(new IRISVisualization(f));
            f.setTitle("Conway's IRIS");
            f.setVisible(true);
        });
    }
}

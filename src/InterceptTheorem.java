import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

public class InterceptTheorem extends JButton {

    private boolean drawDist = false;
    private boolean drawParellels = false;

    private class Handle extends Ellipse2D.Double {
        public boolean selected = false;

        public Handle(double p1, double p2, int w, int h) {
            super(p1, p2, w, h);
        }
    }

    private Color red = new Color(180, 0, 0);
    private Color blue = new Color(0, 0, 80);

    private Color green = new Color(80, 140, 0);
    private Handle handle1 = null;
    private Handle handle2 = null;
    private Point2D.Double pto1 = null;
    private Point2D.Double pto2 = null;
    private static int size = 800;
    private int gapX = 100;
    private int gapY = 100;

    public InterceptTheorem() {

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (handle1.contains(e.getX(), e.getY())) {
                    handle1.selected = true;
                    handle2.selected = false;
                } else if (handle2.contains(e.getX(), e.getY())) {
                    handle1.selected = false;
                    handle2.selected = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handle1.selected = false;
                handle2.selected = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (handle1.selected) {
                    pto1.x = e.getX();
                    pto1.y = e.getY();
                    handle1.x = pto1.x - 6;
                    handle1.y = pto1.y - 6;
                } else if (handle2.selected) {
                    pto2.x = e.getX();
                    pto2.y = e.getY();
                    handle2.x = pto2.x - 6;
                    handle2.y = pto2.y - 6;
                }
                repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    System.exit(0);
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    drawParellels = !drawParellels;
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    drawDist = !drawDist;
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    gapX += 10;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    gapX -= 10;
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    gapX -= 10;
                    gapY += 10;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    gapX += 10;
                    gapY -= 10;
                }
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        g2d.setStroke(new BasicStroke(3));
        DecimalFormat df = new DecimalFormat("#.##");

        double ps = 6.0;
        double shift = ps / 2.0;

        Point2D.Double pointS = new Point2D.Double(getWidth() - 100, getHeight() - 100);
        if (pto1 == null) {
            pto1 = new Point2D.Double(3 * gapX, gapX / 2);
        }
        if (pto2 == null) {
            pto2 = new Point2D.Double(gapX / 2, 3 * gapX);
        }

        Line2D leg1 = new Line2D.Double(pointS, pto1);
        Line2D leg2 = new Line2D.Double(pointS, pto2);

        /// draw rays //////////////////////////////////////////////////////////////////////////////////////////////////

        g2d.setColor(red);
        if (handle1 == null) {
            handle1 = new Handle(pto1.x - 6, pto1.y - 6, 12, 12);
        }
        if (handle2 == null) {
            handle2 = new Handle(pto2.x - 6, pto2.y - 6, 12, 12);
        }

        g2d.draw(leg1);
        g2d.draw(leg2);
        g2d.fill(handle1);
        g2d.fill(handle2);

        /// draw S /////////////////////////////////////////////////////////////////////////////////////////////////////

        g2d.setColor(blue);

        Point2D.Double pS = getIntersectionPoint(leg1, leg2);
        g2d.fill(new Ellipse2D.Double(pS.x - shift, pS.y - shift, ps, ps));
        g2d.drawString("S", (int) (pS.x + ps), (int) (pS.y + ps));

        /// draw parallels /////////////////////////////////////////////////////////////////////////////////////////////
        g2d.setColor(blue);

        int sizeX = getWidth();
        int sizeY = getHeight();

        Line2D parallel1 = new Line2D.Double(0, sizeY - gapY, sizeX - gapX, 0);
        Line2D parallel2 = new Line2D.Double(gapX, sizeY, sizeX, gapY);

        if (drawParellels) {
            g2d.draw(parallel1);
            g2d.draw(parallel2);
        }

        Point2D.Double pA = getIntersectionPoint(leg1, parallel2);
        Point2D.Double pB = getIntersectionPoint(leg2, parallel2);

        Point2D.Double pC = getIntersectionPoint(leg1, parallel1);
        Point2D.Double pD = getIntersectionPoint(leg2, parallel1);

        g2d.setColor(green);
        g2d.draw(new Line2D.Double(pA, pB));
        g2d.draw(new Line2D.Double(pC, pD));

        g2d.setColor(blue);
        g2d.fill(new Ellipse2D.Double(pA.getX() - shift, pA.getY() - shift, ps, ps));
        g2d.drawString("A", (int) (pA.x + ps), (int) (pA.y + ps));

        g2d.fill(new Ellipse2D.Double(pB.getX() - shift, pB.getY() - shift, ps, ps));
        g2d.drawString("B", (int) (pB.x - 2 * ps), (int) (pB.y + 3.5 * ps));

        g2d.fill(new Ellipse2D.Double(pC.getX() - shift, pC.getY() - shift, ps, ps));
        g2d.drawString("C", (int) (pC.x + ps), (int) (pC.y + ps));

        g2d.fill(new Ellipse2D.Double(pD.getX() - shift, pD.getY() - shift, ps, ps));
        g2d.drawString("D", (int) (pD.x - 2 * ps), (int) (pD.y + 3.5 * ps));


        /// get the distances //////////////////////////////////////////////////////////////////////////////////////////

        g2d.setColor(Color.BLACK);

        Point2D.Double pSC = calculateMidpoint(pS, pC);
        ///g2d.fill(new Ellipse2D.Double(pSC.getX() - shift, pSC.getY() - shift, ps, ps));
        double distSC = calculateDistance(pS, pC);
        if (drawDist) {
            g2d.drawString(df.format(distSC), (int) (pSC.x + shift), (int) (pSC.y - shift));
        }

        double distSA = calculateDistance(pS, pA);
        double distSB = calculateDistance(pS, pB);

        Point2D.Double pSD = calculateMidpoint(pS, pD);
        ///g2d.fill(new Ellipse2D.Double(pSD.getX() - shift, pSD.getY() - shift, ps, ps));
        double distSD = calculateDistance(pS, pD);
        if (drawDist) {
            g2d.drawString(df.format(distSD), (int) (pSD.x + shift), (int) (pSD.y - shift));
        }

        Point2D.Double pBD = calculateMidpoint(pB, pD);
        ///g2d.fill(new Ellipse2D.Double(pBD.getX() - shift, pBD.getY() - shift, ps, ps));
        double distBD = calculateDistance(pB, pD);
        if (drawDist) {
            g2d.drawString(df.format(distBD), (int) (pBD.x + shift), (int) (pBD.y - shift));
        }

        Point2D.Double pAC = calculateMidpoint(pA, pC);
        ///g2d.fill(new Ellipse2D.Double(pAC.getX() - shift, pAC.getY() - shift, ps, ps));
        double distAC = calculateDistance(pA, pC);
        if (drawDist) {
            g2d.drawString(df.format(distAC), (int) (pAC.x + shift), (int) (pAC.y - shift));
        }

        Point2D.Double pAB = calculateMidpoint(pA, pB);
        ///g2d.fill(new Ellipse2D.Double(pAB.getX() - shift, pAB.getY() - shift, ps, ps));
        double distAB = calculateDistance(pA, pB);
        if (drawDist) {
            g2d.drawString(df.format(distAB), (int) (pAB.x + shift), (int) (pAB.y - shift));
        }

        Point2D.Double pCD = calculateMidpoint(pC, pD);
        ///g2d.fill(new Ellipse2D.Double(pCD.getX() - shift, pCD.getY() - shift, ps, ps));
        double distCD = calculateDistance(pC, pD);
        if (drawDist) {
            g2d.drawString(df.format(distCD), (int) (pCD.x + shift), (int) (pCD.y - shift));
        }

        /// calculate the rations //////////////////////////////////////////////////////////////////////////////////////

        double ratioSC_CD = distSC / distCD;
        double ratioSA_AB = distSA / distAB;

        double ratioSA_SC = distSA / distSC;
        double ratioAB_CD = distAB / distCD;

        double ratioSB_SD = distSB / distSD;

        String str1 = "SA/AB = " + df.format(ratioSA_AB) + "    SC/CD = " + df.format(ratioSC_CD);
        String str2 = "SA/SC = " + df.format(ratioSA_SC) + "    AB/CD = " + df.format(ratioAB_CD);
        String str3 = "SB/SD = " + df.format(ratioSA_SC) + "    AB/CD = " + df.format(ratioAB_CD);

        g2d.drawString(str1, 20, 40);
        g2d.drawString(str2, 20, 70);
        g2d.drawString(str3, 20, 100);
    }

    private double calculateDistance(Point2D.Double point1, Point2D.Double point2) {

        double deltaX = point2.getX() - point1.getX();
        double deltaY = point2.getY() - point1.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private Point2D.Double calculateMidpoint(Point2D.Double point1, Point2D.Double point2) {

        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new Point2D.Double(midX, midY);
    }

    private Point2D.Double getIntersectionPoint(Line2D line1, Line2D line2) {

        if (!line1.intersectsLine(line2)) {
            return null; // No intersection
        }

        double x1 = line1.getX1(), y1 = line1.getY1();
        double x2 = line1.getX2(), y2 = line1.getY2();
        double x3 = line2.getX1(), y3 = line2.getY1();
        double x4 = line2.getX2(), y4 = line2.getY2();

        double det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (det == 0) {
            return null; // Parallel lines, no intersection
        }

        double px = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / det;
        double py = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / det;

        return new Point2D.Double(px, py);
    }

    public static void main(String[] args) {

        InterceptTheorem rays = new InterceptTheorem();
        JFrame f = new JFrame();
        f.add(rays);
        f.setTitle("Strahlensatz - Intercept Theorem");
        f.setLocation(300, 0);
        f.setSize((int) (1.6*size), (int) (1.0*size));
        f.setVisible(true);
    }
}

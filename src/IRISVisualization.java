import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

public class IRISVisualization extends JFrame {

    private Handle mBC = null;
    private Handle mBA = null;
    private Handle mAC = null;

    private Handle exBC = null;
    private Handle exAB = null;
    private Handle exCB = null;
    private Handle exAC = null;
    private Handle exCA = null;
    private Handle exBA = null;

    private Color grey = new Color(220, 220, 220);
    private Color red = new Color(180, 0, 0);
    private Color blue = new Color(0, 0, 80);
    private Color green = new Color(80, 140, 0);
    private Handle handleA = null;
    private Handle handleB = null;
    private Handle handleC = null;
    private Handle mCA_BA;
    private Handle mCB_AB;
    private Handle mBC_AC;

    public IRISVisualization() {

        super("Conway's IRIS Visualization");
        setSize(760, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        double s = 10;
        double x = 80;
        double y = 20;
        handleA = new Handle(360 + x, 280 - y, s, "A");
        handleB = new Handle(220 + x, 480 - y, s, "B");
        handleC = new Handle(390 + x, 540 - y, s, "C");

        exBA = calculateExtendedPoint(handleB, handleC, handleA);
        exCA = calculateExtendedPoint(handleC, handleB, handleA);

        exBC = calculateExtendedPoint(handleB, handleA, handleC);
        exAC = calculateExtendedPoint(handleA, handleB, handleC);

        exAB = calculateExtendedPoint(handleA, handleC, handleB);
        exCB = calculateExtendedPoint(handleC, handleA, handleB);

        mCA_BA = calculateMidpoint(exCA, exBA);
        mBC_AC = calculateMidpoint(exAC, exBC);
        mCB_AB = calculateMidpoint(exCB, exAB);

        mBC = calculateMidpoint(handleB, handleC);
        mBA = calculateMidpoint(handleB, handleA);
        mAC = calculateMidpoint(handleA, handleC);

        keyAndMouseInit();
    }

    private void keyAndMouseInit() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                }
                repaint();
            }
        });
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
        return new Handle(three.getX() - vector13Scaled_x, three.getY() - vector13Scaled_y, 10, name);
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        g2d.setStroke(new BasicStroke(3));

        g2d.setColor(blue);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, handleB.x, handleB.y));
        g2d.setColor(green);
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, handleC.x, handleC.y));
        g2d.setColor(red);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, handleA.x, handleA.y));

        g2d.setColor(green);
        exBA.fill(g2d);
        exCA.fill(g2d);
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, exBA.x, exBA.y));
        g2d.draw(new Line2D.Double(handleA.x, handleA.y, exCA.x, exCA.y));
        g2d.setColor(blue);
        exBC.fill(g2d);
        exAC.fill(g2d);
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, exBC.x, exBC.y));
        g2d.draw(new Line2D.Double(handleC.x, handleC.y, exAC.x, exAC.y));
        g2d.setColor(red);
        exAB.fill(g2d);
        exCB.fill(g2d);
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, exAB.x, exAB.y));
        g2d.draw(new Line2D.Double(handleB.x, handleB.y, exCB.x, exCB.y));


        g2d.setColor(Color.darkGray);
        handleA.fill(g2d);
        handleB.fill(g2d);
        handleC.fill(g2d);

        g2d.setColor(Color.lightGray);
        g2d.setStroke(new BasicStroke(1));

        drawHandleConnector(g2d, exCA, exBA);
        drawHandleConnector(g2d, exBC, exAC);
        drawHandleConnector(g2d, exCB, exAB);

        mCA_BA.fill(g2d);
        mBC_AC.fill(g2d);
        mCB_AB.fill(g2d);

        g2d.setColor(Color.RED);

        Handle vCA_BA = getVector(exCA, exBA);
//        vCA_BA = vCA_BA.flip();
        mCA_BA.add(vCA_BA).fill(g2d);
        drawHandleConnector(g2d, mCA_BA, mCA_BA.add(vCA_BA));

        Handle vBC_AC = getVector(exBC, exAC);
        mBC_AC.add(vBC_AC).fill(g2d);
        drawHandleConnector(g2d, mBC_AC, mBC_AC.add(vBC_AC));

        Handle vCB_AB = getVector(exCB, exAB);
        mCB_AB.add(vCB_AB).fill(g2d);
        drawHandleConnector(g2d, mCB_AB, mCB_AB.add(vCB_AB));

//        g2d.setColor(Color.RED);
//
//        Handle rayA = getVector(mCB_AB, mCB_AB.add(vCB_AB)).flip();
//        rayA = mCB_AB.add(rayA);
//        rayA.fill(g2d);

    }

    private Handle getVector(Handle h1, Handle h2) {

        return new Handle(h1.x - h2.x, h1.y - h2.y, 2, "");

    }

    private Handle calculateMidpoint(Handle point1, Handle point2) {

        double midX = (point1.getX() + point2.getX()) / 2;
        double midY = (point1.getY() + point2.getY()) / 2;
        return new Handle(midX, midY, 6, "");
    }

    private void drawHandleConnector(Graphics2D g2d, Handle h1, Handle h2) {

        g2d.draw(new Line2D.Double(h1.x, h1.y, h2.x, h2.y));
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            IRISVisualization iris = new IRISVisualization();
            iris.setVisible(true);
        });
    }
}

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TheGame extends JFrame implements KeyListener {

    private final Timer timer;
    private BufferedImage rocket = null;
    private int myDelay = 50;
    private int x = 0;
    private int y = 100;
    private final int diameter = 200; // Durchmesser des Kreises
    private int xRect = 10;
    private int yRect = 10;

    public TheGame() {

        setTitle("The Game");
        setSize(800, 500);
        setLocation(200, 200);

        try {
            rocket = ImageIO.read(new File("C:\\Users\\GTA01\\IdeaProjects\\Demo01\\SpaceShuttle.png"));
        } catch (IOException e) {
            System.out.println(e);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addKeyListener(this);

        timer = new Timer(myDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveObject();
                repaint();
            }
        });

        timer.start();
    }

    private void moveObject() {
        if (x > 0) {
            x -= 5;
        } else {
            x = getWidth();
        }
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        //g2d.setRenderingHints();

        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));

        g2d.drawImage(rocket, x,y, getWidth()/10, getHeight()/10, this);

        g2d.setColor(new Color(0, 0, 80));
        g2d.fillRect(xRect, yRect, 20, 100);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TheGame().setVisible(true);
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                yRect -= 10;
                break;
            case KeyEvent.VK_DOWN:
                yRect += 10;
                break;
        }

        /*
        myDelay -= 5;
        if (myDelay < 50) {
            myDelay = 50;
        }
        timer.setDelay(myDelay);

         */
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}

import java.awt.*;

public class Helper {

    public static void drawHelpPage(Graphics2D g2d) {

        g2d.setColor(Color.lightGray);
        g2d.fillRect(0, 0, 1600, 1600);

        int fs = 26;
//        Font font = new Font("SansSerif", Font.PLAIN, fs);
        Font font = new Font("Arial", Font.PLAIN, fs);
        g2d.setFont(font);
        g2d.setColor(Color.DARK_GRAY);

        int xPos = 20;
        int dy = (int) (fs * 1.5);
        int yPos = dy;
        int tab = 350;

        g2d.drawString("Arrow up / down", xPos, yPos);
        g2d.drawString("move triangle up and down", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Arrow left / right", xPos, yPos);
        g2d.drawString("Move triangle left and right", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Space bar", xPos, yPos);
        g2d.drawString(" ", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Numbers 0 - 9", xPos, yPos);
        g2d.drawString("", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("A", xPos, yPos);
        g2d.drawString("Toggle annotations", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("B", xPos, yPos);
        g2d.drawString("Toggle black mode", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("C", xPos, yPos);
        g2d.drawString("Toggle circle", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("H", xPos, yPos);
        g2d.drawString("Toggle this help page", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("I", xPos, yPos);
        g2d.drawString("Toggle iris", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("P", xPos, yPos);
        g2d.drawString("Toggle iris picture", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("R", xPos, yPos);
        g2d.drawString("Reste position", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("T", xPos, yPos);
        g2d.drawString("Toggle triangle", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("W", xPos, yPos);
        g2d.drawString("Toggle whiskers", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Cmd W", xPos, yPos);
        g2d.drawString("Quit the program", xPos + tab, yPos);
    }
}


import java.awt.*;

public class HelpPage {

    public static void drawHelpPage(Graphics2D g2d) {

        g2d.setColor(Color.lightGray);
        g2d.fillRect(0, 0, 1600, 1600);

        int fs = 26;
        Font font = new Font("Raleway", Font.PLAIN, fs);
        g2d.setFont(font);
        g2d.setColor(Color.DARK_GRAY);

        int xPos = 20;
        int dy = (int) (fs * 1.5);
        int yPos = dy;
        int tab = 350;

        g2d.drawString("Arrow up / down", xPos, yPos);
        g2d.drawString("move bounding box up and down", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Arrow left / right", xPos, yPos);
        g2d.drawString("Move bounding box left and right", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("+ / -", xPos, yPos);
        g2d.drawString("Grow / shrink whiskers | extends", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("A", xPos, yPos);
        g2d.drawString("Toggle annotations", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("0", xPos, yPos);
        g2d.drawString("Reset", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("B", xPos, yPos);
        g2d.drawString("Toggle bounding box whiper curve", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Command B", xPos, yPos);
        g2d.drawString("Toggle black mode", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("C", xPos, yPos);
        g2d.drawString("Toggle circle", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("D", xPos, yPos);
        g2d.drawString("Toggle debug mode", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("E", xPos, yPos);
        g2d.drawString("Toggle whiskers | extends", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("H", xPos, yPos);
        g2d.drawString("Toggle this help page", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("I", xPos, yPos);
        g2d.drawString("Toggle iris circle", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("P", xPos, yPos);
        g2d.drawString("Toggle iris picture", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("R", xPos, yPos);
        g2d.drawString("Rotate bounding box 1° clockwise", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Shift R", xPos, yPos);
        g2d.drawString("Rotate bounding box 1° counterclockwise", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("T", xPos, yPos);
        g2d.drawString("Toggle triangle", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("W", xPos, yPos);
        g2d.drawString("Toggle whiper curve", xPos + tab, yPos);
        yPos += dy;

        g2d.drawString("Command W", xPos, yPos);
        g2d.drawString("Quit the program", xPos + tab, yPos);
        yPos += dy;

        /// Credits
        yPos += dy;
        g2d.drawString("Inspiration by Mathologer", xPos, yPos);
        g2d.drawString("youtu.be/XrTZwPD4O3k?si=KLvk4zhPGRQI_dPi", xPos + tab, yPos);
    }
}


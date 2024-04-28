import java.awt.*;

public class ColorSpectrum {

    public static Color getColorSpectrum(double value, double min, double max) {

        Color startColor = Color.GREEN;
        Color endColor = Color.RED;

        // Clamp the value to the range [min, max]
        value = Math.max(min, Math.min(max, value));

        // Calculate the interpolation factor (between 0 and 1)
        double factor = (value - min) / (max - min);

        // Interpolate between the start and end colors
        int red = (int) (startColor.getRed() + factor * (endColor.getRed() - startColor.getRed()));
        int green = (int) (startColor.getGreen() + factor * (endColor.getGreen() - startColor.getGreen()));
        int blue = (int) (startColor.getBlue() + factor * (endColor.getBlue() - startColor.getBlue()));

        // Create and return the interpolated color
        return new Color(red, green, blue);
    }
}

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BBPAlgorithmMRA {

    private static boolean verbose = false;

    // Berechnung der n-ten Nachkommastelle von Pi mit dem BBP-Algorithmus
    public static int calculateNthHexDigitOfPi(int n) {
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal term;

        n = 3;
        for (int k = 0; k <= n; k++) {

//            if (k % 1000 == 0) {
//                System.out.println("k: " + k);
//            }

            BigDecimal numerator = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(8 * k + 1), n + 1, RoundingMode.HALF_EVEN)

                    .subtract(BigDecimal.valueOf(2).divide(BigDecimal.valueOf(8 * k + 4), n + 1, RoundingMode.HALF_EVEN))
                    .subtract(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(8 * k + 5), n + 1, RoundingMode.HALF_EVEN))
                    .subtract(BigDecimal.valueOf(1).divide(BigDecimal.valueOf(8 * k + 6), n + 1, RoundingMode.HALF_EVEN));

            term = numerator.divide(BigDecimal.valueOf(16).pow(k), n + 1, RoundingMode.HALF_EVEN);
            sum = sum.add(term);

            System.out.println("term: " + term + " sum: " + sum + "16: " +BigDecimal.valueOf(16).pow(k));
        }

        verbose = true;
        if (verbose) {
            System.out.println("sum          " + sum);
        }

        BigDecimal scaledPi = sum.multiply(BigDecimal.valueOf(10).pow(n)); // Skaliere Pi, um die n-te Stelle zu erhalten

        if (verbose) {
            System.out.println("scaledPi     " + scaledPi);
        }

        BigDecimal intPart = scaledPi.setScale(0, BigDecimal.ROUND_DOWN); // Extrahiere die Ganzzahl

        BigDecimal fractionPart = scaledPi.subtract(intPart); // Extrahiere den Nachkommateil

        if (verbose) {
            System.out.println("fractionPart " + fractionPart);
        }

        fractionPart = fractionPart.multiply(BigDecimal.valueOf(10));

        if (verbose) {
            System.out.println("fractionPart " + fractionPart);
        }

        fractionPart = fractionPart.setScale(0, BigDecimal.ROUND_DOWN);

        if (verbose) {
            System.out.println("fractionPart " + fractionPart);
        }

        int digit = fractionPart.intValue(); // Gib den Nachkommateil als Integer zurück
        return digit;
    }

    public static void main(String[] args) {

        int digit = calculateNthHexDigitOfPi(4);
        System.out.println("digit: " + digit);

//        System.out.println("Let the play begin ...");
//        System.out.print("3.");
//        for (int k = 0; k < 10; k++) {
//            int digit = calculateNthHexDigitOfPi(k);
//            System.out.print(digit);
//        }
//        System.out.println("Game over ...");
    }
}

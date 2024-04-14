import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class BBPAlgorithm {

    private String asciiPi = "";

    public BBPAlgorithm() {

        doCalculation();
    }

    private void readPiFromFile() {

        String filePath = "/Users/malvers/IdeaProjects/CGTA_ESZ/src/bigPi.ascii";

        try (FileReader fileReader = new FileReader(filePath);

             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                asciiPi += line;
            }
            asciiPi = asciiPi.replace(" ", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doCalculation() {

        readPiFromFile();

        int digits = 10000;

        digits += 1; // 3.

        String calculatedPi = "";
        BigDecimal pi = BigDecimal.ZERO;

        boolean verbose = false;

        if (digits <= 100) {
            verbose = true;
        }

        System.out.println("length asciiPi: " + asciiPi.length());

        for (int i = 0; i < digits; i++) {

            if (i % 1000 == 0) {
                System.out.println("i: " + i);
            }

            BigDecimal a = BigDecimal.ONE.divide(BigDecimal.valueOf(16).pow(i), digits, HALF_UP);

            BigDecimal b1 = BigDecimal.valueOf(4).divide(BigDecimal.valueOf(8 * i + 1), digits + 1, HALF_UP);
            BigDecimal b2 = BigDecimal.valueOf(2).divide(BigDecimal.valueOf(8 * i + 4), digits + 1, HALF_UP);
            BigDecimal b3 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * i + 5), digits + 1, HALF_UP);
            BigDecimal b4 = BigDecimal.ONE.divide(BigDecimal.valueOf(8 * i + 6), digits + 1, HALF_UP);

            pi = pi.add(a.multiply(b1.subtract(b2).subtract(b3).subtract(b4)));

            calculatedPi = pi.toString().substring(0, digits);

        }

        int errors = calculatedPi.substring(0, digits).compareTo(asciiPi.substring(0, digits));

        if (verbose) {
            System.out.println("");
            System.out.println("calculated pi  " + calculatedPi);
            System.out.println("ascii pi       " + asciiPi.substring(0, digits));
        }

        System.out.println("digits: " + digits + " -> " + errors + " errors");
        BigDecimal piBig = new BigDecimal(calculatedPi);
        System.out.println("BigDecimal pi  " + piBig);
    }

    public static void main(String[] args) {

        new BBPAlgorithm();
    }
}


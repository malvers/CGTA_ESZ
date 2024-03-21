

public class TickerTape {

    public static void main(String[] args) {

        /// capacity in GB
        double capacity = 1024;

        double centimeterPerByte = 0.25;
        double metersPerByte = centimeterPerByte / 100;

        ///                                            in KB  in MB  in GB
        double lengthTickerTapeMeter = metersPerByte * 1000 * 1000 * 1000 * capacity;

        double lengthTickerTapeKilometer = lengthTickerTapeMeter / 1000;

        double widthTickerTapeCentimeter = 2.5;

        double lengthMeter = lengthTickerTapeKilometer * 1000;

        double widthMeter = widthTickerTapeCentimeter / 100;

        double areaTickerTapeSquareMeter = lengthMeter * widthMeter;

        /// taken from wikipedia
        double areaA4PaperSquareCentimeter = 21 * 29.7;
        double areaA4PaperSquareMeter = areaA4PaperSquareCentimeter / 10000;

        double howManyTimes = areaTickerTapeSquareMeter / areaA4PaperSquareMeter;

        /// 500 pages 80g/qm are 5.2 cm high
        double packageHeight = 5.2;
        double thicknessPaperCentimeter = packageHeight / 500;

        double thicknessPaperMeter = thicknessPaperCentimeter / 100;

        double towerHeightMeter = thicknessPaperMeter * howManyTimes;

        double towerHeightCentimeter = towerHeightMeter * 100;

        double towerHeightKilometer = towerHeightMeter / 1000;

        double howManyPackages = towerHeightCentimeter / packageHeight;

        /// cheapest on amazon
        double pricePerPackage = 4.95;

        double totalCost = howManyPackages * pricePerPackage;

        System.out.println("Length ticker tape: " + lengthTickerTapeMeter + " m");
        System.out.println("Width ticker tape:  " + widthMeter + " m");
        System.out.println("Area ticker tape:   " + areaTickerTapeSquareMeter + " qm");
        System.out.println("Area A4 paper:      " + areaA4PaperSquareMeter + " qm");
        System.out.printf("How many times:     "  + "%.0f%n", howManyTimes);
        System.out.println("Tower height:       " + towerHeightCentimeter + " cm");
        System.out.println("Tower height:       " + towerHeightMeter + " m");
        System.out.println("Tower height:       " + towerHeightKilometer + " km");
        System.out.println("How many packages:  " + howManyPackages);
        System.out.printf("Total cost:         "  + "%.0f%n €", totalCost);
    }
}

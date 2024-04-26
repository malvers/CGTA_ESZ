import fr.inria.optimization.cmaes.CMAEvolutionStrategy;
import fr.inria.optimization.cmaes.fitness.IObjectiveFunction;

class Rosenbrock implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible

    public double valueOf(double[] x) {

        double res = 0;
        for (int i = 0; i < x.length - 1; ++i)
            res += 100 * (x[i] * x[i] - x[i + 1]) * (x[i] * x[i] - x[i + 1]) + (x[i] - 1.) * (x[i] - 1.);
        return res;
    }

    public boolean isFeasible(double[] x) {
        return true;
    } // entire R^n is feasible
}

class MiTest implements IObjectiveFunction { // meaning implements methods valueOf and isFeasible

    public double valueOf(double[] data) {

        double res = 0;
        for (int i = 0; i < data.length - 1; ++i)
            res += 100 * (data[i] * data[i]);
        return res;
    }

    public boolean isFeasible(double[] x) {
        return true;
    } // entire R^n is feasible
}

public class CMAExample {

    public static void main(String[] args) {

        IObjectiveFunction fitFun = new MiTest();

        // new a CMA-ES and set some initial values
        CMAEvolutionStrategy cmaes = new CMAEvolutionStrategy();
        cmaes.readProperties();                         // read options, see file CMAEvolutionStrategy.properties
        cmaes.setDimension(20);                         // overwrite some loaded properties
        cmaes.setInitialX(0.5);                         // in each dimension, also setTypicalX can be used
        cmaes.setInitialStandardDeviation(0.2);         // also a mandatory setting
        cmaes.options.stopFitness = 1e-15;              // optional setting

        // initialize cma and get fitness array to fill in later
        double[] fitness = cmaes.init();                // new double[cma.parameters.getPopulationSize()];

        // initial outputs to files
        //cma.writeToDefaultFilesHeaders(0);          // 0 == overwrites old files

        // iteration loop
        while (cmaes.stopConditions.getNumber() == 0) {

            // core iteration step
            double[][] pop = cmaes.samplePopulation();  // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                while (!fitFun.isFeasible(pop[i]))    // test whether solution is feasible,
                    pop[i] = cmaes.resampleSingle(i);   // re-sample solution until it is feasible
                fitness[i] = fitFun.valueOf(pop[i]);  // compute fitness value, where fitFun
            }                                         // is the function to be minimized
            cmaes.updateDistribution(fitness);          // pass fitness array to update search distribution

            // output to console and files
            cmaes.writeToDefaultFiles();
            int outModulo = 150;
            if (cmaes.getCountIter() % (15 * outModulo) == 1) {
                cmaes.printlnAnnotation();              // might write file as well
            }
            if (cmaes.getCountIter() % outModulo == 1) {
                cmaes.println();
            }
        }

        // evaluate mean value as it is the best estimator for the optimum
        cmaes.setFitnessOfMeanX(fitFun.valueOf(cmaes.getMeanX())); // updates the best ever solution

        // final output
        //cma.writeToDefaultFiles(1);
        cmaes.println();
        cmaes.println("Terminated due to");

        for (String s : cmaes.stopConditions.getMessages()) cmaes.println("  " + s);
            cmaes.println("best function value " + cmaes.getBestFunctionValue() + " at evaluation " + cmaes.getBestEvaluationNumber());

        double[] best = cmaes.getBestX();
        for (int i = 0; i < cmaes.getBestX().length; i++)
            cmaes.println(i + " v: " + best[i]);
    }
}

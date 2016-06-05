package distributor;

import java.util.Random;

public class PoissonDistributor extends AbsDistributor{
	
	private double lamda;
	private Random rand;

	public PoissonDistributor(String fileName, int tickTime) {
		// TODO Auto-generated constructor stub
		super(fileName, tickTime);
		lamda = tickTime/3;
		rand = new Random();
		for (int i=0; i<mServiceSize; ++i) {
			int poissonNum = getPoissonRandom(lamda);
			while (poissonNum >= tickTime) {
				poissonNum = getPoissonRandom(lamda);
			}
			mDistribution.add(poissonNum);
		}
	}
	
	private int getPoissonRandom(double mean) {
	    double L = Math.exp(-mean);
	    int k = 0;
	    double p = 1.0;
	    do {
	        p = p * rand.nextDouble();
	        k++;
	    } while (p > L);
	    return k - 1;
	}

}

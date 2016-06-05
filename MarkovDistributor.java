package distributor;

public class MarkovDistributor extends AbsDistributor {
	public MarkovDistributor (String fileName, int tickTime){
		super(fileName, tickTime);
		int[] temp = {10,7,3,4,6,5,3,2, 6, 8};
		double[][] p = new double[mServiceSize][mServiceSize];
		for(int i=0; i<mServiceSize;i++){
			for(int j=0; j<mServiceSize; j++){
				p[i][(j + i) % mServiceSize]=temp[j];
			}
		}
		
		double[] rank = new double[mServiceSize];
		double[] save = new double[mServiceSize];
		for(int l=0; l<mServiceSize; l++){
			rank[l] = 1.0;
		}
		for (int t = 0; t < mServiceSize; t++) {
			double[] newRank = new double[mServiceSize];
			newRank[t] = 0.0;
			for (int j = 0; j < mServiceSize; j++) {
					for (int k = 0; k < mServiceSize; k++)
						 newRank[j] += rank[k]*p[k][j];
			}
			rank = newRank;
			save[t] = rank[t];
			//mDistribution.add((int)rank[t]);
		}
		for (int t = 0; t < mServiceSize; t++) {
			System.out.println((int)(save[t] * (tickTime/save[mServiceSize - 1])));
			
			mDistribution.add((int)(save[t] * (tickTime/save[mServiceSize - 1])));
		}
	}
}
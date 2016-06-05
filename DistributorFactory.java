package distributor;

public class DistributorFactory {
	public Distributor createDistributor(String fileName, int type, int totalTicks) {
		Distributor d = null;
		switch (type) {
			case 0: //MARKOVIAN:
				d = new MarkovDistributor(fileName, totalTicks);
				break;
			case 1: //POSSION:
				d = new PoissonDistributor(fileName, totalTicks);
				break;
			case 2: //CLUSTERED:
			default:
				d = new ClusterDistributor(fileName, totalTicks);
				break;
		}
		return d;
	}
}

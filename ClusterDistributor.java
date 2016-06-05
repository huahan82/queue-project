package distributor;

public class ClusterDistributor extends AbsDistributor{

	private int clusterNum = 6;

	public ClusterDistributor(String fileName, int tick) {
		// TODO Auto-generated constructor stub
		super(fileName, tick);
		int tickPerCluster = tick/clusterNum;
		int servicePerCluster = mServiceSize/clusterNum;
		int serviceAdded = 0;
		int tickTime = 0;
		int clusterTick = 0;
		int clusterService = 0;
		while (serviceAdded < mServiceSize) {
			if (clusterService <= servicePerCluster ) {
				mDistribution.add(tickTime);
				serviceAdded++;
				clusterService++;
			}
			clusterTick++;
			tickTime++;
			if (clusterTick >tickPerCluster) {
				clusterTick = 0;
				clusterService =0;
			}
		}
	}
	
}

package distributor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import queueNode.*;

public abstract class AbsDistributor implements Distributor{
	protected AbsDistributor(String fileName, int tick) {
		mSimuTicks = tick;
		if (!fileName.isEmpty()) {
			try {
				readFile(fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			//no file mode, we can generate the serviceArray on fly
		}
	}
	//read input file from given filePath 
	protected void readFile(String fileName) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		mServiceArray = new LinkedList<String>();
		try {
			String line = br.readLine();
			while (line != null) {
	        	mServiceArray.addLast(line);
	        	mServiceSize++;
	        	line = br.readLine();
	        }
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	protected QueueNode createQueueNode(String serviceInfo) {
		String[] details = (serviceInfo).split("\\|");

		if (details.length == 3) {
			int serviceTime = Integer.parseInt(details[0]);
			return new QueueNode(serviceTime, details[1], details[2]);
		} else {
			return null; 
		}
	}

	@Override
	public QueueNode nextNode(int currenttime) {
		if (mDistribution.peek()!=null && mDistribution.peek().intValue() <= currenttime) {
			mDistribution.poll();
			String serviceInfo = mServiceArray.pollFirst();
			QueueNode node = createQueueNode(serviceInfo);
			return node;
		} else {
			return null;
		}
	}
	public ArrayList<QueueNode> nextNodes(int currenttime) {
		return null;
	}
	//simulated service list read from file, containing service time; service name; queue name
	protected LinkedList<String> mServiceArray = null;
	protected int mServiceSize = 0;
	protected int mSimuTicks = 0;
	//distribution: key is tick time; value is number of events
	//protected HashMap<Integer, Integer> distribution = new HashMap<Integer, Integer>();
	protected PriorityQueue<Integer> mDistribution = new PriorityQueue<Integer>();
}

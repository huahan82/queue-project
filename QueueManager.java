package manager;

import java.util.Scanner;
import queueNode.*;
import distributor.*;

public class QueueManager {
	//Queue Types
	private final int FIFO = 0;
	private final int LIFO = 1;
	private final int PRIORITY = 2;
	//Arrival Distribution
	private final int MARKOVIAN = 0;
	private final int POISSON = 1;
	private final int CLUSTERED = 2;
	//Dispatch Types
	private final int RANDOM = 0;
	private final int SHORTEST = 1;
	private final int SHORTBALANCE = 2;
	//Limited Service Time Behavior
	private final int NOLIMIT = 0;
	private final int DISCARD = 1;
	private final int REQUEUE = 2;
	private final int REQUEUENEW = 3;
	
	private DistributorFactory df = null;
	private Distributor ad = null;
	private QueueNode nextNode = null;
	private QueueNode done = null;
	private QueueNode[] serviceStation;
	private QueueNode[] queueLine;
	private String[] qlNames;
	private String[] ssNames;
	private int[] servTrack;
	private int doneCount = 0;
	private int runTime = 0;
	private int curTime = 0;
	private Scanner in = new Scanner(System.in);
	private String input;
	
	//Settings
	private int serviceCount;
	private int queueCount;
	private int queueType;
	private int arrivalType;
	private int dispatchType;
	private int queueMax;
	private int serviceLimit;
	private int servLimType;
	private String filepath;
	
	public QueueManager(int sc, int qc, int qt, int at, int dt, int qm, int sl, int slt, String fp){
		//Saving settings
		serviceCount = sc;
		queueCount = qc;
		queueType = qt;
		arrivalType = at;
		dispatchType = dt;
		queueMax = qm;
		serviceLimit = sl;
		servLimType = slt;
		filepath = fp;
		
		//Setting all counts to zero
		reset();
		
		ssNames = new String[sc];
		qlNames = new String[qc];
		
		//Naming all queues and station
		for(int s = 0; s < serviceCount; s++){
			System.out.println("Please insert service station name " + s + ". For no name, input ''");
			input = in.nextLine();
			if(input.equals("''")){
				input = "";
			}
			ssNames[s] = input;
		}
		
		for(int q = 0; q < queueCount; q++){
			System.out.println("Please insert queue name " + q + ". For no name, input ''");
			input = in.nextLine();
			if(input.equals("''")){
				input = "";
			}
			qlNames[q] = input;
		}
		
	}

	public void reset(){
		doneCount = 0;
		curTime = 0;
		serviceStation = new QueueNode[serviceCount];
		queueLine = new QueueNode[queueCount];
		servTrack = new int[serviceCount];
		done = null;
		ad = null;
	}
	
	//Need to initialize distributor here due to needing run time
	public void run(int rt){
		df = new DistributorFactory();
		ad = df.createDistributor(filepath, arrivalType, rt);
		
		runTime = rt;
		while(curTime < runTime){
			tick(rt);
		}
	}
	
	public void tick(int rt){
		int waitTime;
		QueueNode cur;
		
		if(ad == null){
			df = new DistributorFactory();
			ad = df.createDistributor(filepath, arrivalType, rt);
		}
		if(runTime == 0)runTime = rt;
		//if not holding a node to add, get next node
		if (nextNode == null){
			nextNode = ad.nextNode(curTime);
		}
		
		//if holding a node, try adding it to queue
		if (nextNode != (QueueNode)null) {
			if(enqueue(nextNode)){
				nextNode = null;
			}
		}
		
		//if nodes can balance,check for balance
		if (dispatchType == SHORTBALANCE) balance();
		
		//For all service stations, if empty, look for new node from queues and record wait time
		for(int s = 0; s < serviceCount; s++){
			if(queueLength(serviceStation[s]) == 0){
				if (dequeue(s)){
					waitTime = curTime - serviceStation[s].getStart();
					serviceStation[s].setWaitTime(waitTime);
					serviceStation[s].setNext(null);
				}
			}
		}
		
		//Decrease remaining service time
		for(int s = 0; s < serviceCount; s++){
			if(queueLength(serviceStation[s]) > 0){
				serviceStation[s].decService();
				if(serviceLimit != 0) servTrack[s]++;
			}
		}
		
		//For all service stations, if occupying service station node is complete
		//remove and record
		//if not done, but limit on servce time, check
		for(int s = 0; s < serviceCount; s++){
			if(queueLength(serviceStation[s]) != 0){
				if(serviceStation[s].getServiceTime() <= 0){
					doneCount += 1;
					if(done == (QueueNode)null){
						done = serviceStation[s];
					}
					else{
						cur = done;
						while(cur.getNext() != (QueueNode)null){
							cur = cur.getNext();
						}
						cur.setNext(serviceStation[s]);
					}
					serviceStation[s] = null;
					servTrack[s] = 0;
				}
				else if(serviceLimit != 0){ //add to done
					if(servTrack[s] >= serviceLimit){
						if(servLimType == DISCARD){
							doneCount += 1;
							if(done == (QueueNode)null){
								done = serviceStation[s];
							}
							else{
								cur = done;
								while(cur.getNext() != (QueueNode)null){
									cur = cur.getNext();
								}
								cur.setNext(serviceStation[s]);
							}
						}
						else if(servLimType == REQUEUE){
							cur = serviceStation[s];
							cur.setNext(null);
							if(queueType == PRIORITY)cur.setPriority(cur.getPriority() + 1);
							if(queueLine[cur.getQueue()] != (QueueNode)null){
								addNode(cur.getQueue(), cur);	
							}
							else{
								queueLine[cur.getQueue()] = cur;
							}
						}
						else if(servLimType == REQUEUENEW){
							cur = serviceStation[s];
							cur.setNext(null);
							if(queueType == PRIORITY)cur.setPriority(cur.getPriority() + 1);
							enqueue(cur);
						}
						serviceStation[s] = null;
						servTrack[s] = 0;
					}
				}
			}
		}
		
		
		curTime++;
	}
	
	//pick queue and add
	private boolean enqueue(QueueNode newNode){
		int queueNum = 0;
		int shortest = -1;
		int length;
		boolean queued = false;
		
		//check to see if open lane with matching name
		for(int q = 0; q < queueCount; q++){
			length = queueLength(queueLine[q]);
			if( ((length < queueMax) || (queueMax == 0)) &&
				((qlNames[q].equals(newNode.getQueueName())) || (qlNames[q].equals("")))){
				queued = true;
				if (length < shortest || shortest == -1){
					shortest = length;
					queueNum = q;
				}
			}
		}
		if(!queued) return false;
		
		queued = false;
		
		if(dispatchType == RANDOM){//Random
			while(!queued){
				queueNum = (int) (Math.random()*queueCount);
				length = queueLength(queueLine[queueNum]);
				if( ((length < queueMax) || (queueMax == 0)) &&
					((qlNames[queueNum].equals(newNode.getQueueName())) || (qlNames[queueNum].equals("")))){
					if(queueLine[queueNum] != (QueueNode)null){
						addNode(queueNum, newNode);	
					}
					else{
						queueLine[queueNum] = newNode;
						queueLine[queueNum].setStart(curTime);
						queueLine[queueNum].setQueue(queueNum);
					}
					queued = true;
				}
			}
		}
		else{//Shortest
			if(queueLength(queueLine[queueNum]) > 0){
				addNode(queueNum, newNode);
			}
			else{
				queueLine[queueNum] = newNode;
				queueLine[queueNum].setStart(curTime);
				queueLine[queueNum].setQueue(queueNum);
			}	
		}
		return true;
	}

	private void addNode(int queueNum, QueueNode n){
		QueueNode cur = queueLine[queueNum];
		n.setQueue(queueNum);
		n.setStart(curTime);
		if(queueType == PRIORITY) n.setPriority((int)(Math.random()*11));
		while(cur.getNext() != (QueueNode)null){
			cur = cur.getNext();
		}
		cur.setNext(n);
	}
	
	//Move from queue to station
	private boolean dequeue(int station){
		boolean remove = false;
		int queueNum;
		QueueNode nextToService = null;
		//If no possible nodes, return false
		for(int q = 0; q < queueCount; q++){
			if(queueLength(queueLine[q]) > 0 && qlNames[q].equals(ssNames[station])){
				remove = true;
				q = queueCount;
			}
		}
		
		if(!remove) return false;
		
		//If same number of service stations and queues and matching names, grab from here first
		if(queueCount == serviceCount && qlNames[station].equals(ssNames[station])){
			if(queueLength(queueLine[station]) > 0) nextToService = removeNode(station);
		}
		
		//If not true, or line was empty, pick a random line for next node
		while(nextToService == (QueueNode)null){
			queueNum = (int) (Math.random()*queueCount);
			if(queueLine[queueNum] != (QueueNode)null && qlNames[queueNum].equals(ssNames[station])){
				nextToService = removeNode(queueNum);
			}
		}
		
		serviceStation[station] = nextToService;
		return true;
	}
	
	private QueueNode removeNode(int queueNum){
		QueueNode remove = queueLine[queueNum];
		QueueNode removeParent = null;
		int p = 0; //highest priority
		int n = 0; //num of node with highest priority
		int c = 0; //current count
		if (queueType == FIFO){
			queueLine[queueNum] = queueLine[queueNum].getNext();
		}
		else if(queueType == LIFO){
			while(remove.getNext() != (QueueNode)null){ //If at the end, set the parent's next to null
				removeParent = remove;
				remove = remove.getNext();
			}
			if(removeParent != (QueueNode)null){
				removeParent.setNext(null);
			}
			else{
				queueLine[queueNum] = null;
			}
		}
		else if(queueType == PRIORITY){
			p = remove.getPriority();
			while(remove.getNext() != (QueueNode)null){ //If at the end, set the parent's next to null
				removeParent = remove;
				remove = remove.getNext();
				c++;
				if(p < remove.getPriority()){
					p = remove.getPriority();
					n = c;
				}
			}
			c = 0;
			remove = queueLine[queueNum];
			removeParent = null;
			while (c < n){
				removeParent = remove;
				remove = remove.getNext();
				c++;
			}
			
			if(removeParent != (QueueNode)null){
				removeParent.setNext(remove.getNext());
			}
			else{
				queueLine[queueNum] = remove.getNext();
			}
		}
		return remove;
	}
	
	private QueueNode removeEndNode(int queueNum){
		QueueNode remove = queueLine[queueNum];
		QueueNode removeParent = null;
		int p = 0; //lowest priority
		int n = 0; //num of node with lowest priority
		int c = 0; //current count
		
		if (queueType == LIFO){
			queueLine[queueNum] = queueLine[queueNum].getNext();
		}
		else if(queueType == FIFO){
			while(remove.getNext() != (QueueNode)null){ //If at the end, set the parent's next to null
				removeParent = remove;
				remove = remove.getNext();
			}
			if(removeParent != (QueueNode)null){
				removeParent.setNext(null);
			}
			else{
				queueLine[queueNum] = null;
			}
			
		}
		else if(queueType == PRIORITY){
			p = remove.getPriority();
			while(remove.getNext() != (QueueNode)null){ //If at the end, set the parent's next to null
				removeParent = remove;
				remove = remove.getNext();
				c++;
				if(p > remove.getPriority()){
					p = remove.getPriority();
					n = c;
				}
			}
			c = 0;
			remove = queueLine[queueNum];
			removeParent = null;
			while (c < n){
				removeParent = remove;
				remove = remove.getNext();
				c++;
			}
			
			if(removeParent != (QueueNode)null){
				removeParent.setNext(remove.getNext());
			}
			else{
				queueLine[queueNum] = remove.getNext();
			}
		}
		return remove;
	}
	
	private void balance(){
		QueueNode shiftNode;
		boolean shift;
		int shiftNum = 0;
		do{
			shift = false;
			for(int q1 = 0; q1 < queueCount - 1; q1++){
				for(int q2 = q1 + 1; q2 < queueCount; q2++){
					if(qlNames[q1].equals(qlNames[q2])){
						if((Math.abs(queueLength(queueLine[q1]) - queueLength(queueLine[q2]))) >= 2){
							shift = true;
							if(queueLength(queueLine[q1]) > queueLength(queueLine[q2])){
								shiftNum = q1;
							}
							else{
								shiftNum = q2;
							}
							q1 = queueCount;
							q2 = queueCount + 1;
						}
					}
				}
			}
			if(shift){
				shiftNode = removeEndNode(shiftNum);
				enqueue(shiftNode);
			}
		}while(shift);
	}
	
	public double avgWait(){
		if(doneCount == 0) return 0;
		double avg = 0;
		QueueNode cur = done;
		
		while(cur != (QueueNode)null){
			avg += cur.getWaitTime();
			cur = cur.getNext();
		}
		
		return avg/doneCount;
	}
	
	public double stdDev(){
		double avg = avgWait();
		double sd = 0;
		
		if(doneCount == 0) return 0;
		
		QueueNode cur = done;
		
		while(cur != (QueueNode)null){
			sd += Math.pow(cur.getWaitTime() - avg, 2);
			cur = cur.getNext();
		}
		
		return Math.sqrt(sd/avg);
	}
	
	public double throughput(){ // average service time, expressed in ticks per node
		if(doneCount == 0) return 0;
		double avg = 0;
		QueueNode cur = done;
		
		while(cur != (QueueNode)null){
			avg += cur.getTotServTime();
			cur = cur.getNext();
		}
		return avg/doneCount;
	}
	
	public int maxWait(){
		int maxWait = 0;
		QueueNode cur = done;
		
		while(cur != (QueueNode)null){
			if(cur.getWaitTime() > maxWait) maxWait = cur.getWaitTime();
			cur = cur.getNext();
		}
		
		return maxWait;
	}
	
	public void describe(){ //Outputs the attributes of the queue simulator
		System.out.println("Number of service stations: " + serviceCount);
		System.out.println("Number of queues: " + queueCount);
		
		System.out.print("Queue Type: ");
		switch(queueType){
		case FIFO: System.out.println("FIFO"); break;
		case LIFO: System.out.println("LIFO"); break;
		case PRIORITY: System.out.println("Priority"); break;
	    default: System.out.println("Error, unknown queue type"); 
		}
		
		System.out.println("Max queue length: " + queueMax);
		
		System.out.print("Arrival Type: ");
		switch(arrivalType){
		case MARKOVIAN: System.out.println("Markovian"); break;
		case POISSON: System.out.println("Poisson"); break;
		case CLUSTERED: System.out.println("Clustered"); break;
	    default: System.out.println("Error, unknown arrival type"); 
		}
		
		System.out.print("Dispatch Type: ");
		switch(dispatchType){
		case RANDOM: System.out.println("Random"); break;
		case SHORTEST: System.out.println("Shortest"); break;
		case SHORTBALANCE: System.out.println("Shortest with balance"); break;
	    default: System.out.println("Error, unknown dispatch type"); 
		}
		
		System.out.println("Service Limit: " + serviceLimit);
		System.out.print("Service Limit Type: ");
		switch(servLimType){
		case NOLIMIT: System.out.println("No Limit"); break;
		case DISCARD: System.out.println("Discard"); break;
		case REQUEUE: System.out.println("Requeue"); break;
		case REQUEUENEW: System.out.println("Requeue as new"); break;
	    default: System.out.println("Error, unknown dispatch type"); 
		}
		
		System.out.println("The path to the data file: " + filepath);
	}
	
	public void metrics(){
		System.out.println("Max wait time: " + maxWait());
		System.out.println("Throughput: " + throughput() + " ticks per node.");
		System.out.println("Average wait time: " + avgWait());
		System.out.println("Standard deviation: " + stdDev());
	}
	
	public void addService(){
		serviceCount++;
		QueueNode[] newService = new QueueNode[serviceCount];
		String[] newServName = new String[serviceCount];
		int[] newServTrack = new int[serviceCount];
		
		System.arraycopy(serviceStation, 0, newService, 0, serviceStation.length);
		System.arraycopy(ssNames, 0, newServName, 0, ssNames.length);
		System.arraycopy(servTrack, 0, newServTrack, 0, servTrack.length);
		
		System.out.println("Please insert new service station name. For no name, input ''");
		input = in.nextLine();
		if(input.equals("''")){
			input = "";
		}
		
		serviceStation = newService;
		ssNames = newServName;
		servTrack = newServTrack;
		ssNames[serviceCount - 1] = input;
	}
	
	public void addService(String name){
		serviceCount++;
		QueueNode[] newService = new QueueNode[serviceCount];
		String[] newServName = new String[serviceCount];
		int[] newServTrack = new int[serviceCount];
		
		System.arraycopy(serviceStation, 0, newService, 0, serviceStation.length);
		System.arraycopy(ssNames, 0, newServName, 0, ssNames.length);
		System.arraycopy(servTrack, 0, newServTrack, 0, servTrack.length);
		
		serviceStation = newService;
		ssNames = newServName;
		servTrack = newServTrack;
		ssNames[serviceCount - 1] = name;
	}
	
	public void removeService(int s){
		serviceCount--;
		QueueNode[] newService = new QueueNode[serviceCount];
		String[] newServName = new String[serviceCount];
		int[] newServTrack = new int[serviceCount];
		
		System.arraycopy(serviceStation, 0, newService, 0, s);
		System.arraycopy(serviceStation, s + 1, newService, s, serviceStation.length - s - 1);
		System.arraycopy(ssNames, 0, newServName, 0, s);
		System.arraycopy(ssNames, s + 1, newServName, s, ssNames.length - s - 1);
		System.arraycopy(servTrack, 0, newServTrack, 0, s);
		System.arraycopy(servTrack, s + 1, newServTrack, s, servTrack.length - s - 1);
		serviceStation = newService;
		ssNames = newServName;
		servTrack = newServTrack;
	}
	
	public void addQueue(){
		queueCount++;
		QueueNode[] newQueue = new QueueNode[queueCount];
		String[] newQName = new String[queueCount];
		
		System.arraycopy(queueLine, 0, newQueue, 0, queueLine.length);
		System.arraycopy(qlNames, 0, newQName, 0, qlNames.length);
		
		System.out.println("Please insert new queue name. For no name, input ''");
		input = in.nextLine();
		if(input.equals("''")){
			input = "";
		}
		
		queueLine = newQueue;
		qlNames = newQName;
		qlNames[queueCount - 1] = input;
		
	}
	
	public void addQueue(String name){
		queueCount++;
		QueueNode[] newQueue = new QueueNode[queueCount];
		String[] newQName = new String[queueCount];
		
		System.arraycopy(queueLine, 0, newQueue, 0, queueLine.length);
		System.arraycopy(qlNames, 0, newQName, 0, qlNames.length);
		
		queueLine = newQueue;
		qlNames = newQName;
		qlNames[queueCount - 1] = name;
		
	}
	
	public void removeQueue(int q){
		queueCount--;
		QueueNode[] newQueue = new QueueNode[queueCount];
		String[] newQName = new String[queueCount];
		
		System.arraycopy(queueLine, 0, newQueue, 0, q);
		System.arraycopy(queueLine, q + 1, newQueue, q, queueLine.length - q - 1);
		System.arraycopy(qlNames, 0, newQName, 0, q);
		System.arraycopy(qlNames, q + 1, newQName, q, qlNames.length - q - 1);
		queueLine = newQueue;
		qlNames = newQName;
	}
	
	public int queueLength(QueueNode head){
		QueueNode cur = head;
		int count = 0;
		
		while(cur != (QueueNode)null){
			count++;
			cur = cur.getNext();
		}
		return count;
	}
	
	public int queueLength(int headNum){
		QueueNode cur = queueLine[headNum];
		int count = 0;
		
		while(cur != (QueueNode)null){
			count++;
			cur = cur.getNext();
		}
		return count;
	}
	
	public boolean isServiceEmpty(int headNum){
		return true;
	}
	
	public boolean isQueueEmpty(int headNum){
		return true;
	}
	
	public void forceService(int q, int s){
		QueueNode node;
		if(serviceStation[s] == (QueueNode)null){
			node = removeNode(q);
			node.setNext(null);
			serviceStation[s] = node;
		}
	}
        public void main( ){
            
        }
}
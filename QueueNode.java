package queueNode;

public class QueueNode {
	private int serviceTime; //Number of ticks to complete
	private int totServTime; //Number of ticks to complete
	private int startTime; //Time entered into the queue
	private int waitTime; //Total wait time for this node
	private int queue; //Number of queue
	private int priority = 0;
	private String serviceName; //Name of the service station to be processed
	private String queueName; //Name of queue to wait in
	private QueueNode next = null; //Points to the next node in the queue towards the end
	
	public QueueNode(int st, String sn, String qn){
		serviceTime = st;
		totServTime = st;
		serviceName = sn;
		queueName = qn;
	}

	public void describe(){
		System.out.println("serviceTime: " + serviceTime);
		System.out.println("startTime: " + startTime);
		System.out.println("waitTime: " + waitTime);
		System.out.println("serviceName: " + serviceName);
		System.out.println("queueName: " + queueName);	
	}
	
	public int getStart() {
		return startTime;
	}

	public int getServiceTime(){
		return serviceTime;
	}
	
	public int getTotServTime(){
		return totServTime;
	}
	
	public String getServiceName(){
		return serviceName;
	}
	
	public String getQueueName(){
		return queueName;
	}
	
	public QueueNode getNext(){
		return next;
	}
	
	public int getWaitTime(){
		return waitTime;
	}
	
	public int getQueue() {
		return queue;
	}
	
	public int getPriority(){
		return priority;
	}
	
	public void setStart(int s){
		startTime = s;
	}

	public void setNext(QueueNode n){
		next = n;
	}
	
	public void setWaitTime(int w){
		waitTime += w;
	}
	
	public void setQueue(int q){
		queue = q;
	}
	
	public void setPriority(int p){
		priority = p;
	}
	
	public void decService(){
		serviceTime--;
	}
}
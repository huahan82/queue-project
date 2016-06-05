package distributor;

import java.util.ArrayList;
import queueNode.*;

public interface Distributor {
	public QueueNode nextNode(int currenttime);
	public ArrayList<QueueNode> nextNodes(int currenttime);
}

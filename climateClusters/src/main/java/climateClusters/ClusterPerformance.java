package climateClusters;

import java.io.Serializable;

public class ClusterPerformance implements Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = -7791808068979572141L;
private int nClusters;
private double cost;
private boolean selected;
public ClusterPerformance() {
	
}
public int getNClusters() {
	return nClusters;
	
}
public void setNClustsers(int n) {
	nClusters =n;
}
public double getCost() {
	return cost;
	
}
public void setCost(double c) {
	cost =c;
}
public boolean getSelected() {
	return selected;
	
}
public void setSelected(boolean select) {
	selected = select;
}
}

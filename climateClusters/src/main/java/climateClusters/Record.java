package climateClusters;
import java.io.Serializable;
import java.util.Comparator;

import org.apache.spark.mllib.linalg.Vector;
import org.joda.time.DateTime;
public class Record implements Serializable {
	private double[] location;
	private double elevation;
	private int clusternum;
	private Vector vector;
	private DateTime datetime;
	public double[] getLocation() {
		return location;
	}
	public void setLocation(double[] coords) {
		location = coords;
	}
	public double getElevation() {
		return elevation;
	}
	public void setElevation(double altitude)
	{
		elevation = altitude;
	}
	public int getClusternum() {
		return clusternum;
	}
	public void setClusternum(int k) {
		clusternum =k;
	}
	public Vector getVector() {
		return vector;
	}
	public void setVector(Vector v)
	{
		vector=v;
	}
	public DateTime getDatetime() {
		return datetime;
	}
	public void setDatetime(DateTime dt)
	{
		datetime =dt;
	}
}
class YearComparator implements Comparator<Record>, Serializable {
    @Override
    public int compare(Record a, Record b) {
        return a.getDatetime().getYear() < b.getDatetime().getYear() ? -1 : a.getDatetime().getYear() == b.getDatetime().getYear() ? 0 : 1;
    }
}

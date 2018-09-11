package climateClusters;
import java.io.Serializable;

import java.util.Comparator;

import org.apache.spark.mllib.linalg.Vector;
import org.joda.time.DateTime;
public class Record implements Serializable {
	private double[] location;
	private double[] psychrometricPoint;
	private double elevation;
	private int clusternum;
	private Vector vectorAllVar;
	private Vector vector;
	private Vector vectornorm;
	private DateTime datetime;
	public double[] getLocation() {
		return location;
	}
	public void setLocation(double[] coords) {
		location = coords;
	}
	public double[] getPsychrometricPoint() {
		return psychrometricPoint;
	}
	public void setPsychrometricPoint(double[] ppoint) {
		psychrometricPoint = ppoint;
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
	public Vector getVectorAllVar() {
		return vectorAllVar;
	}
	public void setVectorAllVar(Vector v)
	{
		vectorAllVar=v;
	}
	public Vector getVectorNorm() {
		return vectornorm;
	}
	public void setVectorNorm(Vector v)
	{
		vectornorm=v;
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
	@Override
	public String toString() {
		return getDatetime()+","+getClusternum()+","+getElevation()
		+","+getLocation()[0]+","+getLocation()[1]+","+getVectorNorm();
	}
}
class YearComparator implements Comparator<Record>, Serializable {
    @Override
    public int compare(Record a, Record b) {
        return a.getDatetime().getYear() < b.getDatetime().getYear() ? -1 : a.getDatetime().getYear() == b.getDatetime().getYear() ? 0 : 1;
    }
}

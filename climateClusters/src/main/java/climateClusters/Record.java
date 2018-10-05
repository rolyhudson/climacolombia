package climateClusters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
	private List<String> inStrategies = new ArrayList<String>();
	private List<String> reqVars = new ArrayList<String>();
	private double utci;
	private double ideamci;
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
	public void addStrategy(String name) {
		this.inStrategies.add(name);
	}
	public List<String> getInStrategies() {
		return this.inStrategies;
	}
	public void setReqVars(List<String> vars) {
		this.reqVars = vars;
	}
	public List<String> getReqVars(){
		return this.reqVars;
	}
	public double getUtci() {
		return utci;
	}
	public void setUtci(double ut) {
		utci = Math.round(ut * 100.0) / 100.0;
	}
	public double getIdeamci() {
		return ideamci;
	}
	public void setIdeamci(double ci) {
		ideamci=Math.round(ci * 100.0) / 100.0;
	}
	@Override
	public String toString() {
		//latitude,longitude,elevation,datetime,temp,vp,rh,tmin,tmax,trange,precip,windSpd
		return getDatetime()+","+getClusternum()+","+getElevation()
		+","+getLocation()[0]+","+getLocation()[1]+","+getReqVars()+","+getVector()
		+",[temp,vp,rh,tmin,tmax,trange,precip,windSpd],"+getVectorAllVar();
	}
	public String toJSONString() {
		String[] allvarNames = {"temp","vp","rh","tmin","tmax","trange","precip","windSpd"};
		double[] allvarValues = getVectorAllVar().toArray();
		List<String> reqVars = getReqVars();
		double[] vectorValues = getVector().toArray();
		List<String> allFoundStrategies = getInStrategies();
		String allParams="{";
		for(int i=0;i<allvarNames.length;i++) {
			if(i==allvarNames.length-1)allParams+="\""+allvarNames[i]+"\":"+allvarValues[i]+"}";
			else allParams+="\""+allvarNames[i]+"\":"+allvarValues[i]+",";
		}
		String vector ="{";
		for(int i=0;i<reqVars.size();i++) {
			if(i==reqVars.size()-1) vector+="\""+reqVars.get(i)+"\":"+vectorValues[i]+"}";
			else vector+="\""+reqVars.get(i)+"\":"+vectorValues[i]+",";
		}
		String strats  ="[";
		for(int i=0;i<allFoundStrategies.size();i++) {
			if(i==allFoundStrategies.size()-1)strats  +="\""+allFoundStrategies.get(i)+"\"]";
			else strats  +="\""+allFoundStrategies.get(i)+"\",";
		}
		return "{\"date\":\""+getDatetime()+"\""+
				",\"clusternum\":"+getClusternum()+
				",\"alt\":"+getElevation()+
				",\"lat\":"+getLocation()[0]+
				",\"lon\":"+getLocation()[1]+
				",\"vector\":"+vector+
				",\"allParams\":" +allParams+
				",\"strategies\":"+strats+
				",\"utci\":"+getUtci()+
				",\"ideamci\":"+getIdeamci()+"}";
	}
}
class YearComparator implements Comparator<Record>, Serializable {
    @Override
    public int compare(Record a, Record b) {
        return a.getDatetime().getYear() < b.getDatetime().getYear() ? -1 : a.getDatetime().getYear() == b.getDatetime().getYear() ? 0 : 1;
    }
}

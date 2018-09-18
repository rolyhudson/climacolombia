import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryAndFile {
	static List<String> resultsObjects = new ArrayList<String>();
	public static void main (String args[]) {

		//displayIt(new File("C:\\Users\\r.hudson\\Documents\\WORK\\softEng\\Diss\\Design\\cycleX\\climacolombia\\results\\62739342\\"));
		transferResultObjects();
	}
	
	private static void getPaths(File node){
		
		System.out.println(node.getAbsoluteFile());
		
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				File f = new File(node, filename);
				getPaths(f);
				resultsObjects.add(f.getPath());
			}
		}
		
	}
	private static void transferResultObjects() {
		String prefixOutput = "C:\\Users\\r.hudson\\Documents\\WORK\\softEng\\Diss\\Design\\cycleX\\climacolombia\\results\\62739342\\";
		String outputfolder = "C:\\Users\\r.hudson\\Documents\\WORK\\softEng\\Diss\\Design\\cycleX\\climacolombia\\results\\webtest\\";
		getPaths(new File("C:\\Users\\r.hudson\\Documents\\WORK\\softEng\\Diss\\Design\\cycleX\\climacolombia\\results\\62739342\\"));
		
		List<String> jsonToCombine = new ArrayList<String>();
		//dumpResults(resultsObjects);
		
		String rootKey="";
		String rootKeyPrev="";
		String filename ="";
		String resultKey ="";
		String resultPath ="";
		for(String line:resultsObjects) {
			filename = line.substring(line.lastIndexOf('/')+1);
			rootKey = line.substring(0,line.lastIndexOf('/')+1);
			resultKey=line.substring(prefixOutput.length());
			resultPath = resultKey.substring(0,resultKey.lastIndexOf('/'));
			if(line.contains("performanceDF")&&line.contains("json"))
			{
				//top level cluster performance report
				//datamanager.copyMove(this.dataBucket, "lacunae.io", line, outputfolder+resultPath+"/clusteringPerformance");
				movefile(outputfolder+resultPath+"/clusteringPerformance",line);
				continue;
			}
			if(filename.equals("_SUCCESS")) continue;
			if(filename.equals("part-00000"))
			{
				//move file and continue
				//datamanager.copyMove(this.dataBucket, "lacunae.io", line, outputfolder+resultPath+"/clusters");
				movefile(outputfolder+resultPath+"/clusters",line);
			}
			else
			{
				if(filename.contains(".json")){
					if(rootKey.equals(rootKeyPrev))
					{
						jsonToCombine.add(line);
					}
					else {
						//process the previous json set
						if(jsonToCombine.size()>1)combineJSON(jsonToCombine,outputfolder,prefixOutput);
						jsonToCombine = new ArrayList<String>();
						jsonToCombine.add(line);
					}
				}
				
			}
			rootKeyPrev=rootKey;
		}
		//add the last group
		if(jsonToCombine.size()>1)combineJSON(jsonToCombine,outputfolder,prefixOutput);
	}
	private static void combineJSON(List<String> jsonToCombine,String output,String prefixOutput) {
		//read json and shift to new directory
		//uploadStringToFile(destinationkeypath+resultPath+".json",sb.toString(),"lacunae.io","application/json");
	}
	private static void movefile(String output,String file) {
		
	}
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class DirectoryAndFile {
	static List<String> resultsObjects = new ArrayList<String>();
	public static void main (String args[]) {

		//displayIt(new File("C:\\Users\\r.hudson\\Documents\\WORK\\softEng\\Diss\\Design\\cycleX\\climacolombia\\results\\62739342\\"));
		transferResultObjects();
	}
	
	private static void getPaths(File node){
		
		//System.out.println(node.getAbsoluteFile());
		
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
		String prefixOutput = "C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\53652781\\";
		String outputfolder = "C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\webtest\\";
		getPaths(new File("C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\53652781\\"));
		
		List<String> jsonToCombine = new ArrayList<String>();
		//dumpResults(resultsObjects);
		
		String rootKey="";
		String rootKeyPrev="";
		String filename ="";
		String resultKey ="";
		String resultPath ="";
		for(String line:resultsObjects) {
			try {
			filename = line.substring(line.lastIndexOf('\\')+1);
			rootKey = line.substring(0,line.lastIndexOf('\\')+1);
			resultKey=line.substring(prefixOutput.length());
			resultPath = resultKey.substring(0,resultKey.lastIndexOf('\\'));
			}
			catch(Exception e){
				System.out.println(line + "error");
			}
			if(line.contains("performanceDF")&&line.contains("json"))
			{
				//top level cluster performance report
				//datamanager.copyMove(this.dataBucket, "lacunae.io", line, outputfolder+resultPath+"/clusteringPerformance");
				movefile(outputfolder+resultPath,line,"clusteringPerformance");
				continue;
			}
			if(filename.equals("_SUCCESS")) continue;
			if(filename.equals("part-00000"))
			{
				//move file and continue
				//datamanager.copyMove(this.dataBucket, "lacunae.io", line, outputfolder+resultPath+"/clusters");
				movefile(outputfolder+resultPath,line,"clusters.json");
			}
			else
			{
				if(filename.contains(".json")){
					if(line.contains("clusterStats")) {
						//back up one folder
						movefile(outputfolder+resultPath.substring(0,resultPath.lastIndexOf('\\')),line,"clusterStats.json");
					}
					else {
						if(line.contains("strategyStats")) {
							//back up one folder
							movefile(outputfolder+resultPath.substring(0,resultPath.lastIndexOf('\\')),line,"strategyStats.json");
						}
						else {
							movefile(outputfolder+resultPath,line,"clusters.json");
						}
					}
					
					
				}
				
			}
			
		}
		//add the last group
		
	}
	private static void combineJSON(List<String> jsonToCombine,String output,String prefixOutput) {
		//read json and shift to new directory
		//uploadStringToFile(destinationkeypath+resultPath+".json",sb.toString(),"lacunae.io","application/json");
	}
	
	private static void movefile(String outputdir,String filepath,String filename) {
		
		try {
			Files.createDirectories(Paths.get(outputdir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Path source = Paths.get(filepath);
		Path target = Paths.get(outputdir+"\\"+filename);
		try {
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}

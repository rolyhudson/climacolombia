import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
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
		String prefixOutput = "C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\58653821\\";
		String outputfolder = "C:\\Users\\Admin\\Documents\\projects\\clusterColombia\\climacolombia\\results\\webtest\\test3\\";
		getPaths(new File(prefixOutput));
		
		List<String> jsonToCombine = new ArrayList<String>();
		//dumpResults(resultsObjects);
		
		String rootKey="";
		String rootKeyPrev="";
		String filename ="";
		String resultKey ="";
		String resultPath ="";
		List<String> lines = Collections.emptyList();
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
			if(filename.equals("_SUCCESS")) continue;
			if(filename.contains(".crc")) continue;
			if(filename.contains("part"))
			{
				//read contents
				try
			    { 
			      lines = Files.readAllLines(Paths.get(line), StandardCharsets.UTF_8);
			    } 
			    catch (IOException e) 
			    { 

			      e.printStackTrace(); 
			    } 
				writeToFile(lines,outputfolder+resultPath,"clusters.json");

			}
		}
		
	}
	private static void writeToFile(List<String> lines,String outputdir,String filename) {
		try {
			Files.createDirectories(Paths.get(outputdir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try (FileWriter f = new FileWriter(outputdir+"\\"+filename, true); 
				BufferedWriter b = new BufferedWriter(f); 
				PrintWriter p = new PrintWriter(b);) { 
			for(String l : lines) {
				p.println(l); 
			}
			 
			} 
		catch (IOException i) { 
			i.printStackTrace(); 
			}
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

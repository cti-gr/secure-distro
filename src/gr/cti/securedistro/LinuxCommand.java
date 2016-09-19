package gr.cti.securedistro;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LinuxCommand  {

	private ArrayList<String> tokenizedCommand = new ArrayList<>();
	private int exitCode = -1;
	Process p = null;
	ProcessBuilder pb = null;	
	BufferedReader reader;
	StringBuilder output;
	
	public LinuxCommand(String... command) {
		for (String item : command)
			tokenizedCommand.add(item);
	}

	public int getExitCode() {
		return exitCode;
	}

	public String toString() {
		return tokenizedCommand.toString();
	}
	
	public String getOutput(){
		return output.toString(); 
	}

	public void execute() {		
		try {
			pb = new ProcessBuilder(tokenizedCommand);
			p = pb.start();
			reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			p.waitFor();
			exitCode = p.exitValue();
			output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
			    output.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();			
		} catch (InterruptedException e1) {
			e1.printStackTrace();			
		} finally {
			//System.out.println("exiting with exit code: " + exitCode + " for command: " + tokenizedCommand.get(0));
		}

	}

}

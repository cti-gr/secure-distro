package test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import gr.cti.securedistro.LinuxCommand;
 
/*
 * Class used for testing purposes only - to be discarded in deployment
 */
 
public class TestLinuxCommand {
 
	
	public static void main(String[] args) {
		String value = "", line="";
		BufferedReader reader=null;
		String cmd[] = {
		       "id", "-u"
		      };     
		try {
			Process p=Runtime.getRuntime().exec(cmd);
			reader=new BufferedReader(new InputStreamReader(p.getInputStream())); 
			line=reader.readLine(); 
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 while(line!=null) 
	      { 
	        value += line + "\n";
	        try {
				line=reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	      }
		System.out.println(value);
	}
 
} 		
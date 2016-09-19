package gr.cti.securedistro;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {

	static ConcurrentHashMap<String, Integer> runningProcesses;

	 private static ProcessManager instance = null;
	   protected ProcessManager() {
		   runningProcesses = new ConcurrentHashMap<String, Integer>();
	   }
	      
	   public static ProcessManager getInstance() {
	      if(instance == null) {
	         instance = new ProcessManager();
	      }
	      return instance;
	   }
	
	int getProcIdByName(String procName) {
		int pid = -1;
		File dir = new File("/proc");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isDirectory() == true) {
					File processNameFile = new File("/proc/" + child.getName()
							+ "/cmdline");
					byte[] bytes;
					try {
						bytes = Files.readAllBytes(processNameFile.toPath());
						String processName = new String(bytes, "UTF-8");
						if (processName.contains(procName) == true) {
							pid = Integer.parseInt(child.getName());
							return pid;
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
		return pid;
	}

	public synchronized void  update() {
		this.runningProcesses.clear();
		File dir = new File("/proc");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				int pid = -1;
				if (child.isDirectory() == true) {
					File processNameFile = new File("/proc/" + child.getName()
							+ "/cmdline");
					byte[] bytes;
					try {
						bytes = Files.readAllBytes(processNameFile.toPath());
						String processName = new String(bytes, "UTF-8");						
						pid = Integer.parseInt(child.getName());
						this.runningProcesses.put(processName, pid);
					} catch (Exception e) {
					}
				}
			}

		}
	}

	
	public void echo() {
		System.out.println("============ACTIVE=========");
		for (String processName: this.runningProcesses.keySet()){
			System.out.println(processName +":"+this.runningProcesses.get(processName));
		}
		System.out.println("===========================");
	}
	
	public boolean isProcessRunning(String name){
		for (String processName: this.runningProcesses.keySet()){
			if(processName.contains(name)) return true;
		}
		return false;
	}

}

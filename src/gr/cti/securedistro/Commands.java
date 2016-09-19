package gr.cti.securedistro;

public final class Commands {
	
	/***************************** START / STOP SERVICES ******************************/
	// AVGD
	public static final String[] stopAvgd = {"sudo", "service", "avgd", "stop", "--force"};
	public static final String[] startAvgd = {"sudo", "service", "avgd", "start"};
	
	// Dansguardian
	public static final String[] stopDG = {"sudo", "service", "dansguardian", "stop"};
	public static final String[] startDG = {"sudo", "service", "dansguardian", "start"};
	
	// Protos Sensor (firelog service)
	public static final String[] stopProtos = {"sudo", "service", "firelog", "stop"};
	public static final String[] startProtos = {"sudo", "service", "firelog", "start"};
	
	// UFW
	public static final String[] stopUfw = {"sudo", "ufw", "disable"};
	public static final String[] startUfw = {"sudo", "ufw", "enable"};
		
	// Privoxy
	public static final String[] stopPrivoxy = {"sudo", "service", "privoxy", "stop"};
	public static final String[] startPrivoxy = {"sudo", "service", "privoxy", "start"};
			
	
}

package gr.cti.securedistro;

public class AvgdException extends Exception {

	private static final long serialVersionUID = 1L;

	public AvgdException(){
		super("Error restarting avgd");
	}
		
}

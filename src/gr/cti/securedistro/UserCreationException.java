package gr.cti.securedistro;

public class UserCreationException extends Exception {

	private static final long serialVersionUID = 1L;
	private String description;
	
	public UserCreationException(String message){
		super(message);
		description = message;
	}
	
	@Override
	public String toString(){
		return this.description;
	}
}

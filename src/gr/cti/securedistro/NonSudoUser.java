package gr.cti.securedistro;

public class NonSudoUser {

	private String username;
	private boolean isProtected;
	
	public NonSudoUser(String username, boolean isProtected){
		this.username = username;
		this.isProtected = isProtected; 
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

}

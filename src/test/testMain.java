package test;
import gr.cti.securedistro.ProcessManager;
import gr.cti.securedistro.UsersAdminController;
import gr.cti.securedistro.NonSudoUser;


public class testMain {
 public static void main(String[] args){
	/* ProcessManager pm=ProcessManager.getInstance();
	 pm.update();
	 pm.echo();
	 System.out.println(pm.isProcessRunning("gedit"));
	 System.out.println(pm.isProcessRunning("avgscand"));*/
	 System.out.println(UsersAdminController.getSystemNonSudoUsers().size());
	 
	 for (NonSudoUser user : UsersAdminController.getSystemNonSudoUsers()){
		 System.out.println(user.getUsername() +", "+ user.isProtected());
	 }
 }
}

package gr.cti.securedistro;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;




public class AddUserController implements Initializable {
	
	/* setting style manually for the alert dialogs, the setWidth method does not
	 * seem to be working
	 * */
	private static final String alertStyle = " -fx-max-width:550px; -fx-max-height: 150px; -fx-pref-width: 550px; "
			+ "-fx-pref-height: 150px; -fx-text-alignment:center;";
	
	private static final String addUser = "/opt/secure-distro/bin/addUser.sh";
	private static final String configUser = "/opt/secure-distro/bin/configUser.sh";
	
	private boolean userCreated;
	private ResourceBundle bundle;

	@FXML
	private Button btnOK, btnCancel;

	@FXML
	private Menu menuUsersAdmin;

	@FXML
	private TextField txtUsername;

	@FXML
	private Label lblMainAddUser;

	@FXML
	private PasswordField txtPassword1, txtPassword2;

	private ExecutorService executorUserCreation = Executors
			.newSingleThreadExecutor();
	private UsersAdminController uac;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		uac = Main.getUAController();

		bundle = resources;

		lblMainAddUser.setText(bundle.getString("lblMainAddUser"));
		btnCancel.setText(bundle.getString("btnCancel"));

	}

	public void setLang() {
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle",
				Locale.getDefault());
		lblMainAddUser.setText(bundle.getString("lblMainAddUser"));
		btnCancel.setText(bundle.getString("btnCancel"));
	}

	public void submitUser() {
		try {
			createUser();
		} catch (UserCreationException e) {
			e.printStackTrace();
		}
		clearFields();
		Main.getAddUsersStage().close();
	}

	public void btnCancelAction() {
		clearFields();
		Main.getAddUsersStage().close();
	}

	public void createUser() throws UserCreationException {
		userCreated = false;
		String username, password1, password2;
		username = txtUsername.getText();
		password1 = txtPassword1.getText();
		password2 = txtPassword2.getText();

		System.out.println("Username: " + username.length());

		if (username.length() == 0) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.getDialogPane().setStyle(alertStyle);
			alert.setTitle(bundle.getString("msgAttention"));
			alert.setHeaderText(bundle.getString("msgNoUsername"));
			alert.setContentText(bundle.getString("msgNoUsernameExplanation"));
			alert.showAndWait();

			return;
		}
		if ((password1.length() == 0)) {
			Alert alert = new Alert(AlertType.WARNING);			
			alert.getDialogPane().setStyle(alertStyle);
			alert.setTitle(bundle.getString("msgAttention"));
			alert.setHeaderText(bundle.getString("msgNoPasswordGiven"));
			alert.setContentText(bundle.getString("msgNoPasswordGivenExplanation"));
			alert.showAndWait();

			return;
		}
		if ((password2.length() == 0)) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.getDialogPane().setStyle(alertStyle);
			alert.setTitle(bundle.getString("msgAttention"));
			alert.setHeaderText(bundle.getString("msgNoPasswordVerification"));
			alert.setContentText(bundle.getString("msgNoPasswordVerificationExplanation"));
			alert.showAndWait();

			return;
		}
		// throw new
		// UserCreationException("You must fill in both password fields1");
		else if (!password1.equals(password2)) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.getDialogPane().setStyle(alertStyle);
			alert.setTitle(bundle.getString("msgAttention"));
			alert.setHeaderText(bundle.getString("msgNoPasswordMatch"));
			alert.setContentText(bundle.getString("msgNoPasswordMatchExplanation"));
			alert.showAndWait();

			return;
		}

		/***************************************************************/
		/********************** USER CREATION **************************/
		/***************************************************************/
		LinuxCommand lcCreate = new LinuxCommand(addUser, username, password2);
		Task<Void> taskCreate = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				lcCreate.execute();
				return null;
			}
		};
		taskCreate.setOnFailed(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent t) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.getDialogPane().setStyle(alertStyle);
				alert.setTitle(bundle.getString("msgAttention"));
				alert.setHeaderText(bundle.getString("msgUserCreationFailure"));
				alert.setContentText(bundle.getString("msgUserCreationFailureExplanation"));
				alert.showAndWait();

				return;
			}
		});
		taskCreate.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				if (lcCreate.getExitCode() != 0) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.getDialogPane().setStyle(alertStyle);
					alert.setTitle(bundle.getString("msgAttention"));
					alert.setHeaderText(bundle.getString("msgUserCreationFailure"));
					alert.setContentText(bundle.getString("msgUserCreationFailureExplanation"));
					alert.showAndWait();

					return;
				}
				
				System.out.println("Successfully created new user:  "
						+ username + " With exit code: "
						+ lcCreate.getExitCode());
				
				//System.out.println(userCreated);
			}
		});

		/***************************************************************/
		/******************** USER CONFIGURATION ************************/
		/***************************************************************/
		LinuxCommand lcConfig = new LinuxCommand(configUser, username);
		Task<Void> taskConfig = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				if (!userCreated) {
					System.out.println("USER NOT CREATED, EXITING - 1");
					return null;
				}
				else {
					lcConfig.execute();
					return null;
				}
			}
		};
		taskConfig.setOnFailed(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent t) {
				if (!userCreated) {
					//System.out.println("USER NOT CREATED, EXITING - 2");
					return;
				}
				// Deleting user in case of configuration failure
				LinuxCommand lcDelFailedUser = new LinuxCommand("userdel",
						"-r", username);
				Platform.runLater(new Task<Void>() {
					@Override
					public Void call() throws Exception {
						lcDelFailedUser.execute();
						return null;

					}
				});
				Alert alert = new Alert(AlertType.ERROR);
				alert.getDialogPane().setStyle(alertStyle);
				alert.setTitle(bundle.getString("msgAttention"));
				alert.setHeaderText(bundle.getString("msgUserConfigurationFailure"));
				alert.setContentText(bundle.getString("msgUserConfigurationFailureExplanation"));
				alert.showAndWait();

				return;
			}
		});
		taskConfig.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {
				if (!userCreated)
					return;
				if (lcConfig.getExitCode() != 0) {
					System.out.println("EXIT CODE PROBLEM: " + lcConfig.getExitCode());
					// Deleting user in case of configuration failure
					LinuxCommand lcDelFailedUser = new LinuxCommand("userdel",
							"-r", username);
					Platform.runLater(new Task<Void>() {
						@Override
						public Void call() throws Exception {
							lcDelFailedUser.execute();
							return null;

						}
					});
					Alert alert = new Alert(AlertType.ERROR);
					alert.getDialogPane().setStyle(alertStyle);
					alert.setTitle(bundle.getString("msgAttention"));
					alert.setHeaderText(bundle.getString("msgUserConfigurationFailure"));
					alert.setContentText(bundle.getString("msgUserConfigurationFailureExplanation"));
					alert.showAndWait();

					return;
				}				
				System.out.println("Successfully CONFIGURED new user:  "
						+ username + " With exit code: "
						+ lcConfig.getExitCode());
				Platform.runLater(new Task<Void>() {
					@Override
					public Void call() throws Exception {
						uac.displayNonSudoUsers(UsersAdminController
								.getSystemNonSudoUsers());
						return null;

					}
				});
			}
		});
		executorUserCreation.submit(taskCreate);
		try {
			executorUserCreation.awaitTermination(1, TimeUnit.SECONDS);
			userCreated = true;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			userCreated=false;
		}
		executorUserCreation.submit(taskConfig);

	}
	
	public void clearFields(){
		txtUsername.clear();
		txtPassword1.clear();
		txtPassword2.clear();
	}
}

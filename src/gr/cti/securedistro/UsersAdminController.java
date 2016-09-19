package gr.cti.securedistro;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class UsersAdminController implements Initializable {

	private static final String alertStyle = " -fx-max-width:550px; -fx-max-height: 150px; -fx-pref-width: 550px; "
			+ "-fx-pref-height: 150px; -fx-text-alignment:center;";

	private static final String delUser = "/opt/secure-distro/bin/delUser.sh";
	private static final String configUser = "/opt/secure-distro/bin/configUser.sh";

	private ResourceBundle bundle;
	private String deleteUser, activateProtection;

	@FXML
	private SplitPane splitPaneAdminUsers;

	@FXML
	private AnchorPane root;

	@FXML
	private ScrollPane srcPaneUsers;

	@FXML
	private Label lblUserAdminMain;

	@FXML
	private Button btnAddUser, btnExitUsers;

	private static String distroPath = ".securedistro";
	private static HashMap<String, String> sudoers;

	private GridPane gridPaneNonSudoUsers;
	private Label lblHeaderUsername, lblHeaderIsProtected;

	private ExecutorService executorUserDeletion = Executors
			.newCachedThreadPool();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lblHeaderUsername = new Label();
		lblHeaderIsProtected = new Label();
		bundle = resources;

		gridPaneNonSudoUsers = new GridPane();
		gridPaneNonSudoUsers.setId("gridPaneNonSudoUsers");

		// setting column widths for the grid pane
		gridPaneNonSudoUsers.getColumnConstraints().add(
				new ColumnConstraints(145));
		gridPaneNonSudoUsers.getColumnConstraints().add(
				new ColumnConstraints(85));
		gridPaneNonSudoUsers.getColumnConstraints().add(
				new ColumnConstraints(210));
		gridPaneNonSudoUsers.getColumnConstraints().add(
				new ColumnConstraints(150));

		srcPaneUsers.setContent(gridPaneNonSudoUsers);
		gridPaneNonSudoUsers.setAlignment(Pos.CENTER);
		setLang();

	}

	public void setLang() {
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle",
				Locale.getDefault());
		lblUserAdminMain.setText(bundle.getString("lblUserAdminMain"));
		lblHeaderUsername.setText(bundle.getString("lblHeaderUsername"));
		lblHeaderIsProtected.setText(bundle.getString("lblHeaderIsProtected"));
		btnAddUser.setText(bundle.getString("btnAddUser"));
		btnExitUsers.setText(bundle.getString("btnExitUsers"));

		deleteUser = bundle.getString("lblDeleteUser");
		activateProtection = bundle.getString("lblActivateProtection");
		displayNonSudoUsers(getSystemNonSudoUsers());
	}

	public void displayNonSudoUsers(ArrayList<NonSudoUser> nonSudoUsers) {

		// clearing previous content to provision for grid pane updates upon new
		// user addition
		gridPaneNonSudoUsers.getChildren().clear();

		// adding some space between lines
		gridPaneNonSudoUsers.setVgap(15);

		lblHeaderUsername.setPadding(new Insets(0, 0, 0, 20));
		lblHeaderUsername.setId("lblHeaderUsername");

		lblHeaderIsProtected.setId("lblHeaderIsProtected");
		GridPane.setHalignment(lblHeaderUsername, HPos.LEFT);
		GridPane.setHalignment(lblHeaderIsProtected, HPos.CENTER);

		// adding header elements to GridPane
		GridPane.setColumnIndex(lblHeaderUsername, 0);
		GridPane.setRowIndex(lblHeaderUsername, 0);
		GridPane.setColumnIndex(lblHeaderIsProtected, 1);
		GridPane.setRowIndex(lblHeaderIsProtected, 0);
		gridPaneNonSudoUsers.getChildren().addAll(lblHeaderUsername,
				lblHeaderIsProtected);

		// adding users
		int i = 1; // starting from 2nd row given that 1st row belongs to
					// headers
		for (NonSudoUser nsuser : nonSudoUsers) {

			/**** Label ****/
			String username = nsuser.getUsername();
			Label lblUsername = new Label(username);
			lblUsername.setPadding(new Insets(5, 0, 5, 20));
			lblUsername.setId("lbl" + username);
			GridPane.setHalignment(lblUsername, HPos.LEFT);

			/**** Image ****/
			boolean isProtected = nsuser.isProtected();
			System.out.println("User: " + username + " Status: " + isProtected);
			ImageView imgIsProtected = new ImageView(
					isProtected ? "/resources/media/ok.png"
							: "/resources/media/notOK.png");
			imgIsProtected.setFitHeight(20);
			imgIsProtected.setFitWidth(20);
			imgIsProtected.setId("img" + username);
			GridPane.setHalignment(imgIsProtected, HPos.CENTER);

			/**** Button for protection ****/
			Button btnSetProtected = new Button(activateProtection);
			btnSetProtected.setId("btnProt" + username);
			btnSetProtected.setDisable(isProtected);
			btnSetProtected.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Task<Void> task = new Task<Void>() {
						@Override
						public Void call() throws Exception {
							LinuxCommand lcConfig = new LinuxCommand(
									configUser, username);
							lcConfig.execute();
							return null;
						}
					};
					task.setOnFailed(new EventHandler<WorkerStateEvent>() {
						public void handle(WorkerStateEvent t) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.getDialogPane().setStyle(alertStyle);
							alert.setTitle(bundle.getString("msgAttention"));
							alert.setHeaderText(bundle
									.getString("msgUserConfigurationFailure"));
							alert.setContentText(bundle
									.getString("msgUserConfigurationFailureExplanation"));
							alert.showAndWait();

							return;
						}
					});
					task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						public void handle(WorkerStateEvent t) {
							Platform.runLater(new Task<Void>() {
								@Override
								public Void call() throws Exception {
									btnSetProtected.setDisable(true);
									imgIsProtected.setImage(new Image(
											"/resources/media/ok.png"));
									Alert alert = new Alert(
											AlertType.INFORMATION);
									alert.getDialogPane().setStyle(alertStyle);
									alert.setTitle(bundle
											.getString("msgUserProtected"));
									alert.setHeaderText(null);
									alert.setContentText(bundle
											.getString("msgUserProtectedExplanation")
											+ username);

									alert.showAndWait();

									return null;
								}
							});
						}
					});
					executorUserDeletion.submit(task);
				}
			});
			GridPane.setHalignment(btnSetProtected, HPos.CENTER);

			/**** Button for deletion ****/
			Button btnDeleteUser = new Button(deleteUser);
			btnDeleteUser.setId(username);
			GridPane.setHalignment(btnDeleteUser, HPos.CENTER);
			// delete user in a seperate thread
			btnDeleteUser.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.getDialogPane().setStyle(alertStyle);
					alert.setTitle(bundle.getString("msgUserDeletion"));
					alert.setHeaderText(null);
					alert.setContentText(bundle.getString("msgAreYouSure"));
					Optional<ButtonType> result = alert.showAndWait();

					if (result.get() == ButtonType.CANCEL)
						// if (response == Dialog.ACTION_CANCEL)
						return;
					Task<Void> task = new Task<Void>() {
						@Override
						public Void call() throws Exception {
							String username = ((Button) event.getSource())
									.getId();
							LinuxCommand lcDelete = new LinuxCommand(delUser,
									username);
							lcDelete.execute();
							return null;
						}
					};
					task.setOnFailed(new EventHandler<WorkerStateEvent>() {
						public void handle(WorkerStateEvent t) {
							System.out.println("FAILURE deleting  " + username);
						}
					});
					task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						public void handle(WorkerStateEvent t) {
							Platform.runLater(new Task<Void>() {
								@Override
								public Void call() throws Exception {
									ArrayList<Node> toErase = new ArrayList<Node>();
									for (Node nodeToErase : gridPaneNonSudoUsers
											.getChildren()) {
										if (nodeToErase.getId().endsWith(
												username))
											toErase.add(nodeToErase);
									}
									gridPaneNonSudoUsers.getChildren()
											.removeAll(toErase);
									// rearrangeGridPane(); - TBD
									return null;
								}
							});
						}
					});
					executorUserDeletion.submit(task);
				}
			});

			// adding user-related gui items
			GridPane.setColumnIndex(lblUsername, 0);
			GridPane.setRowIndex(lblUsername, i);

			GridPane.setColumnIndex(imgIsProtected, 1);
			GridPane.setRowIndex(imgIsProtected, i);

			GridPane.setColumnIndex(btnSetProtected, 2);
			GridPane.setRowIndex(btnSetProtected, i);

			GridPane.setColumnIndex(btnDeleteUser, 3);
			GridPane.setRowIndex(btnDeleteUser, i);

			gridPaneNonSudoUsers.getChildren().addAll(lblUsername,
					imgIsProtected, btnSetProtected, btnDeleteUser);
			i++;
		}
	}

	public void btnExitAction(ActionEvent event) {
		Main.getUsersAdminStage().close();
	}

	public void showAddNonSudoUserPane(ActionEvent event) {
		Main.getAddUsersStage().show();
		// System.out.println(gridPaneNonSudoUsers.getId());
	}

	static public ArrayList<NonSudoUser> getSystemNonSudoUsers() {
		/*
		 * method to get current system NonSudoUsers whether related to
		 * secure-distro or not for each system NonSudoUser check whether is
		 * protected or not
		 */

		ArrayList<NonSudoUser> nonSudoUsersList = new ArrayList<NonSudoUser>();
		File dir = new File("/home");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			try {
				getSudoUsers();
			} catch (IOException e) {
				e.printStackTrace();
				UsersAdminController.sudoers = new HashMap<String, String>();
			}
			for (File child : directoryListing) {
				if (child.isDirectory() == true) {
					if (isUserSudo(child.getName()))
						continue;
					String filename = child.getAbsolutePath() + File.separator
							+ distroPath;
					File distroDir = new File(filename);
					NonSudoUser user = new NonSudoUser(child.getName(),
							distroDir.exists());
					nonSudoUsersList.add(user);
					System.out.println(user);
				}
			}
		}
		return nonSudoUsersList;
	}

	public static boolean isUserSudo(String user) {
		return sudoers.containsKey(user);
	}

	public static void getSudoUsers() throws IOException {
		// less /etc/group | grep sudo returns sudo:x:27:theodori,user2
		String command = "less /etc/group | grep sudo";
		String[] commands = new String[] { "/bin/sh", "-c", command };
		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.redirectErrorStream(true);
		Process process = builder.start();
		InputStream is = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		String output = "";
		while ((line = reader.readLine()) != null) {
			output += line;
		}
		int i = output.lastIndexOf(":");
		output = output.substring(i + 1);
		String[] sudoers = output.split(",");
		UsersAdminController.sudoers = new HashMap<String, String>();
		for (String su : sudoers) {
			UsersAdminController.sudoers.put(su, su);
		}
	}

	public void activateProtection(String username) {
		/*
		 * 1. Set iptables rule 2. Set up firefox accordingly 3. Create
		 * .securedistro folder 4. Add shortcuts 5. Create .avgui personal
		 * directory
		 */
	}
}

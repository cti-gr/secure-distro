package gr.cti.securedistro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Main extends Application {

	private static final String urlsFile = "/etc/dansguardian/lists/blacklists/custom/domains";
	private static Properties prop;
	private static String propFileName;
	private static File propFile;
	
	private static String language;
	private static String country;
	
	// FXML Loaders
	private static FXMLLoader fxmlLoaderMain, uacLoader, usersAddLoader, bsLoader, editWebsitesLoader, copyrightLoader;
	
	// User-related info
	private static boolean isSudo;
	private static String sudoUsername;
	
	// Stages
	private static Stage mainStage, usersAdminStage, addUsersStage, blockedSitesStage, editWebsitesStage, copyrightStage;
	
	// Controllers
	private static MainController mc;
	private static UsersAdminController uac;
	private static AddUserController auc;
	private static BlockedWebsitesController bsc;
	private static BlockedWebsitesEditController bwec;
	private static CopyrightController cc;
	
	
	ExecutorService mainExecutor = Executors.newCachedThreadPool();;

	/*
	 * Needed by main controller to access the primary stage to support
	 * exit function from the respective Exit Button of the main window
	 */
	public static Stage getPrimaryStage() {
		return mainStage;
	}

	public static Stage getUsersAdminStage() {
		return usersAdminStage;
	}
	
	public static Stage getAddUsersStage() {
		return addUsersStage;
	}
	
	public static Stage getBlockedSitesStage() {
		return blockedSitesStage;
	}
	
	public static Stage getBlockedSitesEditStage() {
		return editWebsitesStage;
	}
	
	public static Stage getCopyrightStage() {
		return copyrightStage;
	}
	
	public static String getSudoUsername() {
		return sudoUsername;
	}
	
	// needed by AddUsersController to get a reference to UsersAdminController
	// in order to update the grid pane upon user addition
	public static UsersAdminController getUAController(){
		return uac;
	}
	
	public static BlockedWebsitesEditController getBWController(){
		return bwec;
	}
	
	public static boolean isSudo(){
		return isSudo;
	}
	
	public static String getUrlsFile(){
		return urlsFile;
	}
	
	@Override
	public void init() {
		
		// Checking if run as sudo
		LinuxCommand checkuserID = new LinuxCommand("id", "-u");
		checkuserID.execute();
		String output = checkuserID.getOutput();
		int userID = Integer.parseInt(output);
		if (userID == 0)
			isSudo = true;
		else
			isSudo = false;
		
		LinuxCommand sudoUser = new LinuxCommand("who");
		sudoUser.execute();
		sudoUsername = sudoUser.getOutput().split(" ")[0];
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			prop = new Properties();
			propFileName = "/home/" + sudoUsername + "/.securedistro/application.properties";
			propFile = new File(propFileName);
			InputStream inputStream = new BufferedInputStream(new FileInputStream(propFileName));
			prop.load(inputStream);
			language = prop.getProperty("language");
			country = prop.getProperty("country");
			
			Locale.setDefault(new Locale(language, country));
			
			/*** MAIN STAGE ***/
			mainStage = primaryStage;
			URL locationMain = getClass().getResource(
					"/resources/fxml/MainPane.fxml");
			fxmlLoaderMain = new FXMLLoader();
			
			fxmlLoaderMain.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			fxmlLoaderMain.setLocation(locationMain);
			fxmlLoaderMain.setBuilderFactory(new JavaFXBuilderFactory());
			Parent root = (Parent) fxmlLoaderMain.load(locationMain.openStream());
			Scene scene = new Scene(root);
			mc = fxmlLoaderMain.<MainController>getController();
			scene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());

			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				
				@Override
				public void handle(WindowEvent t) {
					
					Platform.setImplicitExit(true);
					Platform.exit();
				}

			});
			
			
						
			/*** USERS ADMIN STAGE ***/
			usersAdminStage = new Stage(StageStyle.DECORATED);
			uacLoader = new FXMLLoader(getClass().getResource("/resources/fxml/UsersAdminPane.fxml"));
			uacLoader.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			Scene usersAdminScene = new Scene((Pane) uacLoader.load());
			usersAdminStage.setScene(usersAdminScene);
			uac = uacLoader.<UsersAdminController>getController();
			usersAdminScene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());
			usersAdminStage.initModality(Modality.WINDOW_MODAL);
			usersAdminStage.setResizable(false);
			usersAdminStage.initOwner(primaryStage);
			
			
			/*** ADD USERS STAGE ***/
			addUsersStage = new Stage(StageStyle.DECORATED);
			usersAddLoader = new FXMLLoader(getClass().getResource("/resources/fxml/AddUserPane.fxml"));
			usersAddLoader.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			Scene addUsersScene = new Scene((Pane) usersAddLoader.load());
			addUsersStage.setScene(addUsersScene);
			auc = usersAddLoader.<AddUserController>getController();			
			usersAdminScene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());
			addUsersStage.initModality(Modality.WINDOW_MODAL);
			addUsersStage.setResizable(false);
			addUsersStage.initOwner(usersAdminStage);
			
			/*** BLOCKED SITES STAGE ***/
			blockedSitesStage = new Stage(StageStyle.DECORATED);
			bsLoader = new FXMLLoader(getClass().getResource("/resources/fxml/BlockedSitesPane.fxml"));
			bsLoader.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			Scene blockedSitesScene = new Scene((Pane) bsLoader.load());
			blockedSitesStage.setScene(blockedSitesScene);
			bsc = bsLoader.<BlockedWebsitesController>getController();
			blockedSitesScene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());
			blockedSitesStage.initModality(Modality.WINDOW_MODAL);
			blockedSitesStage.setResizable(false);
			blockedSitesStage.initOwner(primaryStage);
			
			/*** EDIT BLOCKED SITES STAGE ***/
			editWebsitesStage = new Stage(StageStyle.DECORATED);
			editWebsitesLoader = new FXMLLoader(getClass().getResource("/resources/fxml/WebsiteEditPane.fxml"));
			editWebsitesLoader.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			Scene editBlockedWebsitesScene = new Scene((Pane) editWebsitesLoader.load());
			editWebsitesStage.setScene(editBlockedWebsitesScene);
			bwec = editWebsitesLoader.<BlockedWebsitesEditController>getController();
			editBlockedWebsitesScene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());
			editWebsitesStage.initModality(Modality.WINDOW_MODAL);
			editWebsitesStage.setResizable(false);
			editWebsitesStage.initOwner(blockedSitesStage);
			
			/*** COPYRIGHT STAGE ***/
			copyrightStage = new Stage(StageStyle.DECORATED);
			copyrightLoader = new FXMLLoader(getClass().getResource("/resources/fxml/CopyrightPane.fxml"));
			copyrightLoader.setResources(ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault()));
			Scene copyrightScene = new Scene((Pane) copyrightLoader.load());
			copyrightStage.setScene(copyrightScene);
			cc = copyrightLoader.<CopyrightController>getController();
			copyrightScene.getStylesheets().add(
					getClass().getResource("/resources/css/application.css")
							.toExternalForm());
			copyrightStage.initModality(Modality.WINDOW_MODAL);
			copyrightStage.setResizable(false);
			copyrightStage.initOwner(primaryStage);
			
	
			primaryStage.show();
		} catch (Exception e) {
			AppLock.releaseLock();
			e.printStackTrace();
		}

	}

	
	public static void main(String[] args) throws Exception {
		//FileInputStream iStream = null;
		//FileLock urlsFileLock = null;
		try {
			//iStream = new FileInputStream(urlsFile);
			// Try to get LOCKS //
			//urlsFileLock = iStream.getChannel().lock();			
		    if (!AppLock.setLock("security_distribution_lock_key")) {
		        throw new RuntimeException("Only one application instance may run at the same time!");
		    }
		    launch(args);
		}
		finally{
			//urlsFileLock.release();
		    AppLock.releaseLock();
		    //if (iStream != null)
			//	iStream.close();
		}
		
	}
	
	public static String getLang(){
		return language;
	}
	
	
	
	public static void setLang(String newLang) {
		language = newLang;
		prop.setProperty("language", newLang.toLowerCase());
		switch(newLang){
		case "EN":
			country="EN";
			prop.setProperty("country", "EN");
			break;
		case "EL":
			country="GR";
			prop.setProperty("country", "GR");
			break;
		}
		 try {
			prop.store(new FileOutputStream(propFile), "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Locale.setDefault(new Locale(language, country));
		mc.setLang();
		uac.setLang();
		auc.setLang();
		bsc.setLang();
		bwec.setLang();
		cc.setLang();
	}
}

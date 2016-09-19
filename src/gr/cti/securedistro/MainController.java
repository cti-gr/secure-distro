package gr.cti.securedistro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

public class MainController implements Initializable {

	private static final String toggleFilter = "/opt/secure-distro/bin/toggleFilter.sh";

	private static Properties prop;
	private static String propFileName;
	private static File propFile;
	private volatile boolean protectionStatus;
	private Object lockRestartAvgd;
	private HashMap<ToggleButton, Service> serviceMap;
	private ExecutorService executor = Executors.newCachedThreadPool();
	Thread threads[] = new Thread[5];
	private ResourceBundle bundle;

	@FXML
	private ComboBox<String> cmbBoxLang;
	/*@FXML
	private CheckBox chkFilterStatus;*/
	@FXML
	private Label lblFilterStatus;
	@FXML
	private Label lblMainTitle, lblUser, lblStatusTitle, lblService, lblStatus;
	@FXML
	private Menu menuAdmin, menuAbout, menuExit;
	@FXML
	private MenuItem menuItemUsersAdmin, menuItemBlockedSites, menuItemHelp,
			menuItemAboutControlPanel, menuItemTerminate;
	@FXML
	private Button btnExit;
	@FXML
	private Text txtUser, txtAvgd, txtDansguardian, txtProtos, txtUfw,
			txtPrivoxy;
	@FXML
	private ToggleButton btnToggleAvgd, btnToggleDG, btnToggleProtos,
			btnToggleUfw, btnTogglePrivoxy;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
		setLang();
		// initialize language chooser combo box
		ObservableList<String> options = FXCollections.observableArrayList(
				"EL", "EN");
		cmbBoxLang.setItems(options);
		switch (Main.getLang()) {
		case "en":
			cmbBoxLang.getSelectionModel().select(1);
			break;
		case "el":
			cmbBoxLang.getSelectionModel().select(0);
			break;
		}

		cmbBoxLang.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				changeLang();
			}
		});

		lockRestartAvgd = new Object();
		serviceMap = new HashMap<ToggleButton, Service>();

		serviceMap.put(btnToggleAvgd, Service.avgd);
		serviceMap.put(btnToggleDG, Service.dansguardian);
		serviceMap.put(btnToggleProtos, Service.firelog);
		serviceMap.put(btnToggleUfw, Service.ufw);
		serviceMap.put(btnTogglePrivoxy, Service.privoxy);

		// setting the user text field
		txtUser.setText(Main.getSudoUsername());

		// Addressing the particularity of AVGD
		Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				if (ex instanceof AvgdException)
					restartAvgd();
			}
		};

		for (HashMap.Entry<ToggleButton, Service> entry : serviceMap.entrySet()) {
			ProcessManager.getInstance().update();
			ToggleButton tButton = entry.getKey();
			if (!Main.isSudo()) {
				tButton.setDisable(true);
			}
			Service service = entry.getValue();
			/* Setting initial state of toggle buttons */
			tButton.setSelected(ProcessManager.getInstance().isProcessRunning(
					service.name()));
			Thread th = new Thread(new ServiceStatusUpdater(service, tButton));
			th.setDaemon(true);
			th.start();

		}

		if (!Main.isSudo()) {
			menuAdmin.setDisable(true);
			//chkFilterStatus.setSelected(true);
			//chkFilterStatus.setDisable(true);

			prop = new Properties();
			propFileName = "/home/" + Main.getSudoUsername()
					+ "/.securedistro/application.properties";
			propFile = new File(propFileName);
			InputStream inputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(
						propFileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
/*			protectionStatus = prop.getProperty("protectionStatus").equals(
					"true") ? true : false;*/
			//chkFilterStatus.setSelected(protectionStatus);
		}
	}

	/*public void setFilterStatus() {
		if (chkFilterStatus.isSelected()) {
			// System.out.println("Setting!");
			Task<Void> task = new Task<Void>() {
				@Override
				public Void call() throws Exception {
					LinuxCommand command = new LinuxCommand(toggleFilter,
							Main.getSudoUsername(), "set");
					command.execute();
					if (command.getExitCode() != 0)
						throw new Exception();
					return null;
				}
			};
			task.setOnFailed(new EventHandler<WorkerStateEvent>() {
				public void handle(WorkerStateEvent t) {
					chkFilterStatus.setSelected(true);
				}
			});
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				public void handle(WorkerStateEvent t) {
					System.out.println("TRUE");
					prop.setProperty("protectionStatus", "true");
					try {
						prop.store(new FileOutputStream(propFile), "");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(prop);
				}
			});
			executor.submit(task);
		} else {
			// System.out.println("Unsetting!");
			Task<Void> task = new Task<Void>() {
				@Override
				public Void call() throws Exception {
					LinuxCommand command = new LinuxCommand(toggleFilter,
							Main.getSudoUsername(), "unset");
					command.execute();
					if (command.getExitCode() != 0)
						throw new Exception();
					return null;
				}
			};
			task.setOnFailed(new EventHandler<WorkerStateEvent>() {
				public void handle(WorkerStateEvent t) {
					chkFilterStatus.setSelected(false);
				}
			});
			task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				public void handle(WorkerStateEvent t) {
					System.out.println("FALSE");
					prop.setProperty("protectionStatus", "false");
					try {
						prop.store(new FileOutputStream(propFile), "");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println(prop);
				}
			});
			executor.submit(task);
		}
	}*/

	public void setLang() {
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle",
				Locale.getDefault());

		// Initializing Menus and Menu Items
		//lblFilterStatus.setText(bundle.getString("lblFilterStatus"));
		menuAdmin.setText(bundle.getString("menuAdmin"));
		menuItemUsersAdmin.setText(bundle.getString("menuItemUsersAdmin"));
		menuItemBlockedSites.setText(bundle.getString("menuItemBlockedSites"));
		menuAbout.setText(bundle.getString("menuAbout"));
		menuExit.setText(bundle.getString("menuExit"));
		menuItemHelp.setText(bundle.getString("menuItemHelp"));
		menuItemTerminate.setText(bundle.getString("menuItemTerminate"));
		menuItemAboutControlPanel.setText(bundle
				.getString("menuItemAboutControlPanel"));
		btnExit.setText(bundle.getString("exit"));

		// Initializing Labels
		lblMainTitle.setText(bundle.getString("lblMainTitle"));
		lblUser.setText(bundle.getString("lblUser"));
		lblStatusTitle.setText(bundle.getString("lblStatusTitle"));
		lblService.setText(bundle.getString("lblService"));
		lblStatus.setText(bundle.getString("lblStatus"));

		txtAvgd.setText(bundle.getString("txtAvgd"));
		txtDansguardian.setText(bundle.getString("txtDansguardian"));
		txtProtos.setText(bundle.getString("txtProtos"));
		txtUfw.setText(bundle.getString("txtUfw"));
		txtPrivoxy.setText(bundle.getString("txtPrivoxy"));
	}

	public void btnExitAction(ActionEvent actionEvent) {
		Main.getPrimaryStage().close();
	}

	public ToggleButton getButton(Service service) throws Exception {
		switch (service) {
		case avgd:
			return this.btnToggleAvgd;
		case dansguardian:
			return this.btnToggleDG;
		case firelog:
			return this.btnToggleProtos;
		case ufw:
			return this.btnToggleUfw;
		case privoxy:
			return this.btnTogglePrivoxy;
		default:
			throw new Exception("Error in getButton() method");
		}
	}

	public void toggleService(ActionEvent actionEvent) throws Exception {

		switch (((ToggleButton) actionEvent.getSource()).getId()) {
		/******************** AVGD **************************/
		case "btnToggleAvgd":
			if (btnToggleAvgd.isSelected()) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleAvgd.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.startAvgd);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleAvgd.setDisable(false);
						btnToggleAvgd.setSelected(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleAvgd.setDisable(false);
					}
				});
				executor.submit(task);

			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleAvgd.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.stopAvgd);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleAvgd.setSelected(true);
						btnToggleAvgd.setDisable(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleAvgd.setDisable(false);
					}
				});
				executor.submit(task);
			}

			break;

		/*******************************************************************/

		/********************** DANSGUARDIAN ************************/
		case "btnToggleDG":
			if (btnToggleDG.isSelected()) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleDG.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.startDG);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleDG.setDisable(false);
						btnToggleDG.setSelected(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleDG.setDisable(false);
					}
				});
				executor.submit(task);

			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleDG.setDisable(true);
						LinuxCommand command = new LinuxCommand(Commands.stopDG);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleDG.setSelected(true);
						btnToggleDG.setDisable(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleDG.setDisable(false);
					}
				});
				executor.submit(task);
			}
			break;
		/*******************************************************************/

		/************************** PROTOS **************************/
		case "btnToggleProtos":
			if (btnToggleProtos.isSelected()) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleProtos.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.startProtos);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleProtos.setDisable(false);
						btnToggleProtos.setSelected(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleProtos.setDisable(false);
					}
				});
				executor.submit(task);

			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleProtos.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.stopProtos);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleProtos.setSelected(true);
						btnToggleProtos.setDisable(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleProtos.setDisable(false);
					}
				});
				executor.submit(task);
			}
			break;
		/*******************************************************************/

		/*************************** UFW ****************************/
		case "btnToggleUfw":
			if (btnToggleUfw.isSelected()) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleUfw.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.startUfw);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleUfw.setDisable(false);
						btnToggleUfw.setSelected(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleUfw.setDisable(false);
					}
				});
				executor.submit(task);

			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnToggleUfw.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.stopUfw);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleUfw.setSelected(true);
						btnToggleUfw.setDisable(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnToggleUfw.setDisable(false);
					}
				});
				executor.submit(task);
			}
			break;
		/*******************************************************************/

		/*************************** PRIVOXY **************************/
		case "btnTogglePrivoxy":
			if (btnTogglePrivoxy.isSelected()) {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnTogglePrivoxy.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.startPrivoxy);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnTogglePrivoxy.setDisable(false);
						btnTogglePrivoxy.setSelected(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnTogglePrivoxy.setDisable(false);
					}
				});
				executor.submit(task);

			} else {
				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
						btnTogglePrivoxy.setDisable(true);
						LinuxCommand command = new LinuxCommand(
								Commands.stopPrivoxy);
						command.execute();
						if (command.getExitCode() != 0)
							throw new Exception();
						return null;
					}
				};
				task.setOnFailed(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnTogglePrivoxy.setSelected(true);
						btnTogglePrivoxy.setDisable(false);
					}
				});
				task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent t) {
						btnTogglePrivoxy.setDisable(false);
					}
				});
				executor.submit(task);
			}
			break;
		default:
			throw new Exception();
		}
	}

	// utility method to address a particular issue of avgd service
	// which sometimes fails to remove the lock file upon exit

	private void restartAvgd() {
		synchronized (lockRestartAvgd) {
			LinuxCommand restartAvgd = new LinuxCommand("rm", "/var/lock/avgd");
			restartAvgd.execute();
		}
	}

	// pops up the User Administration Panel
	public void showUsersAdminPane(ActionEvent event) {
		Main.getUsersAdminStage().show();
	}

	// pops up the Copyright Window
	public void showCopyright(ActionEvent event) {
		Main.getCopyrightStage().show();
	}

	// pops up the Blocked Sites Panel
	public void showBlockedSitesPane(ActionEvent event) {
		Main.getBlockedSitesStage().show();
	}

	public void changeLang() {
		String newLang = (String) cmbBoxLang.getSelectionModel()
				.getSelectedItem();
		if (newLang.equals(Main.getLang()))
			return;
		Main.setLang(newLang);
	}

}

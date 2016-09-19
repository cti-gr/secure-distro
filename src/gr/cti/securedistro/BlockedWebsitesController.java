package gr.cti.securedistro;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;


public class BlockedWebsitesController implements Initializable {

	private BlockedWebsitesEditController bwec;
	private ResourceBundle bundle;
	private ObservableList<BlockedWebsite> blockedWebsitesList = FXCollections
			.observableArrayList();

	@FXML
	private Button btnExit, btnNew, btnEdit, btnDelete;

	@FXML
	private Label lblUrl;

	@FXML
	private TextField txtUrl;

	@FXML
	private TableView<BlockedWebsite> tblBlockedWebsites;

	@FXML
	private TableColumn<BlockedWebsite, String> tblColumn;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Main.getBlockedSitesStage().setOnCloseRequest(
				new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent t) {
						updateFile();
					}
				});
		// System.out.println(resources.toString());
		bundle = resources;
		// bwec = Main.getBWController();
		lblUrl.setText(bundle.getString("lblUrl"));
		btnDelete.setText(bundle.getString("btnDelete"));
		btnEdit.setText(bundle.getString("btnEdit"));
		btnNew.setText(bundle.getString("btnNew"));
		btnExit.setText(bundle.getString("exit"));
		tblColumn.setText(bundle.getString("tblColumn"));

		tblColumn.setCellValueFactory(cellData -> cellData.getValue()
				.siteUrlProperty());

		tblBlockedWebsites
				.getSelectionModel()
				.selectedItemProperty()
				.addListener(
						(observable, oldValue, newValue) -> showBlockedWebsite(newValue));

		populateUrlList();

		showBlockedWebsite(null);

	}

	public void setLang() {
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle",
				Locale.getDefault());
		lblUrl.setText(bundle.getString("lblUrl"));
		btnDelete.setText(bundle.getString("btnDelete"));
		btnEdit.setText(bundle.getString("btnEdit"));
		btnNew.setText(bundle.getString("btnNew"));
		btnExit.setText(bundle.getString("exit"));
		tblColumn.setText(bundle.getString("tblColumn"));

	}

	public void populateUrlList() {
		BufferedReader urlsStream = null;
		String l = null;
		try {
			urlsStream = new BufferedReader(new FileReader(Main.getUrlsFile()));
			try {
				while ((l = urlsStream.readLine()) != null) {
					System.out.println(l);
					blockedWebsitesList.add(new BlockedWebsite(l));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tblBlockedWebsites.setItems(blockedWebsitesList);
	}

	private void showBlockedWebsite(BlockedWebsite blockedWebsite) {
		if (blockedWebsite != null) {
			txtUrl.setText(blockedWebsite.getSiteUrl());
		} else {
			// Person is null, remove all the text.
			txtUrl.setText("");
		}
	}

	public void deleteBlockedWebsite() {
		int index = tblBlockedWebsites.getSelectionModel().getSelectedIndex();
		if (index >= 0)
			tblBlockedWebsites.getItems().remove(index);
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle(bundle.getString("msgNoSelection"));
			alert.setHeaderText(bundle.getString("msgNoSelectionMust"));
			alert.setContentText(bundle.getString("msgNoSelectionExplanation"));

			alert.showAndWait();
			/*Dialogs.create().title(bundle.getString("msgNoSelection"))
					.masthead(bundle.getString("msgNoSelectionMust"))
					.message(bundle.getString("msgNoSelectionExplanation"))
					.showWarning();*/
			
		}

	}

	@FXML
	private void editorNew() {
		BlockedWebsite newBlockedWebsite = new BlockedWebsite();
		boolean okClicked = showEditor(newBlockedWebsite);
		if (okClicked) {
			blockedWebsitesList.add(newBlockedWebsite);
		}
	}

	@FXML
	private void editorEdit() {
		BlockedWebsite selectedSite = tblBlockedWebsites.getSelectionModel()
				.getSelectedItem();
		if (selectedSite != null) {
			boolean okClicked = showEditor(selectedSite);
			if (okClicked) {
				showBlockedWebsite(selectedSite);
			}

		} else {
			// Nothing selected.
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle(bundle.getString("msgNoSelection"));
			alert.setHeaderText(bundle.getString("msgNoSelectionMust"));
			alert.setContentText(bundle.getString("msgNoSelectionExplanation"));
			alert.showAndWait();
/*			Dialogs.create().title(bundle.getString("msgNoSelection"))
					.masthead(bundle.getString("msgNoSelectionMust"))
					.message(bundle.getString("msgNoSelectionExplanation"))
					.showWarning();*/
		}
	}

	public boolean showEditor(BlockedWebsite blockedWebsite) {
		bwec = Main.getBWController();
		System.out.println(bwec.toString());
		bwec.setBlockedWebsite(blockedWebsite);
		Main.getBlockedSitesEditStage().showAndWait();
		// return true;
		return bwec.clickedOK();

	}

	public void closePane() {
		updateFile();
		Main.getBlockedSitesStage().close();
	}

	public void updateFile() {
		PrintWriter outputStream = null;
		try {
			outputStream = new PrintWriter(new FileWriter(Main.getUrlsFile()));
			outputStream.write("");
			for (BlockedWebsite bw : blockedWebsitesList)
				outputStream.println(bw.getSiteUrl());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			outputStream.close();
		}

	}

}

package gr.cti.securedistro;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class BlockedWebsitesEditController implements Initializable {

	@FXML
	private TextField txtEditWebsite;

	@FXML
	private Label lblEditWebsite;

	@FXML
	private Button btnOK, btnCancel;

	private ResourceBundle bundle;
	private BlockedWebsite blockedWebsite;
	private boolean clickedOK = false;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		bundle = resources;
		
		lblEditWebsite.setText(bundle.getString("lblEditWebsite"));
		btnCancel.setText(bundle.getString("btnCancel"));
	}

	public void setBlockedWebsite(BlockedWebsite blockedWebsite) {
		this.blockedWebsite = blockedWebsite;

		txtEditWebsite.setText(blockedWebsite.getSiteUrl());
	}

	public boolean clickedOK() {
		return clickedOK;
	}
	
	public void submit() {
		if(websiteValidation()) {
			blockedWebsite.setSiteUrl(txtEditWebsite.getText());
			clickedOK = true;
			Main.getBlockedSitesEditStage().close();
		}
	}
	
	public void cancel() {
		Main.getBlockedSitesEditStage().close();
	}
	
	private boolean websiteValidation(){
		// TODO: Add website validation
		return true;
	}

	public void setLang(){
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle",
				Locale.getDefault());
		lblEditWebsite.setText(bundle.getString("lblEditWebsite"));
		btnCancel.setText(bundle.getString("btnCancel"));
	}


}

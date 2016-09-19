package gr.cti.securedistro;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CopyrightController implements Initializable {

	private ResourceBundle bundle;

	@FXML
	private Label lblCopyright;
	
	@FXML
	private Button btnOK;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lblCopyright.setWrapText(true);
		bundle = resources;
		setLang();
	}

	public void setLang() {
		bundle = ResourceBundle.getBundle("resources.bundles.SDBundle", Locale.getDefault());
		lblCopyright.setText(bundle.getString("msgCopyright"));
	}
	
	public void closeWindow(){
		Main.getCopyrightStage().close();
	}

}

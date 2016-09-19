package gr.cti.securedistro;

public class ButtonProcessName {

	// private ToggleButton
	// btnToggleAvgd, btnToggleDG, btnToggleProtos, btnToggleUfw,
	// btnTogglePrivoxy;

	public static String getProcessName(String buttonName) {
		String pName = "";
		if (buttonName.contains("Avgd")) {
			pName = "avgd";
		} else if (buttonName.contains("DG")) {
			pName = "dansguardian";
		} else if (buttonName.contains("Privoxy")) {
			pName = "privoxy";
		} else if (buttonName.contains("Ufw")) {
			pName = "ufw";
		} else if (buttonName.contains("Protos")) {
			pName = "firelog";
		}
		return pName;
	}

}

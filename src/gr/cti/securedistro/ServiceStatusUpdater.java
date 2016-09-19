package gr.cti.securedistro;

import java.net.URL;
import java.net.URLDecoder;
import java.io.File;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ToggleButton;

public class ServiceStatusUpdater extends Task {

	private ToggleButton toggleButton;
	private final static int millisecondsInterval = 500;
	private gr.cti.securedistro.Service service;
	private LinuxCommand lc;

	public ServiceStatusUpdater(gr.cti.securedistro.Service service,
			ToggleButton toggleButton) {
		this.toggleButton = toggleButton;
		this.service = service;
		//System.out.println("Working Directory = "+ System.getProperty("user.dir"));
		if (service == Service.ufw) 
			lc = new LinuxCommand("grep", "ENABLED=yes", "/etc/ufw/ufw.conf");
		else
			lc = new LinuxCommand("ps", "-C", service.name());
	}

	@Override
	public Object call() throws AvgdException {
		while (true) {
			lc.execute();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int exitCode = lc.getExitCode();
			boolean serviceStatus;
			if (exitCode == 0)
				serviceStatus = true;
			else if (exitCode == 150)
				throw (new AvgdException());
			else
				serviceStatus = false;
			boolean buttonState = toggleButton.isSelected();
			if (serviceStatus != buttonState) {
				synchronized (this) {
					System.out.println("Update!!!");
					Platform.runLater(new Task<Void>() {
						@Override
						public Void call() throws Exception {
							toggleButton.setSelected(serviceStatus);
							return null;
						}
					});
				}
			}

		}
	}
}

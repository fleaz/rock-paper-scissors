import java.util.Vector;

import rps.client.Application;
import rps.client.GameListener;
import rps.client.ai.BasicAi;
import rps.client.ai.TournamentAi;

public class start_game {

	public static Vector<GameListener> ais = new Vector<GameListener>();

	public static void main(String[] args) {
		showWarningIfLinuxBox();
		registerAIs();
		startUI();
	}

	private static void registerAIs() {
		ais.add(new BasicAi());
		ais.add(new TournamentAi(2000, 100000));
		// TODO: add new AIs here
	}

	private static void startUI() {
		new Application(ais);
	}

	private static void showWarningIfLinuxBox() {
		String osname = System.getProperty("os.name", "generic").toLowerCase();
		if (osname.startsWith("linux")) {
			String warningMessage = "WARNING!\n" + "Apparently you are running a linux box.\n"
					+ "You may need to set the \"java.rmi.server.hostname\" system property\n"
					+ "to your IP address, when starting the VM. For example:\n" + ""
					+ "-Djava.rmi.server.hostname=192.168.1.50\n";
			System.err.println(warningMessage);
		}
	}
}
package rps.client;

import static java.lang.String.format;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import rps.client.ui.GamePane;
import rps.client.ui.Menu;
import rps.client.ui.StartupPane;
import rps.client.ui.WaitingPane;

public class Application {

	private final UIController uiController = new UIController();
	private final GameController gameController = new GameController();

	private final JFrame frame = new JFrame("Rock Paper Scissors");
	private final Container rootPane;

	private final Menu menu;
	private final StartupPane startupPane;
	private final WaitingPane waitingPane;
	private final GamePane gamePane;

	public Application(Vector<GameListener> ais) {

		menu = new Menu(frame, uiController);

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				uiController.handleExit();
			}
		});

		rootPane = frame.getContentPane();
		rootPane.setLayout(new BoxLayout(rootPane, Y_AXIS));

		startupPane = new StartupPane(rootPane, uiController, gameController, ais);
		waitingPane = new WaitingPane(rootPane, uiController);
		gamePane = new GamePane(rootPane);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		wireComponents();
	}

	private void wireComponents() {
		uiController.setComponents(menu, startupPane, waitingPane, gamePane, gameController);
		gameController.setComponents(uiController, gamePane);
	}

	public static void callAsync(final Callable<?> callable) {
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				callable.call();
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (Exception e) {
					showErrorDialog(e);
				}
			}
		}.execute();
	}

	private static void showErrorDialog(Throwable e) {
		showMessage(format("Unexpected problem: %s", e.getMessage()));
		e.printStackTrace();
	}

	public static void showMessage(String message) {
		showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
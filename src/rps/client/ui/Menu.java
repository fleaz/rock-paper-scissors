package rps.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import rps.client.UIController;
import rps.highscore.Highscore;

public class Menu {

	private final JFrame frame;
	private final UIController controller;
	
	private final JFrame aboutFrame = new JFrame("About");
	private final JFrame highFrame = new JFrame("Highscore");
	private final JButton closeHighscore = new JButton("Highscore schliessen");
	private final JLabel hsNick = new JLabel("Spieler:");
	private final JLabel hsAi = new JLabel("Besiegte KI:");
	private final JLabel hsScore = new JLabel("Punktzahl:");
	
	private final JMenuBar menuBar = new JMenuBar();
	
	private final JMenu menuGame = new JMenu("Spiel");
	private final JMenuItem menuGameNew = new JMenuItem("Neues Spiel");
	private final JMenuItem menuGameSurrender = new JMenuItem("Aufgeben");
	private final JMenuItem menuGameExit = new JMenuItem("Beenden");
	
	private final JMenu menuTheme = new JMenu("Theme");
	private final JMenuItem themeDefault = new JMenuItem("Default");
	private final JMenuItem themeMinimal = new JMenuItem("Minimal");

	private final JMenu menuInfo = new JMenu("Info");
	private final JMenuItem infoHigh = new JMenuItem("Highscore");
	private final JMenuItem infoAbout = new JMenuItem("About");
	private final JLabel[] nicks = new JLabel[10];
	private final JLabel[] score = new JLabel[10];
	private final JLabel[] ai = new JLabel[10];
	
	public Menu(JFrame frame, UIController controller) {

		this.frame = frame;
		this.controller = controller;

		buildMenuStructure();
		bindMenuActions();
	}

	private void buildMenuStructure() {
		menuGame.setMnemonic(KeyEvent.VK_G);

		menuBar.add(menuGame);
		menuBar.add(menuTheme);
		menuBar.add(menuInfo);
		
		menuInfo.add(infoAbout);
		menuInfo.add(infoHigh);
		
		menuTheme.add(themeDefault);
		menuTheme.add(themeMinimal);
		
		menuGame.add(menuGameNew);
		menuGame.add(menuGameSurrender);
		menuGame.addSeparator();
		menuGame.add(menuGameExit);
		frame.setJMenuBar(menuBar);

		for(int i=0;i<10;i++){
			
			
			nicks[i] = new JLabel("-");
			ai[i] = new JLabel("-");
			score[i] = new JLabel("-");
			
			nicks[i].setBounds(20, (i+1)*30+30, 150, 20);
			ai[i].setBounds(190, (i+1)*30+30, 100, 20);
			score[i].setBounds(350, (i+1)*30+30, 100, 20);
					
			highFrame.add(nicks[i]);
			highFrame.add(ai[i]);
			highFrame.add(score[i]);			
		}

		hsNick.setBounds(20, 20, 150, 20);
		hsAi.setBounds(190, 20, 100, 20);
		hsScore.setBounds(350, 20, 100, 20);

		highFrame.add(hsNick);
		highFrame.add(hsAi);
		highFrame.add(hsScore);
		
		closeHighscore.setBounds(120, 390, 210, 30);
		highFrame.add(closeHighscore);
		
		highFrame.add(new JLabel());
		highFrame.setLocationRelativeTo(null);
		highFrame.setBounds(500, 300, 450, 480);
		highFrame.setVisible(false);
		
		menuGameSurrender.setEnabled(false);
		menuGameNew.setEnabled(false);
	}

	private void bindMenuActions() {
		menuGameSurrender.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.handleSurrender();
			}
		});
		menuGameNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.handleNewGame();

			}
		});
		menuGameExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.handleExit();
			}
		});
		themeDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.changeTheme("default");
				controller.updateTheme();
			}
		});
		themeMinimal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.changeTheme("minimal");
				controller.updateTheme();
			}
		});
		infoHigh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Highscore highscore = new Highscore();
				ResultSet rs;
				try {
					rs = highscore.getHighscore();
					int i;
					while(rs.next()){
						i = rs.getRow()-1;
						nicks[i].setText(rs.getString("nick"));
						ai[i].setText(rs.getString("ai"));
						score[i].setText(Integer.toString(rs.getInt("score")));
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				highFrame.setVisible(true);
			}
		});
		closeHighscore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				highFrame.setVisible(false);
				
			}
		});
		infoAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(aboutFrame, 
							"\"Rock Paper Scissors\" geschrieben in Java\n\n"+
							"Entwicklerteam:\n"+
							"Felix Breidenstein\n"+
							"Sebastian Bechtel\n"+
							"Wilhelm Werner\n"+
							"Robert Respondek\n\n"+
							"Quellcode kann hier gefunden werden:\n"+
							"https://github.com/f-breidenstein/rock-paper-scissors/"
							);
			}
		});
	}

	public void gameStarted() {
		menuGameNew.setEnabled(true);
		menuGameSurrender.setEnabled(true);
	}

	public void gameEnded() {
		menuGameNew.setEnabled(true);
		menuGameSurrender.setEnabled(false);
	}

	public void reset() {
		menuGameNew.setEnabled(false);
		menuGameSurrender.setEnabled(false);
	}
}
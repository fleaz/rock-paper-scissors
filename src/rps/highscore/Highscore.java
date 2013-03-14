package rps.highscore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collections;

public class Highscore {
    private ArrayList<Score> scores;

    private static final String HIGHSCORE_FILE = "./highscore";

    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    public Highscore() {
        scores = new ArrayList<Score>();
    }
    
    public ArrayList<Score> getScores() {
        loadScoreFile();
        sort();
        return scores;
    }
    
    private void sort() { // the idea to use a comparator to sort the highscore is taken by a tutorial - Will
        ScoreComparison comparison = new ScoreComparison();
        Collections.sort(scores, comparison);
    }
    
    public void addScore(String name, int score) {
        loadScoreFile();
        scores.add(new Score(name, score));
        saveScoreFile();
    }
    
    public void loadScoreFile() {
        try {
            inputStream = new ObjectInputStream(new FileInputStream(HIGHSCORE_FILE));
            scores = (ArrayList<Score>) inputStream.readObject();
        } catch (FileNotFoundException fnfe) {
        	//FileNotFoundException
        } catch (IOException ioe) {
        	//IOException
        } catch (ClassNotFoundException cnfe) {
            //ClassNotFoundException 
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException ioe) {
            	//IOException
            }
        }
    }
    
    public void saveScoreFile() {
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(HIGHSCORE_FILE));
            outputStream.writeObject(scores);
        } catch (FileNotFoundException fnfe) {
            //FileNotFoundException 
        } catch (IOException ioe) {
        	//IOException 
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                //IOException 
            }
        }
    }
    
    public String getHighscoreString() {
        String highscoreString = "";
        final int showEntries = 10;

        ArrayList<Score> scores;
        scores = getScores();

        int i = 0;
        int x = scores.size();
        if (x > showEntries) {
            x = showEntries;
        }
        while (i < x) {
            highscoreString += (i + 1) + ".\t" + scores.get(i).getName() + "\t\t" + scores.get(i).getScore() + "\n";
            i++;
        }
        return highscoreString;
    }
    
}

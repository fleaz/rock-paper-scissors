package rps.highscore;

/**
 * The score-class, which contains the nick, the score and the beaten ai. 
 *
 */
public class Score{
    private int score;
    private String nick;
    private String beatenAi;

    public int getScore() {
        return score;
    }

    public String getNick() {
        return nick;
    }
    
    public String getBeatenAi() {
        return beatenAi;
    }

    public Score(String nick, int score, String beatenAi) {
        this.score = score;
        this.nick = nick;
        this.beatenAi = beatenAi;
    }
}

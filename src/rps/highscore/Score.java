package rps.highscore;

import java.io.Serializable;

@SuppressWarnings("serial") // we want to use the generated serial, that's why we suppress the warning - Will
public class Score  implements Serializable {
    private int score;
    private String name;

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public Score(String name, int score) {
        this.score = score;
        this.name = name;
    }
}

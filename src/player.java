public class player {


private int id;
private String name;
private int score;



public player(int id, String name) {

    this.id = id;
    this.name = name;
    this.score = 0;
}

public void setId(int id) {
    this.id = id;
}

public int getId() {
    return id;
}

public void setName(String name) {
    this.name = name;
}

public String getName() {
    return name;
}


public int getScore() {
    return score;
}



public void updateScore(int points) {
    score += points;
}











}

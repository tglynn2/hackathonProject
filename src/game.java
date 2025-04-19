public class game {


    private boolean lobby;
    private boolean race;
    private boolean gameIsRunning;

    //win state?





    //lobby and race should probably be classes

    public game(boolean gameIsRunning) {
        this.lobby = true;
        this.race = false;
        this.gameIsRunning = gameIsRunning;

    }


    public void setLobby (boolean lobby) {
        this.lobby = lobby;
    }

    public boolean getLobby () {
        return this.lobby;
    }

    public void setRace (boolean race) {
        this.race = race;
    }

    public boolean getRace () {
        return this.race;
    }

    public void setGameIsRunning (boolean gameIsRunning) {
        this.gameIsRunning = gameIsRunning;
    }

    public boolean getGameIsRunning () {
        return this.gameIsRunning;
    }
















}

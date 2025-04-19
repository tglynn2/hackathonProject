public class Main {
    public static void main(String[] args) {


        final int trackSizeX = 100;

        boolean  gameIsRunning;

        game thegame = new game(false);
        player player = new player(1, "blank");
        //need to intitalize players


//game loop
      while (thegame.getGameIsRunning())

        {


          if (player.getScore() == trackSizeX)
          {
              thegame.setRace(false);
              //race is over
              thegame.setLobby(true);
              //should be some third win state?
          }

        }





    }









}
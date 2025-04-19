public class teacher {


   private int id;
  // private String name;
    //should they have a display name?
   private int[][] game_roundId = new int[3][4];
   //list of wrong answers, connected to studant ids, after round it completed
    //could be incremented ?
   private int lobbyID;
   //multiple? should this be an array?
   private int [] players;
   //list of players ids, could be names instead ?

    public teacher (int id)
    {
        this.id = id;


    }

    public  void setlobbyId (int lobbyID)
    {
        this.lobbyID = lobbyID;
    }

    public int getLobbyID ()
    {
        return lobbyID;
    }


    public void addPlayers (int player)
    {
       //add to the array? should this be called some somewhere else,
       //usuing lobby id
    }














}

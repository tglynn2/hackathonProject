import java.util.HashMap;
import java.util.Map;

public class question {

    private int id;
    private String text;
    private Map<String, Boolean> questionChoices = new HashMap<>();



    public question(int id, String text, Map<String, Boolean> questionChoices) {

        this.id = id;
        this.text = text;
        this.questionChoices = questionChoices;


    }


    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Map<String, Boolean> getQuestionChoices() {
        return questionChoices;
    }

    public Boolean isCorrect (String answer)
    {
     return questionChoices.get(answer);
    }

















}

package Client;

public class Strategy {
    private String id;
    private int participantsAmount;
    private double signal;
     private double valuation;
    public Strategy(String id, int participantsAmount, double signal, double valuation) {
        this.id = id;
        this.participantsAmount = participantsAmount;
        this.signal = signal;
        this.valuation = valuation;
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public int getParticipantsAmount() {return participantsAmount;}

    public void setParticipantsAmount(int participantsAmount) {this.participantsAmount = participantsAmount;}

    public double getSignal() {return signal;}

    public void setSignal(double signal) {this.signal = signal;}

    public double getvaluation() {return valuation;}

    public void setvaluation(double valuation) {this.valuation = valuation;}


}

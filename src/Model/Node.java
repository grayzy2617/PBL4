package Model;

public class Node {
    private String id;
    private String type;
    private double Pt;
    private double Gt;
    private double Gr;
    private double B_max;
    private double T;
    private double budget_max;
    private double currentBudget;

    public Node() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPt() {
        return Pt;
    }

    public void setPt(double pt) {
        this.Pt = pt;
    }

    public double getGt() {
        return Gt;
    }

    public void setGt(double gt) {
        this.Gt = gt;
    }

    public double getGr() {
        return Gr;
    }

    public void setGr(double gr) {
        this.Gr = gr;
    }

    public double getB_max() {
        return B_max;
    }

    public void setB_max(double b_max) {
        this.B_max = b_max;
    }

    public double getT() {
        return T;
    }

    public void setT(double t) {
        this.T = t;
    }

    public double getBudget_max() {
        return budget_max;
    }

    public double getCurrentBudget() {
        return currentBudget;
    }

    public void initializeBudget() {
        this.currentBudget = this.budget_max;
    }

    //Hàm này được Server/Manager gọi để cập nhật ngân sách sau khi Node thắng một phiên đấu giá.
    public void deductBudget(double amount) {
        this.currentBudget -= amount;
        if (this.currentBudget < 0) {
            this.currentBudget = 0;
        }
    }


    @Override
    public String toString() {
        return id + " (" + type + ")";
    }
}

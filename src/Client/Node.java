package Client;



public class Node {
    private String id;
    private double Pt;  //công suất phát
    private double Gt;  //độ lợi anten phát
    private double Gr;// độ lợi anten nhận
    private double B_max;// băng thông tối đa
    private double T;// độ trễ tối đa
    private double budget_max;// ngân sách tối đa
    private double currentBudget;// ngân sách hiện tại
    private double acquiredResources = 0;// tài nguyên đã được cấp phát

    public Node ( String id, double Pt, double Gt, double Gr, double B_max, double T, double budget_max, double currentBudget, double acquiredResources) {
        this.id = id;
        this.Pt = Pt;
        this.Gt = Gt;
        this.Gr = Gr;
        this.B_max = B_max;
        this.T = T;
        this.budget_max = budget_max;
        this.currentBudget = currentBudget;
        this.acquiredResources = acquiredResources;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

     
    public void setB_max(int b_max) {
        this.B_max = b_max;
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

    public void setBudgetMax(double budget_max) {
        this.budget_max = budget_max;
    }

    public void setCurrentBudget(double currentBudget) {
        this.currentBudget = currentBudget;
    }

    public void setAcquiredResources(int acquiredResources) {
        this.acquiredResources = acquiredResources;
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

    public double getAcquiredResources() {
        return acquiredResources;
    }

    public void setAcquiredResources(double acquiredResources) {
        this.acquiredResources = acquiredResources;
    }

    //Hàm này được Server/Manager gọi để cập nhật ngân sách sau khi Node thắng một phiên đấu giá.
    public void deductBudget(double amount) {
        this.currentBudget -= amount;
        if (this.currentBudget < 0) {
            this.currentBudget = 0;
        }
    }


}
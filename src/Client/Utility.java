package Client;


public class Utility {
    private static final double BOLTZMANN = 1.38e-23;
    private static final double SPEED_OF_LIGHT = 3e8;

    private static final double SNR_MIN = 0;
    private static final double SNR_MAX = 30;
    private static final double LAT_MIN = 10;
    private static final double LAT_MAX = 300;

    private static final double RISK_AVERSION_K = 2.0;

    // computing path loss
    public static double computePathloss(double distanceKm, double frequencyMHz) {
        return 20 * Math.log10(distanceKm) + 20 * Math.log10(frequencyMHz) + 32.44;

    }

    // Noise power
    public static double computeNoise(double T, double B) {
        return 10 * Math.log10(BOLTZMANN * T * B) + 30;
    }

    // compute SNR
    public static double computeSNR(double Pt, double Gt, double Gr, double distanceKm, double frequencyMHz, double T, double B_alloc) {
        double PL = computePathloss(distanceKm, frequencyMHz);
        double N = computeNoise(T, B_alloc);

        return Pt + Gt + Gr - PL - N;
    }

    //Latency = Tprop + Tproc
    public static double computeLatency(double distanceKm, double Tproc) {
        double distanceMeters = distanceKm * 1000;
        double Tprop = distanceMeters / SPEED_OF_LIGHT;
        return Tprop + Tproc;
    }

    public static double normalizeSNR(double snr) {
        double norm = (snr - SNR_MIN) / (SNR_MAX - SNR_MIN);
        return Math.max(0, Math.min(norm, 1)); // đảm bảo trong [0,1]
    }

    public static double normalizeLatency(double latency) {
        double norm = (latency - LAT_MIN) / (LAT_MAX - LAT_MIN);
        norm = 1 - norm; // nghịch đảo: latency thấp → giá trị cao
        return Math.max(0, Math.min(norm, 1));
    }

    public static double computeAlpha(double snrNorm, double latencyNorm) {
        double alpha = snrNorm / (snrNorm + latencyNorm + 1e-9); // tránh chia 0
        return Math.max(0, Math.min(alpha, 1));
    }

    public static double computeValue(double snr, double latency) {
        double snrNorm = normalizeSNR(snr);
        double latNorm = normalizeLatency(latency);
        double alpha = computeAlpha(snrNorm, latNorm);

        double value = alpha * snrNorm + (1 - alpha) * latNorm;
        return value;
    }

    public static double calculateBid(
            double value,        // V_i_L: Giá trị hữu ích (đã tính từ computeValue)
            double n,           // n: Số đối thủ ước tính (từ Server)
            double budget,          // budget: Ngân sách còn lại của Node i
            double budget_max,
            double priority,  // delta_priority: Hệ số ưu tiên Link (từ Link.json)
            double b_min            // B_min: Giá sàn (từ Server)
    ) {

        if (budget <= 0 || value <= 0) {
            return 0.0;
        }

        // 1. TÍNH HỆ SỐ CẠNH TRANH (delta_comp,L)
        // Công thức: delta_comp = (n - 1) / n  hoặc (1 - 1/n)
        double competitors = Math.max(n, 2.0);
        double delta_comp = (competitors - 1.0) / competitors;


        // 2. TÍNH HỆ SỐ TÀI CHÍNH (delta_budget,i)
        // Công thức: delta_budget = (budget_i / budget_max)^k
        double budgetRatio = budget / budget_max;
        double delta_budget = Math.pow(budgetRatio, RISK_AVERSION_K);

        // 3. TÍNH HỆ SỐ CHIẾN LƯỢC TỔNG THỂ (delta_i_L)
        double delta = delta_comp * delta_budget * priority;

        // 4. TÍNH BID CƠ SỞ (Bid = V_i_L * delta_i_L)
        double Bid_base = value * delta;

        // 5. BID CUỐI CÙNG (Max(Bid Cơ sở, Giá sàn))
        // Đảm bảo Bid không thấp hơn giá sàn
        double Bid = Math.max(Bid_base, b_min);

        return Bid;
    }
}
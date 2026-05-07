package finanzmanager;

public class Steuerrechner {

    public static class Ergebnis {
        public final double brutto;
        public final double kv;
        public final double rv;
        public final double av;
        public final double pv;
        public final double lohnsteuer;
        public final double soli;
        public final double kirchensteuer;
        public final double netto;

        public Ergebnis(double brutto, double kv, double rv, double av, double pv,
                        double lohnsteuer, double soli, double kirchensteuer) {
            this.brutto = brutto;
            this.kv = kv;
            this.rv = rv;
            this.av = av;
            this.pv = pv;
            this.lohnsteuer = lohnsteuer;
            this.soli = soli;
            this.kirchensteuer = kirchensteuer;
            this.netto = round(brutto - kv - rv - av - pv - lohnsteuer - soli - kirchensteuer);
        }

        public double getSvGesamt() { return kv + rv + av + pv; }
        public double getAbzuegeGesamt() { return getSvGesamt() + lohnsteuer + soli + kirchensteuer; }
        public double getAbzugsquote() { return brutto > 0 ? (getAbzuegeGesamt() / brutto) * 100 : 0; }
    }

    public static Ergebnis berechne(double brutto, int steuerklasse,
                                    boolean kirchensteuer, double kvZusatzProzent) {
        // Sozialversicherung (Arbeitnehmeranteil)
        double kv  = round(brutto * 0.073);
        double kv2 = round(brutto * (kvZusatzProzent / 100.0) * 0.5);
        double rv  = round(brutto * 0.093);
        double av  = round(brutto * 0.013);
        double pv  = round(brutto * 0.017);
        double sv  = kv + kv2 + rv + av + pv;

        // Lohnsteuer – vereinfachte Berechnung nach Steuerklasse
        double[] steuersatz   = {0, 0.14, 0.12, 0.10, 0.14, 0.22, 0.25};
        double[] freibetragJa = {0, 12888, 12888, 25776, 12888, 0, 0};
        double frei = freibetragJa[steuerklasse] / 12.0;
        double zvE  = Math.max(0, brutto - sv - frei);
        double lst  = round(zvE * steuersatz[steuerklasse]);

        // Soli (ab 2021 nur noch für hohe Einkommen, hier vereinfacht)
        double soli = lst > 97.38 ? round(lst * 0.055) : 0.0;

        // Kirchensteuer (9 %)
        double kt = kirchensteuer ? round(lst * 0.09) : 0.0;

        return new Ergebnis(brutto, kv + kv2, rv, av, pv, lst, soli, kt);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
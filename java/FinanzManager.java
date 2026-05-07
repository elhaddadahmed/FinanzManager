import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

// ════════════════════════════════════════════════════════════════════════════
//  FINANZ MANAGER – Ahmed  (eine einzige .java Datei)
// ════════════════════════════════════════════════════════════════════════════
public class FinanzManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new AppFenster().setVisible(true);
        });
    }
}

// ── Buchung Datenmodell ──────────────────────────────────────────────────────
class Buchung {
    private static int zaehler = 1;
    private final int id;
    private String beschreibung, kategorie, notiz;
    private double betrag;
    private LocalDate datum;
    private final boolean istEinnahme;

    public Buchung(String beschreibung, double betrag, String kategorie,
                   LocalDate datum, String notiz, boolean istEinnahme) {
        this.id = zaehler++;
        this.beschreibung = beschreibung;
        this.betrag = betrag;
        this.kategorie = kategorie;
        this.datum = datum;
        this.notiz = notiz;
        this.istEinnahme = istEinnahme;
    }

    public int getId()               { return id; }
    public String getBeschreibung()  { return beschreibung; }
    public double getBetrag()        { return betrag; }
    public String getKategorie()     { return kategorie; }
    public LocalDate getDatum()      { return datum; }
    public String getNotiz()         { return notiz; }
    public boolean isEinnahme()      { return istEinnahme; }
    public void setBeschreibung(String v) { beschreibung = v; }
    public void setBetrag(double v)       { betrag = v; }
    public void setKategorie(String v)    { kategorie = v; }
    public void setDatum(LocalDate v)     { datum = v; }
    public void setNotiz(String v)        { notiz = v; }
}

// ── Steuerrechner ────────────────────────────────────────────────────────────
class Steuerrechner {
    static class Ergebnis {
        final double brutto, kv, rv, av, pv, lohnsteuer, soli, kirchensteuer, netto;
        Ergebnis(double brutto, double kv, double rv, double av, double pv,
                 double lohnsteuer, double soli, double kirchensteuer) {
            this.brutto = brutto; this.kv = kv; this.rv = rv;
            this.av = av; this.pv = pv; this.lohnsteuer = lohnsteuer;
            this.soli = soli; this.kirchensteuer = kirchensteuer;
            this.netto = r(brutto - kv - rv - av - pv - lohnsteuer - soli - kirchensteuer);
        }
        double svGesamt()      { return kv + rv + av + pv; }
        double abzuegeGesamt() { return svGesamt() + lohnsteuer + soli + kirchensteuer; }
        double abzugsquote()   { return brutto > 0 ? (abzuegeGesamt() / brutto) * 100 : 0; }
    }

    static Ergebnis berechne(double brutto, int klasse, boolean kirche, double kvZusatz) {
        double kv  = r(brutto * 0.073);
        double kv2 = r(brutto * (kvZusatz / 100.0) * 0.5);
        double rv  = r(brutto * 0.093);
        double av  = r(brutto * 0.013);
        double pv  = r(brutto * 0.017);
        double sv  = kv + kv2 + rv + av + pv;
        double[] satz  = {0, 0.14, 0.12, 0.10, 0.14, 0.22, 0.25};
        double[] frei  = {0, 12888, 12888, 25776, 12888, 0, 0};
        double zvE = Math.max(0, brutto - sv - frei[klasse] / 12.0);
        double lst = r(zvE * satz[klasse]);
        double sol = lst > 97.38 ? r(lst * 0.055) : 0.0;
        double kt  = kirche ? r(lst * 0.09) : 0.0;
        return new Ergebnis(brutto, kv + kv2, rv, av, pv, lst, sol, kt);
    }

    private static double r(double v) { return Math.round(v * 100.0) / 100.0; }
}

// ── Haupt-Fenster ────────────────────────────────────────────────────────────
class AppFenster extends JFrame {

    static final Color BG     = new Color(250, 250, 248);
    static final Color WHITE  = Color.WHITE;
    static final Color GREEN  = new Color(59, 109, 17);
    static final Color RED    = new Color(163, 45, 45);
    static final Color BLUE   = new Color(24, 95, 165);
    static final Color AMBER  = new Color(133, 79, 11);
    static final Color GRAY   = new Color(100, 100, 95);
    static final Color BORDER = new Color(220, 220, 215);
    static final Font FN = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FB = new Font("SansSerif", Font.BOLD, 13);
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    final List<Buchung> buchungen = new ArrayList<>();
    JLabel lblEin, lblAus, lblBilanz, lblSpar;
    DefaultTableModel einModel, ausModel;
    JTable einTabelle, ausTabelle;
    JComboBox<String> filterKat;
    JPanel dashEin, dashAus, dashBilanz;
    StatistikComp statistik;
    JTabbedPane tabs;

    AppFenster() {
        setTitle("Finanz-Manager – Ahmed");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Beispieldaten
        buchungen.add(new Buchung("Gehalt Mai (Netto)", 834.40, "Gehalt", LocalDate.of(2026,5,1), "Brutto: €1100, Kl.1", true));
        buchungen.add(new Buchung("Miete", 320.00, "Wohnen",        LocalDate.of(2026,5,1), "", false));
        buchungen.add(new Buchung("Rewe",   45.80, "Lebensmittel",  LocalDate.of(2026,5,2), "Wocheneinkauf", false));
        buchungen.add(new Buchung("BVG",    36.00, "Transport",     LocalDate.of(2026,5,1), "", false));
        buchungen.add(new Buchung("Lidl",   28.50, "Lebensmittel",  LocalDate.of(2026,5,4), "", false));
        buchungen.add(new Buchung("Kino",   12.00, "Freizeit",      LocalDate.of(2026,5,5), "", false));

        add(baueHeader(), BorderLayout.NORTH);
        add(baueInhalt(), BorderLayout.CENTER);
        aktualisiereAlles();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    JPanel baueHeader() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.setBorder(new EmptyBorder(14,16,6,16));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        JPanel tBox = new JPanel(); tBox.setLayout(new BoxLayout(tBox, BoxLayout.Y_AXIS)); tBox.setBackground(BG);
        JLabel t = new JLabel("Meine Finanzen"); t.setFont(new Font("SansSerif", Font.BOLD, 21));
        JLabel s = new JLabel("Mai 2026  ·  Ahmed"); s.setFont(new Font("SansSerif", Font.PLAIN, 12)); s.setForeground(GRAY);
        tBox.add(t); tBox.add(s);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); btns.setBackground(BG);
        btns.add(mkBtn("+ Einnahme", new Color(234,243,222), GREEN, new Color(192,221,151), () -> neueEinnahme()));
        btns.add(mkBtn("− Ausgabe",  new Color(252,235,235), RED,   new Color(247,193,193), () -> neueAusgabe()));
        top.add(tBox, BorderLayout.WEST); top.add(btns, BorderLayout.EAST);

        JPanel metrics = new JPanel(new GridLayout(1,4,10,0));
        metrics.setBackground(BG); metrics.setBorder(new EmptyBorder(10,0,0,0));
        lblEin    = mkMetric(metrics, "Einnahmen",  GREEN);
        lblAus    = mkMetric(metrics, "Ausgaben",   RED);
        lblBilanz = mkMetric(metrics, "Bilanz",     BLUE);
        lblSpar   = mkMetric(metrics, "Sparquote",  AMBER);

        outer.add(top,     BorderLayout.NORTH);
        outer.add(metrics, BorderLayout.CENTER);
        return outer;
    }

    JButton mkBtn(String text, Color bg, Color fg, Color border, Runnable r) {
        JButton b = new JButton(text); b.setFont(FB); b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(border,1,true), new EmptyBorder(7,14,7,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> r.run()); return b;
    }

    JLabel mkMetric(JPanel parent, String label, Color farbe) {
        JPanel card = new JPanel(new BorderLayout(0,4)); card.setBackground(new Color(245,245,242));
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(10,14,10,14)));
        JLabel l = new JLabel(label); l.setFont(new Font("SansSerif",Font.PLAIN,11)); l.setForeground(GRAY);
        JLabel v = new JLabel("€ 0,00"); v.setFont(new Font("SansSerif",Font.BOLD,19)); v.setForeground(farbe);
        card.add(l, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER); parent.add(card); return v;
    }

    // ── Inhalt ────────────────────────────────────────────────────────────────
    JPanel baueInhalt() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG);
        p.setBorder(new EmptyBorder(6,16,14,16));
        tabs = new JTabbedPane(); tabs.setFont(FN);
        tabs.addTab("🏠  Dashboard",     baueDashboard());
        tabs.addTab("🧾  Gehaltszettel", baueGehaltsTab());
        tabs.addTab("📈  Einnahmen",      baueEinnahmenTab());
        tabs.addTab("📉  Ausgaben",       baueAusgabenTab());
        tabs.addTab("📊  Statistik",      baueStatistikTab());
        p.add(tabs); return p;
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    JPanel baueDashboard() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setBackground(BG);
        p.setBorder(new EmptyBorder(10,0,0,0));
        JPanel grid = new JPanel(new GridLayout(1,2,12,0)); grid.setBackground(BG);
        dashEin = new JPanel(); dashEin.setLayout(new BoxLayout(dashEin, BoxLayout.Y_AXIS)); dashEin.setBackground(WHITE);
        dashAus = new JPanel(); dashAus.setLayout(new BoxLayout(dashAus, BoxLayout.Y_AXIS)); dashAus.setBackground(WHITE);
        grid.add(wrapCard(dashEin, "Letzte Einnahmen"));
        grid.add(wrapCard(dashAus, "Letzte Ausgaben"));
        dashBilanz = new JPanel(new BorderLayout(0,8)); dashBilanz.setBackground(WHITE);
        dashBilanz.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(12,14,12,14)));
        p.add(grid, BorderLayout.CENTER); p.add(dashBilanz, BorderLayout.SOUTH); return p;
    }

    JPanel wrapCard(JPanel body, String titel) {
        JPanel card = new JPanel(new BorderLayout(0,8)); card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(12,14,12,14)));
        JLabel t = new JLabel(titel); t.setFont(FB); card.add(t, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(body); sp.setBorder(null); sp.setBackground(WHITE);
        sp.getViewport().setBackground(WHITE); card.add(sp, BorderLayout.CENTER); return card;
    }

    // ── Gehaltszettel Tab ─────────────────────────────────────────────────────
    JTextField gBrutto; JComboBox<String> gKlasse; JCheckBox gKirche; JSpinner gKvZusatz; JPanel gErgebnis;

    JPanel baueGehaltsTab() {
        JPanel p = new JPanel(new GridLayout(1,2,12,0)); p.setBackground(BG);
        p.setBorder(new EmptyBorder(10,0,0,0));

        // Eingabe
        JPanel eingabe = new JPanel(); eingabe.setLayout(new BoxLayout(eingabe, BoxLayout.Y_AXIS));
        eingabe.setBackground(WHITE);
        eingabe.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(14,14,14,14)));

        JLabel titel = new JLabel("Gehaltsrechner"); titel.setFont(new Font("SansSerif",Font.BOLD,15));
        titel.setAlignmentX(Component.LEFT_ALIGNMENT); eingabe.add(titel); eingabe.add(Box.createVerticalStrut(10));

        gBrutto = mkFeld("Brutto-Monatsgehalt (€)", "1100", eingabe);
        eingabe.add(Box.createVerticalStrut(6));
        eingabe.add(mkLabel("Steuerklasse"));
        gKlasse = new JComboBox<>(new String[]{
            "Klasse 1 – ledig","Klasse 2 – alleinerziehend","Klasse 3 – verheiratet (höher)",
            "Klasse 4 – verheiratet (gleich)","Klasse 5 – verheiratet (niedriger)","Klasse 6 – Zweitjob"
        });
        gKlasse.setFont(FN); gKlasse.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        gKlasse.setAlignmentX(Component.LEFT_ALIGNMENT); eingabe.add(gKlasse);
        eingabe.add(Box.createVerticalStrut(8));
        gKirche = new JCheckBox("Kirchensteuer (9%)"); gKirche.setFont(FN); gKirche.setBackground(WHITE);
        gKirche.setAlignmentX(Component.LEFT_ALIGNMENT); eingabe.add(gKirche);
        eingabe.add(Box.createVerticalStrut(8));
        eingabe.add(mkLabel("KV-Zusatzbeitrag (%)"));
        gKvZusatz = new JSpinner(new SpinnerNumberModel(1.6, 0.0, 5.0, 0.1));
        gKvZusatz.setFont(FN); gKvZusatz.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        gKvZusatz.setAlignmentX(Component.LEFT_ALIGNMENT); eingabe.add(gKvZusatz);
        eingabe.add(Box.createVerticalStrut(14));

        JButton btnBer = mkBtn("Berechnen", new Color(30,30,28), WHITE, new Color(30,30,28), this::gehaltszettelBerechnen);
        btnBer.setAlignmentX(Component.LEFT_ALIGNMENT); btnBer.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); eingabe.add(btnBer);
        eingabe.add(Box.createVerticalStrut(6));
        JButton btnUeb = mkBtn("✓ Als Einnahme übernehmen", new Color(234,243,222), GREEN, new Color(192,221,151), this::gehaltszettelUebernehmen);
        btnUeb.setAlignmentX(Component.LEFT_ALIGNMENT); btnUeb.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); eingabe.add(btnUeb);
        eingabe.add(Box.createVerticalGlue());

        // Ergebnis
        gErgebnis = new JPanel(new BorderLayout()); gErgebnis.setBackground(BG);

        p.add(eingabe); p.add(gErgebnis);
        gehaltszettelBerechnen();
        return p;
    }

    JTextField mkFeld(String label, String def, JPanel parent) {
        parent.add(mkLabel(label));
        JTextField f = new JTextField(def); f.setFont(FN);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        f.setAlignmentX(Component.LEFT_ALIGNMENT); parent.add(f);
        return f;
    }
    JLabel mkLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("SansSerif",Font.PLAIN,11));
        l.setForeground(GRAY); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }

    void gehaltszettelBerechnen() {
        double brutto; try { brutto = Double.parseDouble(gBrutto.getText().replace(",",".")); } catch(Exception e){return;}
        int kl = gKlasse.getSelectedIndex()+1;
        double kvZ = ((Number)gKvZusatz.getValue()).doubleValue();
        Steuerrechner.Ergebnis r = Steuerrechner.berechne(brutto, kl, gKirche.isSelected(), kvZ);

        JPanel card = new JPanel(); card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBackground(WHITE); card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(14,14,14,14)));

        JLabel name = new JLabel("Ahmed – Steuerklasse "+kl); name.setFont(new Font("SansSerif",Font.BOLD,15)); name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub  = new JLabel("Monatsabrechnung Mai 2026"); sub.setFont(new Font("SansSerif",Font.PLAIN,11)); sub.setForeground(GRAY); sub.setAlignmentX(LEFT_ALIGNMENT);
        card.add(name); card.add(sub); card.add(sep());

        card.add(slipRow("Brutto", fmt(r.brutto), Color.BLACK, true));
        card.add(secLabel("SOZIALVERSICHERUNG"));
        card.add(slipRow("Krankenversicherung",    "-"+fmt(r.kv),          RED, false));
        card.add(slipRow("Rentenversicherung",      "-"+fmt(r.rv),          RED, false));
        card.add(slipRow("Arbeitslosenvers.",       "-"+fmt(r.av),          RED, false));
        card.add(slipRow("Pflegeversicherung",      "-"+fmt(r.pv),          RED, false));
        card.add(secLabel("STEUERN"));
        card.add(slipRow("Lohnsteuer",              "-"+fmt(r.lohnsteuer),  RED, false));
        card.add(slipRow("Solidaritätszuschlag",    "-"+fmt(r.soli),        RED, false));
        if(r.kirchensteuer>0) card.add(slipRow("Kirchensteuer","-"+fmt(r.kirchensteuer), RED, false));
        card.add(sep());

        JPanel nettoRow = new JPanel(new BorderLayout()); nettoRow.setBackground(WHITE);
        nettoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,34)); nettoRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel nLbl = new JLabel("Nettolohn"); nLbl.setFont(new Font("SansSerif",Font.BOLD,15));
        JLabel nVal = new JLabel(fmt(r.netto)); nVal.setFont(new Font("SansSerif",Font.BOLD,20)); nVal.setForeground(BLUE);
        nettoRow.add(nLbl,BorderLayout.WEST); nettoRow.add(nVal,BorderLayout.EAST);
        card.add(Box.createVerticalStrut(4)); card.add(nettoRow); card.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(1,2,8,0)); grid.setBackground(WHITE);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,54)); grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.add(miniCard("Abzüge gesamt", fmt(r.abzuegeGesamt()), RED));
        grid.add(miniCard("Abzugsquote", String.format("%.1f %%", r.abzugsquote()), RED));
        card.add(grid);

        gErgebnis.removeAll(); gErgebnis.add(card, BorderLayout.NORTH);
        gErgebnis.revalidate(); gErgebnis.repaint();
    }

    void gehaltszettelUebernehmen() {
        double brutto; try { brutto = Double.parseDouble(gBrutto.getText().replace(",",".")); } catch(Exception e){return;}
        int kl = gKlasse.getSelectedIndex()+1;
        double kvZ = ((Number)gKvZusatz.getValue()).doubleValue();
        Steuerrechner.Ergebnis r = Steuerrechner.berechne(brutto, kl, gKirche.isSelected(), kvZ);
        buchungen.add(new Buchung("Gehalt Mai (Netto)", r.netto, "Gehalt", LocalDate.now(),
            String.format("Brutto: %s, Kl.%d", fmt(brutto), kl), true));
        aktualisiereAlles(); tabs.setSelectedIndex(2);
        JOptionPane.showMessageDialog(this, "Netto "+fmt(r.netto)+" als Einnahme gespeichert!", "Gespeichert", JOptionPane.INFORMATION_MESSAGE);
    }

    JPanel slipRow(String label, String val, Color farbe, boolean bold) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,26)); p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label); l.setFont(FN); l.setForeground(GRAY);
        JLabel v = new JLabel(val);   v.setFont(bold ? FB : FN); v.setForeground(farbe);
        p.add(l,BorderLayout.WEST); p.add(v,BorderLayout.EAST); return p;
    }
    JLabel secLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("SansSerif",Font.PLAIN,10)); l.setForeground(GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT); l.setBorder(new EmptyBorder(5,0,2,0)); return l;
    }
    JSeparator sep() {
        JSeparator s = new JSeparator(); s.setMaximumSize(new Dimension(Integer.MAX_VALUE,8)); s.setAlignmentX(LEFT_ALIGNMENT); return s;
    }
    JPanel miniCard(String label, String val, Color col) {
        JPanel p = new JPanel(new BorderLayout(0,4)); p.setBackground(new Color(245,245,242));
        p.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(8,10,8,10)));
        JLabel l = new JLabel(label); l.setFont(new Font("SansSerif",Font.PLAIN,11)); l.setForeground(GRAY);
        JLabel v = new JLabel(val);   v.setFont(new Font("SansSerif",Font.BOLD,14));  v.setForeground(col);
        p.add(l,BorderLayout.NORTH); p.add(v,BorderLayout.CENTER); return p;
    }

    // ── Einnahmen Tab ─────────────────────────────────────────────────────────
    JPanel baueEinnahmenTab() {
        einModel = new DefaultTableModel(new String[]{"Datum","Beschreibung","Kategorie","Betrag","Notiz","Aktion"},0){public boolean isCellEditable(int r,int c){return c==5;}};
        einTabelle = mkTabelle(einModel);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.setBorder(new EmptyBorder(10,0,0,0));
        JPanel card = new JPanel(new BorderLayout(0,8)); card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(10,10,10,10)));
        JLabel t = new JLabel("Alle Einnahmen"); t.setFont(FB); card.add(t,BorderLayout.NORTH);
        card.add(new JScrollPane(einTabelle), BorderLayout.CENTER); p.add(card); return p;
    }

    // ── Ausgaben Tab ──────────────────────────────────────────────────────────
    JPanel baueAusgabenTab() {
        ausModel = new DefaultTableModel(new String[]{"Datum","Beschreibung","Kategorie","Betrag","Notiz","Aktion"},0){public boolean isCellEditable(int r,int c){return c==5;}};
        ausTabelle = mkTabelle(ausModel);
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.setBorder(new EmptyBorder(10,0,0,0));
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); topBar.setBackground(WHITE);
        topBar.add(new JLabel("Kategorie:") {{ setFont(FN); setForeground(GRAY); }});
        filterKat = new JComboBox<>(new String[]{"Alle","Wohnen","Lebensmittel","Transport","Gesundheit","Freizeit","Kleidung","Sonstiges"});
        filterKat.setFont(FN); filterKat.addActionListener(e -> renderAusgaben()); topBar.add(filterKat);
        JPanel card = new JPanel(new BorderLayout(0,8)); card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(10,10,10,10)));
        JLabel t = new JLabel("Alle Ausgaben"); t.setFont(FB); card.add(t,BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout(0,6)); inner.setBackground(WHITE);
        inner.add(topBar,BorderLayout.NORTH); inner.add(new JScrollPane(ausTabelle),BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER); p.add(card); return p;
    }

    JTable mkTabelle(DefaultTableModel model) {
        JTable t = new JTable(model); t.setFont(FN); t.setRowHeight(28);
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
        t.getTableHeader().setBackground(new Color(245,245,242));
        t.setGridColor(new Color(235,235,230)); t.setSelectionBackground(new Color(230,240,255));
        t.setShowVerticalLines(false); return t;
    }

    // ── Statistik Tab ─────────────────────────────────────────────────────────
    JPanel baueStatistikTab() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG); p.setBorder(new EmptyBorder(10,0,0,0));
        statistik = new StatistikComp(buchungen); p.add(statistik); return p;
    }

    // ── Aktionen ──────────────────────────────────────────────────────────────
    void neueEinnahme() {
        EingabeDialog dlg = new EingabeDialog(this, true, null);
        dlg.setVisible(true);
        if(dlg.ergebnis!=null){ buchungen.add(dlg.ergebnis); aktualisiereAlles(); }
    }
    void neueAusgabe() {
        EingabeDialog dlg = new EingabeDialog(this, false, null);
        dlg.setVisible(true);
        if(dlg.ergebnis!=null){ buchungen.add(dlg.ergebnis); aktualisiereAlles(); }
    }
    void loeschen(int id) {
        if(JOptionPane.showConfirmDialog(this,"Buchung löschen?","Löschen",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            buchungen.removeIf(b->b.getId()==id); aktualisiereAlles();
        }
    }

    // ── Aktualisierung ────────────────────────────────────────────────────────
    void aktualisiereAlles() {
        double ein = buchungen.stream().filter(Buchung::isEinnahme).mapToDouble(Buchung::getBetrag).sum();
        double aus = buchungen.stream().filter(b->!b.isEinnahme()).mapToDouble(Buchung::getBetrag).sum();
        double bil = ein-aus; double spar = ein>0?(bil/ein)*100:0;
        lblEin  .setText(fmt(ein));
        lblAus  .setText(fmt(aus));
        lblBilanz.setText(fmt(bil)); lblBilanz.setForeground(bil>=0?GREEN:RED);
        lblSpar .setText(String.format("%.0f %%",spar)); lblSpar.setForeground(spar>=10?GREEN:AMBER);

        // Dashboard
        dashEin.removeAll();
        buchungen.stream().filter(Buchung::isEinnahme).sorted(Comparator.comparing(Buchung::getDatum).reversed()).limit(5)
            .forEach(b->dashEin.add(dRow(b.getBeschreibung(),"+"+fmt(b.getBetrag()),GREEN)));
        if(buchungen.stream().noneMatch(Buchung::isEinnahme)) dashEin.add(new JLabel("Keine Einnahmen"){{setFont(FN);setForeground(GRAY);}});
        dashEin.add(Box.createVerticalGlue()); dashEin.revalidate(); dashEin.repaint();

        dashAus.removeAll();
        buchungen.stream().filter(b->!b.isEinnahme()).sorted(Comparator.comparing(Buchung::getDatum).reversed()).limit(5)
            .forEach(b->dashAus.add(dRow(b.getBeschreibung(),"-"+fmt(b.getBetrag()),RED)));
        if(buchungen.stream().noneMatch(b->!b.isEinnahme())) dashAus.add(new JLabel("Keine Ausgaben"){{setFont(FN);setForeground(GRAY);}});
        dashAus.add(Box.createVerticalGlue()); dashAus.revalidate(); dashAus.repaint();

        dashBilanz.removeAll();
        boolean pos = bil>=0;
        JLabel bt = new JLabel("Monatsbilanz"); bt.setFont(FB);
        JPanel bi = new JPanel(new FlowLayout(FlowLayout.LEFT,16,0));
        bi.setBackground(pos?new Color(234,243,222):new Color(252,235,235));
        bi.setBorder(new CompoundBorder(new LineBorder(pos?new Color(192,221,151):new Color(247,193,193),1,true),new EmptyBorder(8,12,8,12)));
        JLabel bv = new JLabel(fmt(bil)); bv.setFont(new Font("SansSerif",Font.BOLD,18)); bv.setForeground(pos?GREEN:RED);
        JLabel bx = new JLabel(pos?"✓ Diesen Monat gespart":"⚠ Mehr ausgegeben als eingenommen"); bx.setFont(FN); bx.setForeground(pos?GREEN:RED);
        bi.add(bv); bi.add(bx);
        dashBilanz.add(bt,BorderLayout.NORTH); dashBilanz.add(bi,BorderLayout.CENTER);
        dashBilanz.revalidate(); dashBilanz.repaint();

        // Tabellen
        renderEinnahmen(); renderAusgaben();
        if(statistik!=null) statistik.aktualisiere(buchungen);
    }

    JPanel dRow(String label, String val, Color farbe) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE,26));
        p.setBorder(new MatteBorder(0,0,1,0,new Color(240,240,238)));
        JLabel l = new JLabel(label); l.setFont(FN);
        JLabel v = new JLabel(val);   v.setFont(FB); v.setForeground(farbe);
        p.add(l,BorderLayout.WEST); p.add(v,BorderLayout.EAST); return p;
    }

    void renderEinnahmen() {
        einModel.setRowCount(0);
        buchungen.stream().filter(Buchung::isEinnahme)
            .sorted(Comparator.comparing(Buchung::getDatum).reversed())
            .forEach(b->einModel.addRow(new Object[]{b.getDatum().format(FMT),b.getBeschreibung(),b.getKategorie(),fmt(b.getBetrag()),b.getNotiz(),"🗑 ["+b.getId()+"]"}));
        installLoeschBtn(einTabelle, einModel);
    }

    void renderAusgaben() {
        ausModel.setRowCount(0);
        String f = filterKat!=null?(String)filterKat.getSelectedItem():"Alle";
        buchungen.stream().filter(b->!b.isEinnahme())
            .filter(b->"Alle".equals(f)||b.getKategorie().equals(f))
            .sorted(Comparator.comparing(Buchung::getDatum).reversed())
            .forEach(b->ausModel.addRow(new Object[]{b.getDatum().format(FMT),b.getBeschreibung(),b.getKategorie(),fmt(b.getBetrag()),b.getNotiz(),"🗑 ["+b.getId()+"]"}));
        installLoeschBtn(ausTabelle, ausModel);
    }

    void installLoeschBtn(JTable tab, DefaultTableModel model) {
        tab.getColumnModel().getColumn(5).setCellRenderer((t,v,s,h,r,c)->{
            JButton b=new JButton("🗑 Löschen"); b.setFont(new Font("SansSerif",Font.PLAIN,11));
            b.setForeground(RED); b.setBackground(WHITE); b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(2,6,2,6)); return b;
        });
        tab.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private int row = -1;
            { editorComponent.setVisible(false); }
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                row = r; return editorComponent;
            }
            public boolean stopCellEditing() {
                if (row >= 0 && row < model.getRowCount()) {
                    try {
                        String cell = (String) model.getValueAt(row, 5);
                        int id = Integer.parseInt(cell.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                        SwingUtilities.invokeLater(() -> loeschen(id));
                    } catch (Exception ignored) {}
                }
                row = -1; return super.stopCellEditing();
            }
        });
    }

    static String fmt(double v) { return String.format("€ %,.2f", v); }
}

// ── Eingabe Dialog ────────────────────────────────────────────────────────────
class EingabeDialog extends JDialog {
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    Buchung ergebnis = null;
    JTextField fDesc, fAmt, fDate, fNote;
    JComboBox<String> fKat;
    static final String[] KAT_E = {"Gehalt","Freelance","Kindergeld","Sonstiges"};
    static final String[] KAT_A = {"Wohnen","Lebensmittel","Transport","Gesundheit","Freizeit","Kleidung","Sonstiges"};

    EingabeDialog(Frame owner, boolean istEinnahme, Buchung v) {
        super(owner, v==null?(istEinnahme?"Einnahme hinzufügen":"Ausgabe hinzufügen"):(istEinnahme?"Einnahme bearbeiten":"Ausgabe bearbeiten"), true);
        setSize(420,295); setLocationRelativeTo(owner); setResizable(false);

        JPanel p = new JPanel(new GridBagLayout()); p.setBorder(new EmptyBorder(16,16,8,16)); p.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints(); c.insets=new Insets(4,4,4,4); c.fill=GridBagConstraints.HORIZONTAL;
        Font fn = new Font("SansSerif",Font.PLAIN,13);

        fDesc = new JTextField(v!=null?v.getBeschreibung():"");
        fAmt  = new JTextField(v!=null?String.format("%.2f",v.getBetrag()):"");
        fKat  = new JComboBox<>(istEinnahme?KAT_E:KAT_A);
        fDate = new JTextField(v!=null?v.getDatum().format(FMT):LocalDate.now().format(FMT));
        fNote = new JTextField(v!=null?v.getNotiz():"");
        if(v!=null) fKat.setSelectedItem(v.getKategorie());
        for(JComponent x:new JComponent[]{fDesc,fAmt,fDate,fNote,fKat}) x.setFont(fn);

        addRow(p,c,fn,0,"Beschreibung:",fDesc); addRow(p,c,fn,1,"Betrag (€):",fAmt);
        addRow(p,c,fn,2,"Kategorie:",fKat);     addRow(p,c,fn,3,"Datum (TT.MM.JJJJ):",fDate);
        addRow(p,c,fn,4,"Notiz:",fNote);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8)); btns.setBackground(Color.WHITE);
        JButton ab = new JButton("Abbrechen"); ab.setFont(fn); ab.addActionListener(e->dispose());
        JButton sp = new JButton("Speichern");
        sp.setFont(new Font("SansSerif",Font.BOLD,13)); sp.setBackground(new Color(30,30,28)); sp.setForeground(Color.WHITE); sp.setFocusPainted(false);
        sp.addActionListener(e->onSpeichern(v, istEinnahme));
        btns.add(ab); btns.add(sp);

        setLayout(new BorderLayout()); add(p,BorderLayout.CENTER); add(btns,BorderLayout.SOUTH);
        getContentPane().setBackground(Color.WHITE);
    }

    void addRow(JPanel p, GridBagConstraints c, Font fn, int row, String lbl, JComponent field) {
        c.gridx=0;c.gridy=row;c.weightx=0.35;
        JLabel l=new JLabel(lbl); l.setFont(new Font("SansSerif",Font.PLAIN,12)); l.setForeground(new Color(100,100,95)); p.add(l,c);
        c.gridx=1;c.weightx=0.65; p.add(field,c);
    }

    void onSpeichern(Buchung v, boolean istEinnahme) {
        String desc = fDesc.getText().trim();
        if(desc.isEmpty()){err("Bitte Beschreibung eingeben.");return;}
        double amt; try{amt=Double.parseDouble(fAmt.getText().replace(",","."));}catch(Exception e){err("Ungültiger Betrag.");return;}
        if(amt<=0){err("Betrag muss > 0 sein.");return;}
        LocalDate datum; try{datum=LocalDate.parse(fDate.getText().trim(),FMT);}catch(Exception e){err("Datum: TT.MM.JJJJ");return;}
        String kat=(String)fKat.getSelectedItem(), note=fNote.getText().trim();
        if(v!=null){v.setBeschreibung(desc);v.setBetrag(amt);v.setKategorie(kat);v.setDatum(datum);v.setNotiz(note);ergebnis=v;}
        else ergebnis=new Buchung(desc,amt,kat,datum,note,istEinnahme);
        dispose();
    }
    void err(String m){JOptionPane.showMessageDialog(this,m,"Fehler",JOptionPane.ERROR_MESSAGE);}
}

// ── Statistik Komponente ──────────────────────────────────────────────────────
class StatistikComp extends JPanel {
    private List<Buchung> buchungen;
    static final Color[] A_FARBEN = {new Color(181,212,244),new Color(159,225,203),new Color(250,199,117),new Color(244,192,209),new Color(192,221,151),new Color(175,169,236),new Color(211,209,199)};
    static final Color[] E_FARBEN = {new Color(181,212,244),new Color(159,225,203),new Color(192,221,151),new Color(211,209,199)};
    static final String[] KAT_A = {"Wohnen","Lebensmittel","Transport","Gesundheit","Freizeit","Kleidung","Sonstiges"};
    static final String[] KAT_E = {"Gehalt","Freelance","Kindergeld","Sonstiges"};
    static final Color BG=new Color(250,250,248), WHITE=Color.WHITE, BORDER=new Color(220,220,215);
    static final Color GREEN=new Color(59,109,17), RED=new Color(163,45,45), GRAY=new Color(100,100,95);
    static final Font FN=new Font("SansSerif",Font.PLAIN,13), FB=new Font("SansSerif",Font.BOLD,13);

    StatistikComp(List<Buchung> b) { this.buchungen=b; rebuild(); }

    void aktualisiere(List<Buchung> b) { this.buchungen=b; removeAll(); rebuild(); revalidate(); repaint(); }

    void rebuild() {
        setBackground(BG); setLayout(new GridLayout(1,2,12,0)); setBorder(new EmptyBorder(0,0,0,0));
        add(baueChart("Ausgaben nach Kategorie", KAT_A, A_FARBEN, false));
        add(baueChart("Einnahmen nach Kategorie", KAT_E, E_FARBEN, true));
    }

    JPanel baueChart(String titel, String[] kats, Color[] farben, boolean ein) {
        JPanel card = new JPanel(new BorderLayout(0,10)); card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER,1,true), new EmptyBorder(14,14,14,14)));
        JLabel t=new JLabel(titel); t.setFont(FB); card.add(t,BorderLayout.NORTH);

        Map<String,Double> sum = new LinkedHashMap<>();
        for(String k:kats) sum.put(k,0.0);
        for(Buchung b:buchungen) if(b.isEinnahme()==ein&&sum.containsKey(b.getKategorie())) sum.merge(b.getKategorie(),b.getBetrag(),Double::sum);
        double max=sum.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);

        JPanel bars=new JPanel(); bars.setLayout(new BoxLayout(bars,BoxLayout.Y_AXIS)); bars.setBackground(WHITE);
        for(int i=0;i<kats.length;i++){
            String k=kats[i]; double v=sum.getOrDefault(k,0.0); int pct=max>0?(int)(v/max*100):0;
            JPanel row=new JPanel(new BorderLayout(8,0)); row.setBackground(WHITE);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,28)); row.setAlignmentX(LEFT_ALIGNMENT);
            JLabel lbl=new JLabel(k); lbl.setFont(FN); lbl.setForeground(GRAY); lbl.setPreferredSize(new Dimension(110,20)); lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            BalkenComp bar=new BalkenComp(pct,farben[i%farben.length]); bar.setPreferredSize(new Dimension(0,18));
            JLabel vl=new JLabel(String.format("€ %,.0f",v)); vl.setFont(new Font("SansSerif",Font.PLAIN,11)); vl.setForeground(GRAY); vl.setPreferredSize(new Dimension(72,20));
            row.add(lbl,BorderLayout.WEST); row.add(bar,BorderLayout.CENTER); row.add(vl,BorderLayout.EAST);
            bars.add(row); bars.add(Box.createVerticalStrut(5));
        }
        card.add(bars,BorderLayout.CENTER);
        double gesamt=sum.values().stream().mapToDouble(Double::doubleValue).sum();
        JLabel total=new JLabel(String.format("Gesamt: € %,.2f",gesamt)); total.setFont(FB); total.setForeground(ein?GREEN:RED);
        card.add(total,BorderLayout.SOUTH); return card;
    }
}

class BalkenComp extends JComponent {
    int pct; Color farbe;
    BalkenComp(int pct,Color farbe){this.pct=pct;this.farbe=farbe;}
    protected void paintComponent(Graphics g){
        Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight();
        g2.setColor(new Color(240,240,238)); g2.fillRoundRect(0,0,w,h,6,6);
        int bw=(int)(w*pct/100.0); if(bw>0){g2.setColor(farbe);g2.fillRoundRect(0,0,bw,h,6,6);}
    }
}
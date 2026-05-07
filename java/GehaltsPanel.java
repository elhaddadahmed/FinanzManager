package finanzmanager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.function.Consumer;

public class GehaltsPanel extends JPanel {

    private JTextField fBrutto;
    private JComboBox<String> fKlasse;
    private JCheckBox fKirche;
    private JSpinner fKvZusatz;
    private JPanel ergebnisPanel;
    private Consumer<Buchung> onUebernehmen;

    private static final Color BG    = new Color(250, 250, 248);
    private static final Color GREEN = new Color(59, 109, 17);
    private static final Color RED   = new Color(163, 45, 45);
    private static final Color BLUE  = new Color(24, 95, 165);
    private static final Color GRAY  = new Color(100, 100, 95);
    private static final Font  FN    = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  FB    = new Font("SansSerif", Font.BOLD, 13);

    public GehaltsPanel(Consumer<Buchung> onUebernehmen) {
        this.onUebernehmen = onUebernehmen;
        setBackground(BG);
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(baueEingabe(), BorderLayout.WEST);
        ergebnisPanel = new JPanel(new BorderLayout());
        ergebnisPanel.setBackground(BG);
        add(ergebnisPanel, BorderLayout.CENTER);
        berechne();
    }

    private JPanel baueEingabe() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 215), 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));
        p.setPreferredSize(new Dimension(240, 0));

        p.add(sectionLabel("Gehaltsrechner"));
        p.add(Box.createVerticalStrut(8));

        fBrutto = new JTextField("1100");
        fBrutto.setFont(FN);
        fBrutto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(feldLabel("Brutto-Monatsgehalt (€)"));
        p.add(fBrutto);
        p.add(Box.createVerticalStrut(8));

        String[] klassen = {
            "Klasse 1 – ledig",
            "Klasse 2 – alleinerziehend",
            "Klasse 3 – verheiratet (höher)",
            "Klasse 4 – verheiratet (gleich)",
            "Klasse 5 – verheiratet (niedriger)",
            "Klasse 6 – Zweitjob"
        };
        fKlasse = new JComboBox<>(klassen);
        fKlasse.setFont(FN);
        fKlasse.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(feldLabel("Steuerklasse"));
        p.add(fKlasse);
        p.add(Box.createVerticalStrut(8));

        fKirche = new JCheckBox("Kirchensteuer (9%)");
        fKirche.setFont(FN);
        fKirche.setBackground(Color.WHITE);
        p.add(fKirche);
        p.add(Box.createVerticalStrut(8));

        SpinnerNumberModel kvModel = new SpinnerNumberModel(1.6, 0.0, 5.0, 0.1);
        fKvZusatz = new JSpinner(kvModel);
        fKvZusatz.setFont(FN);
        fKvZusatz.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.add(feldLabel("KV-Zusatzbeitrag (%)"));
        p.add(fKvZusatz);
        p.add(Box.createVerticalStrut(12));

        JButton btnBerechne = new JButton("Berechnen");
        btnBerechne.setFont(FB);
        btnBerechne.setBackground(new Color(30, 30, 28));
        btnBerechne.setForeground(Color.WHITE);
        btnBerechne.setFocusPainted(false);
        btnBerechne.setBorder(new EmptyBorder(8, 0, 8, 0));
        btnBerechne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnBerechne.setAlignmentX(LEFT_ALIGNMENT);
        btnBerechne.addActionListener(e -> berechne());
        p.add(btnBerechne);
        p.add(Box.createVerticalStrut(6));

        JButton btnUebernehmen = new JButton("Als Einnahme übernehmen ✓");
        btnUebernehmen.setFont(FN);
        btnUebernehmen.setBackground(new Color(234, 243, 222));
        btnUebernehmen.setForeground(GREEN);
        btnUebernehmen.setFocusPainted(false);
        btnUebernehmen.setBorder(new CompoundBorder(
            new LineBorder(new Color(192, 221, 151), 1, true),
            new EmptyBorder(7, 0, 7, 0)
        ));
        btnUebernehmen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnUebernehmen.setAlignmentX(LEFT_ALIGNMENT);
        btnUebernehmen.addActionListener(e -> uebernehmen());
        p.add(btnUebernehmen);

        return p;
    }

    private void berechne() {
        double brutto;
        try { brutto = Double.parseDouble(fBrutto.getText().replace(",", ".")); }
        catch (NumberFormatException e) { return; }

        int klasse = fKlasse.getSelectedIndex() + 1;
        boolean kirche = fKirche.isSelected();
        double kvZusatz = ((Number) fKvZusatz.getValue()).doubleValue();

        Steuerrechner.Ergebnis r = Steuerrechner.berechne(brutto, klasse, kirche, kvZusatz);
        zeigeErgebnis(r, klasse);
    }

    private void zeigeErgebnis(Steuerrechner.Ergebnis r, int klasse) {
        ergebnisPanel.removeAll();

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 215), 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));

        // Header
        JLabel name = new JLabel("Ahmed – Steuerklasse " + klasse);
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Monatsabrechnung Mai 2026");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(GRAY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        card.add(name); card.add(sub);
        card.add(sep());

        card.add(zeile("Brutto", fmt(r.brutto), Color.BLACK, true));
        card.add(sepLabel("Sozialversicherung"));
        card.add(zeile("Krankenversicherung", "-" + fmt(r.kv), RED, false));
        card.add(zeile("Rentenversicherung",  "-" + fmt(r.rv), RED, false));
        card.add(zeile("Arbeitslosenvers.",   "-" + fmt(r.av), RED, false));
        card.add(zeile("Pflegeversicherung",  "-" + fmt(r.pv), RED, false));
        card.add(sepLabel("Steuern"));
        card.add(zeile("Lohnsteuer",          "-" + fmt(r.lohnsteuer), RED, false));
        card.add(zeile("Solidaritätszuschlag","-" + fmt(r.soli), RED, false));
        if (r.kirchensteuer > 0)
            card.add(zeile("Kirchensteuer",   "-" + fmt(r.kirchensteuer), RED, false));
        card.add(sep());

        JPanel nettoRow = new JPanel(new BorderLayout());
        nettoRow.setBackground(Color.WHITE);
        nettoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        nettoRow.setAlignmentX(LEFT_ALIGNMENT);
        JLabel nettoLabel = new JLabel("Nettolohn");
        nettoLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel nettoVal = new JLabel(fmt(r.netto));
        nettoVal.setFont(new Font("SansSerif", Font.BOLD, 18));
        nettoVal.setForeground(BLUE);
        nettoRow.add(nettoLabel, BorderLayout.WEST);
        nettoRow.add(nettoVal, BorderLayout.EAST);
        card.add(Box.createVerticalStrut(4));
        card.add(nettoRow);
        card.add(Box.createVerticalStrut(10));

        // Summary grid
        JPanel grid = new JPanel(new GridLayout(1, 2, 8, 0));
        grid.setBackground(Color.WHITE);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        grid.setAlignmentX(LEFT_ALIGNMENT);
        grid.add(miniCard("Abzüge gesamt", fmt(r.getAbzuegeGesamt()), RED));
        grid.add(miniCard("Abzugsquote", String.format("%.1f %%", r.getAbzugsquote()), RED));
        card.add(grid);

        ergebnisPanel.add(card, BorderLayout.CENTER);
        ergebnisPanel.revalidate();
        ergebnisPanel.repaint();
    }

    private void uebernehmen() {
        double brutto;
        try { brutto = Double.parseDouble(fBrutto.getText().replace(",", ".")); }
        catch (NumberFormatException e) { return; }
        int klasse = fKlasse.getSelectedIndex() + 1;
        double kvZusatz = ((Number) fKvZusatz.getValue()).doubleValue();
        Steuerrechner.Ergebnis r = Steuerrechner.berechne(brutto, klasse, fKirche.isSelected(), kvZusatz);
        Buchung b = new Buchung(
            "Gehalt Mai (Netto)", r.netto, "Gehalt",
            LocalDate.now(),
            String.format("Brutto: %s, Steuerkl. %d", fmt(brutto), klasse),
            true
        );
        onUebernehmen.accept(b);
        JOptionPane.showMessageDialog(this,
            "Netto " + fmt(r.netto) + " als Einnahme gespeichert!",
            "Gespeichert", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel zeile(String label, String wert, Color farbe, boolean bold) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(FN); l.setForeground(GRAY);
        JLabel v = new JLabel(wert);
        v.setFont(bold ? FB : FN); v.setForeground(farbe);
        p.add(l, BorderLayout.WEST); p.add(v, BorderLayout.EAST);
        return p;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        s.setAlignmentX(LEFT_ALIGNMENT);
        return s;
    }

    private JLabel sepLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.PLAIN, 10));
        l.setForeground(GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(6, 0, 2, 0));
        return l;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel feldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(GRAY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel miniCard(String label, String val, Color col) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(new Color(245, 245, 242));
        p.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 215), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(GRAY);
        JLabel v = new JLabel(val);
        v.setFont(new Font("SansSerif", Font.BOLD, 14));
        v.setForeground(col);
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private static String fmt(double v) {
        return String.format("€ %,.2f", v);
    }
}
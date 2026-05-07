package finanzmanager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BuchungDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final boolean istEinnahme;
    private Buchung ergebnis = null;

    private JTextField fDesc, fAmt, fDate, fNote;
    private JComboBox<String> fKat;

    private static final String[] KAT_EINNAHME = {"Gehalt", "Freelance", "Kindergeld", "Sonstiges"};
    private static final String[] KAT_AUSGABE  = {
        "Wohnen", "Lebensmittel", "Transport",
        "Gesundheit", "Freizeit", "Kleidung", "Sonstiges"
    };

    public BuchungDialog(Frame owner, boolean istEinnahme, Buchung vorhandene) {
        super(owner, vorhandene == null
                ? (istEinnahme ? "Einnahme hinzufügen" : "Ausgabe hinzufügen")
                : (istEinnahme ? "Einnahme bearbeiten" : "Ausgabe bearbeiten"),
              true);
        this.istEinnahme = istEinnahme;
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setResizable(false);
        buildUI(vorhandene);
    }

    private void buildUI(Buchung v) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(16, 16, 8, 16));
        panel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        Font f = new Font("SansSerif", Font.PLAIN, 13);

        fDesc = new JTextField(v != null ? v.getBeschreibung() : "");
        fAmt  = new JTextField(v != null ? String.format("%.2f", v.getBetrag()) : "");
        fKat  = new JComboBox<>(istEinnahme ? KAT_EINNAHME : KAT_AUSGABE);
        fDate = new JTextField(v != null
                ? v.getDatum().format(FMT)
                : LocalDate.now().format(FMT));
        fNote = new JTextField(v != null ? v.getNotiz() : "");

        if (v != null) fKat.setSelectedItem(v.getKategorie());

        for (JComponent comp : new JComponent[]{fDesc, fAmt, fDate, fNote, fKat})
            comp.setFont(f);

        addRow(panel, c, 0, "Beschreibung:", fDesc);
        addRow(panel, c, 1, "Betrag (€):",   fAmt);
        addRow(panel, c, 2, "Kategorie:",     fKat);
        addRow(panel, c, 3, "Datum:",         fDate);
        addRow(panel, c, 4, "Notiz:",         fNote);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setBackground(Color.WHITE);

        JButton abbrechen = new JButton("Abbrechen");
        JButton speichern = new JButton("Speichern");
        speichern.setBackground(new Color(30, 30, 28));
        speichern.setForeground(Color.WHITE);
        speichern.setFocusPainted(false);
        abbrechen.setFont(f);
        speichern.setFont(new Font("SansSerif", Font.BOLD, 13));

        abbrechen.addActionListener(e -> dispose());
        speichern.addActionListener(e -> onSpeichern(v));

        buttons.add(abbrechen);
        buttons.add(speichern);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getContentPane().setBackground(Color.WHITE);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(100, 100, 95));
        p.add(l, c);
        c.gridx = 1; c.weightx = 0.7;
        p.add(field, c);
    }

    private void onSpeichern(Buchung vorhandene) {
        String desc = fDesc.getText().trim();
        String amtStr = fAmt.getText().replace(",", ".").trim();
        String dateStr = fDate.getText().trim();

        if (desc.isEmpty()) { error("Bitte Beschreibung eingeben."); return; }

        double amt;
        try { amt = Double.parseDouble(amtStr); }
        catch (NumberFormatException e) { error("Ungültiger Betrag."); return; }
        if (amt <= 0) { error("Betrag muss größer als 0 sein."); return; }

        LocalDate datum;
        try { datum = LocalDate.parse(dateStr, FMT); }
        catch (DateTimeParseException e) { error("Datum im Format TT.MM.JJJJ eingeben."); return; }

        String kat  = (String) fKat.getSelectedItem();
        String note = fNote.getText().trim();

        if (vorhandene != null) {
            vorhandene.setBeschreibung(desc);
            vorhandene.setBetrag(amt);
            vorhandene.setKategorie(kat);
            vorhandene.setDatum(datum);
            vorhandene.setNotiz(note);
            ergebnis = vorhandene;
        } else {
            ergebnis = new Buchung(desc, amt, kat, datum, note, istEinnahme);
        }
        dispose();
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Eingabefehler", JOptionPane.ERROR_MESSAGE);
    }

    public Buchung getErgebnis() { return ergebnis; }
}
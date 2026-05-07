package finanzmanager;

import java.time.LocalDate;

public class Buchung {
    private static int zaehler = 1;

    private final int id;
    private String beschreibung;
    private double betrag;
    private String kategorie;
    private LocalDate datum;
    private String notiz;
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

    public int getId()                  { return id; }
    public String getBeschreibung()     { return beschreibung; }
    public double getBetrag()           { return betrag; }
    public String getKategorie()        { return kategorie; }
    public LocalDate getDatum()         { return datum; }
    public String getNotiz()            { return notiz; }
    public boolean isEinnahme()         { return istEinnahme; }

    public void setBeschreibung(String v) { this.beschreibung = v; }
    public void setBetrag(double v)       { this.betrag = v; }
    public void setKategorie(String v)    { this.kategorie = v; }
    public void setDatum(LocalDate v)     { this.datum = v; }
    public void setNotiz(String v)        { this.notiz = v; }
}
package myapp.Applicazione;

public class RigaFattura {

    private Articolo articolo;
    private int quantita;
    private double prezzoUnitario;

    public RigaFattura(Articolo articolo, int quantita) {
        this.articolo = articolo;
        this.quantita = quantita;
        this.prezzoUnitario = articolo.getPrezzo();
    }

    public RigaFattura(Articolo articolo, int quantita, double prezzoUnitario) {
        this.articolo = articolo;
        this.quantita = quantita;
        this.prezzoUnitario = prezzoUnitario;
    }

    public Articolo getArticolo() {
        return articolo;
    }

    public int getQuantita() {
        return quantita;
    }

    public double getPrezzoUnitario() {
        return prezzoUnitario;
    }

    public double getTotale() {
        return quantita * prezzoUnitario;
    }
}

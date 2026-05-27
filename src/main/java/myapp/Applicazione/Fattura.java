package myapp.Applicazione;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Fattura {

    private int numeroFattura;
    private LocalDate data;
    private List<RigaFattura> righe;

    public Fattura(int numeroFattura) {
        this.numeroFattura = numeroFattura;
        this.data = LocalDate.now();
        this.righe = new ArrayList<>();
    }

    public void addRiga(Articolo articolo, int quantita) {
        RigaFattura r = new RigaFattura(articolo, quantita);
        righe.add(r);
    }

    public void addRiga(Articolo articolo, int quantita, double prezzoUnitario) {
        RigaFattura r = new RigaFattura(articolo, quantita, prezzoUnitario);
        righe.add(r);
    }

    public int getNumeroFattura() {
        return numeroFattura;
    }

    public LocalDate getData() {
        return data;
    }

    public List<RigaFattura> getRighe() {
        return righe;
    }

    public double getTotale() {
        return righe.stream()
                .mapToDouble(RigaFattura::getTotale)
                .sum();
    }
}

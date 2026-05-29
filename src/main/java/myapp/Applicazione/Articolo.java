package myapp.Applicazione;

public class Articolo {

    private int id;
    private String codiceArticolo;
    private String nome;
    private String descrizione;
    private double prezzo;
    private int quantita;
    private int quantitaImpegnata;

    // ===== COSTRUTTORI =====

    public Articolo(int id, String codiceArticolo, String nome, String descrizione,
                    double prezzo, int quantita, int quantitaImpegnata) 
    {
        this.id = id;
        this.codiceArticolo = codiceArticolo;
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzo=prezzo;
        this.quantita=quantita;
        this.quantitaImpegnata=quantitaImpegnata;
    }

    public Articolo(String codiceArticolo, String nome, String descrizione,
                    double prezzo, int quantita)
    {
        this(0, codiceArticolo, nome, descrizione, prezzo, quantita, 0);
    }

    public Articolo(Articolo a) {
        this(a.id, a.codiceArticolo, a.nome, a.descrizione,
             a.prezzo, a.quantita, a.quantitaImpegnata);
    }

    // ===== GETTER & SETTER =====

    public String getCodiceArticolo() { return codiceArticolo; }
    public void setCodiceArticolo(String codiceArticolo) {
        this.codiceArticolo = codiceArticolo;
    }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) {
        if (quantita < 0) throw new IllegalArgumentException("Quantità non valida");
        this.quantita = quantita;
    }

    public int getQuantitaImpegnata() { return quantitaImpegnata; }
    public void setQuantitaImpegnata(int q) {
        if (q < 0) throw new IllegalArgumentException("Quantità impegnata non valida");
        if (q > quantita) throw new IllegalArgumentException("Impegnata > quantità totale");
        this.quantitaImpegnata = q;
    }

    public int getDisponibile() {
        return quantita - quantitaImpegnata;
    }

    // ===== OPERAZIONI SULLA QUANTITÀ =====

    public void impegna(int q) {
        if (q <= 0) throw new IllegalArgumentException("Quantità da impegnare non valida");
        if (q > getDisponibile()) throw new IllegalArgumentException("Non abbastanza quantità");
        quantitaImpegnata += q;
    }

    public void libera(int q) {
        if (q <= 0) throw new IllegalArgumentException("Quantità da liberare non valida");
        if (q > quantitaImpegnata) throw new IllegalArgumentException("Si libera troppo");
        quantitaImpegnata -= q;
    }

    public void diminuisciQuantita(int q) {
        if (q <= 0) throw new IllegalArgumentException("Quantità da diminuire non valida");
        if (q > getDisponibile()) throw new IllegalArgumentException("Stock insufficiente");
        quantita -= q;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public double getPrezzo() { return prezzo; }
    public void setPrezzo(double prezzo) {
        if (prezzo < 0) throw new IllegalArgumentException("Prezzo non valido");
        this.prezzo = prezzo;
    }

    @Override
    public String toString() {
        return nome;
    }
}



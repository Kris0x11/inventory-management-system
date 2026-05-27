package myapp.Applicazione;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestioneArticoli {
    
    public static final String url = "jdbc:sqlite:magazzino.db";

    // ------------------------------
    //   LETTURA ARTICOLI
    // ------------------------------
    public static List<Articolo> getArticoli() {
        List<Articolo> lista = new ArrayList<>();

        String sql = "SELECT * FROM articolo";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            while (rs.next()) {
                int id = rs.getInt("id");
                String codice = rs.getString("CodiceArticolo");
                String nome = rs.getString("nome");
                String descrizione = rs.getString("descrizione");
                double prezzo = rs.getDouble("prezzo");
                int quantita = rs.getInt("quantità");
                int impegnata = rs.getInt("quantita_impegnata");

                lista.add(new Articolo(id, codice, nome, descrizione, prezzo, quantita, impegnata));
            }
        } 
        catch (SQLException e) {
            System.out.println("Errore lettura articoli: " + e.getMessage());
        }

        return lista;
    }

    public static Articolo getArticoloById(int idArticolo) {
        String sql = "SELECT * FROM articolo WHERE id=?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, idArticolo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Articolo(
                    rs.getInt("id"),
                    rs.getString("CodiceArticolo"),
                    rs.getString("nome"),
                    rs.getString("descrizione"),
                    rs.getDouble("prezzo"),
                    rs.getInt("quantità"),
                    rs.getInt("quantita_impegnata")
                );
            }
        }
        catch (SQLException e) {
            System.out.println("Errore getArticoloById: " + e.getMessage());
        }
        return null;
    }
 // ------------------------------
//  INSERISCI ARTICOLO
//------------------------------
public static boolean inserisciArticolo(String codice, String nome, String descrizione,
                                    double prezzo, int quantita) 
{
String sql = """
    INSERT INTO articolo
    (CodiceArticolo, nome, descrizione, prezzo, quantità, quantita_impegnata)
    VALUES (?, ?, ?, ?, ?, 0)
""";

try (Connection conn = DriverManager.getConnection(url);
     PreparedStatement stmt = conn.prepareStatement(sql)) 
{
    stmt.setString(1, codice);
    stmt.setString(2, nome);
    stmt.setString(3, descrizione);
    stmt.setDouble(4, prezzo);
    stmt.setInt(5, quantita);

    return stmt.executeUpdate() > 0;
} 
catch (SQLException e) {
    System.out.println("Errore inserimento articolo: " + e.getMessage());
    return false;
}
}


    // ------------------------------
    //     IMPEGNA ARTICOLO
    // ------------------------------
    public static boolean impegnaArticolo(int id, int qta) {

        Articolo art = getArticoloById(id);
        if (art == null) return false;

        if (qta > art.getDisponibile()) {
            System.out.println("Quantità non disponibile per impegno!");
            return false;
        }

        String sql = """
            UPDATE articolo
            SET quantita_impegnata = quantita_impegnata + ?
            WHERE id=?
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            stmt.setInt(1, qta);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            System.out.println("Errore impegno articolo: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------
    //     LIBERA ARTICOLO
    // ------------------------------
    public static boolean liberaArticolo(int id, int qta) {

        String sql = """
            UPDATE articolo
            SET quantita_impegnata = quantita_impegnata - ?
            WHERE id=?
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, qta);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            System.out.println("Errore liberazione articolo: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------
    //      VENDITA ARTICOLO
    // ------------------------------
    public static boolean vendita(int id, int qtaVenduta) {

        Articolo art = getArticoloById(id);
        if (art == null) return false;

        if (qtaVenduta > art.getQuantita()) {
            System.out.println("Quantità non sufficiente in magazzino!");
            return false;
        }

        int qLiberare = Math.min(qtaVenduta, art.getQuantitaImpegnata());

        String sql = """
            UPDATE articolo
            SET quantità = quantità - ?,
                quantita_impegnata = quantita_impegnata - ?
            WHERE id=?
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql))
        {
            stmt.setInt(1, qtaVenduta);
            stmt.setInt(2, qLiberare);
            stmt.setInt(3, id);

            return stmt.executeUpdate() > 0;
        }
        catch (SQLException e) {
            System.out.println("Errore vendita: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------
    //   AGGIORNAMENTO E CANCELLAZIONE
    // ------------------------------
    public static boolean cancArticolo(int id) {
        String sql = "DELETE FROM articolo WHERE id=?";

        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stato = con.prepareStatement(sql)) 
        {
            stato.setInt(1, id);
            return stato.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            System.out.println("Errore durante cancellazione " + e.getMessage());
            return false;
        }
    }

    public static boolean aggiornaArticolo(String codice, String nome, String descrizione,
                                           double prezzo, int quantita, int id) 
    {
        String sql = """
            UPDATE articolo
            SET CodiceArticolo=?, nome=?, descrizione=?, prezzo=?, quantità=?
            WHERE id=?
        """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stat = conn.prepareStatement(sql)) 
        {
            stat.setString(1, codice);
            stat.setString(2, nome);
            stat.setString(3, descrizione);
            stat.setDouble(4, prezzo);
            stat.setInt(5, quantita);
            stat.setInt(6, id);

            return stat.executeUpdate() > 0;
        } 
        catch (SQLException e) {
            System.out.println("Errore durante aggiornamento: " + e.getMessage());
            return false;
        }
    }
}


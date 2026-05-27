package myapp.Applicazione;

import java.sql.*;

public class DBsetup {

    private static final String url = "jdbc:sqlite:magazzino.db";

    public static void DBsetup() {

        String sql = """
            CREATE TABLE IF NOT EXISTS articolo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                CodiceArticolo TEXT NOT NULL,
                nome TEXT NOT NULL,
                descrizione TEXT NOT NULL,
                prezzo REAL NOT NULL,
                quantità INTEGER NOT NULL,
                quantita_impegnata INTEGER NOT NULL DEFAULT 0
            )
        """;

        try (Connection con = DriverManager.getConnection(url);
             Statement stmt = con.createStatement())
        {
            stmt.execute(sql);
        } 
        catch (SQLException e)
        {
            System.out.println("Errore nella creazione tabella " + e.getMessage());
        }
    }

}



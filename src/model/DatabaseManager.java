package src.model;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.io.FileInputStream;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("resources/config.properties"));

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAlleFerienwohnungen() {
        List<Map<String, Object>> liste = new ArrayList<>();

        String sql = "SELECT ID, Name, Größe, PreisProTag, Zimmer FROM Ferienwohnung";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> fw = new HashMap<>();
                int fwId = rs.getInt("ID");
                
                fw.put("ID", fwId);
                fw.put("Name", rs.getString("Name"));
                fw.put("Größe", rs.getInt("Größe"));
                fw.put("PreisProTag", rs.getDouble("PreisProTag"));
                fw.put("Zimmer", rs.getInt("Zimmer"));

                fw.put("Bilder", getBilderForFW(fwId));

                fw.put("Ausstattung", getAusstattungForFW(fwId));
                fw.put("Attraktionen", getAttraktionenForFW(fwId));

                liste.add(fw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public List<Map<String, Object>> getGefilterteFerienwohnungen(String land, Date anreise, Date abreise, String ausstattungOptional) {
        List<Map<String, Object>> liste = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT fw.ID, fw.Name, fw.Größe, fw.PreisProTag, fw.Zimmer,
                AVG(b.Sterne) AS Durchschnittsbewertung
            FROM Ferienwohnung fw
            JOIN Adresse a ON fw.ADRESSE_ID = a.ID
            LEFT JOIN Buchung b ON fw.ID = b.FERIENWOHNUNG_ID AND b.Sterne IS NOT NULL
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (land != null && !land.isBlank()) {
            sql.append(" AND a.Land = ?\n");
            params.add(land);
        }

        if (anreise != null && abreise != null) {
            sql.append("""
                AND NOT EXISTS (
                    SELECT 1 FROM Buchung bu
                    WHERE bu.FERIENWOHNUNG_ID = fw.ID
                    AND (? < bu.BUCHUNGSZEITRAUM_ENDE AND ? > bu.BUCHUNGSZEITRAUM_START)
                )
            """);
            params.add(anreise); // WICHTIG: erst Anreise!
            params.add(abreise); // dann Abreise!
        }

        if (ausstattungOptional != null && !ausstattungOptional.isBlank()) {
            sql.append("""
                AND EXISTS (
                    SELECT 1 FROM Ferienwohnung_Ausstattung fa
                    WHERE fa.FERIENWOHNUNG_ID = fw.ID
                    AND fa.AUSTATTUNGS_BEZEICHNUNG = ?
                )
            """);
            params.add(ausstattungOptional);
        }

        sql.append(" GROUP BY fw.ID, fw.Name, fw.Größe, fw.PreisProTag, fw.Zimmer");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Date) {
                    stmt.setDate(i + 1, (Date) param);
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> fw = new HashMap<>();
                int fwId = rs.getInt("ID");
                fw.put("ID", fwId);
                fw.put("Name", rs.getString("Name"));
                fw.put("Größe", rs.getInt("Größe"));
                fw.put("PreisProTag", rs.getDouble("PreisProTag"));
                fw.put("Zimmer", rs.getInt("Zimmer"));
                fw.put("Durchschnitt", rs.getDouble("Durchschnittsbewertung"));
                fw.put("Bilder", getBilderForFW(fwId));
                fw.put("Ausstattung", getAusstattungForFW(fwId));
                fw.put("Attraktionen", getAttraktionenForFW(fwId));

                liste.add(fw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return liste;
    }




    public List<String> getAlleLaender() {
        List<String> laender = new ArrayList<>();
        String sql = """
            SELECT DISTINCT a.Land
            FROM Adresse a
            JOIN Ferienwohnung f ON f.ADRESSE_ID = a.ID
            """;

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                laender.add(rs.getString("Land"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return laender;
    }


    public List<String> getAlleAusstattungen() {
        List<String> ausstattungen = new ArrayList<>();
        String sql = "SELECT DISTINCT AUSTATTUNGS_BEZEICHNUNG FROM Ferienwohnung_Ausstattung";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ausstattungen.add(rs.getString("AUSTATTUNGS_BEZEICHNUNG"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ausstattungen;
    }



    private List<String> getBilderForFW(int fwId) {
        List<String> bilder = new ArrayList<>();
        String sql = "SELECT URL FROM Bilder WHERE FERIENWOHNUNG_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fwId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bilder.add(rs.getString("URL"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bilder;
    }


    private List<String> getAusstattungForFW(int fwId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT AUSTATTUNGS_BEZEICHNUNG FROM Ferienwohnung_Ausstattung WHERE FERIENWOHNUNG_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fwId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<String> getAttraktionenForFW(int fwId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT ATTRAKTIONS_NAME FROM Ferienwohnung_Touristenattraktion WHERE FERIENWOHNUNG_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fwId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String executeSQL(String sql) {
        try (Statement stmt = conn.createStatement()) {
            if (sql.trim().toLowerCase().startsWith("select")) {
                ResultSet rs = stmt.executeQuery(sql);
                StringBuilder result = new StringBuilder();
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // Spaltennamen
                for (int i = 1; i <= colCount; i++) {
                    result.append(meta.getColumnName(i)).append("\t");
                }
                result.append("\n");

                // Daten
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        result.append(rs.getString(i)).append("\t");
                    }
                    result.append("\n");
                }
                return result.toString();
            } else {
                int count = stmt.executeUpdate(sql);
                return "Erfolgreich ausgeführt, " + count + " Zeilen betroffen.";
            }
        } catch (SQLException e) {
            return "Fehler: " + e.getMessage();
        }
    }

    /**
     * Prüft, ob eine Ferienwohnung im gewünschten Zeitraum verfügbar ist.
     * Gibt true zurück, wenn keine Überschneidung mit bestehenden Buchungen vorliegt.
     */
    public boolean istFerienwohnungVerfuegbar(int fwId, Date anreise, Date abreise) {
        String sql = """
            SELECT COUNT(*) 
            FROM Buchung 
            WHERE FERIENWOHNUNG_ID = ? 
            AND (? < BUCHUNGSZEITRAUM_ENDE AND ? > BUCHUNGSZEITRAUM_START)
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fwId);
            stmt.setDate(2, anreise); // neue Anreise
            stmt.setDate(3, abreise); // neue Abreise

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Überschneidungen gefunden: " + count);
                return count == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public List<Map<String, Object>> getBuchungenFuerKunde(String mailadresse) {
        List<Map<String, Object>> buchungen = new ArrayList<>();
        String sql = "SELECT b.ID, b.FERIENWOHNUNG_ID, b.BUCHUNGSZEITRAUM_START, b.BUCHUNGSZEITRAUM_ENDE,\r\n" +
             "       b.BUCHUNGSDATUM, b.RECHNUNGSDATUM, b.RECHNUNGSBETRAG, b.STERNE, f.Name,\r\n" +
             "       NVL(SUM(a.GELDBETRAG), 0) AS ANZAHLUNG_SUMME,\r\n" +
             "       (b.RECHNUNGSBETRAG - NVL(SUM(a.GELDBETRAG), 0)) AS OFFENER_BETRAG\r\n" +
             "FROM Buchung b\r\n" +
             "JOIN Ferienwohnung f ON b.FERIENWOHNUNG_ID = f.ID\r\n" +
             "LEFT JOIN Anzahlung a ON b.ID = a.BUCHUNG_ID\r\n" +
             "WHERE b.MAILADRESSE = ?\r\n" +
             "GROUP BY b.ID, b.FERIENWOHNUNG_ID, b.BUCHUNGSZEITRAUM_START, b.BUCHUNGSZEITRAUM_ENDE,\r\n" +
             "         b.BUCHUNGSDATUM, b.RECHNUNGSDATUM, b.RECHNUNGSBETRAG, b.STERNE, f.Name\r\n" +
             "ORDER BY b.BUCHUNGSZEITRAUM_START DESC\r\n";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mailadresse);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> buchung = new HashMap<>();
                buchung.put("ID", rs.getInt("ID"));
                buchung.put("FERIENWOHNUNG_ID", rs.getInt("FERIENWOHNUNG_ID"));
                buchung.put("BUCHUNGSZEITRAUM_START", rs.getDate("BUCHUNGSZEITRAUM_START"));
                buchung.put("BUCHUNGSZEITRAUM_ENDE", rs.getDate("BUCHUNGSZEITRAUM_ENDE"));
                buchung.put("Sterne", rs.getObject("Sterne"));
                buchung.put("Name", rs.getString("Name"));
                buchung.put("BUCHUNGSDATUM", rs.getDate("BUCHUNGSDATUM"));
                buchung.put("RECHNUNGSDATUM", rs.getDate("RECHNUNGSDATUM"));
                buchung.put("RECHNUNGSBETRAG", rs.getBigDecimal("RECHNUNGSBETRAG"));
                buchung.put("ANZAHLUNG", rs.getBigDecimal("ANZAHLUNG_SUMME"));
                buchung.put("OFFENER_BETRAG", rs.getBigDecimal("OFFENER_BETRAG"));

                buchungen.add(buchung);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return buchungen;
    }

    /**
     * Fügt eine neue Buchung für einen Kunden in die Datenbank ein.
     * Die Buchungsnummer (ID) wird automatisch vergeben.
     * Der Rechnungsbetrag wird anhand der Tage und PreisProTag aus der DB berechnet.
     */
    public boolean bucheFerienwohnung(int fwId, String mail, Date anreise, Date abreise) {
        if(!istFerienwohnungVerfuegbar(fwId, anreise, abreise)) {
            System.out.println("Einfügen fehlgeschlagen: Ferienwohnung nicht verfügbar.");
            return false; // Keine Buchung möglich, wenn nicht verfügbar
        }
        // Preis pro Tag aus DB holen
        double preisProTag = 0.0;
        String preisSql = "SELECT PreisProTag FROM Ferienwohnung WHERE ID = ?";
        try (PreparedStatement preisStmt = conn.prepareStatement(preisSql)) {
            preisStmt.setInt(1, fwId);
            ResultSet rs = preisStmt.executeQuery();
            if (rs.next()) {
                preisProTag = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        long diff = abreise.getTime() - anreise.getTime();
        int tage = (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
        if (tage < 1) tage = 1;
        double gesamtpreis = preisProTag * tage;
        String sql = "INSERT INTO Buchung (FERIENWOHNUNG_ID, MAILADRESSE, BUCHUNGSDATUM, BUCHUNGSZEITRAUM_START, BUCHUNGSZEITRAUM_ENDE, RECHNUNGSBETRAG) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, fwId);
            stmt.setString(2, mail);
            stmt.setDate(3, new java.sql.Date(System.currentTimeMillis())); // BUCHUNGSDATUM = heute
            stmt.setDate(4, anreise);
            stmt.setDate(5, abreise);
            stmt.setDouble(6, gesamtpreis);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // oder loggen
            return false;
        }
    }
}

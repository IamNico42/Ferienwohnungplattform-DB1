package src.controller;

import javax.swing.JOptionPane;
import src.model.DatabaseManager;
import src.view.FerienwohnungView;

import java.util.List;
import java.util.Map;

public class FerienwohnungController {

    private DatabaseManager model;
    private FerienwohnungView view;

    public FerienwohnungController(DatabaseManager model, String kundenName) {
        this.model = model;

        List<String> laender = model.getAlleLaender();
        List<String> ausstattungen = model.getAlleAusstattungen();

        view = new FerienwohnungView(kundenName, laender, ausstattungen, (params, resultPanel) -> {
            List<Map<String, Object>> treffer = model.getGefilterteFerienwohnungen(
                params.land, params.anreise, params.abreise, params.ausstattung
            );
            for (Map<String, Object> fw : treffer) {
                resultPanel.add(view.createCard(fw));
            }
        });

        // Buchungs-Callback für Verfügbarkeit und Buchung setzen
        view.setBuchungCallback(new FerienwohnungView.BuchungCallback() {
            @Override
            public void pruefeBuchung(Map<String, Object> fw, java.sql.Date anreise, java.sql.Date abreise, Runnable onSuccess, Runnable onFailure) {
                int fwId = (int) fw.get("ID");
                boolean verfuegbar = model.istFerienwohnungVerfuegbar(fwId, anreise, abreise);
                if (verfuegbar) {
                    onSuccess.run();
                } else {
                    onFailure.run();
                }
            }
            @Override
            public void bestaetigeBuchung(Map<String, Object> fw, java.sql.Date anreise, java.sql.Date abreise, String name) {
                int fwId = (int) fw.get("ID");
                // kundenName ist die Mailadresse, die im Konstruktor übergeben wurde
                boolean success = model.bucheFerienwohnung(fwId, kundenName, anreise, abreise);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Buchung erfolgreich!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(view, "Buchung fehlgeschlagen!", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Listener für "Meine Buchungen"-Button
        view.setBuchungenListener(() -> {
            List<Map<String, Object>> buchungen = model.getBuchungenFuerKunde(kundenName);
            view.zeigeBuchungenDialog(buchungen);
        });

        // Optional initialer Aufruf mit allen Ferienwohnungen
        List<Map<String, Object>> ferienwohnungen = model.getAlleFerienwohnungen();
        view.zeigeErgebnisse(ferienwohnungen);

        // Logout-Button
        view.getLogoutButton().addActionListener(e -> {
            view.dispose();
            // zurück zum Login oder Anwendung beenden
        });
    }
}

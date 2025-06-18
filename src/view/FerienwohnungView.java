package src.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class FerienwohnungView extends JFrame {
    private JLabel welcomeLabel;
    private JButton logoutButton;
    private JComboBox<String> landDropdown;
    private JComboBox<String> ausstattungDropdown;
    private JCheckBox anreiseCheck;
    private JCheckBox abreiseCheck;
    private JSpinner anreiseSpinner;
    private JSpinner abreiseSpinner;
    private JButton suchButton;
    private JPanel resultPanel;

    public FerienwohnungView(String kundenName, List<String> laender, List<String> ausstattungen,
                             BiConsumer<FilterParams, JPanel> suchAktion) {
        setTitle("Ferienwohnungen");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Willkommen, " + kundenName);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(welcomeLabel, BorderLayout.WEST);

        logoutButton = new JButton("Abmelden");
        JButton buchungenButton = new JButton("Meine Buchungen");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(buchungenButton);
        buttonPanel.add(logoutButton);
        header.add(buttonPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        landDropdown = new JComboBox<>();
        landDropdown.addItem("Kein spezifisches Land");
        for (String l : laender) landDropdown.addItem(l);

        ausstattungDropdown = new JComboBox<>();
        ausstattungDropdown.addItem("-");
        for (String a : ausstattungen) ausstattungDropdown.addItem(a);

        anreiseSpinner = new JSpinner(new SpinnerDateModel());
        abreiseSpinner = new JSpinner(new SpinnerDateModel());
        anreiseSpinner.setEditor(new JSpinner.DateEditor(anreiseSpinner, "yyyy-MM-dd"));
        abreiseSpinner.setEditor(new JSpinner.DateEditor(abreiseSpinner, "yyyy-MM-dd"));
        anreiseCheck = new JCheckBox("Anreise aktivieren");
        abreiseCheck = new JCheckBox("Abreise aktivieren");

        suchButton = new JButton("Suchen");
        suchButton.addActionListener((ActionEvent e) -> {
            String land = (String) landDropdown.getSelectedItem();
            String ausstattung = (String) ausstattungDropdown.getSelectedItem();

            if ("Kein spezifisches Land".equals(land)) land = null;
            if ("-".equals(ausstattung)) ausstattung = null;

            Date anreise = null;
            Date abreise = null;

            if (anreiseCheck.isSelected()) {
                anreise = new Date(((java.util.Date) anreiseSpinner.getValue()).getTime());
            }
            if (abreiseCheck.isSelected()) {
                abreise = new Date(((java.util.Date) abreiseSpinner.getValue()).getTime());
            }

            FilterParams p = new FilterParams(land, anreise, abreise, ausstattung);
            resultPanel.removeAll();
            suchAktion.accept(p, resultPanel);
            resultPanel.revalidate();
            resultPanel.repaint();
        });

        filterPanel.add(new JLabel("Land:"));
        filterPanel.add(landDropdown);

        filterPanel.add(anreiseCheck);
        filterPanel.add(anreiseSpinner);

        filterPanel.add(abreiseCheck);
        filterPanel.add(abreiseSpinner);

        filterPanel.add(new JLabel("Ausstattung:"));
        filterPanel.add(ausstattungDropdown);

        filterPanel.add(suchButton);
        add(filterPanel, BorderLayout.SOUTH);

        // Ergebnisse Panel
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);

        buchungenButton.addActionListener(e -> {
            if (buchungenListener != null) {
                buchungenListener.run();
            }
        });
    }

    public JButton getLogoutButton() {
        return logoutButton;
    }

    public void zeigeErgebnisse(List<Map<String, Object>> ferienwohnungen) {
        resultPanel.removeAll();
        for (Map<String, Object> fw : ferienwohnungen) {
            resultPanel.add(createCard(fw));
        }
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    public interface BuchungCallback {
        void pruefeBuchung(Map<String, Object> fw, Date anreise, Date abreise, Runnable onSuccess, Runnable onFailure);
        void bestaetigeBuchung(Map<String, Object> fw, Date anreise, Date abreise, String name);
    }

    private BuchungCallback buchungCallback;
    public void setBuchungCallback(BuchungCallback callback) {
        this.buchungCallback = callback;
    }

    public JPanel createCard(Map<String, Object> fw) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        List<String> bilder = (List<String>) fw.get("Bilder");
        final int[] currentIndex = {0};

        JPanel imagePanel = new JPanel(new BorderLayout());
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(200, 150));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (bilder != null && !bilder.isEmpty()) {
            setImage(imageLabel, bilder.get(currentIndex[0]));
        } else {
            setImage(imageLabel, "assets/images/kein_bild.png");
        }

        JButton leftButton = new JButton("<");
        JButton rightButton = new JButton(">");
        for (JButton btn : new JButton[]{leftButton, rightButton}) {
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setForeground(Color.WHITE);
        }

        leftButton.addActionListener(e -> {
            if (!bilder.isEmpty()) {
                currentIndex[0] = (currentIndex[0] - 1 + bilder.size()) % bilder.size();
                setImage(imageLabel, bilder.get(currentIndex[0]));
            }
        });
        rightButton.addActionListener(e -> {
            if (!bilder.isEmpty()) {
                currentIndex[0] = (currentIndex[0] + 1) % bilder.size();
                setImage(imageLabel, bilder.get(currentIndex[0]));
            }
        });

        imagePanel.add(leftButton, BorderLayout.WEST);
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.add(rightButton, BorderLayout.EAST);
        card.add(imagePanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel("Name: " + fw.get("Name")));
        infoPanel.add(new JLabel("Größe: " + fw.get("Größe") + " m²"));
        infoPanel.add(new JLabel("Preis pro Tag: " + fw.get("PreisProTag") + " €"));
        infoPanel.add(new JLabel("Zimmer: " + fw.get("Zimmer")));

        if (fw.containsKey("Durchschnitt")) {
            infoPanel.add(new JLabel("Ø Bewertung: " + String.format("%.2f", fw.get("Durchschnitt"))));
        }

        infoPanel.add(new JLabel("Ausstattung: " + String.join(", ", (List<String>) fw.get("Ausstattung"))));
        infoPanel.add(new JLabel("In der Nähe: " + String.join(", ", (List<String>) fw.get("Attraktionen"))));

        JButton buchenButton = new JButton("Ferienwohnung buchen");
        buchenButton.addActionListener(e -> {
            showBuchungsDialog(fw);
        });
        infoPanel.add(buchenButton);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private void setImage(JLabel label, String relativePath) {
        File imageFile = new File("resources/" + relativePath);
        if (imageFile.exists()) {
            ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
            Image scaled = originalIcon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
            label.setText(null);
        } else {
            System.err.println("❌ Bild nicht gefunden: " + imageFile.getAbsolutePath());
            label.setIcon(null);
            label.setText("Kein Bild");
        }
    }

    private void showBuchungsDialog(Map<String, Object> fw) {
        JDialog dialog = new JDialog(this, "Ferienwohnung buchen", true);
        dialog.setSize(400, 320);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel anreiseLabel = new JLabel("Anreise (yyyy-MM-dd):");
        JSpinner anreiseSpinner = new JSpinner(new SpinnerDateModel());
        anreiseSpinner.setEditor(new JSpinner.DateEditor(anreiseSpinner, "yyyy-MM-dd"));
        JLabel abreiseLabel = new JLabel("Abreise (yyyy-MM-dd):");
        JSpinner abreiseSpinner = new JSpinner(new SpinnerDateModel());
        abreiseSpinner.setEditor(new JSpinner.DateEditor(abreiseSpinner, "yyyy-MM-dd"));
        JButton pruefenButton = new JButton("Buchung überprüfen");
        JButton bestaetigenButton = new JButton("Buchung bestätigen");
        bestaetigenButton.setVisible(false);
        JLabel statusLabel = new JLabel();
        JLabel preisLabel = new JLabel();
        preisLabel.setVisible(false);

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(anreiseLabel, gbc);
        gbc.gridx = 1; dialog.add(anreiseSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(abreiseLabel, gbc);
        gbc.gridx = 1; dialog.add(abreiseSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; dialog.add(pruefenButton, gbc);
        gbc.gridy = 3; dialog.add(bestaetigenButton, gbc);
        gbc.gridy = 4; dialog.add(statusLabel, gbc);
        gbc.gridy = 5; dialog.add(preisLabel, gbc);

        pruefenButton.addActionListener(ev -> {
            Date anreise = new Date(((java.util.Date) anreiseSpinner.getValue()).getTime());
            Date abreise = new Date(((java.util.Date) abreiseSpinner.getValue()).getTime());
            if (anreise == null || abreise == null) {
                statusLabel.setText("Bitte alle Felder ausfüllen.");
                return;
            }
            if (!anreise.before(abreise)) {
                statusLabel.setText("Abreise muss nach Anreise liegen.");
                return;
            }
            if (buchungCallback != null) {
                pruefenButton.setEnabled(false);
                statusLabel.setText("Prüfe Verfügbarkeit...");
                buchungCallback.pruefeBuchung(fw, anreise, abreise, () -> {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Verfügbar! Jetzt bestätigen.");
                        pruefenButton.setVisible(false);
                        bestaetigenButton.setVisible(true);
                        // Preis berechnen
                        double preisProTag = 0.0;
                        try {
                            preisProTag = Double.parseDouble(fw.get("PreisProTag").toString());
                        } catch (Exception ex) {}
                        long diff = abreise.getTime() - anreise.getTime();
                        int tage = (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));
                        if (tage < 1) tage = 1;
                        double gesamtpreis = preisProTag * tage;
                        preisLabel.setText("Gesamtpreis für " + tage + " Tag(e): " + String.format("%.2f", gesamtpreis) + " €");
                        preisLabel.setVisible(true);
                    });
                }, () -> {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Nicht verfügbar oder ungültig.");
                        pruefenButton.setEnabled(true);
                        preisLabel.setVisible(false);
                    });
                });
            }
        });
        bestaetigenButton.addActionListener(ev -> {
            Date anreise = new Date(((java.util.Date) anreiseSpinner.getValue()).getTime());
            Date abreise = new Date(((java.util.Date) abreiseSpinner.getValue()).getTime());
            if (buchungCallback != null) {
                buchungCallback.bestaetigeBuchung(fw, anreise, abreise, null);
                dialog.dispose();
            }
        });
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Runnable buchungenListener;
    public void setBuchungenListener(Runnable listener) {
        this.buchungenListener = listener;
    }

    public void zeigeBuchungenDialog(List<Map<String, Object>> buchungen) {
        JDialog dialog = new JDialog(this, "Meine Buchungen", true);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Fewo", "Von", "Bis", "Sterne", "Bewertung", "Buchungsdatum", "Rechnungsdatum", "Betrag", "Anzahlung", "Offen"};
        Object[][] data = new Object[buchungen.size()][columns.length];
        for (int i = 0; i < buchungen.size(); i++) {
            Map<String, Object> b = buchungen.get(i);
            data[i][0] = b.get("Name");
            data[i][1] = b.get("BUCHUNGSZEITRAUM_START");
            data[i][2] = b.get("BUCHUNGSZEITRAUM_ENDE");
            data[i][3] = b.get("Sterne") != null ? b.get("Sterne").toString() : "-";
            data[i][4] = b.get("Bewertungstext") != null ? b.get("Bewertungstext") : "";
            data[i][5] = b.get("BUCHUNGSDATUM");
            data[i][6] = b.get("RECHNUNGSDATUM");
            data[i][7] = b.get("RECHNUNGSBETRAG") != null ? b.get("RECHNUNGSBETRAG").toString() + " €" : "-";
            data[i][8] = b.get("ANZAHLUNG_SUMME") != null ? b.get("ANZAHLUNG_SUMME").toString() + " €" : "0 €";
            data[i][9] = b.get("OFFENER_BETRAG") != null ? b.get("OFFENER_BETRAG").toString() + " €" : "-";
        }

        JTable table = new JTable(data, columns);
        JScrollPane scroll = new JScrollPane(table);
        dialog.add(scroll, BorderLayout.CENTER);
        JButton close = new JButton("Schließen");
        close.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(close);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static class FilterParams {
        public String land;
        public Date anreise;
        public Date abreise;
        public String ausstattung;

        public FilterParams(String land, Date anreise, Date abreise, String ausstattung) {
            this.land = land;
            this.anreise = anreise;
            this.abreise = abreise;
            this.ausstattung = ausstattung;
        }
    }
}

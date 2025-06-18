package src.controller;

import src.model.AuthManager;
import src.model.DatabaseManager;
import src.view.LoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {
    private LoginView view;
    private AuthManager model;

    public LoginController(LoginView view, AuthManager model) {
        this.view = view;
        this.model = model;

        view.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = view.userField.getText();
                String pass = new String(view.passField.getPassword());
                if (model.checkLogin(user, pass)) {
                    view.showMessage("Login erfolgreich!");

                    // Login-Fenster schließen
                    view.dispose();

                    // Weiterleitung zu Ferienwohnung-Übersicht
                    DatabaseManager db = new DatabaseManager();
                    new FerienwohnungController(db, user);  // user = Mailadresse
                } else {
                    view.showMessage("Login fehlgeschlagen.");
                }
            }
        });
    }
}

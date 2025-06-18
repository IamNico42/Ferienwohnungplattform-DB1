package src;

import src.model.AuthManager;
import src.view.LoginView;
import src.controller.LoginController;

public class MainLogin {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView view = new LoginView();
            AuthManager model = new AuthManager();
            new LoginController(view, model);
        });
    }
}

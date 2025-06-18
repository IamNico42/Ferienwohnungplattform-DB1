package src.view;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    public JTextField userField;
    public JPasswordField passField;
    public JButton loginButton;
    public JLabel messageLabel;

    public LoginView() {
        setTitle("Kunden-Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);

        userField = new JTextField();
        passField = new JPasswordField();
        loginButton = new JButton("Login");
        messageLabel = new JLabel(" ");

        setLayout(new GridLayout(5, 1));
        add(new JLabel("Mail-Adresse:"));
        add(userField);
        add(new JLabel("Passwort:"));
        add(passField);
        add(loginButton);
        add(messageLabel);

        setVisible(true);
    }

    public void showMessage(String msg) {
        messageLabel.setText(msg);
    }
}

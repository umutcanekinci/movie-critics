package org.example.frame;
import org.example.DatabaseManager;
import org.example.data.User;
import org.example.widget.FloatingLabelField;
import org.example.widget.LogoAnimator;
import org.example.widget.UIHelper;

import org.example.widget.WidgetFactory;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class LoginFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrame.class.getName());
    
    private transient DatabaseManager dbManager = new DatabaseManager();
    
    private transient LogoAnimator logoAnimator;
    private boolean isLogin;
    
    private FloatingLabelField usernameFloating;
    private FloatingLabelField passwordFloating;
    private javax.swing.JButton confirmButton;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel footerLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JTextField passwordField;

    public LoginFrame() {
        setUndecorated(true);
        initComponents();

        var userList = dbManager.getAllUsers();
        isLogin = !userList.isEmpty();

        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);

        hideForm();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                layoutAndAnimate();
            }
        });

        confirmButton.registerKeyboardAction(e -> onConfirmPressed(null),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        rootPane.registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void initComponents() {
        mainPanel = new javax.swing.JPanel(null);
        iconLabel = new javax.swing.JLabel();
        confirmButton = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBackground(new java.awt.Color(0, 0, 0));

        iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/logo.png"))); // NOI18N
        iconLabel.setText("");
        iconLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        confirmButton.setBackground(new java.awt.Color(140, 82, 255));
        confirmButton.setFont(new java.awt.Font(WidgetFactory.FONT, 1, 18));
        confirmButton.setForeground(new java.awt.Color(255, 255, 255));
        confirmButton.setText(isLogin ? "Sign In" : "Register");
        confirmButton.setBorder(null);

        confirmButton.addActionListener(this::onConfirmPressed);

        usernameFloating = new FloatingLabelField("Username or email");
        usernameField = usernameFloating.getTextField();

        passwordFloating = new FloatingLabelField("Password", true);
        passwordField = passwordFloating.getTextField();

        titleLabel.setFont(new java.awt.Font(WidgetFactory.FONT, 1, 24));
        titleLabel.setForeground(new java.awt.Color(255, 255, 255));
        titleLabel.setText(isLogin ? "Sign In" : "Register");

        footerLabel = new javax.swing.JLabel("2026 @MovieCritics - All rights reserved.", javax.swing.SwingConstants.CENTER);
        footerLabel.setFont(new java.awt.Font(WidgetFactory.FONT, java.awt.Font.PLAIN, 12));
        footerLabel.setForeground(new java.awt.Color(90, 90, 90));

        mainPanel.add(iconLabel);
        mainPanel.add(titleLabel);
        mainPanel.add(usernameFloating);
        mainPanel.add(passwordFloating);
        mainPanel.add(confirmButton);
        mainPanel.add(footerLabel);

        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void layoutAndAnimate() {
        int sw = mainPanel.getWidth();
        int sh = mainPanel.getHeight();

        Icon icon = iconLabel.getIcon();
        int logoW = icon != null ? icon.getIconWidth() : 120;
        int logoH = icon != null ? icon.getIconHeight() : 120;

        int contentW = 300;
        int signInH = titleLabel.getPreferredSize().height;
        int fieldH = 64;
        int btnH = 40;
        int totalH = logoH + 30 + signInH + 18 + fieldH + 18 + fieldH + 28 + btnH;

        int contentY = (sh - totalH) / 2;
        int cx = (sw - contentW) / 2;
        int logoX      = (sw - logoW) / 2;
        int logoFinalY = contentY;
        int logoStartY = (sh - logoH) / 2;

        int y = contentY + logoH + 30;
        titleLabel.setBounds(cx, y, contentW, signInH);
        y += signInH + 18;
        usernameFloating.setBounds(cx, y, contentW, fieldH);
        y += fieldH + 18;
        passwordFloating.setBounds(cx, y, contentW, fieldH);
        y += fieldH + 28;
        confirmButton.setBounds(cx, y, contentW, btnH);

        iconLabel.setBounds(logoX, logoStartY, logoW, logoH);
        footerLabel.setBounds(0, sh - 36, sw, 20);

        logoAnimator = new LogoAnimator(iconLabel, mainPanel);
        logoAnimator.animateIn(logoX, logoStartY, logoFinalY, this::showForm);
    }

    private void hideForm() {
        titleLabel.setVisible(false);
        usernameFloating.setVisible(false);
        passwordFloating.setVisible(false);
        confirmButton.setVisible(false);
        mainPanel.repaint();
    }

    private void showForm() {
        titleLabel.setVisible(true);
        usernameFloating.setVisible(true);
        passwordFloating.setVisible(true);
        confirmButton.setVisible(true);
        mainPanel.repaint();
    }

    private void onConfirmPressed(java.awt.event.ActionEvent evt) {
        var username = usernameField.getText();
        var password = passwordField.getText();
        
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isLogin) {
             handleLogin(username, password);
        } else {
            handleRegistration(username, password);
        }
        
    }

    private void handleLogin(String username, String password) {
        var user = dbManager.getUserByUsernameOrEmail(username);
        if (user == null || !user.getPassword().equals(password)) {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        logger.info("User logged in: " + user.getUsername());
        animateOutThenOpen(user);
    }

    private void handleRegistration(String username, String password) {
        var existingUser = dbManager.getUserByUsernameOrEmail(username);
        if (existingUser != null) {
            JOptionPane.showMessageDialog(this, "Username or email already exists", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var newUser = new User(0, username, password, 2, username); // userType 2 for regular users
        UIHelper.runWithDbHandler(this, () -> {
            dbManager.addUser(newUser);
            logger.info("New user registered: " + newUser.getUsername());
            animateOutThenOpen(newUser);
        });
    }

    private void animateOutThenOpen(User user) {
        hideForm();
        confirmButton.setEnabled(false);
        logoAnimator.animateOut(() -> openMainFrame(user));
    }

    private void openMainFrame(User user) {
        if (user.getUserType() == 1) {
            new Type1MainFrame(dbManager, user).setVisible(true);
        } else {
            new Type2MainFrame(dbManager, user).setVisible(true);
        }
        dispose();
    }
}

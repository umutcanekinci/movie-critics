package org.example.dialog;

import org.example.DatabaseManager;
import org.example.data.User;
import org.example.widget.UIHelper;
import org.example.widget.WidgetFactory;

import javax.swing.*;
import java.awt.*;

public class UserEditDialog extends BaseEditDialog {

    private final User user;
    private final DatabaseManager dbManager;

    private JTextField        usernameField;
    private JTextField        emailField;
    private JTextField        passwordField;
    private JComboBox<String> userTypeCombo;

    public UserEditDialog(Frame parent, User user, DatabaseManager dbManager, Runnable onSave) {
        super(parent, onSave);
        this.user      = user;
        this.dbManager = dbManager;
        setup();
    }

    @Override
    public String getDialogTitle() {
        return user == null ? "Add User" : "Edit User";
    }

    @Override
    public boolean isEditMode() {
        return user != null;
    }

    @Override
    public JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WidgetFactory.DARK);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 0, 7, 0);

        int r = 0;
        usernameField = textRow(form, gbc, r++, "Username", user != null ? user.getUsername() : "");
        emailField    = textRow(form, gbc, r++, "Email",    user != null ? user.getEmail()    : "");
        passwordField = textRow(form, gbc, r++, "Password", user != null ? user.getPassword() : "");

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(rowLabel("User Type"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        userTypeCombo = new JComboBox<>(new String[]{"Child (0)", "Parent (1)"});
        userTypeCombo.setSelectedIndex(user != null && user.getUserType() == 1 ? 1 : 0);
        userTypeCombo.setBackground(WidgetFactory.CARD_BG);
        userTypeCombo.setForeground(Color.WHITE);
        userTypeCombo.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
        form.add(userTypeCombo, gbc);

        return form;
    }

    @Override
    public void doSave() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int type = userTypeCombo.getSelectedIndex();
        UIHelper.runWithDbHandler(this, () -> {
            if (user == null) dbManager.addUser(new User(0, username, password, type, email));
            else              dbManager.updateUser(new User(user.getId(), username, password, type, email));
            onSave.run();
            dispose();
        });
    }

    @Override
    public void doDelete() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete user \"" + user.getUsername() + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            UIHelper.runWithDbHandler(this, () -> {
                dbManager.deleteUser(user.getId());
                onSave.run();
                dispose();
            });
        }
    }
}

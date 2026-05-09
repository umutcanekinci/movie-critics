package org.example.widget;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;

public class UIHelper {

    @FunctionalInterface
    public interface DatabaseAction {
        void execute() throws SQLException;
    }

    /**
     * Executes a database action and automatically catches and displays SQLExceptions.
     * 
     * @param parent The parent GUI component for the error dialog.
     * @param action The database action to execute.
     * @return true if the action succeeded, false if it failed.
     */
    public static boolean runWithDbHandler(Component parent, DatabaseAction action) {
        try {
            action.execute();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                parent,
                ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}

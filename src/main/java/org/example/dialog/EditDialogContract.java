package org.example.dialog;

import javax.swing.*;

public interface EditDialogContract {
    String  getDialogTitle();
    JPanel  buildForm();
    boolean isEditMode();
    void    doSave();
    void    doDelete();
}

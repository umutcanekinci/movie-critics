package org.example.dialog;

import org.example.widget.WidgetFactory;
import javax.swing.*;
import java.awt.*;

public abstract class BaseEditDialog extends JDialog implements EditDialogContract {

    
    
    
    protected static final Color  RED     = new Color(200, 60, 60);

    protected final Runnable onSave;

    protected BaseEditDialog(Frame parent, Runnable onSave) {
        super(parent, true);
        this.onSave = onSave;
        setUndecorated(true);
        // buildUI() is NOT called here — subclasses must call setup() after
        // initialising their own fields, so interface methods see valid state.
    }

    /** Call at the end of every subclass constructor. */
    protected final void setup() {
        buildUI();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WidgetFactory.DARK);
        root.setBorder(BorderFactory.createLineBorder(WidgetFactory.PURPLE, 2));
        root.add(buildTitleBar(),    BorderLayout.NORTH);
        root.add(buildFormWrapper(), BorderLayout.CENTER);
        root.add(buildButtonRow(),   BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(30, 30, 30));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 12));
        JLabel lbl = new JLabel(getDialogTitle());
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 15));
        bar.add(lbl, BorderLayout.WEST);
        JButton close = WidgetFactory.createFlatButton("✕", new Color(30, 30, 30));
        close.addActionListener(e -> dispose());
        bar.add(close, BorderLayout.EAST);
        return bar;
    }

    /**
     * Wraps the form for the CENTER slot.  Default: returns {@code buildForm()}
     * directly.  Override to add a JScrollPane or other decoration.
     */
    protected JComponent buildFormWrapper() {
        return buildForm();
    }

    private JPanel buildButtonRow() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WidgetFactory.DARK);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));

        if (isEditMode()) {
            JButton delete = WidgetFactory.createFlatButton("Delete", RED);
            delete.setPreferredSize(new Dimension(90, 34));
            delete.addActionListener(e -> doDelete());
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            left.setBackground(WidgetFactory.DARK);
            left.add(delete);
            panel.add(left, BorderLayout.WEST);
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setBackground(WidgetFactory.DARK);
        JButton cancel = WidgetFactory.createFlatButton("Cancel", new Color(60, 60, 60));
        cancel.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dispose());
        JButton save = WidgetFactory.createFlatButton(isEditMode() ? "Save" : "Add", WidgetFactory.PURPLE);
        save.setPreferredSize(new Dimension(90, 34));
        save.addActionListener(e -> doSave());
        right.add(cancel);
        right.add(save);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ── Default no-op delete (override in subclasses that support deletion) ──

    @Override
    public void doDelete() {}

    // ── Shared UI helpers (not duplicated in subclasses) ──────────────────────

    /** Override to adjust the label column width for wider field names. */
    protected int getLabelWidth() {
        return 110;
    }

    protected JTextField textRow(JPanel form, GridBagConstraints gbc, int r,
                                  String label, String value) {
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(rowLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField f = WidgetFactory.createStyledField(value);
        form.add(f, gbc);
        return f;
    }

    protected JLabel rowLabel(String text) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setForeground(new Color(160, 160, 160));
        lbl.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(getLabelWidth(), 28));
        return lbl;
    }

    

    
}

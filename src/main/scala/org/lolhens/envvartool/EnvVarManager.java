package org.lolhens.envvartool;

import javax.swing.*;

/**
 * Created by LolHens on 09.12.2014.
 */
public class EnvVarManager extends JFrame {
    private JPanel contentPane;
    private JList lstEnvVars;
    private JButton btnDelete;
    private JButton btnEdit;
    private JTextField txtValue;
    private JButton btnValue;
    private JButton btnAdd;
    private JPanel panelValue;

    public EnvVarManager() {
        super(("EnvVarTool v" + Main.version() + " - Environment Variable Selector"));

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setEditEnabled(false);

        pack();
        setVisible(true);
    }

    private void setEditEnabled(boolean value) {
        txtValue.setEnabled(value);
        btnValue.setEnabled(value);
    }
}

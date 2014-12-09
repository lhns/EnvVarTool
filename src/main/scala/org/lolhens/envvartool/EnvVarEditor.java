package org.lolhens.envvartool;

import at.jta.Key;
import at.jta.RegistryErrorException;
import at.jta.Regor;

import javax.swing.*;

/**
 * Created by LolHens on 09.12.2014.
 */
public class EnvVarEditor extends JFrame {
    private JPanel panelEdit;
    private JPanel panelValue;
    private JButton btnValue;
    private JTextArea txtValue;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnEdit;
    private JPanel contentPane;
    private JPanel panelFile;
    private JButton btnSave;
    private JButton btnReload;
    private JButton btnCancel;
    private JPanel panelReject;
    private JList lstEntries;

    private final String envVar;
    private DefaultListModel<String> lstModelEntries = new DefaultListModel<>();

    public EnvVarEditor(String envVar) {
        super(("EnvVarTool v" + Main.version() + " - Environment Variable Editor"));

        this.envVar = envVar;

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lstEntries.setModel(lstModelEntries);

        setEditEnabled(false);

        System.out.println(envVar);
        String value = WindowsRegistry.readValue(Main.envVarPath(), envVar);
        WindowsRegistry2.testKey(WindowsRegistry2.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", "Path");
        System.out.println(value);
        try {
            System.out.println(Main.regor().readAnyValueString(new Key(Regor._HKEY_LOCAL_MACHINE, "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"), "Path"));
        } catch (RegistryErrorException e) {
            e.printStackTrace();
        }
        //if (value != null && !value.equals("")) for (String part : value.split(";")) lstModelEntries.addElement(part);

        pack();
        setVisible(true);
    }

    private void setEditEnabled(boolean value) {
        txtValue.setEnabled(value);
        btnValue.setEnabled(value);
    }
}

package org.lolhens.envvartool;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;

import javax.swing.*;

import java.rmi.*;
import java.rmi.registry.Registry;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

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

        WinReg.HKEYByReference ref = Advapi32Util.registryGetKey(HKEY_LOCAL_MACHINE, Main.envVarPath(), WinNT.KEY_READ);
        System.out.println(ref);
        System.out.println(ref.getValue());
        System.out.println(ref.nativeType());
        System.out.println(ref.getValue().nativeType());
        System.out.println(Advapi32Util.registryGetValue(HKEY_LOCAL_MACHINE, Main.envVarPath(), envVar));
        System.out.println(Advapi32Util.registry(HKEY_LOCAL_MACHINE, Main.envVarPath(), envVar));
        System.out.println(envVar);
        String value = WindowsRegistry.readValue(Main.envVarPath(), envVar);
        System.out.println(value);
        //if (value != null && !value.equals("")) for (String part : value.split(";")) lstModelEntries.addElement(part);

        pack();
        setVisible(true);
    }

    private void setEditEnabled(boolean value) {
        txtValue.setEnabled(value);
        btnValue.setEnabled(value);
    }
}

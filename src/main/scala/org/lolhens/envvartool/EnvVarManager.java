package org.lolhens.envvartool;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinNT;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.HashMap;
import java.util.Map;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

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
    private JButton btnReload;
    private JCheckBox ckValueExp;

    private DefaultListModel<String> lstModelEnvVars = new DefaultListModel<>();
    private Map<JComponent, Integer> componentState = new HashMap<>();

    public EnvVarManager() {
        super(("EnvVarTool v" + Main.version() + " - Environment Variable Manager"));

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lstEnvVars.setModel(lstModelEnvVars);

        setState(false, btnDelete, btnEdit, txtValue, ckValueExp, btnValue);

        reload();

        lstEnvVars.addListSelectionListener((e) -> {
            int index = lstEnvVars.getSelectedIndex();
            if (!txtValue.isEnabled()) setState(index > -1 && lstModelEnvVars.get(index) != null, btnEdit, btnDelete);
        });

        btnReload.addActionListener((e) -> reload());
        btnAdd.addActionListener((e) -> {
            setState(false, btnReload, btnAdd, btnDelete, btnEdit);
            setState(true, txtValue, ckValueExp);
        });
        btnDelete.addActionListener((e) -> {
            int index = lstEnvVars.getSelectedIndex();
            if (index > -1) {
                lstEnvVars.setSelectedIndex(-1);
                String value = lstModelEnvVars.get(index);
                Advapi32Util.registryDeleteValue(HKEY_LOCAL_MACHINE, Main.envVarPath(), value);

                lstModelEnvVars.remove(index);
            }
        });
        btnEdit.addActionListener((e) -> {
            int index = lstEnvVars.getSelectedIndex();
            if (index > -1) {
                new EnvVarEditor(lstModelEnvVars.get(index));
            }
        });

        btnValue.addActionListener((e) -> {
            String value = txtValue.getText();
            boolean exp = ckValueExp.isSelected();

            Advapi32UtilExt.registrySetValue(HKEY_LOCAL_MACHINE, Main.envVarPath(), value, "", exp ? WinNT.REG_EXPAND_SZ : WinNT.REG_SZ);

            int index = lstModelEnvVars.size();
            lstModelEnvVars.add(index, value);

            txtValue.setText("");
            ckValueExp.setSelected(false);
            setState(true, btnReload, btnAdd);
            setState(false, txtValue, ckValueExp, btnValue);
            int selIndex = lstEnvVars.getSelectedIndex();
            if (selIndex > -1 && lstModelEnvVars.get(selIndex) != null) setState(true, btnEdit, btnDelete);
        });
        txtValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!txtValue.getText().equals("") && !btnValue.isEnabled()) setState(true, btnValue);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (txtValue.getText().equals("") && btnValue.isEnabled()) setState(false, btnValue);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        pack();
        setVisible(true);
    }

    private void reload() {
        lstModelEnvVars.clear();
        for (String key : Advapi32Util.registryGetValues(HKEY_LOCAL_MACHINE, Main.envVarPath()).keySet()) {
            lstModelEnvVars.addElement(key);
        }
    }

    private void setState(boolean value, JComponent... components) {
        for (JComponent component : components) {
            int state;
            if (!componentState.containsKey(component))
                state = value ? 0 : 1;
            else
                state = componentState.get(component);
            if ((state == 0 && value) || (state == 1 && !value)) component.setEnabled(value);
            state += (value ? 1 : -1);
            if (state > 1) state = 1;
            else if (state < 0) state = 0;
            componentState.put(component, state);
        }
    }
}

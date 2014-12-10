package org.lolhens.envvartool;

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
public class EnvVarEditor extends JFrame {
    private JPanel panelEdit;
    private JPanel panelValue;
    private JButton btnValueOk;
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
    private JButton btnValueCancel;
    private JPanel panelValueFile;

    private DefaultListModel<String> lstModelEntries = new DefaultListModel<>();
    private Map<JComponent, Integer> componentState = new HashMap<>();

    private final String envVar;
    private int envVarType;
    private String editing = null;

    public EnvVarEditor(String envVar) {
        super(("EnvVarTool v" + Main.version() + " - Environment Variable Editor"));

        this.envVar = envVar;

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        lstEntries.setModel(lstModelEntries);

        setState(false, txtValue, btnValueOk, btnValueCancel, btnEdit, btnDelete);

        reload();

        lstEntries.addListSelectionListener((e) -> {
            int index = lstEntries.getSelectedIndex();
            setState(index > -1 && lstModelEntries.get(index) != null, btnEdit, btnDelete);
        });

        btnSave.addActionListener((e) -> save());
        btnReload.addActionListener((e) -> reload());
        btnCancel.addActionListener((e) -> dispose());

        btnAdd.addActionListener((e) -> {
            setState(false, btnSave, btnReload, btnAdd, btnEdit, btnDelete);
            setState(true, txtValue, btnValueCancel);
        });
        btnDelete.addActionListener((e) -> {
            int index = lstEntries.getSelectedIndex();
            if (index > -1) {
                lstEntries.setSelectedIndex(-1);
                lstModelEntries.remove(index);
            }
        });
        btnEdit.addActionListener((e) -> {
            int index = lstEntries.getSelectedIndex();
            if (index > -1) {
                editing = lstModelEntries.get(index);
                txtValue.setText(editing);

                setState(false, btnSave, btnReload, btnAdd, btnEdit, btnDelete);
                setState(true, txtValue, btnValueCancel);
            }
        });

        btnValueOk.addActionListener((e) -> {
            int index = -1;
            if (editing != null) {
                index = lstModelEntries.indexOf(editing);
                if (index > -1) lstModelEntries.remove(index);
                editing = null;
            }
            if (index < 0) index = lstModelEntries.size();
            lstModelEntries.add(index, txtValue.getText());

            txtValue.setText("");
            setState(true, btnSave, btnReload, btnAdd);
            setState(false, txtValue, btnValueOk, btnValueCancel);
            int selIndex = lstEntries.getSelectedIndex();
            if (selIndex > -1 && lstModelEntries.get(selIndex) != null) setState(true, btnEdit, btnDelete);
        });
        btnValueCancel.addActionListener((e) -> {
            editing = null;

            txtValue.setText("");
            setState(true, btnSave, btnReload, btnAdd);
            setState(false, txtValue, btnValueOk, btnValueCancel);
            int selIndex = lstEntries.getSelectedIndex();
            if (selIndex > -1 && lstModelEntries.get(selIndex) != null) setState(true, btnEdit, btnDelete);
        });
        txtValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!txtValue.getText().equals("") && !btnValueOk.isEnabled()) setState(true, btnValueOk);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (txtValue.getText().equals("") && btnValueOk.isEnabled()) setState(false, btnValueOk);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        pack();
        setVisible(true);
    }

    private void reload() {
        setState(false, btnSave);
        lstModelEntries.clear();
        envVarType = Advapi32UtilExt.registryGetValueType(HKEY_LOCAL_MACHINE, Main.envVarPath(), envVar);
        if (envVarType == WinNT.REG_EXPAND_SZ || envVarType == WinNT.REG_SZ) {
            String value = (String) Advapi32UtilExt.registryGetValue(HKEY_LOCAL_MACHINE, Main.envVarPath(), envVar);
            if (value != null) {
                for (String part : value.split(";")) if (!part.equals("")) lstModelEntries.addElement(part);
                setState(true, btnSave);
            }
        }
    }

    private void save() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < lstModelEntries.getSize(); i++) {
            string.append(lstModelEntries.get(i));
            if (i + 1 < lstModelEntries.getSize()) string.append(";");
        }
        Advapi32UtilExt.registrySetValue(HKEY_LOCAL_MACHINE, Main.envVarPath(), envVar, string.toString(), envVarType);
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

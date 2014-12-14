package org.lolhens.envvartool;

import com.sun.jna.platform.win32.WinNT;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
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
        super(("EnvVarTool v" + Main.version + " - Environment Variable Editor"));

        this.envVar = envVar;

        setContentPane(contentPane);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        lstEntries.setModel(lstModelEntries);

        setState(false, txtValue, btnValueOk, btnValueCancel, btnEdit, btnDelete);

        reload();

        lstEntries.addListSelectionListener((e) -> {
            int index = lstEntries.getSelectedIndex();
            if (!txtValue.isEnabled()) setState(index > -1 && lstModelEntries.get(index) != null, btnEdit, btnDelete);
        });

        btnSave.addActionListener((e) -> {
            save();
            dispose();
        });
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
        envVarType = Advapi32UtilExt.registryGetValueType(HKEY_LOCAL_MACHINE, Main.envVarPath, envVar);
        if (envVarType == WinNT.REG_EXPAND_SZ || envVarType == WinNT.REG_SZ) {
            String value = (String) Advapi32UtilExt.registryGetValue(HKEY_LOCAL_MACHINE, Main.envVarPath, envVar);
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
        Advapi32UtilExt.registrySetValue(HKEY_LOCAL_MACHINE, Main.envVarPath, envVar, string.toString(), envVarType);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        panelFile = new JPanel();
        panelFile.setLayout(new BorderLayout(0, 0));
        contentPane.add(panelFile, BorderLayout.SOUTH);
        panelFile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null));
        btnSave = new JButton();
        btnSave.setHorizontalAlignment(0);
        btnSave.setIcon(new ImageIcon(getClass().getResource("/ok.png")));
        btnSave.setText("Save");
        panelFile.add(btnSave, BorderLayout.CENTER);
        panelReject = new JPanel();
        panelReject.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 10, 0, 0), -1, -1));
        panelFile.add(panelReject, BorderLayout.EAST);
        btnReload = new JButton();
        btnReload.setIcon(new ImageIcon(getClass().getResource("/renew.png")));
        btnReload.setText("Reload");
        panelReject.add(btnReload, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setIcon(new ImageIcon(getClass().getResource("/delete.png")));
        btnCancel.setText("Cancel");
        panelReject.add(btnCancel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelEdit = new JPanel();
        panelEdit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 4, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.add(panelEdit, BorderLayout.CENTER);
        panelEdit.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        panelValue = new JPanel();
        panelValue.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelValue.setEnabled(true);
        panelEdit.add(panelValue, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panelValue.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        panelValueFile = new JPanel();
        panelValueFile.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelValue.add(panelValueFile, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnValueOk = new JButton();
        btnValueOk.setEnabled(true);
        btnValueOk.setIcon(new ImageIcon(getClass().getResource("/ok.png")));
        btnValueOk.setText("");
        panelValueFile.add(btnValueOk, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnValueCancel = new JButton();
        btnValueCancel.setIcon(new ImageIcon(getClass().getResource("/delete.png")));
        btnValueCancel.setText("");
        panelValueFile.add(btnValueCancel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelValue.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtValue = new JTextArea();
        txtValue.setEnabled(true);
        scrollPane1.setViewportView(txtValue);
        btnAdd = new JButton();
        btnAdd.setIcon(new ImageIcon(getClass().getResource("/add.png")));
        btnAdd.setText("");
        panelEdit.add(btnAdd, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(30, -1), new Dimension(30, -1), new Dimension(30, -1), 0, false));
        btnDelete = new JButton();
        btnDelete.setHorizontalAlignment(0);
        btnDelete.setIcon(new ImageIcon(getClass().getResource("/delete.png")));
        btnDelete.setText("");
        panelEdit.add(btnDelete, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(30, -1), new Dimension(30, -1), new Dimension(30, -1), 0, false));
        btnEdit = new JButton();
        btnEdit.setHorizontalAlignment(0);
        btnEdit.setHorizontalTextPosition(11);
        btnEdit.setIcon(new ImageIcon(getClass().getResource("/edit.png")));
        btnEdit.setText("");
        panelEdit.add(btnEdit, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(30, -1), new Dimension(30, -1), new Dimension(30, -1), 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panelEdit.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panelEdit.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        lstEntries = new JList();
        lstEntries.setEnabled(true);
        scrollPane2.setViewportView(lstEntries);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}

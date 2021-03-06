package org.mikewellback.plugins.project_configuration;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.Arrays;

public class ConfigurationView {

    private final boolean USE_DEBUG_BUTTONS = true;

    private String basePath = "";

    private final JPanel basePanel = new JPanel();
    private ConfigurationProperty[] currentProps;

    public ConfigurationView(String basePath, ToolWindow toolWindow) {
        this.basePath = basePath;
        LocalFileSystem.getInstance().addVirtualFileListener(new VirtualFileChangeListener(
                new VirtualFileChangeListener.ContentsChangedListener() {
                    @Override
                    public void contentsChanged(@NotNull VirtualFileEvent event) {
                        if (ConfigurationProperty.CONFIG_FILE_NAME.equals(event.getFileName())) {
                            composeConfigurationView();
                            basePanel.updateUI();
                            basePanel.revalidate();
                        }
                    }

                    @Override
                    public void contentsDeleted(@NotNull VirtualFileEvent event) {
                        if (ConfigurationProperty.CONFIG_FILE_NAME.equals(event.getFileName())) {
                            composeEmptyView();
                            basePanel.updateUI();
                            basePanel.revalidate();
                        }
                    }
                })
        );
    }

    public JComponent initView() {
        if (ConfigurationProperty.getFile(basePath).exists()) {
            composeConfigurationView();
        } else {
            composeEmptyView();
        }
        return new JBScrollPane(basePanel);
    }

    private void composeEmptyView() {
        GridBagLayout glm = new GridBagLayout();
        Insets ins = JBUI.insets(5, 15);
        basePanel.removeAll();
        basePanel.setLayout(glm);
        JLabel info = new JLabel("Properties configuration file not found");
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = ins;
        basePanel.add(info, gc);
        GridBagConstraints gc1 = new GridBagConstraints();
        gc1.gridy = 1;
        gc1.insets = ins;
        JButton create = new JButton("Create it now");
        create.addActionListener(e -> {
            currentProps = ConfigurationProperty.getDummyProps();
            ConfigurationProperty.writeProperties(basePath, currentProps);
            composeConfigurationView();
            basePanel.updateUI();
            basePanel.revalidate();
        });
        basePanel.add(create, gc1);
        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.gridy = 2;
        gc2.insets = ins;
        JButton check = new JButton("Check again");
        check.addActionListener(e -> {
            if (ConfigurationProperty.getFile(basePath).exists()) {
                composeConfigurationView();
                basePanel.updateUI();
                basePanel.revalidate();
            }
        });
        basePanel.add(check, gc2);
    }

    private void composeConfigurationView() {
        currentProps = ConfigurationProperty.readProperties(basePath);
        int vn = 0;
        for (ConfigurationProperty prop: currentProps) {
            if (prop.isValue()) {
                vn++;
            }
        }
        double[] rowWeights = new double[vn + (USE_DEBUG_BUTTONS ? 1 : 0)];
        Arrays.fill(rowWeights, 1);
        GridBagLayout glm = new GridBagLayout();
        glm.columnWeights = new double[]{3, 70};
        glm.rowWeights = rowWeights;
        Insets ins = JBUI.insets(5, 15);
        basePanel.removeAll();
        basePanel.setLayout(glm);
        int r = 0;
        for (ConfigurationProperty v: currentProps) {
            if (!v.isValue()) {
                continue;
            }
            JLabel t1 = new JLabel(v.getName());
            GridBagConstraints gc1 = new GridBagConstraints();
            gc1.gridy = r;
            gc1.gridx = 0;
            gc1.anchor = GridBagConstraints.EAST;
            gc1.insets = ins;
            basePanel.add(t1, gc1);
            Component t2 = null;
            switch (v.getType()) {
                case BOOLEAN:
                    JCheckBox cb = new JCheckBox(v.getValue(), v.getBooleanValue());
                    cb.addChangeListener(e -> {
                        JCheckBox s = (JCheckBox) e.getSource();
                        v.setValue(s.isSelected() ? "true" : "false");
                        s.setText(v.getValue());
                    });
                    t2 = cb;
                    break;
                case NUMBER:
                    SpinnerNumberModel mod = new SpinnerNumberModel();
                    mod.setValue(v.getNumberValue());
                    JSpinner spin = new JSpinner(mod);
                    spin.addChangeListener(e -> {
                        JSpinner s = (JSpinner) e.getSource();
                        v.setValue(s.getValue().toString());
                    });
                    t2 = spin;
                    break;
                case TEXT:
                    JTextField tf = new JTextField(v.getValue());
                    DocumentListener dl = new DocumentListener() {

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            setFullValue(e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            setFullValue(e);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            setFullValue(e);
                        }

                        private void setFullValue(DocumentEvent e) {
                            try {
                               v.setValue(e.getDocument().getText(0, e.getDocument().getLength()));
                            } catch (BadLocationException ignored) {}
                        }
                    };

                    tf.getDocument().addDocumentListener(dl);
                    t2 = tf;
                    break;
                case ARRAY:
                    JComboBox<String> jcb = new ComboBox<>(v.getValues());
                    jcb.setSelectedItem(v.getSelectedValue());
                    jcb.addItemListener(e -> {
                        v.setValue(e.getItem().toString());
                    });
                    t2 = jcb;
                    break;
                case LOCKED:
                    t2 = new JLabel(v.getValue());
                    break;
            }
            if (t2 != null) {
                GridBagConstraints gc2 = new GridBagConstraints();
                gc2.gridy = r;
                gc2.gridx = 1;
                gc2.fill = GridBagConstraints.HORIZONTAL;
                gc2.insets = ins;
                basePanel.add(t2, gc2);
            }
            r++;
        }

        if (USE_DEBUG_BUTTONS) {
            addDebugButtons(r);
        }
    }

    private void addDebugButtons(int r) {
        GridBagConstraints gc1 = new GridBagConstraints();
        gc1.gridy = r;
        gc1.gridx = 0;
        JButton f5 = new JButton("Refresh");
        f5.addActionListener(e -> {
            composeConfigurationView();
            basePanel.updateUI();
            basePanel.revalidate();
            GradleFileMaker.writeGradleFile(basePath, currentProps);
        });
        basePanel.add(f5, gc1);

        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.gridy = r;
        gc2.gridx = 1;
        JButton ctrls = new JButton("Save");
        ctrls.addActionListener(e -> {
            ConfigurationProperty.writeProperties(basePath, currentProps);
        });
        basePanel.add(ctrls, gc2);
    }
}
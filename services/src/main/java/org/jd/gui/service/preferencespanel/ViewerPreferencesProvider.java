/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.preferencespanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.jd.gui.spi.PreferencesPanel;
import org.jd.gui.util.MessageUtil;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ViewerPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener {
    protected static final int MIN_VALUE = 2;
    protected static final int MAX_VALUE = 40;
    protected static final String FONT_SIZE_KEY = "ViewerPreferences.fontSize";
    protected static final String LANGUAGE_KEY = "ViewerPreferences.languageKey";

    protected PreferencesPanel.PreferencesPanelChangeListener listener = null;
    protected JTextField fontSizeTextField;
    protected JComboBox<String> languageComboBox;
    protected Color errorBackgroundColor = Color.RED;
    protected Color defaultBackgroundColor;

    public ViewerPreferencesProvider() {
        super(new GridLayout(0, 2));

        add(new JLabel(String.format(MessageUtil.getMessage("preferences.viewer.appearance.1"), MIN_VALUE, MAX_VALUE)), BorderLayout.WEST);

        fontSizeTextField = new JTextField();
        fontSizeTextField.getDocument().addDocumentListener(this);
        add(fontSizeTextField, BorderLayout.CENTER);

        add(new JLabel(MessageUtil.getMessage("preferences.viewer.language.title")), BorderLayout.WEST);

        languageComboBox = new JComboBox<>();//创建一个下拉列表框c1
        languageComboBox.addItem("EN");
        languageComboBox.addItem("ZH_CN");
        languageComboBox.addItemListener(e -> {
            if (ItemEvent.SELECTED == e.getStateChange()) {
                //getSelectedItem()返回当前所选项
                //getSelectedIndex()返回列表中与给定项匹配的第一个选项。
                Object selectedItem = languageComboBox.getSelectedItem();
                if (selectedItem instanceof String) {
                    MessageUtil.setLocale(new Locale(selectedItem.toString().toLowerCase()));
                }
            }
        });
        add(languageComboBox);

        defaultBackgroundColor = fontSizeTextField.getBackground();
    }

    // --- PreferencesPanel --- //
    @Override
    public String getPreferencesGroupTitle() {
        return MessageUtil.getMessage("preferences.viewer.title");
    }

    @Override
    public String getPreferencesPanelTitle() {
        return MessageUtil.getMessage("preferences.viewer.appearance.title");
    }

    @Override
    public JComponent getPanel() {
        return this;
    }

    @Override
    public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public void loadPreferences(Map<String, String> preferences) {
        String fontSize = preferences.get(FONT_SIZE_KEY);

        if (fontSize == null) {
            // Search default value for the current platform
            RSyntaxTextArea textArea = new RSyntaxTextArea();

            try {
                Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream("rsyntaxtextarea/themes/eclipse.xml"));
                theme.apply(textArea);
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }

            fontSize = String.valueOf(textArea.getFont().getSize());
        }

        fontSizeTextField.setText(fontSize);
        fontSizeTextField.setCaretPosition(fontSizeTextField.getText().length());

        String language = preferences.get(LANGUAGE_KEY);
        if (language != null) {
            languageComboBox.setSelectedItem(language);
        }
    }

    @Override
    public void savePreferences(Map<String, String> preferences) {
        preferences.put(FONT_SIZE_KEY, fontSizeTextField.getText());
        preferences.put(LANGUAGE_KEY, languageComboBox.getSelectedItem().toString());
    }

    @Override
    public boolean arePreferencesValid() {
        try {
            int i = Integer.parseInt(fontSizeTextField.getText());
            return (i >= MIN_VALUE) && (i <= MAX_VALUE);
        } catch (NumberFormatException e) {
            assert ExceptionUtil.printStackTrace(e);
            return false;
        }
    }

    @Override
    public void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {
        this.listener = listener;
    }

    // --- DocumentListener --- //
    @Override
    public void insertUpdate(DocumentEvent e) {
        onTextChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onTextChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        onTextChange();
    }

    public void onTextChange() {
        fontSizeTextField.setBackground(arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor);

        if (listener != null) {
            listener.preferencesPanelChanged(this);
        }
    }
}

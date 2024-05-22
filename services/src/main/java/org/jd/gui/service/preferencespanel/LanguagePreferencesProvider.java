package org.jd.gui.service.preferencespanel;

import org.jd.gui.spi.PreferencesPanel;
import org.jd.gui.util.MessageUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Map;

public class LanguagePreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener {
    protected static final String LANGUAGE_KEY = "ClassFileSaverPreferences.writeMetadata";

    protected PreferencesPanel.PreferencesPanelChangeListener listener = null;
    protected Color errorBackgroundColor = Color.RED;
    protected JComboBox<String> languageComboBox;

    public LanguagePreferencesProvider() {
        super(new GridLayout(0,1));

        languageComboBox = new JComboBox<>();//创建一个下拉列表框c1
        languageComboBox.addItem("ZH_CN");
        languageComboBox.addItem("ENG");
        add(languageComboBox);
    }

    // --- PreferencesPanel --- //
    @Override public String getPreferencesGroupTitle() { return MessageUtil.getMessage("preferences.viewer.language.title"); }
    @Override public String getPreferencesPanelTitle() { return MessageUtil.getMessage("preferences.viewer.language.1"); }
    @Override public JComponent getPanel() { return this; }

    @Override public void init(Color errorBackgroundColor) {}

    @Override public boolean isActivated() { return true; }

    @Override
    public void loadPreferences(Map<String, String> preferences) {
        MessageUtil.setLanguage(preferences.get(LANGUAGE_KEY));
    }

    @Override
    public void savePreferences(Map<String, String> preferences) {
        preferences.put(LANGUAGE_KEY, languageComboBox.getPrototypeDisplayValue());
    }

    @Override public boolean arePreferencesValid() { return true; }

    @Override public void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {}

    // --- DocumentListener --- //
    @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void changedUpdate(DocumentEvent e) { onTextChange(); }

    public void onTextChange() {
    }

}

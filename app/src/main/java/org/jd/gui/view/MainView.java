/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.Constants;
import org.jd.gui.api.API;
import org.jd.gui.api.feature.*;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.model.history.History;
import org.jd.gui.service.platform.PlatformService;
import org.jd.gui.util.MessageUtil;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.view.component.IconButton;
import org.jd.gui.view.component.panel.MainTabbedPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.jd.gui.util.swing.SwingUtil.*;

@SuppressWarnings("unchecked")
public class MainView<T extends JComponent & UriGettable> implements UriOpenable, PreferencesChangeListener {
    protected History history;
    protected Consumer<File> openFilesCallback;
    protected JFrame mainFrame;
    protected JMenu recentFiles;
    protected Action closeAction;
    protected Action openTypeAction;
    protected Action backwardAction;
    protected Action forwardAction;
    protected MainTabbedPanel mainTabbedPanel;
    protected Box findPanel;
    protected JComboBox findComboBox;
    protected JCheckBox findCaseSensitive;
    protected JCheckBox findRegex;
    protected Color findBackgroundColor;
    protected Color findErrorBackgroundColor;

    public MainView(
            Configuration configuration, API api, History history,
            ActionListener openActionListener,
            ActionListener closeActionListener,
            ActionListener saveActionListener,
            ActionListener saveAllSourcesActionListener,
            ActionListener exitActionListener,
            ActionListener copyActionListener,
            ActionListener pasteActionListener,
            ActionListener selectAllActionListener,
            ActionListener findActionListener,
            ActionListener findPreviousActionListener,
            ActionListener findNextActionListener,
            ActionListener findCaseSensitiveActionListener,
            Runnable findCriteriaChangedCallback,
            ActionListener openTypeActionListener,
            ActionListener openTypeHierarchyActionListener,
            ActionListener goToActionListener,
            ActionListener backwardActionListener,
            ActionListener forwardActionListener,
            ActionListener searchActionListener,
            ActionListener jdWebSiteActionListener,
            ActionListener jdGuiIssuesActionListener,
            ActionListener jdCoreIssuesActionListener,
            ActionListener preferencesActionListener,
            ActionListener aboutActionListener,
            Runnable panelClosedCallback,
            Consumer<T> currentPageChangedCallback,
            Consumer<File> openFilesCallback) {
        this.history = history;
        this.openFilesCallback = openFilesCallback;
        // Build GUI
        invokeLater(() -> {
            mainFrame = new JFrame("Java Decompiler");
            mainFrame.setIconImages(Arrays.asList(getImage("/org/jd/gui/images/jd_icon_32.png"), getImage("/org/jd/gui/images/jd_icon_64.png"), getImage("/org/jd/gui/images/jd_icon_128.png")));
            mainFrame.setMinimumSize(new Dimension(Constants.MINIMAL_WIDTH, Constants.MINIMAL_HEIGHT));
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Find panel //
            Action findNextAction = newAction("Next", newImageIcon("/org/jd/gui/images/next_nav.png"), true, findNextActionListener);
            findPanel = Box.createHorizontalBox();
            findPanel.setVisible(false);
            findPanel.add(new JLabel("Find: "));
            findComboBox = new JComboBox();
            findComboBox.setEditable(true);
            JComponent editorComponent = (JComponent)findComboBox.getEditor().getEditorComponent();
            editorComponent.addKeyListener(new KeyAdapter() {
                protected String lastStr = "";

                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ESCAPE:
                            findPanel.setVisible(false);
                            break;
                        case KeyEvent.VK_ENTER:
                            String str = getFindText();
                            if (str.length() > 1) {
                                int index = ((DefaultComboBoxModel)findComboBox.getModel()).getIndexOf(str);
                                if(index != -1 ) {
                                    findComboBox.removeItemAt(index);
                                }
                                findComboBox.insertItemAt(str, 0);
                                findComboBox.setSelectedIndex(0);
                                findNextAction.actionPerformed(null);
                            }
                            break;
                        default:
                            str = getFindText();
                            if (! lastStr.equals(str)) {
                                findCriteriaChangedCallback.run();
                                lastStr = str;
                            }
                    }
                }
            });
            editorComponent.setOpaque(true);
            findComboBox.setBackground(this.findBackgroundColor = editorComponent.getBackground());
            this.findErrorBackgroundColor = Color.decode(configuration.getPreferences().get("JdGuiPreferences.errorBackgroundColor"));

            findPanel.add(findComboBox);
            findPanel.add(Box.createHorizontalStrut(5));
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setRollover(true);

            IconButton findNextButton = new IconButton("Next", newAction(newImageIcon("/org/jd/gui/images/next_nav.png"), true, findNextActionListener));
            toolBar.add(findNextButton);

            toolBar.add(Box.createHorizontalStrut(5));

            IconButton findPreviousButton = new IconButton("Previous", newAction(newImageIcon("/org/jd/gui/images/prev_nav.png"), true, findPreviousActionListener));
            toolBar.add(findPreviousButton);

            findPanel.add(toolBar);
            findCaseSensitive = new JCheckBox();
            findCaseSensitive.setAction(newAction("Case sensitive", true, findCaseSensitiveActionListener));
            findPanel.add(findCaseSensitive);
            findRegex = new JCheckBox();
            findRegex.setAction(newAction("Regex", true, findCaseSensitiveActionListener));
            findPanel.add(findRegex);
            findPanel.add(Box.createHorizontalGlue());

            IconButton findCloseButton = new IconButton(newAction(null, null, true, e -> findPanel.setVisible(false)));
            findCloseButton.setContentAreaFilled(false);
            findCloseButton.setIcon(newImageIcon("/org/jd/gui/images/close.gif"));
            findCloseButton.setRolloverIcon(newImageIcon("/org/jd/gui/images/close_active.gif"));
            findPanel.add(findCloseButton);

            if (PlatformService.getInstance().isMac()) {
                findPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
                Border border = BorderFactory.createEmptyBorder();
                findNextButton.setBorder(border);
                findPreviousButton.setBorder(border);
                findCloseButton.setBorder(border);
            } else {
                findPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
            }

            // Actions //
            boolean browser = Desktop.isDesktopSupported() ? Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) : false;

            // file menu actions
            Action openAction = newAction(MessageUtil.getMessage("file.submenu.1"), newImageIcon("/org/jd/gui/images/open.png"), true, "Open a file", openActionListener);
            closeAction = newAction(MessageUtil.getMessage("file.submenu.2"), false, closeActionListener);
            Action saveAction = newAction(MessageUtil.getMessage("file.submenu.3"), newImageIcon("/org/jd/gui/images/save.png"), false, saveActionListener);
            Action saveAllSourcesAction = newAction(MessageUtil.getMessage("file.submenu.4"), newImageIcon("/org/jd/gui/images/save_all.png"), false, saveAllSourcesActionListener);
            Action exitAction = newAction(MessageUtil.getMessage("file.submenu.6"), true, "Quit this program", exitActionListener);

            // edit menu actions
            Action copyAction = newAction(MessageUtil.getMessage("edit.submenu.1"), newImageIcon("/org/jd/gui/images/copy.png"), false, copyActionListener);
            Action pasteAction = newAction(MessageUtil.getMessage("edit.submenu.2"), newImageIcon("/org/jd/gui/images/paste.png"), true, pasteActionListener);
            Action selectAllAction = newAction(MessageUtil.getMessage("edit.submenu.3"), false, selectAllActionListener);
            Action findAction = newAction(MessageUtil.getMessage("edit.submenu.4"), false, findActionListener);

            // navigation menu actions
            openTypeAction = newAction(MessageUtil.getMessage("navigation.submenu.1"), newImageIcon("/org/jd/gui/images/open_type.png"), false, openTypeActionListener);
            Action openTypeHierarchyAction = newAction(MessageUtil.getMessage("navigation.submenu.2"), false, openTypeHierarchyActionListener);
            Action goToAction = newAction(MessageUtil.getMessage("navigation.submenu.3"), false, goToActionListener);
            backwardAction = newAction(MessageUtil.getMessage("navigation.submenu.4"), newImageIcon("/org/jd/gui/images/backward_nav.png"), false, backwardActionListener);
            forwardAction = newAction(MessageUtil.getMessage("navigation.submenu.5"), newImageIcon("/org/jd/gui/images/forward_nav.png"), false, forwardActionListener);

            // search menu actions
            Action searchAction = newAction(MessageUtil.getMessage("search.submenu.1"),newImageIcon("/org/jd/gui/images/search_src.png"), false, searchActionListener);

            // charset menu actions
//            Action gbkAction = newAction("GBK", newImageIcon("/org/jd/gui/images/search_src.png"), false, searchActionListener);

            // help menu actions
            Action jdWebSiteAction = newAction(MessageUtil.getMessage("help.submenu.1"), browser, "Open JD Web site", jdWebSiteActionListener);
            Action jdGuiIssuesActionAction = newAction(MessageUtil.getMessage("help.submenu.2"), browser, "Open JD-GUI issues page", jdGuiIssuesActionListener);
            Action jdCoreIssuesActionAction = newAction(MessageUtil.getMessage("help.submenu.3"), browser, "Open JD-Core issues page", jdCoreIssuesActionListener);
            Action preferencesAction = newAction(MessageUtil.getMessage("help.submenu.4"), newImageIcon("/org/jd/gui/images/preferences.png"), true, "Open the preferences panel", preferencesActionListener);
            Action aboutAction = newAction(MessageUtil.getMessage("help.submenu.5"), true, "About JD-GUI", aboutActionListener);

            // Menu //
            int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            JMenuBar menuBar = new JMenuBar();

            JMenu fileMenu = new JMenu(MessageUtil.getMessage("file.menu"));
            menuBar.add(fileMenu);
            fileMenu.add(openAction).setAccelerator(KeyStroke.getKeyStroke('O', menuShortcutKeyMask));
            fileMenu.addSeparator();
            fileMenu.add(closeAction).setAccelerator(KeyStroke.getKeyStroke('W', menuShortcutKeyMask));
            fileMenu.addSeparator();
            fileMenu.add(saveAction).setAccelerator(KeyStroke.getKeyStroke('S', menuShortcutKeyMask));
            fileMenu.add(saveAllSourcesAction).setAccelerator(KeyStroke.getKeyStroke('S', menuShortcutKeyMask|InputEvent.ALT_MASK));
            fileMenu.addSeparator();
            recentFiles = new JMenu(MessageUtil.getMessage("file.submenu.5"));
            fileMenu.add(recentFiles);
            if (!PlatformService.getInstance().isMac()) {
                fileMenu.addSeparator();
                fileMenu.add(exitAction).setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.ALT_MASK));
            }

            JMenu editMenu = new JMenu(MessageUtil.getMessage("edit.menu"));
            menuBar.add(editMenu);
            editMenu.add(copyAction).setAccelerator(KeyStroke.getKeyStroke('C', menuShortcutKeyMask));
            editMenu.add(pasteAction).setAccelerator(KeyStroke.getKeyStroke('V', menuShortcutKeyMask));
            editMenu.addSeparator();
            editMenu.add(selectAllAction).setAccelerator(KeyStroke.getKeyStroke('A', menuShortcutKeyMask));
            editMenu.addSeparator();
            editMenu.add(findAction).setAccelerator(KeyStroke.getKeyStroke('F', menuShortcutKeyMask));

            JMenu navigationMenu = new JMenu(MessageUtil.getMessage("navigation.menu"));
            menuBar.add(navigationMenu);
            navigationMenu.add(openTypeAction).setAccelerator(KeyStroke.getKeyStroke('T', menuShortcutKeyMask));
            navigationMenu.add(openTypeHierarchyAction).setAccelerator(KeyStroke.getKeyStroke('H', menuShortcutKeyMask));
            navigationMenu.addSeparator();
            navigationMenu.add(goToAction).setAccelerator(KeyStroke.getKeyStroke('L', menuShortcutKeyMask));
            navigationMenu.addSeparator();
            navigationMenu.add(backwardAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));
            navigationMenu.add(forwardAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));

            JMenu searchMenu = new JMenu(MessageUtil.getMessage("search.menu"));
            menuBar.add(searchMenu);
            searchMenu.add(searchAction).setAccelerator(KeyStroke.getKeyStroke('S', menuShortcutKeyMask|InputEvent.SHIFT_MASK));

//            JMenu charsetMenu = new JMenu("Charset");
//            menuBar.add(charsetMenu);
//            charsetMenu.add(searchAction).setAccelerator(KeyStroke.getKeyStroke('S', menuShortcutKeyMask|InputEvent.SHIFT_MASK));

            JMenu helpMenu = new JMenu(MessageUtil.getMessage("help.menu"));
            menuBar.add(helpMenu);
            if (browser) {
                helpMenu.add(jdWebSiteAction);
                helpMenu.add(jdGuiIssuesActionAction);
                helpMenu.add(jdCoreIssuesActionAction);
                helpMenu.addSeparator();
            }
            helpMenu.add(preferencesAction).setAccelerator(KeyStroke.getKeyStroke('P', menuShortcutKeyMask|InputEvent.SHIFT_MASK));
            if (!PlatformService.getInstance().isMac()) {
                helpMenu.addSeparator();
                helpMenu.add(aboutAction).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
            }

            mainFrame.setJMenuBar(menuBar);

            // Icon bar //
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            toolBar = new JToolBar();
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.add(new IconButton(openAction));
            toolBar.addSeparator();
            toolBar.add(new IconButton(openTypeAction));
            toolBar.add(new IconButton(searchAction));
            toolBar.addSeparator();
            toolBar.add(new IconButton(backwardAction));
            toolBar.add(new IconButton(forwardAction));
            panel.add(toolBar, BorderLayout.PAGE_START);

            mainTabbedPanel = new MainTabbedPanel(api);
            mainTabbedPanel.getPageChangedListeners().add(new PageChangeListener() {
                protected JComponent currentPage = null;

                @Override public <U extends JComponent & UriGettable> void pageChanged(U page) {
                    if (currentPage != page) {
                        // Update current page
                        currentPage = page;
                        currentPageChangedCallback.accept((T)page);

                        invokeLater(() -> {
                            if (page == null) {
                                // Update title
                                mainFrame.setTitle("Java Decompiler");
                                // Update menu
                                saveAction.setEnabled(false);
                                copyAction.setEnabled(false);
                                selectAllAction.setEnabled(false);
                                openTypeHierarchyAction.setEnabled(false);
                                goToAction.setEnabled(false);
                                // Update find panel
                                findPanel.setVisible(false);
                            } else {
                                // Update title
                                String path = page.getUri().getPath();
                                int index = path.lastIndexOf('/');
                                String name = (index == -1) ? path : path.substring(index + 1);
                                mainFrame.setTitle((name != null) ? name + " - Java Decompiler" : "Java Decompiler");
                                // Update history
                                history.add(page.getUri());
                                // Update history actions
                                updateHistoryActions();
                                // Update menu
                                saveAction.setEnabled(page instanceof ContentSavable);
                                copyAction.setEnabled(page instanceof ContentCopyable);
                                selectAllAction.setEnabled(page instanceof ContentSelectable);
                                findAction.setEnabled(page instanceof ContentSearchable);
                                openTypeHierarchyAction.setEnabled(page instanceof FocusedTypeGettable);
                                goToAction.setEnabled(page instanceof LineNumberNavigable);
                                // Update find panel
                                if (findPanel.isVisible()) {
                                    findPanel.setVisible(page instanceof ContentSearchable);
                                }
                            }
                        });
                    }
                }
            });
            mainTabbedPanel.getTabbedPane().addChangeListener(new ChangeListener() {
                protected int lastTabCount = 0;

                @Override
                public void stateChanged(ChangeEvent e) {
                    int tabCount = mainTabbedPanel.getTabbedPane().getTabCount();
                    boolean enabled = (tabCount > 0);

                    closeAction.setEnabled(enabled);
                    openTypeAction.setEnabled(enabled);
                    searchAction.setEnabled(enabled);
                    saveAllSourcesAction.setEnabled((mainTabbedPanel.getTabbedPane().getSelectedComponent() instanceof SourcesSavable));

                    if (tabCount < lastTabCount) {
                        panelClosedCallback.run();
                    }

                    lastTabCount = tabCount;
                }
            });
            mainTabbedPanel.preferencesChanged(configuration.getPreferences());
            panel.add(mainTabbedPanel, BorderLayout.CENTER);

            panel.add(findPanel, BorderLayout.PAGE_END);
            mainFrame.add(panel);
        });
    }

    public void show(Point location, Dimension size, boolean maximize) {
        invokeLater(() -> {
            // Set position, resize and show
            mainFrame.setLocation(location);
            mainFrame.setSize(size);
            mainFrame.setExtendedState(maximize ? JFrame.MAXIMIZED_BOTH : 0);
            mainFrame.setVisible(true);
        });
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public void showFindPanel() {
        invokeLater(() -> {
            findPanel.setVisible(true);
            findComboBox.requestFocus();
        });
    }

    public void setFindBackgroundColor(boolean wasFound) {
        invokeLater(() -> {
            findComboBox.getEditor().getEditorComponent().setBackground(wasFound ? findBackgroundColor : findErrorBackgroundColor);
        });
    }

    public <T extends JComponent & UriGettable> void addMainPanel(String title, Icon icon, String tip, T component) {
        invokeLater(() -> {
            mainTabbedPanel.addPage(title, icon, tip, component);
        });
    }

    public <T extends JComponent & UriGettable> List<T> getMainPanels() {
        return mainTabbedPanel.getPages();
    }

    public <T extends JComponent & UriGettable> T getSelectedMainPanel() {
        return (T)mainTabbedPanel.getTabbedPane().getSelectedComponent();
    }

    public void closeCurrentTab() {
        invokeLater(() -> {
            Component component = mainTabbedPanel.getTabbedPane().getSelectedComponent();
            if (component instanceof PageClosable) {
                if (!((PageClosable)component).closePage()) {
                    mainTabbedPanel.removeComponent(component);
                }
            } else {
                mainTabbedPanel.removeComponent(component);
            }
        });
    }

    public void updateRecentFilesMenu(List<File> files) {
        invokeLater(() -> {
            recentFiles.removeAll();

            for (File file : files) {
                JMenuItem menuItem = new JMenuItem(reduceRecentFilePath(file.getAbsolutePath()));
                menuItem.addActionListener(e -> openFilesCallback.accept(file));
                recentFiles.add(menuItem);
            }
        });
    }

    public String getFindText() {
        Document doc = ((JTextField)findComboBox.getEditor().getEditorComponent()).getDocument();

        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            assert ExceptionUtil.printStackTrace(e);
            return "";
        }
    }

    public boolean getFindCaseSensitive() { return findCaseSensitive.isSelected(); }

    public boolean getFindRegex() { return findRegex.isSelected(); }

    public void updateHistoryActions() {
        invokeLater(() -> {
            backwardAction.setEnabled(history.canBackward());
            forwardAction.setEnabled(history.canForward());
        });
    }

    // --- Utils --- //
    static String reduceRecentFilePath(String path) {
        int lastSeparatorPosition = path.lastIndexOf(File.separatorChar);

        if ((lastSeparatorPosition == -1) || (lastSeparatorPosition < Constants.RECENT_FILE_MAX_LENGTH)) {
            return path;
        }

        int length = Constants.RECENT_FILE_MAX_LENGTH/2 - 2;
        String left = path.substring(0, length);
        String right = path.substring(path.length() - length);

        return left + "..." + right;
    }

    // --- URIOpener --- //
    @Override
    public boolean openUri(URI uri) {
        boolean success = mainTabbedPanel.openUri(uri);

        if (success) {
            closeAction.setEnabled(true);
            openTypeAction.setEnabled(true);
        }

        return success;
    }

    // --- PreferencesChangeListener --- //
    @Override
    public void preferencesChanged(Map<String, String> preferences) {
        mainTabbedPanel.preferencesChanged(preferences);
    }
}

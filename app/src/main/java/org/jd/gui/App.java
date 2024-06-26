/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.jd.gui.controller.MainController;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.service.configuration.ConfigurationPersister;
import org.jd.gui.service.configuration.ConfigurationPersisterService;
import org.jd.gui.service.platform.PlatformService;
import org.jd.gui.util.MessageUtil;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.net.InterProcessCommunicationUtil;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class App {
    protected static final String SINGLE_INSTANCE = "UIMainWindowPreferencesProvider.singleInstance";

    protected static MainController controller;

    // 设置全局字体
    public static void initGlobalFontSetting(Font fnt){
        FontUIResource fontRes = new FontUIResource(fnt);
        for(Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements();){
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if(value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

    public static void main(String[] args) {
		if (checkHelpFlag(args)) {
			JOptionPane.showMessageDialog(null, "Usage: jd-gui [option] [input-file] ...\n\nOption:\n -h Show this help message and exit", Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
		} else {
            // Load preferences
            ConfigurationPersister persister = ConfigurationPersisterService.getInstance().get();
            Configuration configuration = persister.load();
            MessageUtil.setLocale(new Locale(configuration.getPreferences().getOrDefault("ViewerPreferences.languageKey", "en")));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> persister.save(configuration)));

            initGlobalFontSetting(new Font("Microsoft YaHei UI", Font.PLAIN, 16));

            if ("true".equals(configuration.getPreferences().get(SINGLE_INSTANCE))) {
                InterProcessCommunicationUtil ipc = new InterProcessCommunicationUtil();
                try {
                    ipc.listen(receivedArgs -> controller.openFiles(newList(receivedArgs)));
                } catch (Exception notTheFirstInstanceException) {
                    // Send args to main windows and exit
                    ipc.send(args);
                    System.exit(0);
                }
            }

            // Create SwingBuilder, set look and feel
            try {
                PlatformService instance = PlatformService.getInstance();
                if (instance.isMac()) {
                    FlatMacLightLaf.setup();
                } else if (instance.isWindows()) {
                    FlatIntelliJLaf.setup();
                } else {
                    FlatLightLaf.setup();
                }
//                UIManager.setLookAndFeel(configuration.getLookAndFeel());
            } catch (Exception e) {
                configuration.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                try {
                    UIManager.setLookAndFeel(configuration.getLookAndFeel());
                } catch (Exception ee) {
                    assert ExceptionUtil.printStackTrace(ee);
                }
           }

            // Create main controller and show main frame
            controller = new MainController(configuration);
            controller.show(newList(args));
		}
	}

    protected static boolean checkHelpFlag(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if ("-h".equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static List<File> newList(String[] paths) {
        if (paths == null) {
            return Collections.emptyList();
        } else {
            ArrayList<File> files = new ArrayList<>(paths.length);
            for (String path : paths) {
                files.add(new File(path));
            }
            return files;
        }
    }
}

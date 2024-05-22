package org.jd.gui.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static Map<String, Properties> properties = new HashMap<>();
    private static String language = "ZH_CN";

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return;
        }
        MessageUtil.language = language;
    }

    static {
        try {
            Properties prop1 = new Properties();
            prop1.load(MessageUtil.class.getClassLoader().getResourceAsStream("i18n/ZH_CN.properties"));
            properties.put("ZH_CN", prop1);
            Properties prop2 = new Properties();
            prop2.load(MessageUtil.class.getClassLoader().getResourceAsStream("i18n/ENG.properties"));
            properties.put("ENG", prop2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMessage(String key) {
        return properties.get(language).getProperty(key);
    }

    public static List<String> getOpenTips() {
        Properties prop = properties.get(language);
        Pattern pattern = Pattern.compile("open\\.tip\\[(\\d+)\\]");
        List<String> tips = new ArrayList<>();
        for (String name : prop.stringPropertyNames()) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                tips.add(prop.getProperty(name));
            }
        }
        return tips;
    }

    public static void main(String[] args) {
        System.out.println(getMessage("file.menu"));
        System.out.println(getOpenTips());
    }

}

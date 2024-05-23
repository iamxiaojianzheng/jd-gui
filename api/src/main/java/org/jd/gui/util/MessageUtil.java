package org.jd.gui.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static ResourceBundle resourceBundle;
    private static Locale locale = Locale.ENGLISH;

    static {
        resourceBundle = ResourceBundle.getBundle("", locale);
    }

    public static void setLocale(Locale locale) {
        if (locale != null) {
            MessageUtil.locale = locale;
            resourceBundle = ResourceBundle.getBundle("", locale);
        }
    }

    public static String getMessage(String key) {
        return resourceBundle.getString(key);
    }

    public static List<String> getOpenTips() {
        Pattern pattern = Pattern.compile("open\\.tip\\[(\\d+)\\]");
        String[] tips = new String[3];
        Enumeration<String> keys = resourceBundle.getKeys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                int index = Integer.parseInt(matcher.group(1));
                String tip = resourceBundle.getString(name);
                tips[index] = tip;
            }
        }
        return Arrays.asList(tips);
    }

    public static void main(String[] args) {
        System.out.println(getOpenTips());
//        System.out.println(getMessage("file.menu"));
//        System.out.println(getOpenTips());
    }

}

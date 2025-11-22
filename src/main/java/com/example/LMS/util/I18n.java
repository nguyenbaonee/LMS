package com.example.LMS.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
@Component
public class I18n {

    private static ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    public I18n(ReloadableResourceBundleMessageSource messageSource) {
        I18n.messageSource = messageSource;
    }

    public static String t(String keyword) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            log.info("locale: " + locale.getLanguage());
            return messageSource.getMessage(keyword, null, locale);
        } catch (Exception e) {
            log.error("Error: ", e);
            return keyword;
        }
    }

    public static String t(String key, String language) {
        String locate = "vi";
        if (("en".equalsIgnoreCase(language) || "vi".equalsIgnoreCase(language))) {
            locate = language;
        }
        ResourceBundle rb = ResourceBundle.getBundle("messages_" + locate.toLowerCase());
        try {
            if (rb.containsKey(key)) {
                return rb.getString(key);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }
}
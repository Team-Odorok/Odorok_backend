package com.odorok.OdorokApplication.diary.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromptTemplate {

    private final String template;
    private final Map<String, String> values = new HashMap<>();

    public static PromptTemplate of(String template) {
        return new PromptTemplate(template);
    }

    private PromptTemplate(String template) {
        this.template = template;
    }

    public PromptTemplate with(String key, String value) {
        values.put(key, value);
        return this;
    }

    public String build() {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replaceAll("\\{" + Pattern.quote(entry.getKey()) + "}", Matcher.quoteReplacement(entry.getValue()));
        }
        return result;
    }
}

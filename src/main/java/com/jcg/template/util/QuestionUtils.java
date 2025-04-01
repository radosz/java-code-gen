package com.jcg.template.util;

import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class QuestionUtils {
    public static String extractDefaultValue(String question) {
        if (question == null || !question.contains("(e.g.,")) {
            return null;
        }
        
        int defaultStart = question.indexOf("(e.g.,") + 6;
        int defaultEnd = question.indexOf(")", defaultStart);
        if (defaultEnd > defaultStart) {
            return question.substring(defaultStart, defaultEnd).trim();
        }
        return null;
    }
    
    public static String formatQuestionPrompt(String question, String defaultValue) {
        String prompt = question.endsWith("?") ? question : question + "?";
        return defaultValue != null ? prompt + " [" + defaultValue + "]" : prompt;
    }
} 
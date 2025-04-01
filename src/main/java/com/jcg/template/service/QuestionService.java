package com.jcg.template.service;

import com.google.inject.Inject;
import com.jcg.core.FileIO;
import com.jcg.template.util.QuestionUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class QuestionService {
    private final FileIO fileIO;
    private final DescriptionService descriptionService;
    private Map<String, String> defaultValues;
    
    @Inject
    public QuestionService(FileIO fileIO, DescriptionService descriptionService) {
        this.fileIO = fileIO;
        this.descriptionService = descriptionService;
        this.defaultValues = new HashMap<>();
    }
    
    public List<Pair<String, String>> prepareQuestions(File preparedFile, String pattern) throws IOException {
        List<Pair<String, String>> questionReplacer = new ArrayList<>();
        
        // First check for variables section
        parseVariablesSection(preparedFile, questionReplacer);
        
        // Then check if there's a questions section
        boolean hasQuestionsSection = parseQuestionsSection(preparedFile, questionReplacer);
        
        // If no questions section, fall back to looking for placeholders
        if (!hasQuestionsSection) {
            parsePlaceholders(preparedFile, questionReplacer);
        }
        
        return questionReplacer;
    }
    
    private void parseVariablesSection(File preparedFile, List<Pair<String, String>> questionReplacer) throws IOException {
        List<String> variableSection = fileIO.readContentBeetwenFirstMatch(
            preparedFile, 
            "{!variables}",
            "{end_variables}"
        );
        
        if (variableSection == null || variableSection.isEmpty()) {
            return;
        }
        
        for (String line : variableSection) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            
            int equalsPos = line.indexOf('=');
            if (equalsPos > 0) {
                String key = line.substring(0, equalsPos).trim();
                String defaultValue = line.substring(equalsPos + 1).trim();
                String questionText = formatVariableQuestion(key);
                questionReplacer.add(new ImmutablePair<>(questionText, key));
                defaultValues.put(key, defaultValue);
            }
        }
    }
    
    private String formatVariableQuestion(String key) {
        // Convert camelCase or snake_case to space-separated words
        String formatted = key.replaceAll("([A-Z])", " $1").toLowerCase();
        formatted = formatted.replaceAll("_", " ");
        return "Enter " + formatted + ":";
    }
    
    private boolean parseQuestionsSection(File preparedFile, List<Pair<String, String>> questionReplacer) throws IOException {
        List<String> questionSection = fileIO.readContentBeetwenFirstMatch(
            preparedFile, 
            "{!questions}",
            "{end_questions}"
        );
        
        if (questionSection == null || questionSection.isEmpty()) {
            return false;
        }
        
        for (String line : questionSection) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            
            int equalsPos = line.indexOf('=');
            if (equalsPos > 0) {
                String key = line.substring(0, equalsPos).trim();
                String questionText = line.substring(equalsPos + 1).trim();
                questionReplacer.add(new ImmutablePair<>(questionText, key));
            }
        }
        
        return !questionReplacer.isEmpty();
    }
    
    private void parsePlaceholders(File preparedFile, List<Pair<String, String>> questionReplacer) throws IOException {
        List<String> questionLines = fileIO.readLineContentBeetwen(preparedFile, "{!", "}");
        
        for (String str : questionLines) {
            if (str.equals("cartridge_dir")) {
                String displayQuestion = str.replace("_", " ");
                questionReplacer.add(new ImmutablePair<>(displayQuestion, str));
            }
        }
    }
    
    public String getDefaultValue(String question, String key, String pattern) {
        if (key.equals("description")) {
            return descriptionService.getDefaultDescription(pattern);
        }
        
        // Check if we have a default value from the variables section
        String defaultValue = defaultValues.get(key);
        if (defaultValue != null) {
            return defaultValue;
        }
        
        // Fall back to extracting from question text
        return QuestionUtils.extractDefaultValue(question);
    }
    
    public String formatPrompt(String question, String defaultValue) {
        return QuestionUtils.formatQuestionPrompt(question, defaultValue);
    }
} 
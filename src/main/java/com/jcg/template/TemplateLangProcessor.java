package com.jcg.template;

import com.google.inject.Inject;
import com.jcg.core.FileIO;
import com.jcg.template.service.DescriptionService;
import com.jcg.template.service.ProjectService;
import com.jcg.template.service.QuestionService;
import com.jcg.template.util.PathUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.lang3.tuple.Pair;

public class TemplateLangProcessor {
    private final FileIO fileIO;
    private final String pattern;
    private final File preparedFile;
    private final Scanner scanner;
    private final Map<String, String> userResponses;
    private final QuestionService questionService;
    private final ProjectService projectService;
    private String outputDir;

    @Inject
    public TemplateLangProcessor(
            FileIO fileIO, 
            String pattern, 
            File preparedFile, 
            String outputDir,
            QuestionService questionService,
            ProjectService projectService) {
        if (fileIO == null) throw new IllegalArgumentException("FileIO cannot be null");
        if (pattern == null) throw new IllegalArgumentException("Pattern cannot be null");
        if (preparedFile == null) throw new IllegalArgumentException("PreparedFile cannot be null");
        if (outputDir == null) throw new IllegalArgumentException("OutputDir cannot be null");
        if (questionService == null) throw new IllegalArgumentException("QuestionService cannot be null");
        if (projectService == null) throw new IllegalArgumentException("ProjectService cannot be null");
        
        this.fileIO = fileIO;
        this.pattern = pattern;
        this.preparedFile = preparedFile;
        this.outputDir = outputDir;
        this.scanner = new Scanner(System.in);
        this.userResponses = new HashMap<>();
        this.questionService = questionService;
        this.projectService = projectService;
    }

    public void process() throws IOException {
        if (!preparedFile.exists()) {
            throw new IOException("Prepared file not found: " + preparedFile.getAbsolutePath());
        }

        try {
            // Ask for output directory first
            String outputPath = readLine("Enter the output directory path (press Enter for default: " + outputDir + "):");
            if (outputPath != null && !outputPath.trim().isEmpty()) {
                this.outputDir = PathUtils.normalizePath(outputPath);
            }
            
            // Process questions
            processQuestions();
            
            // Create project directory based on user's project name
            String projectName = userResponses.get("projectName");
            if (projectName == null || projectName.trim().isEmpty()) {
                projectName = "base-app"; // Default name if not provided
            }
            
            // Create project
            projectService.createProject(outputDir, projectName, preparedFile, userResponses);

            System.out.println("\nTemplate processing completed successfully!");
            System.out.println("Project created at: " + outputDir + File.separator + projectName);
        } catch (Exception e) {
            throw new IOException("Failed to process template: " + e.getMessage(), e);
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt + " ");
        return scanner.nextLine();
    }

    private void processQuestions() throws IOException {
        System.out.println("\n=== Processing Template Questions ===");
        
        // Get questions from template
        List<Pair<String, String>> questions = questionService.prepareQuestions(preparedFile, pattern);
        
        // Process each question
        for (Pair<String, String> pair : questions) {
            String question = pair.getLeft();
            String key = pair.getRight();
            
            System.out.println("\nProcessing Question:");
            System.out.println("- Display: " + question);
            System.out.println("- Key: " + key);
            
            // Get default value
            String defaultValue = questionService.getDefaultValue(question, key, pattern);
            
            // Read user input
            String prompt = questionService.formatPrompt(question, defaultValue);
            String answer = readLine(prompt);
            
            // Use default value if answer is empty
            if ((answer == null || answer.trim().isEmpty()) && defaultValue != null) {
                answer = defaultValue;
                System.out.println("- Using default value: '" + defaultValue + "'");
            } else if (answer == null || answer.trim().isEmpty()) {
                throw new IOException("Template generation requires all questions to be answered. Question: " + question);
            }
            
            // Store the answer
            userResponses.put(key, answer.trim());
        }
    }
}

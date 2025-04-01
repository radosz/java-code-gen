package com.jcg.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jcg.app.AppModule;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class TemplateHandlerStarter {
    public static void start(String[] args) throws IOException {
        try {
            Injector injector = Guice.createInjector(new AppModule());
            TemplateService templateService = injector.getInstance(TemplateService.class);
            
            // Create interactive console input
            Scanner scanner = new Scanner(System.in);
            
            // Display available templates
            templateService.displayAvailableTemplates();
            
            // Get template selection from user
            String selectedTemplate = promptForTemplateSelection(scanner, templateService);
            
            // Process the selected template
            if (selectedTemplate != null) {
                templateService.processTemplate(selectedTemplate);
            }
            
            // Close scanner
            scanner.close();
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Prompts the user to select a template from the available options
     * 
     * @param scanner The scanner for reading user input
     * @param templateService The template service
     * @return The selected template name or null if user cancels
     * @throws IOException If an I/O error occurs
     */
    private static String promptForTemplateSelection(Scanner scanner, TemplateService templateService) throws IOException {
        Map<String, List<String>> categorizedTemplates = templateService.listAvailablePatternsByCategory();
        
        // Check if there are any templates available
        boolean hasTemplates = categorizedTemplates.values().stream()
                .anyMatch(list -> !list.isEmpty());
        
        if (!hasTemplates) {
            System.out.println("No templates found. Please add templates to the templates directory.");
            return null;
        }
        
        System.out.println("\nPlease select a template by entering its number or name (e.g., '1' or 'spring-boot/base-app')");
        System.out.println("Or type 'exit' to quit");
        
        while (true) {
            System.out.print("> ");
            String selection = scanner.nextLine().trim();
            
            if (selection.equalsIgnoreCase("exit")) {
                System.out.println("Exiting template generator.");
                return null;
            }
            
            // Try to parse as number first
            try {
                int number = Integer.parseInt(selection);
                Optional<String> templateByNumber = templateService.findTemplateByNumber(number);
                if (templateByNumber.isPresent()) {
                    return templateByNumber.get();
                } else {
                    System.out.printf("\nInvalid template number. Please enter a number between 1 and %d, or type 'exit' to quit.%n", 
                        templateService.getTemplateCount());
                    continue;
                }
            } catch (NumberFormatException e) {
                // Not a number, try as template name
                if (templateService.findTemplateByPattern(selection).isPresent()) {
                    return selection;
                }
            }
            
            // If not found, show error and continue loop
            System.out.println("\nInvalid template selection. Please try again or type 'exit' to quit.");
            System.out.println("You can enter either the template number or the full template name.");
        }
    }
}

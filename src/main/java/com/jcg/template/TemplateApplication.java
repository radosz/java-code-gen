package com.jcg.template;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;

public class TemplateApplication {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar template-generator.jar <template-pattern>");
            System.exit(1);
        }

        String pattern = args[0];
        
        try {
            // Create Guice injector
            Injector injector = Guice.createInjector(new TemplateModule());
            
            // Get template service from injector
            TemplateService templateService = injector.getInstance(TemplateService.class);
            
            // Process the template
            templateService.processTemplate(pattern);
            
        } catch (IOException e) {
            System.err.println("Error processing template: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 
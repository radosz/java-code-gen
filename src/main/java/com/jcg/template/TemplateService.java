package com.jcg.template;

import com.google.inject.Inject;
import com.jcg.core.FileIO;
import com.jcg.template.service.DescriptionService;
import com.jcg.template.service.ProjectService;
import com.jcg.template.service.QuestionService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateService {
    private final FileIO fileIO;
    private static final String TEMPLATES_BASE_DIR = "src/main/resources/templates";
    private static final String GENERATED_PROJECTS_DIR = "generated-projects";
    private static final String[] TEMPLATE_EXTENSIONS = {".template"};
    
    // Define supported categories
    private static final String SPRING_BOOT_CATEGORY = "spring-boot";
    private static final String CORE_JAVA_CATEGORY = "core-java";
    private static final String PYTHON_CATEGORY = "python";
    private static final String PHP_CATEGORY = "php";
    private static final String GO_CATEGORY = "go";
    private static final String NODEJS_CATEGORY = "nodejs";
    private static final String ELECTRON_CATEGORY = "electron";

    private Map<Integer, String> templateNumberMap = new HashMap<>();
    private int currentTemplateNumber = 1;

    private final QuestionService questionService;
    private final ProjectService projectService;
    private final DescriptionService descriptionService;

    @Inject
    public TemplateService(
            FileIO fileIO,
            QuestionService questionService,
            ProjectService projectService,
            DescriptionService descriptionService) {
        this.fileIO = fileIO;
        this.questionService = questionService;
        this.projectService = projectService;
        this.descriptionService = descriptionService;
    }

    public Map<String, List<String>> listAvailablePatternsByCategory() throws IOException {
        Map<String, List<String>> categorizedTemplates = new LinkedHashMap<>();
        File templatesDir = new File(TEMPLATES_BASE_DIR);
        
        // Get spring-boot templates
        File springBootDir = new File(templatesDir, SPRING_BOOT_CATEGORY);
        if (springBootDir.exists() && springBootDir.isDirectory()) {
            List<String> springBootTemplates = findTemplateFiles(springBootDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, springBootDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(SPRING_BOOT_CATEGORY, springBootTemplates);
        } else {
            categorizedTemplates.put(SPRING_BOOT_CATEGORY, Collections.emptyList());
        }
        
        // Get core-java templates
        File coreJavaDir = new File(templatesDir, CORE_JAVA_CATEGORY);
        if (coreJavaDir.exists() && coreJavaDir.isDirectory()) {
            List<String> coreJavaTemplates = findTemplateFiles(coreJavaDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, coreJavaDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(CORE_JAVA_CATEGORY, coreJavaTemplates);
        } else {
            categorizedTemplates.put(CORE_JAVA_CATEGORY, Collections.emptyList());
        }

        // Get Python templates
        File pythonDir = new File(templatesDir, PYTHON_CATEGORY);
        if (pythonDir.exists() && pythonDir.isDirectory()) {
            List<String> pythonTemplates = findTemplateFiles(pythonDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, pythonDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(PYTHON_CATEGORY, pythonTemplates);
        } else {
            categorizedTemplates.put(PYTHON_CATEGORY, Collections.emptyList());
        }

        // Get PHP templates
        File phpDir = new File(templatesDir, PHP_CATEGORY);
        if (phpDir.exists() && phpDir.isDirectory()) {
            List<String> phpTemplates = findTemplateFiles(phpDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, phpDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(PHP_CATEGORY, phpTemplates);
        } else {
            categorizedTemplates.put(PHP_CATEGORY, Collections.emptyList());
        }

        // Get Go templates
        File goDir = new File(templatesDir, GO_CATEGORY);
        if (goDir.exists() && goDir.isDirectory()) {
            List<String> goTemplates = findTemplateFiles(goDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, goDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(GO_CATEGORY, goTemplates);
        } else {
            categorizedTemplates.put(GO_CATEGORY, Collections.emptyList());
        }

        // Get NodeJS templates
        File nodejsDir = new File(templatesDir, NODEJS_CATEGORY);
        if (nodejsDir.exists() && nodejsDir.isDirectory()) {
            List<String> nodejsTemplates = findTemplateFiles(nodejsDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, nodejsDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(NODEJS_CATEGORY, nodejsTemplates);
        } else {
            categorizedTemplates.put(NODEJS_CATEGORY, Collections.emptyList());
        }

        // Get Electron templates
        File electronDir = new File(templatesDir, ELECTRON_CATEGORY);
        if (electronDir.exists() && electronDir.isDirectory()) {
            List<String> electronTemplates = findTemplateFiles(electronDir)
                    .stream()
                    .map(f -> formatTemplateNameFromFile(f, electronDir))
                    .collect(Collectors.toList());
            categorizedTemplates.put(ELECTRON_CATEGORY, electronTemplates);
        } else {
            categorizedTemplates.put(ELECTRON_CATEGORY, Collections.emptyList());
        }
        
        // Add templates at root level (legacy)
        List<String> rootTemplates = findTemplateFiles(templatesDir)
                .stream()
                .filter(f -> f.getParentFile().equals(templatesDir))
                .map(this::getTemplateNameFromFile)
                .collect(Collectors.toList());
        
        if (!rootTemplates.isEmpty()) {
            categorizedTemplates.put("legacy", rootTemplates);
        }
        
        return categorizedTemplates;
    }
    
    public List<String> listAvailablePatterns() throws IOException {
        Map<String, List<String>> categorizedTemplates = listAvailablePatternsByCategory();
        return categorizedTemplates.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
    
    private String formatTemplateNameFromFile(File file, File categoryDir) {
        String relativePath = file.getAbsolutePath().substring(categoryDir.getAbsolutePath().length() + 1);
        return getTemplateNameFromFile(new File(relativePath)).replace('\\', '/');
    }
    
    private String getTemplateNameFromFile(File file) {
        String fileName = file.getName();
        for (String ext : TEMPLATE_EXTENSIONS) {
            if (fileName.toLowerCase().endsWith(ext.toLowerCase())) {
                return fileName.substring(0, fileName.length() - ext.length());
            }
        }
        return fileName;
    }
    
    private List<File> findTemplateFiles(File directory) throws IOException {
        List<File> allTemplates = new ArrayList<>();
        for (String extension : TEMPLATE_EXTENSIONS) {
            allTemplates.addAll(fileIO.findFiles(directory, extension));
        }
        return allTemplates;
    }

    public void displayAvailableTemplates() throws IOException {
        Map<String, List<String>> categorizedTemplates = listAvailablePatternsByCategory();
        templateNumberMap.clear();
        currentTemplateNumber = 1;
        
        System.out.println("\nAvailable templates:");
        System.out.println("===================");
        
        categorizedTemplates.forEach((category, templates) -> {
            if (!templates.isEmpty()) {
                System.out.println("\n" + category.toUpperCase() + ":");
                templates.forEach(template -> {
                    System.out.printf("  %2d. %s%n", currentTemplateNumber, template);
                    templateNumberMap.put(currentTemplateNumber++, template);
                });
            }
        });
        System.out.println();
    }

    public Optional<String> findTemplateByNumber(int number) {
        return Optional.ofNullable(templateNumberMap.get(number));
    }

    public int getTemplateCount() {
        return templateNumberMap.size();
    }

    public Optional<File> findTemplateByPattern(String pattern) throws IOException {
        if (pattern.contains("/")) {
            String[] parts = pattern.split("/", 2);
            String category = parts[0];
            String templateName = parts[1];
            
            File categoryDir = new File(TEMPLATES_BASE_DIR, category);
            if (categoryDir.exists() && categoryDir.isDirectory()) {
                String templatePath = templateName.replace("/", File.separator);
                
                // Try exact match first
                for (String extension : TEMPLATE_EXTENSIONS) {
                    File exactTemplate = new File(categoryDir, templatePath + extension);
                    if (exactTemplate.exists() && exactTemplate.isFile()) {
                        return Optional.of(exactTemplate);
                    }
                }
                
                // If not found, search recursively
                return findTemplateFiles(categoryDir).stream()
                        .filter(f -> formatTemplateNameFromFile(f, categoryDir).equalsIgnoreCase(templateName))
                        .findFirst();
            }
        } else {
            File templatesDir = new File(TEMPLATES_BASE_DIR);
            
            // First, look in root directory (legacy)
            Optional<File> rootMatch = findTemplateFiles(templatesDir).stream()
                    .filter(f -> f.getParentFile().equals(templatesDir))
                    .filter(f -> getTemplateNameFromFile(f).equalsIgnoreCase(pattern))
                    .findFirst();
            
            if (rootMatch.isPresent()) {
                return rootMatch;
            }
            
            // Then, look in category directories
            for (String category : Arrays.asList(
                    SPRING_BOOT_CATEGORY, 
                    CORE_JAVA_CATEGORY,
                    PYTHON_CATEGORY,
                    PHP_CATEGORY,
                    GO_CATEGORY,
                    NODEJS_CATEGORY,
                    ELECTRON_CATEGORY)) {
                File categoryDir = new File(templatesDir, category);
                if (categoryDir.exists() && categoryDir.isDirectory()) {
                    Optional<File> match = findTemplateFiles(categoryDir).stream()
                            .filter(f -> getTemplateNameFromFile(f).equalsIgnoreCase(pattern))
                            .findFirst();
                    
                    if (match.isPresent()) {
                        return match;
                    }
                }
            }
        }
        
        return Optional.empty();
    }

    public void processTemplate(String pattern) throws IOException {
        // Create output directory if it doesn't exist
        File outputDir = new File(GENERATED_PROJECTS_DIR);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Failed to create output directory: " + outputDir);
        }

        // Find and process the template file
        Optional<File> templateFile = findTemplateByPattern(pattern);
        if (templateFile.isEmpty()) {
            System.out.println("Template not found: " + pattern);
            displayAvailableTemplates();
            throw new IOException("Template not found for pattern: " + pattern);
        }

        // Create and run the processor
        TemplateLangProcessor processor = new TemplateLangProcessor(
            fileIO,
            pattern,
            templateFile.get(),
            GENERATED_PROJECTS_DIR,
            questionService,
            projectService
        );
        processor.process();
    }
}

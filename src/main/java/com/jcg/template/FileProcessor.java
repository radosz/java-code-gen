package com.jcg.template;

import com.jcg.core.FileIO;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileProcessor {
    private final FileIO fileIO;
    private final File preparedFile;
    private final File projectDir;
    private static final String FILE_START_MARKER = "{!file}";
    private static final String FILE_END_MARKER = "{end_file}";
    private Map<String, String> userResponses;

    public FileProcessor(FileIO fileIO, File preparedFile, File projectDir, Map<String, String> userResponses) {
        this.fileIO = fileIO;
        this.preparedFile = preparedFile;
        this.projectDir = projectDir;
        this.userResponses = userResponses;
    }

    public void process() throws IOException {
        System.out.println("\nProcessing file structure...");
        System.out.println("Project directory: " + projectDir.getAbsolutePath());
        
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Failed to create project directory: " + projectDir);
        }
        
        String content = fileIO.readContent(preparedFile.getAbsolutePath());
        if (content == null || content.isEmpty()) {
            throw new IOException("Template content is empty");
        }
        
        int currentPos = 0;
        while (true) {
            int fileStart = content.indexOf(FILE_START_MARKER, currentPos);
            if (fileStart == -1) break;
            
            int pathStart = fileStart + FILE_START_MARKER.length();
            int pathEnd = content.indexOf(FILE_END_MARKER, pathStart);
            if (pathEnd == -1) break;
            
            int contentStart = pathEnd + FILE_END_MARKER.length() + 1; // +1 for newline
            int contentEnd = content.indexOf(FILE_START_MARKER, contentStart);
            if (contentEnd == -1) {
                contentEnd = content.length();
            }
            
            String filePath = content.substring(pathStart, pathEnd).trim();
            String fileContent = content.substring(contentStart, contentEnd);
            
            // Remove the last newline if it exists (to avoid extra blank lines)
            if (fileContent.endsWith("\n")) {
                fileContent = fileContent.substring(0, fileContent.length() - 1);
            }
            
            try {
                createFile(filePath, fileContent);
            } catch (Exception e) {
                System.err.println("Error creating file " + filePath + ": " + e.getMessage());
                throw e;
            }
            
            currentPos = contentEnd;
        }
        
        System.out.println("File structure processing completed");
    }
    
    private void createFile(String relativePath, String content) throws IOException {
        // Replace variables in content and path first
        String processedContent = replaceVariables(content);
        String processedPath = replaceVariables(relativePath);
        
        // Then convert package path to file path
        String filePath = convertPackageToPath(processedPath);
        
        File file = new File(projectDir, filePath);
        File parentDir = file.getParentFile();
        
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + parentDir);
        }
        
        fileIO.createFileWithContent(file, processedContent);
        System.out.println("Creating file: " + file.getAbsolutePath());
    }
    
    private String convertPackageToPath(String path) {
        // Handle both src/main/java and src/test/java paths
        if (path.contains("src/main/java/") || path.contains("src/test/java/")) {
            String[] parts = path.split("src/(main|test)/java/");
            if (parts.length == 2) {
                String sourceType = path.contains("src/main/java/") ? "main" : "test";
                String packagePath = parts[1];
                // Split into package path and filename
                int lastDot = packagePath.lastIndexOf('.');
                if (lastDot > 0) {
                    String packagePart = packagePath.substring(0, lastDot);
                    String fileName = packagePath.substring(lastDot);
                    // Convert dots to directory separators only in the package part
                    packagePart = packagePart.replace(".", File.separator);
                    return "src/" + sourceType + "/java/" + packagePart + fileName;
                }
            }
        }
        return path;
    }
    
    private String replaceVariables(String text) {
        if (userResponses == null) {
            return text;
        }
        
        for (Map.Entry<String, String> entry : userResponses.entrySet()) {
            String variable = entry.getKey();
            String value = entry.getValue();
            text = text.replace("{!" + variable + "}", value);
        }
        
        return text;
    }
}

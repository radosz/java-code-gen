package com.jcg.template.service;

import com.google.inject.Inject;
import com.jcg.core.FileIO;
import com.jcg.template.FileProcessor;
import com.jcg.template.util.PathUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectService {
    private final FileIO fileIO;
    
    @Inject
    public ProjectService(FileIO fileIO) {
        this.fileIO = fileIO;
    }
    
    public void createProject(String outputDir, String projectName, File preparedFile, Map<String, String> userResponses) throws IOException {
        // Normalize output directory
        outputDir = PathUtils.normalizePath(outputDir);
        
        // Create project directory
        File projectDir = new File(outputDir, projectName);
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Failed to create project directory: " + projectDir);
        }
        
        // Process files and create project structure
        FileProcessor fileProcessor = new FileProcessor(fileIO, preparedFile, projectDir, userResponses);
        fileProcessor.process();
    }
} 
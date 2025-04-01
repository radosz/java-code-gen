package com.jcg.template.util;

import java.io.File;

public class PathUtils {
    public static String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return path;
        }
        
        // Normalize the path for the current OS
        String normalizedPath = path.replace("/", File.separator).replace("\\", File.separator);
        
        // Remove trailing separator if present
        if (normalizedPath.endsWith(File.separator)) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        
        return normalizedPath;
    }
} 
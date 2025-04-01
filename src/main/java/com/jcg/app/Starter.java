package com.jcg.app;

import com.jcg.template.TemplateHandlerStarter;

public class Starter {
    public static void main(String[] args) {
        try {
            TemplateHandlerStarter.start(args);
        } catch (Exception e) {
            System.err.println("Error during template generation: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

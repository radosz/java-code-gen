package com.jcg.template;

import com.google.inject.AbstractModule;
import com.jcg.core.FileIO;
import com.jcg.template.service.DescriptionService;
import com.jcg.template.service.ProjectService;
import com.jcg.template.service.QuestionService;

public class TemplateModule extends AbstractModule {
    @Override
    protected void configure() {
        // Bind core services
        bind(FileIO.class).toInstance(new FileIO());
        
        // Bind template services
        bind(DescriptionService.class);
        bind(QuestionService.class);
        bind(ProjectService.class);
        bind(TemplateService.class);
    }
} 
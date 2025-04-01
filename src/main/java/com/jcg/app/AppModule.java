package com.jcg.app;

import com.google.inject.AbstractModule;
import com.jcg.core.FileIO;
import com.jcg.template.TemplateService;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FileIO.class).toInstance(new FileIO());
        bind(TemplateService.class);
    }
}


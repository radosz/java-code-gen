package com.jcg.template;

import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.jcg.core.FileIO;

public class AsmBuildGradleProcessor {
    static final String START_BLOCK = "{!asm_build_gradle}";
    static final String END_BLOCK = "{end_asm_build_gradle}";
    private final FileIO fileIO = new FileIO();

    public void process(File file) throws IOException {
        // Implementation without BuildGradleReplacer
        System.out.println("Assembly build.gradle processing is no longer supported");
    }
}

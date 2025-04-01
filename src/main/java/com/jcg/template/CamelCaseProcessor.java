package com.jcg.template;

import static com.jcg.core.StringHelper.replaceBlock;
import static com.jcg.core.StringHelper.betweenOrSame;
import static com.jcg.core.StringHelper.trim;

import com.jcg.core.StringHelper;
import java.io.File;
import java.io.IOException;
import org.apache.commons.text.CaseUtils;
import com.jcg.core.FileIO;

public class CamelCaseProcessor {

    static final String START_BLOCK = "|CamelCase:";
    static final String END_BLOCK = "|";
    private final FileIO fileIO = new FileIO();

    public void process(File file) throws IOException {
        fileIO.readIfLineContains(file, START_BLOCK).forEach(pair -> {
            Integer lineNumber = pair.getLeft();
            String lineContent = pair.getRight();
            String content = StringHelper.betweenOrSame(lineContent, START_BLOCK, END_BLOCK);
            String newContent = CaseUtils.toCamelCase(content, true, '_');
            newContent = trim(newContent);
            String newLineContent = replaceBlock(lineContent, content, newContent, START_BLOCK, END_BLOCK);
            fileIO.changeContentOnLineNumberNoLog(file, lineNumber, newLineContent);
        });
    }
}

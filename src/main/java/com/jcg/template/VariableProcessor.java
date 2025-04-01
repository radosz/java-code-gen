package com.jcg.template;

import static com.jcg.core.StringHelper.trim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.jcg.core.FileIO;

public class VariableProcessor {

    static final String START_BLOCK = "{!variables}";
    static final String END_BLOCK = "{end_variables}";
    private final FileIO fileIO = new FileIO();

    public void process(File file) throws IOException {
        List<Pair<String, String>> placeHolderValues = new ArrayList<>();

        fileIO.readContentBeetwenFirstMatch(file, START_BLOCK, END_BLOCK).forEach(lineContent -> {
            lineContent = trim(lineContent);
            if (null != lineContent && lineContent.contains("=")) {
                String[] eq = lineContent.split("=");
                Pair<String, String> placeHolderValue = new ImmutablePair<>(eq[0], eq[1]);
                placeHolderValues.add(placeHolderValue);
            }
        });

        for (Pair<String, String> placeHolderValue : placeHolderValues) {
            fileIO.readIfLineContains(file, "{!").forEach(linePait -> {
                String searchFor = placeHolderValue.getLeft();
                String replacement = placeHolderValue.getRight();
                Integer lineNumber = linePait.getLeft();
                String lineContent = linePait.getRight();
                String regex = Pattern.quote(searchFor.trim());
                String newLineContent = lineContent.replaceAll(regex, replacement.replace("\\", "\\\\").trim());
                fileIO.changeContentOnLineNumberNoLog(file, lineNumber, newLineContent);
            });
        }
    }
}

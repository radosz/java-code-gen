package com.jcg.template;

import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.jcg.core.FileIO;

public class DirBlockProcessor {

    static final String START_BLOCK = "{!dir:";
    static final String END_BLOCK = "{end_dir}";
    private final FileIO fileIO = new FileIO();

    public void process(File file) throws IOException {
        // send results to the project
        List<Triple<String, String, String>> triples = fileIO.readContentBetweenIfStartsWith(file, START_BLOCK, END_BLOCK);
        for (Triple<String, String, String> triple : triples) {
            String line = triple.getLeft().trim();
            String dir = substringBetween(line, START_BLOCK, "in");
            dir = dir.trim();
            dir = dir == null ? substringBetween(line, START_BLOCK, "}") : dir;
            String subfolders = substringBetween(line, "in:", "}");
            subfolders = subfolders == null ? "" : subfolders;
            subfolders = subfolders.replace(".", File.separator);
            subfolders = subfolders.replace("in:", "");
            subfolders = subfolders.trim();
            dir = dir + File.separator + subfolders;
            File fdir = new File(dir);
            if (!fdir.mkdirs()) {
                System.err.println("Dir " + fdir.getAbsolutePath() + " cannot be created");
            } else {
                System.out.println("Dir " + fdir.getAbsolutePath() + " (new)");
            }
        }
    }
}

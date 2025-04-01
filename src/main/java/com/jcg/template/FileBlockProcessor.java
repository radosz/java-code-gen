package com.jcg.template;

import static com.jcg.core.ConsoleUtil.readLine;
import static com.jcg.core.StringHelper.betweenOrSame;
import static com.jcg.core.StringHelper.firstLineBefore;
import static com.jcg.core.StringHelper.removeFirstEmptyLine;
import static com.jcg.core.StringHelper.trimAndRemoveSpaces;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.jcg.core.FileIO;

public class FileBlockProcessor {
    
    static final String START_BLOCK = "{!fname:";
    static final String END_BLOCK = "{end_file}";
    private final FileIO fileIO = new FileIO();
    
    public void process(File file) throws IOException {
        //send results to the project
        List<Triple<String, String, String>> triples = fileIO.readContentBetweenIfStartsWith(file, START_BLOCK, END_BLOCK);
        for (Triple<String, String, String> triple : triples) {
            String line = triple.getLeft().trim();
            //String endLine = triple.getMiddle().trim();
            String content = triple.getRight();
            String fname = substringBetween(line, START_BLOCK, "cartridge");
            String cartridgePath = substringBetween(line, "cartridge:", "in");
            String packageP = substringBetween(line, "in:", "}");
            packageP = replace(packageP, ".", File.separator);
            packageP = null == packageP ? "" : packageP;
            String absolutePath = cartridgePath + File.separator + packageP + File.separator + fname;
            absolutePath = trimAndRemoveSpaces(absolutePath);
            File prjFile = new File(absolutePath);
            
            if (prjFile.getName().endsWith(".java") && prjFile.exists()) {
                System.out.println("The " + prjFile.getAbsolutePath() + " file already exists!");
                System.out.println("Do you like to replace/update it ? (replace or update depends on template config)");
                String replaceFileQuestion = readLine("press y (yes) or any key to to continie (no): ");
                if (!"y".equalsIgnoreCase(replaceFileQuestion)) {
                    return;
                }
            }

            if (prjFile.exists()) // if files exists
            {
                String targetLine = firstLineBefore(content, "{!p_text_exists}");
                content = betweenOrSame(content, "{!p_text_exists}");
                List<Pair<Integer, String>> matches = fileIO.readIfTrimAndNoSpacesLineStartsWith(prjFile, targetLine);
                if (null == matches || matches.isEmpty()) {
                    System.err.println("The " + prjFile.getName()
                            + " cannot be modified\nPlease try to add manually: \n" + content);
                } else {
                    System.out.println("File: " + prjFile.getAbsolutePath() + " (modified)");
                }
                for (Pair<Integer, String> pairLineNumberContent : matches) {
                    Integer lineNumber = pairLineNumberContent.getLeft();
                    fileIO.writeStrIfUniqueToFileAtGivenLineNum(removeFirstEmptyLine(content), prjFile, lineNumber);
                }
            }
            
            if (!prjFile.exists() && null != content) {
                content = content.replace("{!p_text_exists}", "");
                System.out.println("File: " + prjFile.getAbsolutePath() + " (new)");
                fileIO.createFileWithContent(prjFile, removeFirstEmptyLine(content));
            }
            
            if (!prjFile.exists()) {
                if (prjFile.createNewFile()) {
                    System.out.println("File: " + prjFile.getAbsolutePath() + " (new)");
                } else {
                    System.out.println("File: " + prjFile.getAbsolutePath() + " (cannot be create)");
                }
            }
        }
    }
}

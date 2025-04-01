package com.jcg.template;

import static com.jcg.core.StringHelper.betweenOrSame;

import com.jcg.core.FileIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class CSVBlockProcessor {

  static final String START_BLOCK = "{!csv}";
  static final String END_BLOCK = "{end_csv}";
  static final String START_BLOCK_ITERATOR = "{!csv-iterator by column}";
  static final String END_BLOCK_ITERATOR = "{end_csv-iterator by column}";
  static final String CSV_COLUMN_START = "{!csv[";
  static final String CSV_COLUMN_END = "]}";
  private final FileIO fileIO = new FileIO();

  private List<List<String>> csv = new ArrayList<>();

  public void process(File file) throws IOException {
    // send results to the project
    List<Triple<String, String, String>> triples = fileIO.readContentBetweenIfStartsWith(file, START_BLOCK, END_BLOCK);
    for (Triple<String, String, String> triple : triples) {
      String content = triple.getRight().trim();
      List<String> res1 = Arrays.asList(content.split("\n"));
      for (String res : res1) {
        csv.add(Arrays.asList(res.split(";")));
      }
    }
    processCsvIteratorByColumn(file);
  }

  public void processCsvIteratorByColumn(File file) throws IOException {
    // send results to the project\
    List<Triple<String, String, String>> triples = fileIO.readContentBetweenIfStartsWith(file, START_BLOCK_ITERATOR, END_BLOCK_ITERATOR);
    StringBuilder sb = new StringBuilder();
    Map<Integer, List<String>> columnValue = getColumnValueMap();
    int columnCount = columnValue.size();
    List<String> rows = columnValue.getOrDefault(0, new ArrayList<>());
    int rowCount = rows.size();
    int size = rowCount > columnCount? rowCount:columnCount;
    List<Pair<String,String>> forReplace = new ArrayList<>();
    for (Triple<String, String, String> triple : triples) {
      String content = triple.getRight();
      for (int i = 0; i < size; i++) {
        String target = CSV_COLUMN_START+i+CSV_COLUMN_END;

        if(content.contains(target))
        {
          for (int j = 0; j < size; j++) {
            String replacement = target.replace(CSV_COLUMN_END, "") + ";" + j + CSV_COLUMN_END;
            forReplace.add(Pair.of(target, replacement));
          }
        }

      }
      sb.append(content);
    }
    //appendFile
    List<Pair<String,String>> replaced = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      String content = sb.toString();
      for (Pair<String,String> pair: forReplace) {
        if(content.contains(pair.getLeft()) && !replaced.contains(pair))
        {
          content = content.replace(pair.getLeft(),pair.getRight());
          replaced.add(pair);
        }
      }
      fileIO.appendToFile(file, content);
    }
  }

  public Map<Integer, List<String>> getColumnValueMap() {
    Map<Integer, List<String>> columnValue = new HashMap<>();
    List<String> internalList = new ArrayList<>();
    for (List<String> lstStr : getCsv()) {
      for (int i = 0; i < lstStr.size(); i++) {
        List<String> mapLst = columnValue.getOrDefault(i, null);
        if (null != mapLst) {
          mapLst.add(lstStr.get(i));
        } else {
          internalList.add(lstStr.get(i));
          columnValue.put(i, internalList);
          internalList = new ArrayList<>();
        }
      }

    }
    return columnValue;
  }

  public List<List<String>> getCsv() {
    return csv;
  }
}

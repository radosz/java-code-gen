package com.jcg.core;

import static com.jcg.core.StringHelper.betweenOrSame;

import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

public class StringHelper {
	
	private static String precondition(String line) {
		return null != line && !line.isEmpty() ? line : "";
	}
	
	public static String removeAllEmptyLines(String content)
	{
		content = precondition(content);
		return content.replaceAll("(?m)^[ \t]*\r?\n", "");
	}
	
	public static String removeFirstEmptyLine(String content) {
		content = precondition(content);
		String[] lines = content.split("\n");
		StringJoiner sj = new StringJoiner("\n");
		if (lines.length > 0 && "".equals(lines[0])) {
			for (int i = 1; i < lines.length; i++) {
				sj.add(lines[i]);
			}
		}
		return precondition(sj.toString());
	}
	
	public static String replaceByStringIndexes(String content, String replacement, int start, int end)
	{
		content = precondition(content);
		replacement = precondition(replacement);
        StringBuffer buf = new StringBuffer(content);
        buf.replace(start, end, replacement); 
        return buf.toString();
	}
	
	public static String replaceBlock(String lineContent, String content, String newContent, String startBlock, String endBlock)
	{
		startBlock = precondition(startBlock);
		endBlock = precondition(endBlock);
		String newLineContent = lineContent.replace(content, newContent);
		newLineContent = lineContent.replace(content, newContent);
		Integer start =  StringUtils.indexOf(newLineContent, newContent) - startBlock.length();
		Integer end =  StringUtils.indexOf(newLineContent, newContent)+newContent.length()+endBlock.length();
        return replaceByStringIndexes(newLineContent, newContent, start, end);
	}
	
	public static String betweenOrSame(String str, String open, String close) {
		String content = precondition(str);
		String possibleResult = StringUtils.substringBetween(str, open, close);
		return  null == possibleResult? content:possibleResult;
	}
	
	public static String firstLineBefore(String content, String tag) {
		tag = precondition(tag);
		tag = trim(tag);
		content = precondition(content);
		String[] lines = content.split("\n");
		for (String line : lines) {
			String cmpLine = trimAndRemoveSpaces(line);
			String tagCmp = trimAndRemoveSpaces(tag);
			if(cmpLine.endsWith(tagCmp))
			{
				return StringUtils.substringBefore(line, tag);
			}
		}
		return  "";
	}
	
	public static String betweenOrSame(String str, String openClose) {
		String content = precondition(str);
		String possibleResult = StringUtils.substringBetween(str, openClose, openClose);
		return  null == possibleResult? content:possibleResult;
	}
	
	public static String beetweenOrSameIncludeBeforeStr(String str, String openClose) {
		String content = precondition(str);
		String before = StringUtils.substringBefore(str, openClose);
		String possibleResult = StringUtils.substringBetween(str, openClose, openClose);
		return  null == possibleResult? content:before+possibleResult;
	}
	
	public static String trim(String line) {
		return precondition(line).trim();
	}

	public static String trimAndRemoveSpaces(String line) {
		return precondition(line).trim().replace(" ", "");
	}
	
	public static String trimAndRemoveSpacesExcept(String line, String exceptWord) {
		String word = precondition(line);
		if(word.equals(exceptWord) || word.trim().startsWith(exceptWord))
		{
			return line;
		}
		return word.trim().replace(" ", "");
	}

	public static String removeQuotes(String line) {
		return precondition(line).replace("'", "").replace("\"", "");
	}
	
	public static String removeBrackets(String line) {
		return precondition(line).replace("(", "").replace(")", "");
	}
	
	public static boolean isStartsWithOrEndsWith(String line, String serchWord) {
		String word = precondition(serchWord);
		return line.startsWith(word) || line.endsWith(word);
	}
	
	public static String addSingleQuotes(String line) {
		String word = precondition(line);
		return "'"+word+"'";
	}
	
	public static String addDoubleQuote(String line) {
		String word = precondition(line);
		return "\""+word+"\"";
	}
	
	public static Triple<String, String, String> getBeforeAfterNew(String content, String oldLineConent,String newLineConent)
	{
		precondition(oldLineConent);
		precondition(newLineConent);
		oldLineConent.indexOf(oldLineConent);
		String oldContentNoSpaces = trimAndRemoveSpaces(oldLineConent);
		String firstChar = oldContentNoSpaces.substring(0,1);
		int indFirst = StringUtils.indexOf(oldLineConent, firstChar);
		String checkForSpaces = oldLineConent.substring(0,indFirst);
		String before = StringUtils.substringBefore(content, oldLineConent);
		if(before.isEmpty())
		{
			before = checkForSpaces;
		}
		String after = StringUtils.substringAfter(content, oldLineConent);
		after = null == after? "":after;
		String replacement = before+newLineConent+after;
		return new ImmutableTriple<String, String, String>(before, after, replacement);
	}
}

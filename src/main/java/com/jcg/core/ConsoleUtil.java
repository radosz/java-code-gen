package com.jcg.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleUtil {

	public static String readLine(String format, Object... args) throws IOException {
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(is);
		System.out.print(String.format(format, args));
		String line = in.readLine();
		return line;
	}
}

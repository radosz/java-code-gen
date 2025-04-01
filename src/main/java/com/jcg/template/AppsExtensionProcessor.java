package com.jcg.template;

import static org.apache.commons.lang3.StringUtils.substringBetween;

import java.io.File;
import java.io.IOException;

import com.jcg.core.FileIO;

public class AppsExtensionProcessor {
	static final String START_BLOCK = "{!apps_extension}";
	static final String END_BLOCK = "{end_apps_extension}";
	private final FileIO fileIO = new FileIO();

	public void process(File file) throws IOException {
		// Implementation without AppExtensionComponentWriter
		System.out.println("Apps extension processing is no longer supported");
	}
}

package com.jcg.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.XML;

import com.jcg.xmljson.SearchJSONObjectResult;

public class FileIO
{
    public List<SearchJSONObjectResult> readRecursivelyXMLAsJSON(File dir, String fName) throws IOException
    {
    	if(!fName.endsWith(".xml"))
    	{
    		throw new IOException("fname must be end with .xml");
    	}
        List<SearchJSONObjectResult> results = new ArrayList<SearchJSONObjectResult>();
        Path sourceDir = Paths.get(dir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                String pathStr = path.toString();
                File file = new File(pathStr);
                String fileName = file.getName();
                if (fileName.equals(fName))
                {
                   SearchJSONObjectResult sr = new SearchJSONObjectResult(file, XML.toJSONObject(readContent(pathStr)));
                   results.add(sr);           
                }  
                return FileVisitResult.CONTINUE;
            }
        });
        return results;
    }
    public List<ZipEntry> getZipEntries(File zipFile) throws IOException
    {
        List<ZipEntry> zipEnties = new ArrayList<>();
        String fileZip = zipFile.getAbsolutePath();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while(zipEntry != null)
        {
            if(!zipEntry.isDirectory())
            {
                zipEnties.add(zipEntry);
            }
            zis.closeEntry();
            zipEntry = zis.getNextEntry(); 
        }
        zis.close();
        return Collections.unmodifiableList(zipEnties);
    }

    
    public List<String> readZipEntryNames(File zipFile) throws IOException
    {
        return getZipEntries(zipFile).stream().map(e -> e.getName()).collect(Collectors.toList());
    }
    
    public List<String> readRecursivelyZipEntryNames(File dir, String fileExtension, String filterExt) throws IOException
    {
        List<String> results = new ArrayList<String>();
        Path sourceDir = Paths.get(dir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                String pathStr = path.toString();
                
                if (pathStr.endsWith(fileExtension))
                {
                    readZipEntryNames(path.toFile())
                                     .stream()
                                     .filter(name -> name.endsWith(filterExt))
                                     .forEach(results::add);
                }
                    
                return FileVisitResult.CONTINUE;

            }
        });
        return results;
    }

    
    public void compress(String dirPath) throws IOException
    {
        Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                Path targetFile = sourceDir.relativize(path);
                outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                byte[] bytes = Files.readAllBytes(path);
                outputStream.write(bytes, 0, bytes.length);
                outputStream.closeEntry();
                path.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        outputStream.close();
    }
    
    public List<File> unzipByFileExtension (File zipFile, String fileExtension) throws IOException
    {
        fileExtension = getFileExtension(fileExtension);
        String fileZip = zipFile.getAbsolutePath();
        File destDir = zipFile.getParentFile();
        List<File> result = new ArrayList<>();

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        
        //Only zipEntries with files
        while(zipEntry != null && zipEntry.isDirectory())
        {
            zipEntry = zis.getNextEntry();
        }
        
        while(zipEntry != null && zipEntry.getName().endsWith(fileExtension))
        {
            File newFile = newFile(destDir,zipFile.getName(), zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while((len = zis.read(buffer)) > 0)
            {
                fos.write(buffer, 0, len);
            }
            fos.close();
            result.add(newFile);
            zipEntry = zis.getNextEntry();          
        }
        zis.closeEntry();
        zis.close();
        return result;
    }
        
	public int detectCountOfSpacesOnBeginingOfFileLine(File file, int lineNum) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		String content = lines.get(lineNum);
		content = content.replace("\t", "    ");
		String[] split = content.split(" ");
		int count = 0;
		for (String string : split) {

			if (!string.isEmpty()) {
				break;
			}
			count++;
		}
		return count;
	}
        
    public List<Pair<Integer,String>> readIfLineContains(File file,String... searchWords) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        List<Pair<Integer,String>> result = new ArrayList<Pair<Integer,String>>();
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
			result = stream.peek(p -> lineCounter.incrementAndGet()).map(line -> {
				for (String searchWord : searchWords) {
					if (line.contains(searchWord)) {
						return new ImmutablePair<Integer, String>(lineCounter.get(), line);
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public List<Pair<Integer,String>> readIfTrimLineStartsWith(File file,String... searchWords) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        List<Pair<Integer,String>> result = new ArrayList<Pair<Integer,String>>();
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
			result = stream.peek(p -> lineCounter.incrementAndGet()).map(line -> {
				line = StringHelper.trim(line);
				for (String searchWord : searchWords) {
					if (line.startsWith(searchWord)) {
						return new ImmutablePair<Integer, String>(lineCounter.get(), line);
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public List<Pair<Integer,String>> readIfTrimAndNoSpacesLineStartsWith(File file,String... searchWords) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        List<Pair<Integer,String>> result = new ArrayList<Pair<Integer,String>>();
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
			result = stream.peek(p -> lineCounter.incrementAndGet()).map(line -> {
				String cmpLine = StringHelper.trimAndRemoveSpaces(line);
				for (String searchWord : searchWords) {
					if (cmpLine.startsWith(StringHelper.trimAndRemoveSpaces(searchWord))) {
						return new ImmutablePair<Integer, String>(lineCounter.get(), line);
					}
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return result;
    }
        
    public void changeContentOnLineNumber(File file, Integer lineNumber, String newContent) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
        	List<String> replaced = stream
											.peek(p-> lineCounter.incrementAndGet())
											.map(line -> {
												if (lineCounter.get() == lineNumber) {
													System.out.println("File: "+file.getName()+" Line number: "+lineNumber);
													System.out.println("Replace with: "+newContent);
													return newContent;
												}
												return line;
											}).collect(Collectors.toList());
        	Files.write(targetPath, replaced);
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public void changeContentOnLineNumberNoLog(File file, Integer lineNumber, String newContent) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
        	List<String> replaced = stream
											.peek(p-> lineCounter.incrementAndGet())
											.map(line -> {
												if (lineCounter.get() == lineNumber) {
													return newContent;
												}
												return line;
											}).collect(Collectors.toList());
        	Files.write(targetPath, replaced);
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public void replaceContentOnLineNumber(File file, Integer lineNumber, String oldContent, String newContent) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        AtomicInteger lineCounter = new AtomicInteger(0);
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
        	List<String> replaced = stream
											.peek(p-> lineCounter.incrementAndGet())
											.map(line -> {
												if (lineCounter.get() == lineNumber) {
													String replacement = line.replaceAll("\\b"+oldContent+"\\b", newContent);
													if(line.contains(replacement))
													{
														return line;
													}
													System.out.println("File: "+file.getName()+" Line number: "+lineNumber);
													System.out.println("Replace: "+oldContent+" -> "+newContent);
													System.out.println("content: "+line.trim()+"\nnew: "+replacement.trim());
													return replacement;
												}
												return line;
											}).collect(Collectors.toList());
        	Files.write(targetPath, replaced);
    		       
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
 
    public Set<String> searchUniqueLinesInContent(File file,String searchWord) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
    	     return stream
    	    	   .filter(Objects::nonNull)
    		       .filter(line-> line.contains(searchWord))
    		       .collect(Collectors.toSet());
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public Set<String> searchAllUniqueLinesInContentStartsWith(File file,String searchWord) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
    	     return searchAllUniqueLinesInContentStartsWith(stream,searchWord);
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
	public Set<String> searchAllUniqueLinesInContentStartsWith(Stream<String> content, String searchWord) {
		return content.filter(Objects::nonNull)
					  .filter(line -> line.startsWith(searchWord))
					  .collect(Collectors.toSet());
	}
    
    public List<String> readContentBeetwenFirstMatch(File file,String firstStartWith, String firstEndWith) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        List<String> fromEnd = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
    	     return stream
    	    	   .filter(Objects::nonNull)
    	    	   .map(line -> {
    	    		   String lineNoSpaces = StringHelper.trimAndRemoveSpaces(line);
    	    		   boolean isStatsWith = fromEnd.isEmpty() && (line.startsWith(firstStartWith) || lineNoSpaces.startsWith(firstStartWith));
    	    		   boolean isEndsWith = fromEnd.size() == 1 && (StringHelper.isStartsWithOrEndsWith(line, firstEndWith)
    	    				   || StringHelper.isStartsWithOrEndsWith(lineNoSpaces, firstEndWith));
    	    		   if(isStatsWith || isEndsWith){
    	    			   fromEnd.add(line);
    	    			   return line;
    	    		   }
    	    		   return fromEnd.size() == 1? line:null;
    	    	   })
    	    	   .filter(Objects::nonNull)
    		       .collect(Collectors.toList());
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Triple<String, String, String>> readContentBetweenIfStartsWith(File file, String start, String close) throws IOException
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        String fullContent = readContent(file.getAbsolutePath());
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
            return stream
                .filter(Objects::nonNull)
                .map(line -> {
                    String lineNoSpaces = StringHelper.trimAndRemoveSpaces(line);
                    boolean isStatsWith = line.startsWith(start) || lineNoSpaces.startsWith(start);
                    if(isStatsWith){
                        String content = StringUtils.substringBetween(fullContent, line, close);
                        return new ImmutableTriple<String, String, String>(line, close, content);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }
    
    public List<String> readLineContentBeetwen(File file,String start, String end) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
    	     return stream
    	    	   .filter(Objects::nonNull)
    	    	   .map(line -> StringUtils.substringBetween(line, start, end))
    	    	   .filter(Objects::nonNull)
    		       .collect(Collectors.toList());
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<String> readContentFrom(File file,String firstStartWith) 
    {
        Path targetPath = Paths.get(file.getAbsolutePath());
        List<String> fromEnd = new ArrayList<String>();
        try (Stream<String> stream = Files.lines(targetPath, StandardCharsets.UTF_8)) 
        {
    	     return stream
    	    	   .filter(Objects::nonNull)
    	    	   .map(line -> {
    	    		   String lineNoSpaces = StringHelper.trimAndRemoveSpaces(line);
    	    		   boolean isStatsWith = fromEnd.isEmpty() && (line.startsWith(firstStartWith) || lineNoSpaces.startsWith(firstStartWith));
    	    		   if(isStatsWith){
    	    			   fromEnd.add(line);
    	    			   return line;
    	    		   }
    	    		   return fromEnd.size() == 1? line:null;
    	    	   })
    	    	   .filter(Objects::nonNull)
    		       .collect(Collectors.toList());
        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
	public void replaceContent(String filePath, String newContent) {
		Path targetPath = Paths.get(filePath);
		try {
			Files.write(targetPath, newContent.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void replaceContent(File file, String serchContent, String newContent) throws IOException {
		if(file.isFile())
		{
			if(newContent.split("\\n").length > 1)
			{
				String content = readContent(file.getAbsolutePath());
				String replacement = StringUtils.replace(content, serchContent, newContent);
				if(!content.equals(replacement))
				{
					replaceContent(file, replacement);
				}
				return;
			}
			replaceContentByLine(file, serchContent, newContent);
			return;
		}

		List<File> files = findFiles(file);
		for (File fileInFolder : files) {
			if (fileInFolder.isFile()) {
				replaceContentByLine(fileInFolder, serchContent, newContent);
			}
		}
	}
	
	public void replaceContentExcept(File file, String fileExc, String serchContent, String newContent) throws IOException {
		if(file.isFile() && !file.getName().endsWith(fileExc))
		{
			if(newContent.split("\\n").length > 1)
			{
				String content = readContent(file.getAbsolutePath());
				String replacement = StringUtils.replace(content, serchContent, newContent);
				if(!content.equals(replacement))
				{
					replaceContent(file, replacement);
				}
				return;
			}
			replaceContentByLine(file, serchContent, newContent);
			return;
		}

		List<File> files = findFiles(file);
		for (File fileInFolder : files) {
			if (fileInFolder.isFile() && !fileInFolder.getName().endsWith(fileExc)) {
				replaceContentByLine(fileInFolder, serchContent, newContent);
			}
		}
	}
	
	public void replaceContent(File file, Collection<Pair<String, String>> oldNew) throws IOException {
    	for (Pair<String, String> pair : oldNew) {
			String searchWord = pair.getLeft();
			String forReplace = pair.getRight();
			replaceContent(file, searchWord, forReplace);
		}
	}
	
	public void replaceContentExcept(File file, String fileExc, Collection<Pair<String, String>> oldNew) throws IOException {
    	for (Pair<String, String> pair : oldNew) {
			String searchWord = pair.getLeft();
			String forReplace = pair.getRight();
			replaceContentExcept(file,fileExc, searchWord, forReplace);
		}
	}
	
	public void replaceContent(File file, List<String> serchContent, List<String> newContent) throws IOException {
    	for (int i = 0; i < serchContent.size(); i++) 
    	{
    		replaceContent(file, serchContent.get(i), newContent.get(i));
		}
	}
	public void replaceContent(File file, String newContent) {
		replaceContent(file.getAbsolutePath(), newContent);
	}
	
	public void replaceContentByLine(File file,String oldLineConent, String newLineConent) 
    {
		List<Pair<Integer, String>> lineContent = readIfLineContains(file, oldLineConent);
		for (Pair<Integer, String> pair : lineContent) {
			Integer lineNumber = pair.getLeft();
			replaceContentOnLineNumber(file, lineNumber, oldLineConent, newLineConent);
		}
    }
	
	public void changeContentByLine(File file,String oldLineConent, String newLineConent) 
    {
		List<Pair<Integer, String>> lineContent = readIfLineContains(file, oldLineConent);
		for (Pair<Integer, String> pair : lineContent) {
			Integer lineNumber = pair.getLeft();
			String content = pair.getRight();
			String replacement = StringHelper.getBeforeAfterNew(content, oldLineConent, newLineConent).getRight();
			changeContentOnLineNumber(file, lineNumber, replacement);
		}
    }
    
    public String readContent(String filePath) throws IOException
    {
        StringBuilder contentBuilder = new StringBuilder();
 
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) 
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) 
        {
            System.out.println("Cannot read: "+filePath+" "+e.getMessage());
            throw e;
        }
 
        return contentBuilder.toString();
    }
    
    public List<File> findFiles(File dir, String fileExtension) throws IOException
    {
        List<File> results = new ArrayList<>();
        Path sourceDir = Paths.get(dir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                String pathStr = path.toString();
                if (pathStr.endsWith(fileExtension))
                {
                	results.add(new File(pathStr));
                }
                    
                return FileVisitResult.CONTINUE;

            }
        });
        return results;
    }
    
    public List<File> findFiles(File dir) throws IOException
    {
        List<File> results = new ArrayList<>();
        Path sourceDir = Paths.get(dir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
        	
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                String pathStr = path.toString();
                results.add(new File(pathStr));
                return FileVisitResult.CONTINUE;

            }
        });
        return results;
    }
    
    public List<File> findDirs(File fdir, String dirName) throws IOException
    {
        List<File> results = new ArrayList<>();
        Path sourceDir = Paths.get(fdir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				if(dirName.equals(dir.toFile().getName()))
				{
					results.add(dir.toFile());
				}
				return super.preVisitDirectory(dir, attrs);
			}	
        });
        return results;
    }
    
    public List<File> findFilesExcept(File dir, String fileExc) throws IOException
    {
        List<File> results = new ArrayList<>();
        Path sourceDir = Paths.get(dir.getAbsolutePath());
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException
            {
                String pathStr = path.toString();
                if(!pathStr.endsWith(fileExc))
                {
                	results.add(new File(pathStr));
                }
                return FileVisitResult.CONTINUE;

            }
        });
        return results;
    }
    
    public void writeStrToFileAtGivenLineNum(String str, File file, int lineNum) throws IOException { 
        List<String> lines = Files.readAllLines(file.toPath());                                
        lines.add(lineNum, str);                                                                             
        Files.write(file.toPath(), lines);                                                     
    }
    
	public void writeStrIfUniqueToFileAtGivenLineNum(String str, File file, int lineNum) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		boolean isUnique = !lines.stream().anyMatch(l -> l.equals(str));
		if (isUnique) {
			lines.add(lineNum, str);
			Files.write(file.toPath(), lines);
		}

	}
    
    public void writeStrToFileBefore(String str, String target, File file) throws IOException { 
        List<String> lines = Files.readAllLines(file.toPath());
        int index = 0;
        for (String line : lines) 
        {	
        	if(line.trim().equals(target))
        	{
        		break;
        	}
        	index++;	
		}
        lines.add(index, str); 
        Files.write(file.toPath(), lines);                                                     
    }
    
    public void writeStrToFileAfter(String str, String target, File file) throws IOException { 
        List<String> lines = Files.readAllLines(file.toPath());
        int index = 0;
        for (String line : lines) 
        {	index++;
        	if(line.trim().equals(target))
        	{
        		break;
        	}
		}
        lines.add(index, str); 
        Files.write(file.toPath(), lines);                                                     
    }
    	
	public int detectFirstLineAfterSequence(List<String> sequence, File file) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		List<Boolean> flags = new ArrayList<Boolean>();
		int lineNumb=-1;
		for (String line : lines) {
			boolean flag = sequence.stream().anyMatch(s -> s.equals(line.trim()));
			flags.add(flag);
		}

		int countTrueFlags = 0;
		for (int i = 0; i < flags.size(); i++) {
			if (flags.get(i)) {
				countTrueFlags++;
			} else {
				countTrueFlags = 0;
			}
			if (countTrueFlags == sequence.size()) {
				lineNumb = i;
				Files.write(file.toPath(), lines);
				break;
			}
		}
		return lineNumb;
	}
    
    private File newFile(File destinationDir, String zipFileName, ZipEntry zipEntry) throws IOException
    {
        File destFile = new File(destinationDir, zipEntry.getName());
        
        String parrentDir = destinationDir.getAbsolutePath()+File.separator+"temp"+File.separator+zipFileName.replace(".zip", "");
        String fileName = destFile.getName();
        
        File dir = new File(parrentDir);
        
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        
        File langDir = new File(parrentDir +File.separator+ destFile.getParentFile().getName());
        
        if (!langDir.exists()) 
        {
            langDir.mkdir();
        }
        
        return new File(langDir + File.separator + fileName);
    }
    
	public File createFileIfNotExistsOtherwiseOwerwrite(File file) throws IOException {
		File parentFile = file.getParentFile();

		if (file.exists()) {
			file.delete();
		}

		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		file.createNewFile();

		return file;
	}
	
	public File createFileWithContent(File file, String content) throws IOException
	{
		File dir = file.getParentFile();
		if(!dir.exists())
		{
			if(!dir.mkdirs())
			{
				throw new IOException("Dir cannot be created");
			}
		}
		file.createNewFile();
		replaceContent(file, content);
		return file;
	}
    
    public void deleteTempFolder(String dir) throws IOException
    {
        String folderPath = dir+File.separator+"temp";
        File folder = new File(folderPath);
        if(!folder.exists()) 
        {
            return;
        }
        Path pathToBeDeleted = Paths.get(folderPath);
        //delete files and folders in temp
        Files.walk(pathToBeDeleted)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);      
        //delete temp
        folder.delete();
    }
    
    public void deleteDir(String path) throws IOException
    {
        File folderOrFiles = new File(path);
        if(!folderOrFiles.exists()) 
        {
            return;
        }
        Path pathToBeDeleted = folderOrFiles.isFile()?  Paths.get(folderOrFiles.getParentFile().getAbsolutePath()):Paths.get(path);
        //delete files and folders in temp
        Files.walk(pathToBeDeleted)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);      
        //delete temp
        folderOrFiles.delete();
    }
    
    public void deleteDir(File dir) throws IOException
    {
    	deleteDir(dir.getAbsolutePath());
    }
    
    public void deleteEmptyDirs(String path) throws IOException
    {
        Files.walk(Paths.get(path))
	        .sorted(Comparator.reverseOrder())
	        .map(Path::toFile)
	        .filter(File::isDirectory)
	        .forEach(File::delete);
    }
    
    public File rename(File targetFile, String newNameWithExt) throws FileSystemException
    {
        String oldFileName = targetFile.getName();
        String newPathName = targetFile.getAbsolutePath().replace(oldFileName,newNameWithExt);
        File result = new File(newPathName);
        boolean isRenamed = targetFile.renameTo(new File(newPathName));
        if(!isRenamed)
        {
        	throw new FileSystemException(targetFile.getAbsolutePath());
        }
        return result;
    }
    
    public File move(File source, File target) throws IOException
    {
    	if(source.isFile())
    	{
    		File targetDir = target.getParentFile();
    		if(!targetDir.exists())
    		{
    			if(!targetDir.mkdirs())
    			{
    				throw new FileSystemException("The directory cannot be made");
    			}
    		}
    	}
    	try {
			Files.move(Paths.get(source.getAbsolutePath()), Paths.get(target.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return target;
    }
    
    public File copy(File source, File target) throws IOException
    {
    	if(source.isFile())
    	{
    		File targetDir = target.getParentFile();
    		if(!targetDir.exists())
    		{
    			if(!targetDir.mkdirs())
    			{
    				throw new FileSystemException("The directory cannot be made");
    			}
    		}
    	}
    	try {
			Files.copy(Paths.get(source.getAbsolutePath()), Paths.get(target.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return target;
    }
    
    public File copy(List<File> sources, File target) throws IOException
    {
    	for (File source : sources) {
			copy(source, target);
		}
        return target;
    }
    
	public void copyFolder(File src, File dest) throws IOException {
		try (Stream<Path> stream = Files.walk(src.toPath())) {
			stream.forEachOrdered(sourcePath -> {
				try {
					String newPath = sourcePath.toString().replace(src.getAbsolutePath(), dest.getAbsolutePath());
					Files.copy(sourcePath, Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
				}
				catch (DirectoryNotEmptyException e) {
					// TODO: handle exception
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
	}
    
	public void searchAndCopyDir(String searchDirName, File sourceDir, File targetDir) throws IOException {		
        Files.walkFileTree(Paths.get(sourceDir.getAbsolutePath()), new SimpleFileVisitor<Path>()
        {
        	@Override
        	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (searchDirName.equals(dir.toFile().getName())) {
					File source = dir.toFile();
					String newDirPath = targetDir.getAbsolutePath()+File.separator+dir.toFile().getName();
					copyFolder(source, new File(newDirPath));
				}
        		return super.postVisitDirectory(dir, exc);
        	}
        	
        });
	}
	
	public List<File> search(String searchDirName, File sourceDir) throws IOException {		
        List<File> results = new ArrayList<File>();
		Files.walkFileTree(Paths.get(sourceDir.getAbsolutePath()), new SimpleFileVisitor<Path>()
        {
        	@Override
        	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if (searchDirName.equals(dir.toFile().getName())) {
					results.add(dir.toFile());
				}
        		return super.postVisitDirectory(dir, exc);
        	}
        	
        });
		return results;
	}

  public void appendToFile(File file, String content) {
    // if file not exists, create and write to it
    // otherwise append to the end of the file
    try {
      Files.write(file.getAbsoluteFile().toPath(), content.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
    
    private String getFileExtension(String fileExtension)
    {
        fileExtension = fileExtension.startsWith("*.")? fileExtension.replace("*.", "."):fileExtension;
        fileExtension = fileExtension.startsWith(".")? fileExtension:"."+fileExtension;
        return fileExtension;
    }
}
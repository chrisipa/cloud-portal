package de.papke.cloud.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class ZipUtil {

	public static void compressZipfile(File sourceDir, File outputFile) throws IOException, FileNotFoundException {
		
		ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(outputFile));
		Path srcPath = Paths.get(sourceDir.toURI());
		compressDirectoryToZipfile(srcPath.getParent().toString(), srcPath.getFileName().toString(), zipFile);
		IOUtils.closeQuietly(zipFile);
	}

	private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException, FileNotFoundException {
		
		String dir = Paths.get(rootDir, sourceDir).toString();
		
		for (File file : new File(dir).listFiles()) {
			
			if (!file.getName().startsWith(".")) {
			
				if (file.isDirectory()) {
					compressDirectoryToZipfile(rootDir, Paths.get(sourceDir,file.getName()).toString(), out);
				} 
				else {
					ZipEntry entry = new ZipEntry(Paths.get(sourceDir,file.getName()).toString());
					out.putNextEntry(entry);
	
					FileInputStream in = new FileInputStream(Paths.get(rootDir, sourceDir, file.getName()).toString());
					IOUtils.copy(in, out);
					IOUtils.closeQuietly(in);
				}
			}
		}
	}
}

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Filter files by name or folder from jar archive.
 *
 */
public final class JarFileRetriever3 {

	protected class FilterRule {
		private static final String FORMAT_FILE_NAME_REX = "(.+?)%s$";
		private static final String FORAMT_FOLDER_NAME_REX = "(.*)(\\/%s\\/)$";
		private static final String FORMAT_FILES_IN_FOLDER_REX = "(.*\\/)?(%s\\/.*[^\\/]$)";

		private String[] files;
		private String[] folders;

		private List<Pattern> filePs;
		private List<Pattern> folderPs;
		private List<Pattern> fileInFolderPs;

		public List<Pattern> getFileInFolderPs() {
			return fileInFolderPs;
		}

		public List<Pattern> getFilePs() {
			return filePs;
		}
		public String[] getFiles() {
			return files;
		}
		public List<Pattern> getFolderPs() {
			return folderPs;
		}

		public String[] getFolders() {
			return folders;
		}

		public void setFileInFolderPs(List<Pattern> fileInFolderPs) {
			this.fileInFolderPs = fileInFolderPs;
		}

		public void setFilePs(List<Pattern> filePs) {
			this.filePs = filePs;
		}

		public void setFiles(String... files) {
			this.files = files;

			filePs = new ArrayList<Pattern>();
			if (files != null) {
				for (String f : files) {
					filePs.add(Pattern.compile(String.format(FORMAT_FILE_NAME_REX, f)));
				}
			}
		}

		public void setFolderPs(List<Pattern> folderPs) {
			this.folderPs = folderPs;
		}

		public void setFolders(String... folders) {
			this.folders = folders;

			folderPs = new ArrayList<Pattern>();
			fileInFolderPs = new ArrayList<Pattern>();

			if (folders != null) {
				for (String f : folders) {
					folderPs.add(Pattern.compile(String.format(FORAMT_FOLDER_NAME_REX, f)));
					fileInFolderPs.add(Pattern.compile(String.format(FORMAT_FILES_IN_FOLDER_REX, f)));
				}
			}
		}

	}

	/**
	 * Size of the buffer to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	private static String getOnlyFileName(String pathName) {
		int lastIndexOf = pathName.lastIndexOf("/");
		return pathName.substring(lastIndexOf + 1);
	}

	private static String getOnlyFolderName(String pathName) {
		String[] split = pathName.split("/");
		return split[split.length - 1];
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
		String tmpName = "version3_" + sdf.format(timestamp);
		String zipFilePath = "test.jar";
		String destDir = System.getProperty("user.dir") + File.separator + tmpName;

		JarFileRetriever3 jfr = new JarFileRetriever3();
		jfr.getRule().setFiles(new String[] { "a", "b", "c" });
		jfr.getRule().setFolders(new String[] { "security", "META-INF" });

		try {
			jfr.unzip(zipFilePath, destDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private FilterRule rule = new FilterRule();



	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		File outFile = new File(filePath);
		outFile.getParentFile().mkdirs();

		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}

	/**
	 * Return file path which is matched in searched folder.
	 * 
	 * @param fileName
	 *            String
	 * @return String
	 */
	private String filterFileInSearchFolder(String fileName) {
		List<Pattern> finfps = getRule().getFileInFolderPs();
		if (finfps != null) {
			for (Pattern p : finfps) {
				Matcher m = p.matcher(fileName);
				if (m.find()) {
					// System.out.println("group(1): " + m.group(1));
					// System.out.println("group(2): " + m.group(2));
					return m.group(2);
				}
			}
		}
		return null;
	}


	public FilterRule getRule() {
		return rule;
	}

	private boolean isFileMatch(String fileName) {
		List<Pattern> filePs = getRule().getFilePs();
		if (filePs != null) {
			for (Pattern p : filePs) {
				if (p.matcher(fileName).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFolderMatch(String folderName) {
		List<Pattern> folderPs = getRule().getFolderPs();
		if (folderPs != null) {
			for (Pattern p : folderPs) {
				if (p.matcher(folderName).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setRule(FilterRule rule) {
		this.rule = rule;
	}


	/**
	 * Extracts a jar file to destination directory according to predefined filter
	 * rules.
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		InputStream ins = getClass().getResourceAsStream(zipFilePath);
		ZipInputStream zipIn = new ZipInputStream(ins);
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				if (isFileMatch(entry.getName())) {
					String pureFileName = getOnlyFileName(entry.getName());
					String filePath = destDirectory + File.separator + pureFileName;
					System.out.println("extract file: " + filePath);

					extractFile(zipIn, filePath);

				} else {
					// if this file is in search folder, extracts it to relative folder path
					String fileInFolder = filterFileInSearchFolder(entry.getName());
					if (fileInFolder != null) {
						String filePath = destDirectory + File.separator + fileInFolder;
						System.out.println("extract file to searched folder: " + filePath);

						extractFile(zipIn, filePath);
					}
				}

			} else {
				// if the entry is a directory, make the directory
				if (isFolderMatch(entry.getName())) {
					String folderPath = destDirectory + File.separator + getOnlyFolderName(entry.getName());
					System.out.println("Create folder: " + folderPath);

					File dir = new File(folderPath);
					dir.mkdirs();
				}

			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

}

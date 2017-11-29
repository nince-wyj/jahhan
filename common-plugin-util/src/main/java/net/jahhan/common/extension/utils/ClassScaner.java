package net.jahhan.common.extension.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

/**
 * @author nince
 */
public class ClassScaner {

	public List<String> parse(final String[] packageNames) {
		List<String> classNameList = new ArrayList<String>(240);
		if (packageNames != null) {
			String packagePath;
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			File file;
			URL url;
			String filePath;
			Enumeration<URL> eUrl;
			for (String packageName : packageNames) {
				packagePath = packageName.replaceAll("\\.", "/");
				try {
					eUrl = classLoader.getResources(packagePath);
					while (eUrl.hasMoreElements()) {
						url = eUrl.nextElement();
						filePath = url.getFile();
						if (filePath.indexOf("src/test/") == -1 && filePath.indexOf("src/main/") == -1) {
							file = new File(url.getPath());
							this.parseFile(classNameList, file, packagePath, url);
						}
					}
				} catch (IOException ex) {
					LogUtil.error("parse 解析指定的包名出现异常", ex);
				}
			}
		} else {
			LogUtil.error("parse 没有输出指定的包路径");
		}
		return classNameList;
	}

	private void parseFile(List<String> classNameList, File file, String packagePath, URL url) {
		File[] subFiles;
		if (file.isDirectory()) {
			subFiles = file.listFiles();
			for (File subFile : subFiles) {
				this.parseFile(classNameList, subFile, packagePath, url);
			}
		} else if (file.getPath().contains(".class")) {
			this.findClass(classNameList, file, packagePath);
		} else if (file.getPath().contains(".jar")) {
			this.findClassInJar(classNameList, url);
		}
	}

	private void findClassInJar(List<String> classNameList, URL url) {
		String urlFile = url.getFile();
		int separatorIndex = urlFile.indexOf("!/");
		String rootEntryPath = "";
		if (separatorIndex != -1) {
			rootEntryPath = urlFile.substring(separatorIndex + "!/".length());
		}
		JarFile jarFile = null;
		try {
			jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
			Enumeration<JarEntry> jarEntryEnum = jarFile.entries();
			JarEntry jarEntry;
			String className;
			while (jarEntryEnum.hasMoreElements()) {
				jarEntry = jarEntryEnum.nextElement();
				if (jarEntry.getName().endsWith(".class") && jarEntry.getName().startsWith(rootEntryPath)) {
					className = jarEntry.getName().replaceAll("/", ".").substring(0, jarEntry.getName().length() - 6);
					if (!classNameList.contains(className)) {
						classNameList.add(className);
					}
				}
			}
		} catch (IOException ex) {
			LogUtil.error("findClassInJar 找不到相应的jar或者class", ex);
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException ex) {
					LogUtil.error("findClassInJar 关闭jarFile出现异常", ex);
				}
			}
		}
	}

	private void findClass(List<String> classNameList, File file, String packagePath) {
		String absolutePath = file.getAbsolutePath().replaceAll("\\\\", "/");
		int index = absolutePath.indexOf(packagePath);
		String className = absolutePath.substring(index);
		className = className.replaceAll("/", ".");
		className = className.substring(0, className.length() - 6);
		if (!classNameList.contains(className)) {
			classNameList.add(className);
		}
	}
	
	public static Set<String> findResourceByPathRule(String rule, String... parentPaths) {
		Set<String> result = new LinkedHashSet<>();
		try {
			ClassLoader cl = ClassScaner.class.getClassLoader();

			for (String parentPath : parentPaths) {
				Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(parentPath)
						: ClassLoader.getSystemResources(parentPath));
				while (resourceUrls.hasMoreElements()) {
					URL url = resourceUrls.nextElement();
					Set<String> fileNameSet = new LinkedHashSet<>();
					Pattern pattern = Pattern.compile(rule);
					if (url.toString().startsWith("jar")) {
						fileNameSet = findPathMatchingJarResources(url, pattern);
					} else if (url.toString().startsWith("file")) {
						fileNameSet = findPathMatchingFiles(url, pattern);
					}

					for (String fileName : fileNameSet) {
						result.add(parentPath + fileName);
					}
				}
			}
		} catch (Exception e) {
			LogUtil.error("scan resource", e);
		}
		return result;
	}

	private static Set<String> findPathMatchingFiles(URL url, Pattern pattern) {
		Set<String> result = new LinkedHashSet<>(8);

		File folder = new File(url.getFile());
		String[] fileNames = folder.list();
		for (String fileName : fileNames) {
			if (pattern.matcher(fileName).matches()) {
				result.add(fileName);
			}
		}
		return result;
	}

	private static Set<String> findPathMatchingJarResources(URL url, Pattern pattern) throws IOException {
		URLConnection con = url.openConnection();
		JarFile jarFile;
		String jarFileUrl;
		String rootEntryPath;
		boolean newJarFile = false;

		if (con instanceof JarURLConnection) {
			JarURLConnection jarCon = (JarURLConnection) con;
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			String urlFile = url.getFile();
			try {
				int separatorIndex = urlFile.indexOf("!/");
				if (separatorIndex != -1) {
					jarFileUrl = urlFile.substring(0, separatorIndex);
					rootEntryPath = urlFile.substring(separatorIndex + "!/".length());
					jarFile = new JarFile(jarFileUrl);
				} else {
					jarFile = new JarFile(urlFile);
					jarFileUrl = urlFile;
					rootEntryPath = "";
				}
				newJarFile = true;
			} catch (ZipException ex) {
				ex.printStackTrace();
				return Collections.emptySet();
			}
		}

		try {
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				rootEntryPath = rootEntryPath + "/";
			}

			Set<String> result = new LinkedHashSet<>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					String relativePath = entryPath.substring(rootEntryPath.length());

					if (pattern.matcher(relativePath).matches() && relativePath.indexOf("/") == -1) {
						result.add(relativePath);
					}
				}
			}
			return result;
		} finally {
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static Set<Class> findClassInPackage(String rule, Package... pkgs) {
		Set<String> pathSet = new HashSet<>();
		for (Package pkg : pkgs) {
			pathSet.add(pkg.getName().replace('.', '/') + "/");
		}

		String[] paths = pathSet.toArray(new String[pathSet.size()]);

		return findClassInPath(rule, paths);
	}

	@SuppressWarnings("rawtypes")
	public static Set<Class> findClassInPath(String rule, String... paths) {
		Set<Class> result = new HashSet<>();
		String classNameRule = rule + "\\.class";

		Set<String> classPaths = findResourceByPathRule(classNameRule, paths);
		for (String classPath : classPaths) {
			String className = classPath.substring(0, classPath.length() - 6).replace('/', '.');
			try {
				result.add(Class.forName(className));
			} catch (Exception e) {
				LogUtil.error("scan resource1", e);
			}
		}

		return result;
	}
}

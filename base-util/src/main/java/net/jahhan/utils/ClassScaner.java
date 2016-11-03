package net.jahhan.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nince
 */
public class ClassScaner {

    private final Logger logger = LoggerFactory.getLogger(ClassScaner.class);

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
                    logger.error("parse 解析指定的包名出现异常", ex);
                }
            }
        } else {
            logger.error("parse 没有输出指定的包路径");
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
    	String rootEntryPath="";
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
                if (jarEntry.getName().endsWith(".class")&&jarEntry.getName().startsWith(rootEntryPath)) {
                    className = jarEntry.getName().replaceAll("/", ".").substring(0, jarEntry.getName().length() - 6);
                    if (!classNameList.contains(className)) {
                        classNameList.add(className);
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("findClassInJar 找不到相应的jar或者class", ex);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ex) {
                    logger.error("findClassInJar 关闭jarFile出现异常", ex);
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
}

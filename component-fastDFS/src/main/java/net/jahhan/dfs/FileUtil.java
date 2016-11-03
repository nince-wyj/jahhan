package net.jahhan.dfs;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	/**
	 * 获取所有文件以为 endWith 结尾（忽略大小写）
	 * 
	 * @param file
	 * @param endWith
	 * @return
	 */
	public static Set<File> getChildFiles(File file, String endWith) {
		Set<File> set = new HashSet<File>();
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; i++) {
					set.addAll(getChildFiles(files[i], endWith));
				}
			}
		} else {
			if (file.getName().toLowerCase().endsWith(endWith)) {
				set.add(file);
			}
		}

		return set;
	}

	public static byte[] file2Bytes(String filename) throws IOException {
		return file2Bytes(new File(filename));
	}
	/**
	 * 如果出错就返回null
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static ByteArrayOutputStream inStream2OutStream(InputStream in) throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] b = new byte[1024];
			int n;
			while ((n = in.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			return bos;
		} finally{
			try {
				in.close();
				bos.close();
			} catch (final IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	public static byte[] file2Bytes(File file) throws IOException {

		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static File bytes2File(byte[] b, String outputFile)
			throws IOException {
		return bytes2File(b, new File(outputFile), false);
	}

	public static File bytes2File(byte[] b, String outputFile, boolean overwrite)
			throws IOException {
		return bytes2File(b, new File(outputFile), true);
	}

	public static File bytes2File(byte[] b, File file) throws IOException {
		return bytes2File(b, file, false);
	}

	public static File bytes2File(byte[] b, File file, boolean overwrite)
			throws IOException {
		BufferedOutputStream stream = null;
		try {
			FileOutputStream fstream = new FileOutputStream(file, overwrite);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}
	
	public static String getExt(String fileName){
		int index=fileName.lastIndexOf(".");
		if(index<0){
			return "";
		}
		return fileName.substring(index+1);
	}
	
	/**
	 * 在后缀名和文件名之间加上后缀
	 * @param suf 如果fileName有后缀名，就不能再包含文件后缀名
	 * @return
	 */
	public static String appendSuffix(String fileName,String suf){
		if(fileName==null){
			return null;
		}
		int index=fileName.lastIndexOf(".");
		if(index<0){
			return fileName+suf;
		}
		return fileName.substring(0,index)+suf+fileName.substring(index);
	}
	
	/**
	 * 在后缀名和文件名之间加上后缀
	 * @param suf 如果fileName有后缀名，就不能再包含文件后缀名
	 * @return
	 */
	public static String removeSuffix(String fileName){
		if(fileName==null){
			return null;
		}
		int index=fileName.lastIndexOf("*");
		if(index<0){
			return fileName;
		}
		int _index=fileName.lastIndexOf("_",index);
		return fileName.substring(0,_index)+"."+getExt(fileName);
	}

}

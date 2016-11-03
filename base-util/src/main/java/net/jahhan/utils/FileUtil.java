package net.jahhan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	private final static int BUFFER = 10 * 1024;
	/**
	 * 功能：文件的写入
	 * 
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @param args
	 * @throws IOException
	 */
	public static void writeFile(String filePath, String fileName, String[] args)
			throws IOException {
		FileOutputStream writerStream = new FileOutputStream(filePath + fileName);    
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8")); 
		for (int i = 0; i < args.length; i++) {
			bw.write(args[i]);
			bw.write("\n");
			bw.flush();
		}
		bw.close(); 
		
	}
	
	/**
	 * 功能：文件的写入
	 * 
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @param args
	 * @throws IOException
	 */
	public static void writeFile(String filePath, String fileName, String args)
			throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(
				filePath + fileName, true), "UTF-8");
		osw.write(args + "\r\n");

		osw.close();
	}
	


	/**
	 * 功能： 一行一行的读取文件中的数据
	 * 
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @throws IOException
	 */
	public String[] readLineFile(String filePath, String fileName)
			throws IOException {

		FileInputStream fr = new FileInputStream(filePath + "\\" + fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fr,
				"UTF-8"));
		List<String> lineFile = new ArrayList<String>();
		String line = br.readLine();
		lineFile.add(line);
		while (line != null) {
			line = br.readLine();
			lineFile.add(line);
		}
		br.close();
		fr.close();
		if (lineFile != null && lineFile.size() > 0) {
			return (String[]) lineFile.toArray(new String[0]);
		}
		return null;
	}
	
	/**
	 * 功能：检测文件在指定目录下是否存在
	 * 
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 * @return true 文件存在 false 文件不存在
	 */
	public static boolean isExistFile(String filePath) {
		boolean result = false;
		File file = new File(filePath);
		if (file.exists()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}
	
	/**
	 * 判断文件路径是否存在，不存在即创建
	 * 
	 * @author JianWeiChen
	 * @param filePath
	 */
	public static void isExistsFilePath(String filePath) {
		File file = new File(filePath);
		logger.info("上传的路径地址" + file.getAbsolutePath());
		if (!file.exists()) {
			file.setWritable(true, false);
			file.mkdirs();
		}
	}
	/**
	 * 输出流
	 * @param fName
	 * @param response
	 */
	public static void getDownLoadFiles(String fName, HttpServletResponse response){
		   
				 File f=new File(fName);
				 
				 try (
						 BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
						 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))
						 
						 ) {
					 
					 	byte[] buff = new byte[1024];
					    int len = 0;
					    while((len = bis.read(buff, 0, buff.length))!=-1){
					      bos.write(buff, 0, len);
					     }
					    bos.flush();
					
				} catch (Exception e) {
					logger.error("error : ", e);
				}
				 
				 
			
			
		
	}

	/**
	 * 获取上传文件信息
	 * 
	 * @author JianWeiChen
	 * @param request
	 * @param file
	 * @param fileName
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public static boolean getUploadFiles(HttpServletRequest request, File file,
			String fileName, String path) throws FileNotFoundException,
			IOException, Exception {
		boolean flag = false;
		String photospath = FileUtil.getTomcatUploadPath(request, path);
		FileUtil.isExistsFilePath(photospath);
		File fs = new File(photospath, fileName);
		try (FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(fs)) {
			
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.flush();
			flag = true;
		} catch (FileNotFoundException e) {
			flag = false;
			logger.error("error : ", e);
		} catch (IOException e) {
			flag = false;
			logger.error("error : ", e);
		} catch (Exception e) {
			flag = false;
			logger.error("error : ", e);
		}
		return flag;
	}

	/**
	 * 获取上传的目录目录
	 * 
	 * @param request
	 * @param path
	 * @return
	 */
	public static String getTomcatUploadPath(HttpServletRequest request,String path) {
		String tomcatRoot = request.getServletContext().getRealPath("");
		StringBuilder tomcatWebAppsUpload = new StringBuilder();
		tomcatWebAppsUpload.append(tomcatRoot.substring(0,tomcatRoot.lastIndexOf("\\") + 1));
		tomcatWebAppsUpload.append(path);
		return tomcatWebAppsUpload.toString();
	}



	public static List<String> getFiles(String filePath) {
		List<String> list = new ArrayList<String>();
		getFiles(filePath, list);
		return list;
	}

	public static void getFiles(String filePath, List<String> list) {
		File root = new File(filePath);
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getFiles(file.getAbsolutePath(), list);
			} else {
				list.add(file.getAbsolutePath());

			}
		}

	}
	
	/**
	 * 压缩成GZIP
	 * @param file
	 * @param os
	 */
	public static void compress(File file, OutputStream os,boolean delete) {
		
		try (OutputStream mos = os;
				FileInputStream fis = new FileInputStream(file);
				GZIPOutputStream gos = new GZIPOutputStream(mos)) {
				int count;
				byte data[] = new byte[BUFFER];
				while ((count = fis.read(data, 0, BUFFER)) != -1) {
					gos.write(data, 0, count);
				}
				gos.finish();
				gos.flush();
				//gos.close();
		} catch (Exception e) {
			logger.error("error : ", e);
		} 
	}

	public static String readFileToString(String path, String charset) {
		String result = null;

		File file = new File(path);
		logger.info("file "+path+" last modify "+ DateUtils.dateFormat.format(file.lastModified()));

		StringBuilder sb = new StringBuilder();
		try (FileInputStream fis = new FileInputStream(file);
			 InputStreamReader isr = new InputStreamReader(fis, charset);
			 BufferedReader br = new BufferedReader(isr)) {
			
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}

			result = sb.toString();
		} catch (Exception e) {
			logger.error("error : ", e);
		}
		
		
		
		
		return result;
	}
	
}

package net.jahhan.utils;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.enumeration.HttpConnectionEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.dfs.FileSys;
import net.jahhan.factory.httpclient.HttpConnection;
import net.jahhan.factory.httpclient.HttpResponseEntity;

/**
 * 
 * @author nince
 */
public class FileUtils {

	private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

	private FileUtils() {
	}

	/**
	 * 用于存储所有文件
	 * 
	 * @param contents
	 *            图片的字节
	 * @param fileName
	 *            只是提取后缀名，可以为null
	 * @return 文件的全路径名，用于url访问
	 * @throws IOException
	 */
	public static String writeFile(final byte[] contents, String fileName) throws Exception {
		String suffix = null;
		int index = fileName.lastIndexOf(".");
		if (index > -1) {
			suffix = fileName.substring(index);
		}
		String fileId = uploadFile(contents, suffix);
		if (fileId == null) {
			return null;
		}
		return SysConfiguration.getUploadURL() + fileId;
	}

	/**
	 * 只是用来做图片的
	 * 
	 * @param contents
	 *            图片的字节
	 * @param fileName
	 *            要有后缀名
	 * @return 文件的全路径名，用于url访问.文件名会含有文件的宽度和高度
	 * @throws IOException
	 */
	public static String writeImageToDir(final byte[] contents, String fileName) throws Exception {
		ByteArrayInputStream out = new ByteArrayInputStream(contents);
		Image src = ImageIO.read(out); // 构造Image对象
		int wideth = src.getWidth(null); // 得到源图宽
		int height = src.getHeight(null); // 得到源图长

		// 组合图片宽高 字符串
		String suffix = "_" + wideth + "*" + height;
		int index = fileName.lastIndexOf(".");
		if (index > -1) {
			suffix += fileName.substring(index);
		} else {
			suffix += ".png";
		}
		String fileId = uploadFile(contents, suffix);
		if (fileId == null) {
			return null;
		}
		return SysConfiguration.getUploadURL() + fileId;
	}

	@Deprecated
	private static byte[] getOldFile(String url) {
		logger.info("{} from old file", url);
		if (!url.toLowerCase().startsWith("http:")) {
			return null;
		}
		HttpConnection httpConnection = ApplicationContext.CTX.getHttpConnectionFactory()
				.getHttpClient(HttpConnectionEnum.MULTION);
		HttpResponseEntity httpResponse = httpConnection.executeGetFile(url, null);
		System.out.println("result:" + httpResponse.getResult());
		return httpResponse.getCode() == 200 ? httpResponse.getContents() : null;
	}

	// TODO:以后用这个直接读取的
	public static byte[] getFile_new(String fileId) throws Exception {
		if (org.apache.commons.lang3.StringUtils.isEmpty(fileId)) {
			return null;
		}
		fileId = fileId.replace(SysConfiguration.getUploadURL(), "");
		byte[] bts = downloadFile(fileId);
		if (ArrayUtils.isEmpty(bts)) {
			return null;
		}
		return bts;
	}

	public static byte[] getFile(String fileId) throws Exception {
		String low = fileId.toLowerCase();
		if (low.startsWith("http://weile.v89.com") || low.startsWith("http://of.v89.com")
				|| low.startsWith("http://manager.v89.com")) {
			return getOldFile(fileId);
		}
		return getFile_new(fileId);
	}

	private static String getFilePath(final byte[] contents, String fileName, final String filePath) {
		FileOutputStream fos = null;
		String path = "";
		boolean isSuccess = false;
		String ext = fileName.substring(fileName.lastIndexOf("."));
		fileName = UUID.randomUUID().toString() + ext;
		try {
			if (filePath != null && !filePath.isEmpty()) {
				File file = new File(filePath);
				if (file.exists()) {
					isSuccess = true;
				} else {
					isSuccess = file.mkdirs();
				}
			}
			if (isSuccess) {
				path = filePath + fileName;
				fos = new FileOutputStream(path);
				fos.write(contents);
				fos.flush();
			} else {
				logger.error("新建文件夹出错");
			}
		} catch (FileNotFoundException ex) {
			logger.error("文件无法找到", ex);
		} catch (IOException ex) {
			logger.error("写文件出错", ex);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException ex) {
				logger.error("写文件出错", ex);
			}
		}
		logger.debug("path=" + path);
		return path;
	}

	public static String getWebFilePath(String url) throws Exception {
		logger.debug("url={}", url);
		byte[] btys = getFile(url);
		String filePath = SysConfiguration.getUploadDir();
		String path = getFilePath(btys, url, filePath);
		logger.debug("path=" + path);
		return path;
	}

	/**
	 * 删除文件系统中的文件
	 * 
	 * @param fileName
	 * @param allowFiles
	 * @return
	 */
	public static boolean deleteRemoteFile(String fileId) throws Exception {
		if (StringUtils.isEmpty(fileId)) {
			return false;
		}
		fileId = fileId.replace(SysConfiguration.getUploadURL(), "");
		return deleteFile(fileId);
	}

	/**
	 * 删除本地文件
	 * 
	 * @param uploadDir
	 * @param filePath
	 * @return
	 */
	public static boolean deleteLocalFile(String uploadDir, String filePath) {
		boolean isSuccess = false;
		if (filePath != null && !filePath.isEmpty()) {
			int index = filePath.lastIndexOf(File.separator);
			if (index > -1) {
				String fileName = filePath.substring(index + 1);
				File file = new File(uploadDir, fileName);
				if (file.exists()) {
					file.delete();
					isSuccess = true;
				} else {
					isSuccess = true;
				}
			}
		}
		return isSuccess;
	}

	public static boolean checkFileType(String fileName, String[] allowFiles) {
		Iterator<String> type = Arrays.asList(allowFiles).iterator();
		while (type.hasNext()) {
			String ext = type.next();
			if (fileName.toLowerCase().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param btys
	 * @param ext
	 *            后缀名，支持_200*400.jpg格式
	 * @return
	 * @throws Exception
	 */
	private static String uploadFile(byte[] btys, String ext) throws Exception {
		return FileSys.uploadFile(btys, ext);
	}

	private static boolean deleteFile(String fileId) throws Exception {
		return FileSys.deleteFile(fileId);
	}

	private static byte[] downloadFile(String fileId) throws Exception {
		return FileSys.downloadFile(fileId);
	}
}

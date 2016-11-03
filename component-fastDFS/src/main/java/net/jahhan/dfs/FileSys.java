package net.jahhan.dfs;


import java.io.IOException;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对_200*400.jpg类型的fileId进行转义
 * @author nince
 *
 */
public class FileSys {
	
	private static Logger logger = LoggerFactory.getLogger(FileSys.class);
	public static void init(){
		
	}
	static{
		String f=FileSys.class.getClassLoader().getResource("dfs_client.conf").getFile();
		try {
			ClientGlobal.init(f);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			System.exit(-1);
		}
	}
	
	private static StorageClient1 getStorageClient1() throws IOException{
//		TrackerGroup tg = ClientGlobal.g_tracker_group;
//		TrackerClient tc = new TrackerClient(tg);
//		
//		TrackerServer ts = tc.getConnection();
		
		return new StorageClient1(null,null);
	}
	
	/**
	 * 
	 * @param btys
	 * @param ext 后缀名，支持_200*400.jpg格式
	 * @return
	 * @throws Exception
	 */
	public static String uploadFile(byte[] btys) throws Exception {  
		
		StorageClient1 sc =getStorageClient1();
		NameValuePair[] metaList = new NameValuePair[0];
		String group=ClientGlobal.getRandomGroup();
		return sc.upload_file1(group,btys, null, metaList);
	}
	
	/**
	 * 
	 * @param btys
	 * @param ext 后缀名，支持_200*400.jpg格式
	 * @return
	 * @throws Exception
	 */
	public static String uploadFile(byte[] btys, String ext) throws Exception {  
		
		StorageClient1 sc =getStorageClient1();
		NameValuePair[] metaList = new NameValuePair[0];
		String group=ClientGlobal.getRandomGroup();
		int index=ext.lastIndexOf(".");
		String pre=null;
		if(index==0){
			ext=ext.substring(1);
		}else if(index>0){
			pre=ext.substring(0,index);
			ext=ext.substring(index+1);
		}
		String fileId=sc.upload_file1(group,btys, ext, metaList);
		return pre==null?fileId:FileUtil.appendSuffix(fileId, pre); 
	}
	
	public static byte[] downloadFile(String fileId) throws Exception {
		StorageClient1 sc =getStorageClient1();
		return sc.download_file1(FileUtil.removeSuffix(fileId)); 
	}
	
	public static boolean deleteFile(String fileId) throws Exception {  
		StorageClient1 sc =getStorageClient1();
		return sc.delete_file1(FileUtil.removeSuffix(fileId))==0;
	}
	
	/**
	 * 
	 * @param masterId 原始图片的地址
	 * @param btys
	 * @param ext 后缀名，支持_200X400.jpg格式
	 * @return
	 * @throws Exception
	 */
	public static String uploadChildFile(String masterId,byte[] btys, String ext) throws Exception {  
		
		StorageClient1 sc =getStorageClient1();
		NameValuePair[] metaList = new NameValuePair[0];
		int index=ext.lastIndexOf(".");
		String pre=null;
		if(index==0){
			ext=ext.substring(1);
		}else if(index>0){
			pre=ext.substring(0,index);
			ext=ext.substring(index+1);
		}
		return sc.upload_file1(masterId,pre,btys, ext, metaList);
	}
}

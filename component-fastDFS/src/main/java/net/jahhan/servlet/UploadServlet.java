package net.jahhan.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Singleton;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.utils.HttpResponse;
import net.jahhan.utils.Uploader;

/**
 * @author nince
 *
 */
@Singleton
@WebServlet(name = "uploadServlet", urlPatterns = { "/upload/*" })
@MultipartConfig(fileSizeThreshold = 5000)
public class UploadServlet extends HttpServlet {

	private static final long serialVersionUID = 7437808236758903291L;

	private final static Logger logger = LoggerFactory
			.getLogger("uploadServlet.servlet");

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		HttpResponse httpresponse = new HttpResponse();
		resp.setContentType("application/json;charset=utf-8");
		resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.addHeader("P3P", "CP=CAO PSA OUR");
		try {
			req.setCharacterEncoding("utf-8");

			Uploader up = new Uploader(req);
			up.setSavePath("upload");
			String[] fileType = { ".gif", ".png", ".jpg", ".jpeg" };
			up.setAllowFiles(fileType);
			up.setMaxSize(10000); // 单位KB
			up.upload();
			if (up.getState().equalsIgnoreCase("success")) {
				httpresponse.setR_code(SystemErrorCode.SUCCESS);
			} else {
				httpresponse.setR_code(SystemErrorCode.FILEUPLOAD_ERROR);
			}
			httpresponse.setR_msg(up.getState());
			httpresponse.setR_content(up.getUrl());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} catch (Error e) {
			logger.error(e.getMessage(), e);
		} finally {
			PrintWriter out;
			try {
				out = resp.getWriter();
				out.println(JSONObject.toJSONString(httpresponse));
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getfileName(String header) {
		String[] tempArr1 = header.split(";");
		String[] tempArr2 = tempArr1[2].split("=");
		String fileName = tempArr2[1].substring(
				tempArr2[1].lastIndexOf("\\") + 1).replaceAll("\"", "");
		return fileName;
	}
}

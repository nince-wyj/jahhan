package net.jahhan.api;

import java.io.InputStream;

import net.jahhan.constant.enumeration.ContentTypeEnum;

public class ResponseFile {
	private String fileName;
	private ContentTypeEnum contentType;
	private InputStream in;
	private long fileLength;
	
	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ContentTypeEnum getContentType() {
		return contentType;
	}

	public void setContentType(ContentTypeEnum contentType) {
		this.contentType = contentType;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

}
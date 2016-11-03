package net.jahhan.constant.enumeration;

public enum ContentTypeEnum {
	ZIP("application/zip"), GIF("image/gif"), JPEG("image/jpeg"), XML(
			"text/xml"), TXT("text/plain"), HTML("text/html"), XLA(
			"application/vnd.ms-excel");

	String mimeType;

	ContentTypeEnum(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}
}

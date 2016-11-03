package net.jahhan.utils;

import com.alibaba.fastjson.JSONObject;

public class HttpResponse {

	private int r_code;
	private String r_msg;
	private Object r_content;

	public HttpResponse() {
	}

	public int getR_code() {
		return r_code;
	}

	public void setR_code(int r_code) {
		this.r_code = r_code;
	}

	public String getR_msg() {
		return r_msg;
	}

	public void setR_msg(String r_msg) {
		this.r_msg = r_msg;
	}

	public Object getR_content() {
		if(null != r_content && !"".equals(r_content)){
			return r_content;
		}else{
			return JSONObject.toJSONString(new Object());
		}
	}

	public void setR_content(Object r_content) {
		if(null != r_content && !"".equals(r_content)){
			this.r_content = r_content;
		}else{
			this.r_content = JSONObject.toJSONString(new Object());
		}
	}
}

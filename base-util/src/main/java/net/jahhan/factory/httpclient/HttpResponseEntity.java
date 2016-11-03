package net.jahhan.factory.httpclient;

import org.apache.http.client.CookieStore;

/**
 * @author nince
 */
public final class HttpResponseEntity {
    private String result;

    private byte[] contents;

    private String fileName;

    private CookieStore cookie;

    private int code;

    public HttpResponseEntity(String result, CookieStore cookie) {
        this.result = result;
        this.cookie = cookie;
    }

    public HttpResponseEntity(byte[] contents, String fileName, CookieStore cookie, int statusCode) {
        this.contents = contents;
        this.fileName = fileName;
        this.cookie = cookie;
        this.code = statusCode;
    }

    public int getCode() {
        return code;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * @return the cookie
     */
    public CookieStore getCookie() {
        return cookie;
    }

    /**
     * @param cookie
     *            the cookie to set
     */
    public void setCookie(CookieStore cookie) {
        this.cookie = cookie;
    }

    /**
     * @return the contents
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     * @param contents
     *            the contents to set
     */
    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

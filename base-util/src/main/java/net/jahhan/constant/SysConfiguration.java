package net.jahhan.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import net.jahhan.cache.ThirdEncryptKeyCache;
import net.jahhan.constant.enumeration.SessionStrategyEnum;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.utils.PropertiesUtil;

/**
 * @author nince
 */
public class SysConfiguration {
	private static final Logger logger = LoggerFactory.getInstance().getLogger(SysConfiguration.class);
	/**
	 * 文件上传地址
	 */
	private static String uploadDir;
	/**
	 * 文件url
	 */
	private static String uploadURL;
	/**
	 * 数据库连接路径
	 */
	private static String jdbcFileName;
	/**
	 * 是否开启接口耗时记录
	 */
	private static Boolean recordTimeConsume;
	/**
	 * 全局秘钥
	 */
	private static String encryptkey;
	/**
	 * 公司名
	 */
	private static String companyName;
	/**
	 * 是否开启html过滤
	 */
	private static Boolean htmlFilterInuse;
	/**
	 * 端类型
	 */
	private static Integer appType;
	/**
	 * 无权限返回页面
	 */
	private static String noAuthorityUrl;
	/**
	 * session多端登陆策略
	 */
	private static SessionStrategyEnum sessionStrategy = SessionStrategyEnum.MULTI;
	/**
	 * websocket接受的路径
	 */
	private static List<String> wsPathAccept = new ArrayList<>();
	/**
	 * 消息队列实现方式
	 */
	private static String mqActualize;
	/**
	 * 是否debug模式
	 */
	private static Boolean isDebug;
	/**
	 * 是否保存接口
	 */
	private static Boolean actSave;
	/**
	 * 异步超时
	 */
	private static Long asyncTimeOut;
	/**
	 * 业务代码对应框架版本
	 */
	private static String version;
	/**
	 * 是否开启权限管理
	 */
	private static Boolean authorityInuse;
	/**
	 * 是否跨域
	 */
	private static Boolean allowAllOrigin;
	/**
	 * 是否开启关系数据库
	 */
	private static Boolean useSQLDB;
	/**
	 * 使用pipeline请求的域名列表
	 */
	private static List<String> pipelineOriginList = new ArrayList<>();
	/**
	 * 是否使用注册中心注册服务器
	 */
	private static Boolean registHost;
	/**
	 * zk注册中心地址
	 */
	private static String zkHost;
	/**
	 * 注册类型
	 */
	private static String applicationType;
	/**
	 * zk注册中心命名空间
	 */
	private static String zkRegistNamespace;

	static {
		Properties property = PropertiesUtil.getProperties("sys_baseconf");
		try {
			setVersion(property.getProperty("version", "1.2"));
			setJdbcFileName(property.getProperty("jdbcFileName", "jdbc"));
			setNoAuthorityUrl(property.getProperty("noAuthorityUrl", "index.html"));
			setMqActualize(property.getProperty("mq.actualize", "redis"));
			setActSave(BooleanUtils.toBoolean(property.getProperty("actSave", "false")));
			setIsDebug(BooleanUtils.toBoolean(property.getProperty("isDebug", "false")));
			setRecordTimeConsume(BooleanUtils.toBoolean(property.getProperty("recordTimeConsume", "false")));
			setHtmlFilterInuse(BooleanUtils.toBoolean(property.getProperty("htmlFilterInuse", "false")));
			setAuthorityInuse(BooleanUtils.toBoolean(property.getProperty("use.authority", "false")));
			setUseSQLDB(BooleanUtils.toBoolean(property.getProperty("useSQLDB", "true")));
			setAllowAllOrigin(BooleanUtils.toBoolean(property.getProperty("allowAllOrigin", "false")));
			setRegistHost(BooleanUtils.toBoolean(property.getProperty("registHost", "false")));
			setAppType(NumberUtils.toInt(property.getProperty("appType"), 0));
			setAsyncTimeOut(NumberUtils.toLong(property.getProperty("appType"), 90000));
			setZkRegistNamespace(property.getProperty("zk.registNamespace", "hostRegister"));

			setZkHost(property.getProperty("zk.host"));
			setUploadDir(property.getProperty("uploadDir"));
			setUploadURL(property.getProperty("uploadURL"));
			setEncryptkey(property.getProperty("encryptkey.common"));
			setCompanyName(property.getProperty("companyName"));

			if (null != PropertiesUtil.getProperties("dubbo")) {
				setApplicationType(PropertiesUtil.get("dubbo", "dubbo.application.name"));
			} else {
				setApplicationType(property.getProperty("appType", "0"));
			}
			if (null != property.getProperty("session.strategy"))
				setSessionStrategy(SessionStrategyEnum.valueOf(property.getProperty("session.strategy")));
			if (null != property.getProperty("thirdLogin.name")) {
				String[] thirds = property.getProperty("thirdLogin.name").split(",");
				ThirdEncryptKeyCache tekc = ThirdEncryptKeyCache.getInstance();
				for (int i = 0; i < thirds.length; i++) {
					tekc.setEncryptKey(thirds[i], property.getProperty("encryptkey." + thirds[i]));
				}
			}
			if (null != property.getProperty("websocket.pathaccept")) {
				setWsPathAccept(Arrays.asList(property.getProperty("websocket.pathaccept").split(",")));
			}
			if (null != property.getProperty("pipelineOriginList")) {
				String pipelineOriginString = property.getProperty("pipelineOriginList");
				String[] pipelineOrigins = pipelineOriginString.split(",");
				setPipelineOriginList(Arrays.asList(pipelineOrigins));
			}
		} catch (Exception ex) {
			logger.error("加载系统sys_baseconf.properties配置出错", ex);
			throw new RuntimeException("加载系统配置出错");
		}
	}

	public static String getUploadDir() {
		return uploadDir;
	}

	public static void setUploadDir(String uploadDir) {
		SysConfiguration.uploadDir = uploadDir;
	}

	public static String getUploadURL() {
		return uploadURL;
	}

	public static void setUploadURL(String uploadURL) {
		SysConfiguration.uploadURL = uploadURL;
	}

	public static String getJdbcFileName() {
		return jdbcFileName;
	}

	public static void setJdbcFileName(String jdbcFileName) {
		SysConfiguration.jdbcFileName = jdbcFileName;
	}

	public static Boolean getRecordTimeConsume() {
		return recordTimeConsume;
	}

	public static void setRecordTimeConsume(Boolean recordTimeConsume) {
		SysConfiguration.recordTimeConsume = recordTimeConsume;
	}

	public static String getEncryptkey() {
		return encryptkey;
	}

	public static void setEncryptkey(String encryptkey) {
		SysConfiguration.encryptkey = encryptkey;
	}

	public static String getCompanyName() {
		return companyName;
	}

	public static void setCompanyName(String companyName) {
		SysConfiguration.companyName = companyName;
	}

	public static Boolean getHtmlFilterInuse() {
		return htmlFilterInuse;
	}

	public static void setHtmlFilterInuse(Boolean htmlFilterInuse) {
		SysConfiguration.htmlFilterInuse = htmlFilterInuse;
	}

	public static Integer getAppType() {
		return appType;
	}

	public static void setAppType(Integer appType) {
		SysConfiguration.appType = appType;
	}

	public static String getNoAuthorityUrl() {
		return noAuthorityUrl;
	}

	public static void setNoAuthorityUrl(String noAuthorityUrl) {
		SysConfiguration.noAuthorityUrl = noAuthorityUrl;
	}

	public static SessionStrategyEnum getSessionStrategy() {
		return sessionStrategy;
	}

	public static void setSessionStrategy(SessionStrategyEnum sessionStrategy) {
		SysConfiguration.sessionStrategy = sessionStrategy;
	}

	public static List<String> getWsPathAccept() {
		return wsPathAccept;
	}

	public static void setWsPathAccept(List<String> wsPathAccept) {
		SysConfiguration.wsPathAccept = wsPathAccept;
	}

	public static String getMqActualize() {
		return mqActualize;
	}

	public static void setMqActualize(String mqActualize) {
		SysConfiguration.mqActualize = mqActualize;
	}

	public static Boolean getIsDebug() {
		return isDebug;
	}

	public static void setIsDebug(Boolean isDebug) {
		SysConfiguration.isDebug = isDebug;
	}

	public static Boolean getActSave() {
		return actSave;
	}

	public static void setActSave(Boolean actSave) {
		SysConfiguration.actSave = actSave;
	}

	public static Long getAsyncTimeOut() {
		return asyncTimeOut;
	}

	public static void setAsyncTimeOut(Long asyncTimeOut) {
		SysConfiguration.asyncTimeOut = asyncTimeOut;
	}

	public static String getVersion() {
		return version;
	}

	public static void setVersion(String version) {
		SysConfiguration.version = version;
	}

	public static Boolean getAuthorityInuse() {
		return authorityInuse;
	}

	public static void setAuthorityInuse(Boolean authorityInuse) {
		SysConfiguration.authorityInuse = authorityInuse;
	}

	public static Boolean getAllowAllOrigin() {
		return allowAllOrigin;
	}

	public static void setAllowAllOrigin(Boolean allowAllOrigin) {
		SysConfiguration.allowAllOrigin = allowAllOrigin;
	}

	public static Boolean getUseSQLDB() {
		return useSQLDB;
	}

	public static void setUseSQLDB(Boolean useSQLDB) {
		SysConfiguration.useSQLDB = useSQLDB;
	}

	public static List<String> getPipelineOriginList() {
		return pipelineOriginList;
	}

	public static void setPipelineOriginList(List<String> pipelineOriginList) {
		SysConfiguration.pipelineOriginList = pipelineOriginList;
	}

	public static Boolean getRegistHost() {
		return registHost;
	}

	public static void setRegistHost(Boolean registHost) {
		SysConfiguration.registHost = registHost;
	}

	public static String getZkHost() {
		return zkHost;
	}

	public static void setZkHost(String zkHost) {
		SysConfiguration.zkHost = zkHost;
	}

	public static String getApplicationType() {
		return applicationType;
	}

	public static void setApplicationType(String applicationType) {
		SysConfiguration.applicationType = applicationType;
	}

	public static String getZkRegistNamespace() {
		return zkRegistNamespace;
	}

	public static void setZkRegistNamespace(String zkRegistNamespace) {
		SysConfiguration.zkRegistNamespace = zkRegistNamespace;
	}

}

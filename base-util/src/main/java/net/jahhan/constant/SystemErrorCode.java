package net.jahhan.constant;

/**
 * @author nince
 */
public final class SystemErrorCode {
	public static String MESSAGE_SUCCESS = "success";
	public static String MESSAGE_FAILED = "failed";
	// 成功返回跳转页面
	public static final int SUCCESS_REDICT = -1;
	// 成功
	public static final int SUCCESS = 0;
	// 未知错误
	public static final int UNKOWN_ERROR = 999;

	// 启动时错误
	public static final int INIT_ERROR = 998;

	// 编码时错误
	public static final int CODE_ERROR = 997;

	// 业务级错误
	public static final int BUSINESS_ERROR = 996;

	// Session无效或者过期
	public static final int INVALID_SESSION = 900;

	// session驗證不通過
	public static final int SESSION_NO_EXIST = 901;

	// 文件上传失败
	public static final int FILEUPLOAD_ERROR = 902;

	// 数据库错误
	public static final int DATABASE_ERROR = 903;

	// 服务器维护中
	public static final int SERVER_MAINTAINING = 904;

	// 锁超时
	public static final int LOCK_OVERTIME = 905;

	// 参数错误
	public static final int PARAMETER_ERROR = 906;

	// 权限不足
	public static final int NO_AUTHORITY = 908;

	// 页面不存在
	public static final int NO_PAGE = 909;

	// 接口已经失效或者不存在
	public static final int NO_SERVICE_INTERFACE = 910;

	// 获取认证KEY失败
	public static final int KEY_ERROR = 911;

	// 已连接同类型websocket
	public static final int WSSESSION_EXIIT = 912;

	// 用户已经登陆
	public static final int USER_EXIIT = 913;

	// 系统配置错误
	public static final int SYSTEM_CONFIG_ERROR = 914;

	// 报文格式不正确
	public static final int CONTENT_FORMAT_ERROR = 915;

	// 报文为空
	public static final int CONTENT_NULL_ERROR = 916;

	// 加密失败
	public static final int ENCRYPT_ERROR = 917;

	// 解密失败
	public static final int DECRYPT_ERROR = 918;

	// 签名验证失败
	public static final int SIGN_ERROR = 919;

	// 响应失败
	public static final int RESPONSE_ERROR = 920;

	// 快速响应失败-
	public static final int FAST_RESPONSE_ERROR = 921;

	// 未支持
	public static final int UNSUPPORT_ERROR = 922;
}

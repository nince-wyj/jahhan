package net.jahhan.common.extension.constant;

/**
 * @author nince
 */
public class JahhanErrorCode {
	// 未知错误
	public static final int UNKNOW_ERROR = 999;
	// 启动时错误
	public static final int INIT_ERROR = 998;

	public static final int NETWORK_EXCEPTION = 951;

	public static final int TIMEOUT_EXCEPTION = 952;

	public static final int BIZ_EXCEPTION = 953;

	public static final int FORBIDDEN_EXCEPTION = 954;

	public static final int SERIALIZATION_EXCEPTION = 955;

	public static final int NOT_SUCH_PROPERTIES_EXCEPTION = 956;
	// 字段验证失败
	public static final int VALIATION_EXCEPTION = 957;
	// 未知服务
	public static final int UNKNOW_SERVICE_EXCEPTION = 958;
	// 数据库错误
	public static final int DATABASE_ERROR = 903;
	// 服务器维护中
	public static final int SERVER_MAINTAINING = 904;
	// 锁超时
	public static final int LOCK_OVERTIME = 905;
	// 锁错误
	public static final int LOCK_ERROE = 905;
	// 参数错误
	public static final int PARAMETER_ERROR = 906;
	// 权限不足
	public static final int NO_AUTHORITY = 908;
	// 加密失败
	public static final int ENCRYPT_ERROR = 917;
	// 解密失败
	public static final int DECRYPT_ERROR = 918;
	// 响应失败
	public static final int RESPONSE_ERROR = 920;
	// 快速响应失败-
	public static final int FAST_RESPONSE_ERROR = 921;
	// 未支持
	public static final int UNSUPPORT_ERROR = 922;
}

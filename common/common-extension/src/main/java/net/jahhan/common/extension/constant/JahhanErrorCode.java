package net.jahhan.common.extension.constant;

/**
 * @author nince
 */
public class JahhanErrorCode {
	// 未知错误
	public static final String UNKNOW_ERROR = "UNKNOW_ERROR";
	// 启动时错误
	public static final String INIT_ERROR = "INIT_ERROR";

	public static final String NETWORK_EXCEPTION = "NETWORK_EXCEPTION";

	public static final String TIMEOUT_EXCEPTION = "TIMEOUT_EXCEPTION";

	public static final String BIZ_EXCEPTION = "BIZ_EXCEPTION";

	public static final String FORBIDDEN_EXCEPTION = "FORBIDDEN_EXCEPTION";

	public static final String SERIALIZATION_EXCEPTION = "SERIALIZATION_EXCEPTION";

	public static final String NOT_SUCH_PROPERTIES_EXCEPTION = "NOT_SUCH_PROPERTIES_EXCEPTION";
	// 字段验证失败
	public static final String VALIATION_EXCEPTION = "VALIATION_EXCEPTION";
	// 未知服务
	public static final String UNKNOW_SERVICE_EXCEPTION = "UNKNOW_SERVICE_EXCEPTION";
	// 数据库错误
	public static final String DATABASE_ERROR = "DATABASE_ERROR";
	// 服务器维护中
	public static final String SERVER_MAINTAINING = "SERVER_MAINTAINING";
	// 锁超时
	public static final String LOCK_OVERTIME = "LOCK_OVERTIME";
	// 锁错误
	public static final String LOCK_ERROE = "LOCK_ERROE";
	// 参数错误
	public static final String PARAMETER_ERROR = "PARAMETER_ERROR";
	// 权限不足
	public static final String NO_AUTHORITY = "NO_AUTHORITY";
	// 加密失败
	public static final String ENCRYPT_ERROR = "ENCRYPT_ERROR";
	// 解密失败
	public static final String DECRYPT_ERROR = "DECRYPT_ERROR";
	// 响应失败
	public static final String RESPONSE_ERROR = "RESPONSE_ERROR";
	// 快速响应失败-
	public static final String FAST_RESPONSE_ERROR = "FAST_RESPONSE_ERROR";
	// 未支持
	public static final String UNSUPPORT_ERROR = "UNSUPPORT_ERROR";
	/** 配置错误 */
	public static final String CONFIGURATION_ERROR = "CONFIGURATION_ERROR";
	/** 实例化错误 */
	public static final String INSTANTIATION_ERROR = "INSTANTIATION_ERROR";
}

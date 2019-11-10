package net.jahhan.variable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.common.extension.annotation.ThreadVariable;
import net.jahhan.common.extension.context.Variable;

@Data
@EqualsAndHashCode(callSuper = false)
@ThreadVariable("base")
public class BaseThreadVariable extends Variable {
	/**
	 * 请求id
	 */
	private String requestId = "";
	/**
	 * 请求链id
	 */
	private String chainId = "";
	/**
	 * 行为链id
	 */
	private String behaviorId = "";
	/**
	 * 数据库懒提交
	 */
	private boolean dbLazyCommit = false;
	/**
	 * 全局同步事务起点请求
	 */
	private boolean globalSyncTransactionHold = false;
	/**
	 * 请求解密串签名
	 */
	private String sign;

}

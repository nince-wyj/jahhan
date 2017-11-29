package net.jahhan.context;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseVariable extends Variable {
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

	public static BaseVariable getBaseVariable() {
		VariableContext variableContext = BaseContext.CTX.getVariableContext();
		if (null == variableContext) {
			return null;
		}
		BaseVariable variable = (BaseVariable) variableContext.getVariable("base");
		if (null == variable) {
			variable = new BaseVariable();
			variableContext.putVariable("base", variable);
		}
		return variable;
	}
}

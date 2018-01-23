package net.jahhan.com.alibaba.dubbo.common.compiler;

public interface Compiler {
	Class<?> compile(String code, ClassLoader classLoader);
}

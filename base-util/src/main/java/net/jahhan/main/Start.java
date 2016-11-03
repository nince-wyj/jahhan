package net.jahhan.main;

import net.jahhan.init.InitMethod;

/**
 * appliation main方法入口
 * @author nince
 *
 */
public class Start {
	
	public static void main(String[] args) {
		InitMethod initMethod = new InitMethod(false);
		initMethod.getInjector();
		initMethod.init();
	}
}

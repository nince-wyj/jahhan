package net.jahhan.main;

import com.google.inject.Injector;

import net.jahhan.init.InitMethod;

/**
 * main方法入口
 * @author nince
 *
 */
public class Start {
	
	public static void main(String[] args) {
		InitMethod initMethod = new InitMethod(false);
		Injector injector = initMethod.getInjector();
		initMethod.init();
	}
}

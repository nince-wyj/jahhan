package net.jahhan;

import java.io.IOException;
import java.util.Scanner;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.init.InitMethod;

/**
 * main方法入口
 * 
 * @author nince
 *
 */
@Slf4j(topic = "message.start.info")
public class Start {
	private static volatile boolean running = true;

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		InitMethod initMethod = new InitMethod(true);
		Injector injector = initMethod.getInjector();
		initMethod.init();
		log.debug("start cost:{}ms", System.currentTimeMillis() - startTime);

		synchronized (Start.class) {
			while (running) {
				try {
					System.out.println("press exit to call System.exit() and run the shutdown routine.");
					Scanner scan = new Scanner(System.in);
					String read = scan.nextLine();
					if (read.equals("exit")) {
						running = false;
					}
				} catch (Throwable e) {
				}
			}
			System.exit(0);
		}
	}
}

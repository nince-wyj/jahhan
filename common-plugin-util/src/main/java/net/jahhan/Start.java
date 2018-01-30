package net.jahhan;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.context.Node;
import net.jahhan.init.InitMethod;

/**
 * main方法入口
 * 
 * @author nince
 *
 */
public class Start {
	private static volatile boolean running = true;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Node.getInstance();
		long startTime = System.currentTimeMillis();
		InitMethod initMethod = new InitMethod(true);
		initMethod.getInjector(args);
		initMethod.init();
		Logger log = LoggerFactory.getLogger("message.start.info");
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

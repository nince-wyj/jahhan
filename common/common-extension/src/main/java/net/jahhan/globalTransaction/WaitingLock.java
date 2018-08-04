package net.jahhan.globalTransaction;

import lombok.Data;

@Data
public class WaitingLock {
	private String lock;
	private String chain;
}

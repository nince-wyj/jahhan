package net.jahhan.globalTransaction;

import lombok.Data;

@Data
public class LockStatus {
	private LockThreadStatus status = LockThreadStatus.BLOCK;
	private String key;
}

package net.jahhan.constant.enumeration;

public enum DBConnectionType {
	NONE, READ,
	/** STRONG_WRITE, **/
	WEAK_WRITE, BATCH_WRITE, DISTRIBUTED_WEAK_WRITE
}
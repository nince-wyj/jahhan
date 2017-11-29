package net.jahhan.lock;

public interface DistributedLock extends AutoCloseable {
	public void lock();

	public boolean tryLock();

	public void unlock();

}

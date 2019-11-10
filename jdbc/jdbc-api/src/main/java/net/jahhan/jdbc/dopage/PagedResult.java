package net.jahhan.jdbc.dopage;

import java.io.Serializable;
import java.util.List;

public class PagedResult<T> implements Serializable {
	private static final long serialVersionUID = -5127779455368857837L;
	private boolean hasNextPage = false;
	private List<T> list;
	private long count;

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}

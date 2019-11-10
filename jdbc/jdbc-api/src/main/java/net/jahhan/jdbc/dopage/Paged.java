package net.jahhan.jdbc.dopage;

public class Paged implements Pagable {

	private int pageIndex = 1;

	private int pageSize = 50;

	@Override
	public Integer getBeginDATAIndex() {
		if (pageSize < 1) {
			return null;
		}
		return (pageIndex - 1) * (pageSize - 1);
	}

	@Override
	public void setPageSize(int size) {
		this.pageSize = size;

	}

	@Override
	public void setPageIndex(int index) {
		this.pageIndex = index;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	public Paged(int pageSize) {
		this.pageSize = pageSize;
	}

	public Paged() {
	}

	@Override
	public boolean isNextPage() {
		return false;
	}

	@Override
	public void setNextPage(boolean t) {
	}

}

package net.jahhan.demo.pojo.page;

import java.math.BigDecimal;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import net.jahhan.demo.pojo.TUserDemoInfo;
import net.jahhan.jdbc.dopage.Pagable;
import net.jahhan.jdbc.dopage.Paged;

/**
 * t_user_demo_info:用户信息表
 * 开发人员在此可新增参数
 * @author code-generate-service
 */
public class TUserDemoInfoPage extends TUserDemoInfo implements Pagable{
    private static final long serialVersionUID = -100000L;
    
    private Paged paged=new Paged();
    private String order_by;
    private Boolean nextPage;
    private String group_by;
    
    public String getGroup_by() {
		return group_by;
	}

	public void setGroup_by(String group_by) {
		this.group_by = group_by;
	}
	
	public String getOrder_by() {
		return order_by;
	}

	public void setOrder_by(String order_by) {
		this.order_by = order_by;
	}
	@Override
	public Integer getBeginDATAIndex() {
		return paged.getBeginDATAIndex();
	}
	
	@Override
	public void setPageSize(int size) {
		paged.setPageSize(size);
	}
	@Override
	public void setPageIndex(int index) {
		paged.setPageIndex(index);
	}
	
	@Override
	public int getPageSize() {
		return paged.getPageSize();
	}
	
	@Override
	public void setNextPage(boolean nextPage) {
	    this.nextPage = nextPage;
	}
    
	@Override
	public boolean isNextPage() {
	    return this.nextPage;
	}    
}

package net.jahhan.jdbc.event;

/**
 * 简单的任务类型，用于MQ发送等
 * 
 * @author nince
 */
public class SimpleDBEvent extends DBEvent {

	private static final long serialVersionUID = 8826040294417933465L;

	/**
	 * @param type
	 *            类型
	 * @param id
	 *            涉及的id
	 */
	public SimpleDBEvent(String dataSource, String type, String id) {
		super(new Object(), dataSource, type, EventOperate.SIMPLE, id);
	}

	/**
	 * @param type
	 *            类型
	 * @param id
	 *            涉及的id
	 */
	public SimpleDBEvent(Object obj, String dataSource, String type, String id) {
		super(obj, dataSource, type, EventOperate.SIMPLE, id);
	}

}

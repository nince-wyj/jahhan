package net.jahhan.dblogistics.publish;

import org.apache.commons.lang3.StringUtils;

import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.event.DBEvent;
import net.jahhan.db.event.EventOperate;

public class DBPublisherHandler extends AbstractPublisherHandler {

	private static DBPublisherHandler publisher = new DBPublisherHandler();

	private DBPublisherHandler() {

	}

	public static DBPublisherHandler getPublisher() {
		return publisher;
	}

	public void publishWrite(DBEvent event) {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		invocationContext.setDBEvent(event);
		DBLogisticsConnectionType dbLogisticsConnType = invocationContext.getDBLogisticsConnType();
		if (dbLogisticsConnType.equals(DBLogisticsConnectionType.WRITE)) {
			String id = event.getId();
			String op = event.getOperate();
			if (StringUtils.isEmpty(id)) {
				return;
			}
			if (op.equals(EventOperate.GET) || op.equals(EventOperate.INSERT) || op.equals(EventOperate.UPDATE)) {
				ApplicationContext.CTX.getInvocationContext().addPojo(event.getSource().getClass(), id,
						event.getSource());
			} else if (EventOperate.isModify(op)) {
				ApplicationContext.CTX.getInvocationContext().delPojo(event.getSource().getClass(), id);
			}
		}
	}

}
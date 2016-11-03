package net.jahhan.db.publish;

import java.sql.Connection;

import net.jahhan.context.AppContext;
import net.jahhan.db.PublisherHandler;
import net.jahhan.db.conn.ConnectionWarpper;
import net.jahhan.db.event.DBEvent;

public class DBPublisherHandler extends AbstractPublisherHandler {

	private static PublisherHandler publisher = new DBPublisherHandler();

	private DBPublisherHandler() {

	}

	public static void init() {

	}

	static {
		AppContext.setPublisher(publisher);
	}

	public void publishWrite(Connection conn, DBEvent event) {
		if (ConnectionWarpper.class.isInstance(conn)) {
			ConnectionWarpper w = (ConnectionWarpper) conn;
			w.addEvent(event);
			return;
		}
	}
}
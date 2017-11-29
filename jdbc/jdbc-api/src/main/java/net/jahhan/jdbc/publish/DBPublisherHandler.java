package net.jahhan.jdbc.publish;

import java.sql.Connection;

import javax.inject.Singleton;

import net.jahhan.jdbc.conn.ConnectionWarpper;
import net.jahhan.jdbc.event.DBEvent;

@Singleton
public class DBPublisherHandler extends AbstractPublisherHandler {

	public void publishWrite(Connection conn, DBEvent event) {
		if (ConnectionWarpper.class.isInstance(conn)) {
			ConnectionWarpper w = (ConnectionWarpper) conn;
			w.addEvent(event);
			return;
		}
	}
}
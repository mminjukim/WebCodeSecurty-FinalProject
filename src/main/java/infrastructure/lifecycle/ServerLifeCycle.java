package main.java.infrastructure.lifecycle;

import main.java.infrastructure.database.DataInitializer;

public class ServerLifeCycle {

	private ServerLifeCycle() {

	}

	public static final void start() {
		DataInitializer.createTables();
		DataInitializer.initializeRoles();
	}

	public static final void stop() {

	}
}

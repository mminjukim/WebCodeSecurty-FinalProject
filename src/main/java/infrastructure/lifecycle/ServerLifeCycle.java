package main.java.config.lifecycle;

import main.java.config.database.DataInitializer;

public class ServerLifeCycle {

	private ServerLifeCycle() {

	}

	public static final void start() {
		DataInitializer.createTables();
	}

	public static final void stop() {

	}
}

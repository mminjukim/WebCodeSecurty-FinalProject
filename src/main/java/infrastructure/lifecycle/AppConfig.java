package main.java.infrastructure.lifecycle;

import java.util.Scanner;

import main.java.user.UserService;
import main.java.user.controller.UserController;
import main.java.user.dao.UserDao;

public class AppConfig {

	private final Scanner scanner;
	private final UserDao userDao;
	private final UserService userService;
	private final UserController userController;

	public AppConfig() {
		this.scanner = new Scanner(System.in);
		this.userDao = new UserDao();
		this.userService = new UserService(this.userDao);
		this.userController = new UserController(this.scanner, this.userService);
	}

	public UserController getUserController() {
		return userController;
	}

	public Scanner getScanner() {
		return scanner;
	}
}

package main.java.infrastructure.lifecycle;

import java.util.Scanner;

import main.java.document.controller.DocController;
import main.java.document.dao.DocumentDao;
import main.java.document.dao.WhitelistDao;
import main.java.document.service.DocEncryptService;
import main.java.document.service.DocService;
import main.java.document.service.DocSignatureService;
import main.java.document.service.EnvelopeService;
import main.java.user.UserService;
import main.java.user.controller.UserController;
import main.java.user.dao.UserDao;

public class AppConfig {

	private final Scanner scanner;

	// DAO
	private final UserDao userDao;
	private final DocumentDao documentDao;
	private WhitelistDao whitelistDao;

	// Service
	private final UserService userService;
	private final DocService docService;
	private final DocEncryptService docEncryptService;
	private final DocSignatureService docSignatureService;
	private final EnvelopeService envelopeService;

	// Controller
	private final UserController userController;
	private final DocController docController;

	public AppConfig() {
		this.scanner = new Scanner(System.in);

		// DAO 생성
		this.userDao = new UserDao();
		this.documentDao = new DocumentDao();
		this.whitelistDao = new WhitelistDao();

		// 비즈니스 로직 Service 생성
		this.userService = new UserService(this.userDao);
		this.docEncryptService = new DocEncryptService();
		this.docSignatureService = new DocSignatureService();
		this.envelopeService = new EnvelopeService();
		this.docService = new DocService(this.documentDao, this.userDao, this.whitelistDao, 
				this.docEncryptService, this.docSignatureService, this.envelopeService);

		// Controller 생성
		this.userController = new UserController(this.scanner, this.userService);
		this.docController = new DocController(this.scanner, this.docService);
	}

	public UserController getUserController() {
		return userController;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public DocController getDocController() {
		return docController;
	}

	public DocService getDocService() {
		return docService;
	}
}

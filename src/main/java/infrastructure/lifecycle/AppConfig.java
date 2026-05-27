package main.java.infrastructure.lifecycle;

import java.io.Console;
import java.util.Scanner;

import main.java.document.controller.DocController;
import main.java.document.dao.DocumentDao;
import main.java.document.dao.WhitelistDao;
import main.java.document.service.DocDecryptService;
import main.java.document.service.DocEncryptService;
import main.java.document.service.DocService;
import main.java.document.service.DocSignatureService;
import main.java.document.service.EnvelopeService;
import main.java.log.controller.ReadLogController;
import main.java.log.dao.ReadLogDao;
import main.java.log.service.ReadLogService;
import main.java.user.UserService;
import main.java.user.controller.UserController;
import main.java.user.dao.UserDao;

public class AppConfig {

	private final Scanner scanner;
	private final Console console;

	// DAO
	private final UserDao userDao;
	private final DocumentDao documentDao;
	private WhitelistDao whitelistDao;
	private ReadLogDao readLogDao;
	
	// Service
	private final UserService userService;
	private final DocService docService;
	private final DocEncryptService docEncryptService;
	private final DocDecryptService docDecryptService;
	private final DocSignatureService docSignatureService;
	private final EnvelopeService envelopeService;
	private final ReadLogService readLogService;
	
	// Controller
	private final UserController userController;
	private final DocController docController;
	private final ReadLogController readLogController;

	public AppConfig() {
		this.scanner = new Scanner(System.in);
		this.console = System.console();

		// DAO 생성
		this.userDao = new UserDao();
		this.documentDao = new DocumentDao();
		this.whitelistDao = new WhitelistDao();
		this.readLogDao = new ReadLogDao();
		
		// 비즈니스 로직 Service 생성
		this.userService = new UserService(this.userDao);
		this.docEncryptService = new DocEncryptService();
		this.docDecryptService = new DocDecryptService();
		this.docSignatureService = new DocSignatureService();
		this.envelopeService = new EnvelopeService();
		this.readLogService = new ReadLogService(this.readLogDao, this.documentDao, this.userDao);
		this.docService = new DocService(this.documentDao, this.userDao, this.whitelistDao, 
				this.docEncryptService, this.docDecryptService, this.docSignatureService, this.envelopeService, this.readLogService);
		
		// Controller 생성
		this.userController = new UserController(this.scanner, this.console, this.userService);
		this.docController = new DocController(this.scanner, this.docService);
		this.readLogController = new ReadLogController(this.scanner, this.readLogService, this.docService);
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
	
	public ReadLogController getReadLogController() {
		return readLogController;
	}
}

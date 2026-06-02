package main.application.lifecycle;

import java.io.Console;
import java.util.Scanner;

import main.application.initializer.AdminInitializer;
import main.application.session.Session;
import main.document.controller.DocController;
import main.document.dao.DocumentDao;
import main.document.dao.WhitelistDao;
import main.document.service.DocDecryptService;
import main.document.service.DocEncryptService;
import main.document.service.DocPermissionService;
import main.document.service.DocReadService;
import main.document.service.DocService;
import main.document.service.DocSignatureService;
import main.document.service.DocUploadService;
import main.document.service.EnvelopeService;
import main.log.controller.ReadLogController;
import main.log.dao.ReadLogDao;
import main.log.service.ReadLogFormatter;
import main.log.service.ReadLogIntegrityService;
import main.log.service.ReadLogPermissionService;
import main.log.service.ReadLogService;
import main.user.controller.UserController;
import main.user.dao.RoleDao;
import main.user.dao.UserDao;
import main.user.service.UserService;

public class AppConfig {

	private final Scanner scanner;
	private final Console console;
	private final Session session;
	private final AdminInitializer adminInitializer;

	// DAO
	private final UserDao userDao;
	private final RoleDao roleDao;
	private final DocumentDao documentDao;
	private final WhitelistDao whitelistDao;
	private final ReadLogDao readLogDao;
	
	// Service
	private final UserService userService;

	private final DocService docService;
	private final DocPermissionService docPermissionService;
	private final DocUploadService docUploadService;
	private final DocReadService docReadService;
	private final DocEncryptService docEncryptService;
	private final DocDecryptService docDecryptService;
	private final DocSignatureService docSignatureService;
	private final EnvelopeService envelopeService;

	private final ReadLogService readLogService;
	private final ReadLogPermissionService readLogPermissionService;
	private final ReadLogIntegrityService readLogIntegrityService;
	private final ReadLogFormatter readLogFormatter;
	
	// Controller
	private final UserController userController;
	private final DocController docController;
	private final ReadLogController readLogController;

	public AppConfig() {
		this.scanner = new Scanner(System.in);
		this.console = System.console();
		this.session = new Session();

		// DAO 생성
		this.userDao = new UserDao();
		this.roleDao = new RoleDao();
		this.documentDao = new DocumentDao();
		this.whitelistDao = new WhitelistDao();
		this.readLogDao = new ReadLogDao();
		
		// 비즈니스 로직 Service 생성
		this.userService = new UserService(this.userDao, this.roleDao);
		
		this.readLogPermissionService = new ReadLogPermissionService(this.session, this.documentDao, this.userService);
	  	this.readLogIntegrityService = new ReadLogIntegrityService(this.userDao);
	  	this.readLogFormatter = new ReadLogFormatter(this.userDao);
	  	this.readLogService = new ReadLogService(
	  			this.session,
	  			this.readLogDao,
	  			this.userDao,
	  			this.userService,
	  			this.readLogPermissionService,
	  			this.readLogIntegrityService,
	  			this.readLogFormatter
	  	);

		this.docEncryptService = new DocEncryptService();
		this.docDecryptService = new DocDecryptService();
		this.docSignatureService = new DocSignatureService();
		this.envelopeService = new EnvelopeService();
	  	this.docPermissionService = new DocPermissionService(this.whitelistDao, this.roleDao);
	  	this.docUploadService = new DocUploadService(
	  			this.session, this.documentDao, this.userDao, this.roleDao, this.whitelistDao, 
	  			this.docEncryptService, this.docSignatureService, this.envelopeService
	  	);
	  	this.docReadService = new DocReadService(
	  			this.session, this.documentDao, this.userDao, this.roleDao,
	  			this.docPermissionService, this.docDecryptService,
	  			this.docSignatureService, this.envelopeService, this.readLogService
	  	);
	  	this.docService = new DocService(this.docUploadService, this.docReadService, this.documentDao);

	  	this.adminInitializer = new AdminInitializer();
	  	this.userController = new UserController(this.scanner, this.console, this.userService, this.session);
	  	this.docController = new DocController(this.scanner, this.docService);
	  	this.readLogController = new ReadLogController(this.scanner, this.readLogService, this.docService);
	}

	public UserController getUserController() {
		return userController;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public Session getSession() {
		return session;
	}

	public DocController getDocController() {
		return docController;
	}
	
	public ReadLogController getReadLogController() {
		return readLogController;
	}

	public UserService getUserService() {
		return userService;
	}

	public AdminInitializer getAdminInitializer() {
		return adminInitializer;
	}
}

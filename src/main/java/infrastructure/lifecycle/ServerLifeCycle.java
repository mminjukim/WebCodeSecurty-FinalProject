package main.java.infrastructure.lifecycle;

import main.java.document.service.DocService;
import main.java.infrastructure.database.DataInitializer;
import main.java.infrastructure.key.KeyFileService;

public class ServerLifeCycle {

	private ServerLifeCycle() {

	}

	public static final void start() {
//		DataInitializer.dropAllTables(); // 필요에 따라 주석 처리

		DataInitializer.createTables();
		KeyFileService.makeKeyDirs();
		DataInitializer.initializeRoles();
		DocService.makeDocumentDir();

		printWelcome();
	}

	public static final void stop() {

	}

	private static void printWelcome() {
		String str = """

				************************************************************************
				*                                                                      *
				*    ██████╗  ██████╗  ██████╗                                         *
				*    ██╔══██╗██╔═══██╗██╔════╝                                         *
				*    ██║  ██║██║   ██║██║                                              *
				*    ██║  ██║██║   ██║██║                                              *
				*    ██████╔╝╚██████╔╝╚██████╗██╗                                      *
				*    ╚═════╝  ╚═════╝  ╚═════╝╚═╝                                      *
				*                                                                      *
				*    ███╗   ███╗ █████╗ ███╗   ██╗ █████╗  ██████╗ ███████╗            *
				*    ████╗ ████║██╔══██╗████╗  ██║██╔══██╗██╔════╝ ██╔════╝            *
				*    ██╔████╔██║███████║██╔██╗ ██║███████║██║  ███╗█████╗              *
				*    ██║╚██╔╝██║██╔══██║██║╚██╗██║██╔══██║██║   ██║██╔══╝              *
				*    ██║ ╚═╝ ██║██║  ██║██║ ╚████║██║  ██║╚██████╔╝███████╗            *
				*    ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝            *
				*                                                                      *
				*    ███████╗██╗   ██╗███████╗████████╗███████╗███╗   ███╗             *
				*    ██╔════╝╚██╗ ██╔╝██╔════╝╚══██╔══╝██╔════╝████╗ ████║             *
				*    ███████╗ ╚████╔╝ ███████╗   ██║   █████╗  ██╔████╔██║             *
				*    ╚════██║  ╚██╔╝  ╚════██║   ██║   ██╔══╝  ██║╚██╔╝██║             *
				*    ███████║   ██║   ███████║   ██║   ███████╗██║ ╚═╝ ██║             *
				*    ╚══════╝   ╚═╝   ╚══════╝   ╚═╝   ╚══════╝╚═╝     ╚═╝             *
				*                                                                      *
				************************************************************************
				*                                                                      *
				*   Document Management System                                         *
				*   전자봉투를 이용한 기업 문서 관리 시스템 프로토타입                 *
				*                                                                      *
				*                                                                      *
				************************************************************************


				""";
		System.out.println(str);
	}
}

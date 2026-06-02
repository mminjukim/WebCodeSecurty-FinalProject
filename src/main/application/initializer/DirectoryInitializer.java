package main.application.initializer;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import main.document.service.DocService.FileType;

public class DirectoryInitializer {

	/**
	 * 키 디렉토리 생성하기
	 */
	public static void makeKeyDir() {
		List<String> keyDirs = List.of(".keys/user/private", ".keys/user/public", ".keys/role/public",
				".keys/role/private");
		for (String dirPath : keyDirs) {
			Path path = Paths.get(dirPath);
			try {
				Files.createDirectories(path);
			} catch (FileAlreadyExistsException e) {
				continue;
			} catch (IOException e) {
				System.out.println("[오류] 초기 키 디렉토리 구성에 실패했습니다:" + path);
				System.exit(1);
			}
		}
	}

	/**
	 * 문서 관련 파일들을 저장할 경로를 생성
	 */
	public static void makeDocumentDir() {
		for (FileType filetype : FileType.values()) {
			Path path = Paths.get(".document/", filetype.getDirName());
			try {
				Files.createDirectories(path);
			} catch (FileAlreadyExistsException e) {
				continue;
			} catch (IOException e) {
				System.out.println("[오류] 초기 문서 디렉토리 구성에 실패했습니다:" + path);
				System.exit(1);
			}
		}
	}
}

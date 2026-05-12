package main.java.config.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	
	// 간단하게 각자 로컬에서 연결해서 개발 
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/docsystem_db?serverTimezone=Asia/Seoul";
    private static final String USER = "docsystem_user";
    private static final String PASS = "1234";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("DB 드라이버 로딩 실패");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("DB 연결 생성 실패");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

}

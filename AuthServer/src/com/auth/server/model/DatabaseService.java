package com.auth.server.model;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.auth.server.mail.MailSender;

import javafx.scene.control.TextArea;

public class DatabaseService {
	
	public Connection con;
	TextArea databaseLog;
	
	public DatabaseService(TextArea databaseLog) throws ClassNotFoundException, SQLException, IOException{
		/*this.databaseLog = databaseLog;
		Class.forName("org.sqlite.JDBC");
		
		con = DriverManager.getConnection("jdbc:sqlite:test1.db");
		Statement statement = con.createStatement();
		statement.execute("CREATE TABLE if not exists contractor( id INTEGER PRIMARY KEY, name VARCHAR(255))");
		statement.execute("CREATE TABLE if not exists usr( id INTEGER PRIMARY KEY, name VARCHAR(255), password VARCHAR(255))");
		statement.execute("CREATE TABLE if not exists key( id INTEGER PRIMARY KEY, time DATETIME, hash VARCHAR(255))");
		long start = System.currentTimeMillis();
		PreparedStatement prepStmt = con.prepareStatement("INSERT INTO contractor(name) values (?);");
		for(int i = 1; i <= 100; i++){
			prepStmt.setString(1, "someName" + i);
			prepStmt.executeUpdate();
		}
		databaseLog.setText("");
		databaseLog.appendText("Time1: " + (System.currentTimeMillis() - start) + "\n");
		con.close();*/
		
		/*Connection conn2 = DriverManager.getConnection("jdbc:sqlite:test2.db");
        conn2.setAutoCommit(false);
        Statement stat2 = conn2.createStatement();
        stat2.execute("CREATE TABLE if not exists contractor ( id INTEGER PRIMARY KEY, name VARCHAR(255))");
 
        start = System.currentTimeMillis();
        PreparedStatement prepStmt2 = conn2
                .prepareStatement("INSERT INTO contractor(name) values (?);");
        for (int i = 1; i <= 5; i++) {
            prepStmt2.setString(1, "someName" + i);
            prepStmt2.addBatch();
        }
        
        prepStmt2.setString(1, "KWK SP Z.O.O.");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "MAKLOWICZ PHU");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "ZBYS SA");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "LOWY WIES");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "EKSA SA");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "OLO Z.O.O.");
        prepStmt2.addBatch();
        prepStmt2.setString(1, "MONIKA SA");
        prepStmt2.addBatch();
        
        prepStmt2.executeBatch();
        conn2.commit();
        databaseLog.appendText("Time2: " + (System.currentTimeMillis() - start) + "\n");
        conn2.close();*/
        
        /*UUID lol = UUID.randomUUID();
        String hash = MD5("userReg201510251" + lol.toString());
        databaseLog.appendText("Created HASH: " + hash + " " + "\n");*/
        /*MailSender.SendEmail(hash);*/
	}
	
	public Connection getConnection(){
		return con;
	}
	
}

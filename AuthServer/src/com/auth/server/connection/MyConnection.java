package com.auth.server.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.auth.server.main.MainController;
import com.auth.server.model.DatabaseService;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MyConnection extends Thread {

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;
	
	TextField statusBox;
	TextArea databaseLog;
	
	public String uname, password;
	
	DatabaseService  database;
	
	MainController main;
	
	public MyConnection(TextField statusBox, TextArea databaseLog, DatabaseService database, MainController main){
		this.statusBox = statusBox;
		this.databaseLog = databaseLog;
		this.database = database;
		this.main = main;
	}
	
	@Override
	public void run() {
		System.out.println("MyThread running");

		int portNumber = 4515;
		try {
			serverSocket = new ServerSocket(portNumber);
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port " + portNumber
					+ " or listening for a connection");
			System.out.println(e.getMessage());
		}

		String inputLine, outputLine = null;

		// Initiate conversation with client
		KnockKnockProtocol kkp = new KnockKnockProtocol(databaseLog, this);
		try {
			outputLine = kkp.processInput(null);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		out.println(outputLine);

		try {
			while (true) {
				while ((inputLine = in.readLine()) != null) {
					System.out.println("\"" + inputLine + "\"");
					if (inputLine.equals("|3BYE")) {
						statusBox.setText("CLOSED CONNECTION");
						out.println("|3BYE");
						System.out.println("Closed");
						kkp.state = 0;
						kkp.encryptionCounter = 0;
						break;
					}
					outputLine = kkp.processInput(inputLine);
					if(kkp.getEncryptionCounter() == 4){
						kkp.setEncryptionCounter(5);
					}else if(kkp.getEncryptionCounter() > 4){
						outputLine = kkp.encryptMessage(outputLine);
					}
					System.out.println("OUT: " + outputLine );
					out.println(outputLine);
				}

				kkp.state = 0;
				out.close();
				in.close();
				clientSocket.close();
				clientSocket = serverSocket.accept();
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputLine = kkp.processInput(null);
				out.println(outputLine);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Closed thread");
	}

	public void insertKey(String key) throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		database.con = DriverManager.getConnection("jdbc:sqlite:test1.sqlite");
		long start = System.currentTimeMillis();
		PreparedStatement prepStmt = database.con.prepareStatement("INSERT INTO key(time,hash) values (datetime('now'),?);");
		prepStmt.setString(1, key);
		prepStmt.executeUpdate();
		databaseLog.appendText("Added new key: " + key + " IN TIME: " + (System.currentTimeMillis() - start) + "\n");
		database.con.close();
	}
	public void insertUsr(String uname, String password) throws SQLException, ClassNotFoundException{
		Class.forName("org.sqlite.JDBC");
		database.con = DriverManager.getConnection("jdbc:sqlite:test1.sqlite");
		long start = System.currentTimeMillis();
		PreparedStatement prepStmt = database.con.prepareStatement("INSERT INTO usr(name,password) values (?,?);");
		prepStmt.setString(1, uname);
		prepStmt.setString(2, password);
		prepStmt.executeUpdate();
		databaseLog.appendText("Added new usr: " + uname + " IN TIME: " + (System.currentTimeMillis() - start) + "\n");
		database.con.close();
	}
	public boolean findKey(String key){
		try {
			Class.forName("org.sqlite.JDBC");
			database.con = DriverManager.getConnection("jdbc:sqlite:test1.sqlite");
			long start = System.currentTimeMillis();
			Statement prepStmt = database.con.createStatement();
			List<String> keys = new LinkedList<String>();
	        ResultSet result = prepStmt.executeQuery("SELECT hash FROM key where (strftime('%s',datetime('now')) - strftime('%s',time)) < 600");
	        while(result.next()) {
	        	System.out.println("KEY IN DATABASE: '" + result.getString(1) +"'");
	            if(result.getString(1).equals(key))
	            {
	            	database.con.close();
	            	return true;
	            }
	        }
	        database.con.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
		return false;
	}
	
	public List<String> findAllCon(){
		try {
			Class.forName("org.sqlite.JDBC");
			database.con = DriverManager.getConnection("jdbc:sqlite:test1.sqlite");
			long start = System.currentTimeMillis();
			Statement prepStmt = database.con.createStatement();
			List<String> keys = new LinkedList<String>();
	        ResultSet result = prepStmt.executeQuery("SELECT name FROM contractor");
	        while(result.next()) {
	        	keys.add(result.getString(1));
	        }
	        database.con.close();
	        return keys;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
		return null;
	}
	public boolean findUser(String user, String password){
		try {
			Class.forName("org.sqlite.JDBC");
			database.con = DriverManager.getConnection("jdbc:sqlite:test1.sqlite");
			Statement prepStmt = database.con.createStatement();
	        ResultSet result = prepStmt.executeQuery("SELECT name, password FROM usr");
	        while(result.next()) {
	        	System.out.println("KEY IN DATABASE: '" + result.getString(1) +"'");
	            if(result.getString(1).equals(user) && result.getString(2).equals(password))
	            {
	            	database.con.close();
	            	return true;
	            }
	        }
	        database.con.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
		return false;
	}
}
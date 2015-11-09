package com.auth.server.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.auth.server.connection.MyConnection;
import com.auth.server.model.DatabaseService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable {
	@FXML
	public TextArea databaseLog;
	@FXML
	public Button connectBox;

	@FXML
	public TextField statusBox;

	@FXML
	public TextArea textText;
	
	
	DatabaseService database;

	MyConnection my;

	int countall = 0;

	List<Character> list;

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;
	File file;

	public void setMyConnection(MyConnection myConnection){
		my = myConnection;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	@FXML
	private void connectBoxAction(ActionEvent event) {
		// Button was clicked, do something...
		try {
			database = new DatabaseService(databaseLog);
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		statusBox.setText("STARTING...");
		my = new MyConnection(statusBox , databaseLog, database,this);
		my.start();
	}

	public void closeThread() {
		if(my != null)
			my.interrupt();
	}
}

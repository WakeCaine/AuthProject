package com.auth.client.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.ResourceBundle;

import com.auth.client.connection.MyConnection;
import com.auth.client.connection.Status;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainController implements Initializable {

	/*
	 * Debug text areas
	 */
	@FXML
	public TextArea dataBox;
	@FXML
	public TextArea inputLogBox;
	@FXML
	public TextArea clientLogBox;

	/*
	 * Buttons
	 */
	@FXML
	public Button connectButton;
	@FXML
	public Button loginButton;
	@FXML
	public CheckBox registerCheckBox;

	/*
	 * Login and connect fields
	 */
	@FXML
	public TextField ipBox;
	@FXML
	public TextField loginBox;
	@FXML
	public TextField passBox;
	@FXML
	public TextField emailBox;
	@FXML
	public TextField keyBox;

	int countall = 0;
	
	MyConnection my;
	
	List<Character> list;

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;
	File file;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		dataBox.setText("");
		clientLogBox.setText("");
		inputLogBox.setText("");
		ipBox.setText("192.168.1.");
		loginBox.setText("123");
		passBox.setText("pass");
		emailBox.setText("dako123d@gmail.com");
		
		my = new MyConnection(this);
		my.start();
	}

	@FXML
	private void connectButtonAction(ActionEvent event) {
		my.setConnectTo(true);

	}
	
	@FXML
	private void registerButtonAction(ActionEvent event){
		if (my.getStatus() == Status.CONFIRM)
        {
            String message = "|3KEY" + "|KEY" + keyBox.getText();

            // Sends data to a connected Socket. 
            out.println(message);

            try {
				my.ReceiveDataFromServer();
			} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else if (registerCheckBox.isSelected() == false)
        {
            String message = "|5LOGIN" + "|" + loginBox.getText().length() + "USR" + loginBox.getText() + "|" + passBox.getText().length() + "PASS" + passBox.getText();

            // Sends data to a connected Socket. 
            out.println(message);

            try {
				my.ReceiveDataFromServer();
			} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else
        {
            String message = "|8REGISTER" + "|" + loginBox.getText().length() + "USR" + loginBox.getText() + "|" + passBox.getText().length() + "PASS" + passBox.getText() + "|EMAIL" + emailBox.getText();
            // Sends data to a connected Socket. 
            out.println(message);

            try {
				my.ReceiveDataFromServer();
			} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public void closeThread() {
		my.closeConnection();
		if(my != null)
			my.interrupt();
	}
}

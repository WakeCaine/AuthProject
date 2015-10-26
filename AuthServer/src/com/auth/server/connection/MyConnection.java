package com.auth.server.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.control.TextField;

public class MyConnection extends Thread {

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;
	
	TextField statusBox;
	
	public MyConnection(TextField statusBox){
		this.statusBox = statusBox;
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

		String inputLine, outputLine;

		// Initiate conversation with client
		KnockKnockProtocol kkp = new KnockKnockProtocol();
		outputLine = kkp.processInput(null);
		out.println(outputLine);

		try {
			while (true) {
				while ((inputLine = in.readLine()) != null) {
					System.out.println("\"" + inputLine + "\"");
					if (inputLine.equals("BYE")) {
						statusBox.setText("CLOSED CONNECTION");
						out.println("|3BYE");
						System.out.println("Closed");
						break;
					}
					outputLine = kkp.processInput(inputLine);
					out.println(outputLine);
					if (outputLine.equals("Bye."))
						break;
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Closed thread");
	}
}
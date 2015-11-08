package com.auth.client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.spec.DHPublicKeySpec;

import com.auth.client.main.MainController;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MyConnection extends Thread {

	public BigInteger p, g;
	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;

	MainController mainController;

	TextField statusBox;
	TextArea databaseLog;

	int counterRaw = 0;

	boolean connectTo = false;

	public String uname, password;

	public MyConnection(MainController mainController) {
		this.mainController = mainController;
	}

	public void setConnectTo(boolean connectTo) {
		this.connectTo = connectTo;
	}

	private Status status = Status.IDLE;

	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public Status getAnswer(String message) {
		if (message.charAt(0) != '|') {
			return Status.MALFORMED;
		}

		int count = Character.getNumericValue(message.charAt(1));
		String cutmessage = message.substring(2, count);
		if (cutmessage == "BYE") {
			return Status.BYE;
		} else if (cutmessage == "READY") {
			return Status.CONNECTED;
		} else if (cutmessage == "OKLOG" || cutmessage == "NOLOG") {
			if (cutmessage == "OKLOG")
				return Status.LOGGED;
			else
				return Status.ERROR;
		} else if (cutmessage == "KEY") {
			;
			return Status.CONFIRM;
		} else if (cutmessage == "OKREG" || cutmessage == "NOREG") {
			if (cutmessage == "OKREG")
				return Status.REGISTERED;
			else
				return Status.ERROR;
		} else
			return Status.MALFORMED;
	}

	public void ReceiveDataFromServer() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		String inputLine = null, outputLine;

		inputLine = in.readLine();
		mainController.inputLogBox.appendText("INPUT: \"" + inputLine + "\"\n");
		System.out.println("1");
		System.out.println("1");
		System.out.println("HERE");
		if (counterRaw > 2) {
			analyzeMessage(inputLine);
		} else {
			System.out.println("HERE");
			mainController.clientLogBox.appendText("GOT P\n");
			byte[] msg = null;
			if (counterRaw == 1) {
				/*dh = GenerateParameters();
				G = new BigInteger(dh.G.ToString());
				msg = Encoding.ASCII.GetBytes(dh.G.ToString() + "\n");*/
			} else {
				/*P = new BigInteger(theMessageToReceive);
				dh1 = new DHParameters(P, G);
				AsymmetricCipherKeyPair d1 = GenerateKeys(dh1);

				msg = Encoding.ASCII.GetBytes(d1.Public.ToString() + "\n");*/
			

			// Sends data to a connected Socket.
			/*int bytesSend = out.println(msg);*/
			mainController.clientLogBox.appendText("NOTHING TO SEE HERE YET\n");
			
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

			kpg.initialize(512);
			KeyPair kp1 = kpg.generateKeyPair();
			KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
			DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp1.getPublic(), DHPublicKeySpec.class);
			System.out.println("G: " + kspec.getG());
			System.out.println("P: " + kspec.getP());
			System.out.println("Public key Alice :" + kp1.getPublic());
			
			out.println(kspec.getP());
			counterRaw = +1;
			ReceiveDataFromServer();
			}
		}

	}
	
	public void analyzeMessage(String message)
    {
        Status localStatus = getAnswer(message);

        if (localStatus == Status.CONNECTED && getStatus() == Status.CONNECTED)
        {
        	mainController.clientLogBox.appendText("CONNECTED TO SERVER\n");;
        	mainController.loginBox.setDisable(false);
        	mainController.passBox.setDisable(false);
        	mainController.registerCheckBox.setDisable(false);
        	mainController.emailBox.setDisable(false);
        	mainController.loginButton.setDisable(false);
        	mainController.keyBox.setDisable(false);
        }
        else if (localStatus == Status.CONFIRM)
        {
        	mainController.clientLogBox.appendText("Check email for info about your key to register!\n");
            setStatus(Status.CONFIRM);
        }
        else if (getStatus() == Status.CONFIRM && (localStatus == Status.ERROR || localStatus == Status.REGISTERED))
        {
            if(localStatus == Status.REGISTERED)
            {
            	mainController.clientLogBox.appendText("REGISTRATION SUCCESSFUL!\n");
            }
            else
            {
            	mainController.clientLogBox.appendText("SOMETHING WENT WRONG!\n");
            }
        }
        else if (localStatus == Status.BYE && getStatus() == Status.CONNECTED)
        {
        	System.out.println("Closed");
			out.close();
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
			mainController.clientLogBox.appendText("Disconnected from server!\n");
			mainController.loginBox.setDisable(true);
        	mainController.passBox.setDisable(true);
        	mainController.registerCheckBox.setDisable(true);
        	mainController.emailBox.setDisable(true);
        	mainController.loginButton.setDisable(true);
        	mainController.keyBox.setDisable(true);
        }
        else if (localStatus == Status.MALFORMED)
        {
        	mainController.clientLogBox.appendText("MALFORMED CONNECTION OR INTERNAL SERVER ERROR\nTURN OFF APPLICATION\n");
        }
    }

	@Override
	public void run() {
		mainController.clientLogBox.appendText("MyThread running...\n");

		int portNumber = 4510;
		try {
			System.out.println("Hello");
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Hello");
			int loop = 0;
			while (loop == 0) {
				if (connectTo != false) {
					clientSocket = new Socket(mainController.ipBox.getText(), 4515);
					loop = -1;
				} else {
					try {
						sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			System.out.println("Hello");
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			System.out.println("Hello");
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("Hello");
			mainController.clientLogBox.appendText("Started listening on port: " + portNumber + "\n");
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
		System.out.println("BEFORE");
		try {
			ReceiveDataFromServer();
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Closed thread");
	}
	
	public void closeConnection(){
		if(clientSocket != null){
			if(clientSocket.isClosed() != true){
				out.println("|3BYE");
				System.out.println("Closed");
				out.close();
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
package com.auth.client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.auth.client.main.MainController;
import com.auth.client.security.SecureCon;

public class MyConnection extends Thread {

	public BigInteger p, g;
	/*
	 * Socket part
	 */
	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out = null;
	BufferedReader in = null;

	MainController mainController;

	/*
	 * Numbers
	 */

	int counterRaw = 0;
	boolean connectTo = false;
	public String uname, password;
	private Status status = Status.IDLE;

	boolean loopin = true;
	/*
	 * Encryption variables
	 */
	KeyPairGenerator keyPairGenerator;
	KeyPair keyPair;
	KeyFactory keyFactory;
	DHPublicKeySpec dhPublicKeySpec;
	PublicKey serverPublicKey;
	PublicKey clientPublicKey;
	PrivateKey clientPrivateKey;
	SecretKey key1;
	byte[] secureRandom = new byte[16];
	Cipher c;
	byte[] iv;
	IvParameterSpec ips;

	SecureCon secureConnection;

	public MyConnection(MainController mainController) {
		this.mainController = mainController;
	}

	public void setConnectTo(boolean connectTo) {
		this.connectTo = connectTo;
	}

	/*
	 * Getters and Setters
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	/*
	 * Main functions
	 */

	private static final int AES_KEY_SIZE = 128;

	public static SecretKey agreeSecretKey(PrivateKey prk_self, PublicKey pbk_peer, boolean lastPhase)
			throws Exception {
		// instantiates and inits a KeyAgreement
		KeyAgreement ka = KeyAgreement.getInstance("DH");
		ka.init(prk_self);
		// Computes the KeyAgreement
		ka.doPhase(pbk_peer, lastPhase);
		// Generates the shared secret
		byte[] secret = ka.generateSecret();

		// === Generates an AES key ===

		// you should really use a Key Derivation Function instead, but this is
		// rather safe

		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] bkey = Arrays.copyOf(sha256.digest(secret), AES_KEY_SIZE / Byte.SIZE);
		System.out.println("SIZE: " + bkey.length);
		System.out.println(Cipher.getMaxAllowedKeyLength("AES"));
		byte[] data = new byte[32];
		System.arraycopy(secret, 0, data, 0, 16);

		System.out.println("Bkey: " + bkey);
		SecretKey desSpec = new SecretKeySpec(bkey, "AES");
		return desSpec;
	}

	public Status getAnswer(String message) {
		System.out.println("MESSAGE: " + message);
		if (message.charAt(0) != '|') {
			return Status.MALFORMED;
		}

		int count = Character.getNumericValue(message.charAt(1));
		String cutmessage = message.substring(2, 2 + count);
		System.out.println("CUTMESSAGE: " + cutmessage);
		if (cutmessage.equals("BYE")) {
			return Status.BYE;
		} else if (cutmessage.equals("READY")) {
			return Status.CONNECTED;
		} else if (cutmessage.equals("OKLOG") || cutmessage.equals("NOLOG")) {
			if (cutmessage.equals("OKLOG"))
				return Status.LOGGED;
			else
				return Status.ERROR;
		} else if (cutmessage.equals("KEY")) {
			;
			return Status.CONFIRM;
		} else if (cutmessage.equals("OKREG") || cutmessage.equals("NOREG")) {
			if (cutmessage.equals("OKREG"))
				return Status.REGISTERED;
			else
				return Status.ERROR;
		} else if(cutmessage.equals("DATA") || cutmessage.equals("NODATA")){
			if(cutmessage.equals("DATA"))
				return Status.GOTDATA;
			else
				return Status.ERROR;
		}else
			return Status.MALFORMED;
	}

	public void ReceiveDataFromServer() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		String inputLine = null;
		inputLine = in.readLine();
		mainController.inputLogBox.appendText("INPUT: \"" + inputLine + "\"\n");
		analyzeMessage(inputLine);
	}
	
	public void SendAndReceiveDataFromServer(String input) throws IOException {
		out.println(input);
		String inputLine = null;
		inputLine = in.readLine();
		mainController.inputLogBox.appendText("INPUT: \"" + inputLine + "\"\n");
		analyzeMessage(inputLine);
	}

	public void analyzeMessage(String message) {
		Status localStatus = getAnswer(message);

		if (localStatus == Status.CONNECTED && getStatus() == Status.CONNECTED) {
			mainController.clientLogBox.appendText("CONNECTED TO SERVER\n");
			mainController.loginBox.setDisable(false);
			mainController.passBox.setDisable(false);
			mainController.registerCheckBox.setDisable(false);
			mainController.emailBox.setDisable(false);
			mainController.loginButton.setDisable(false);
			mainController.keyBox.setDisable(false);
			
		} else if (localStatus == Status.CONFIRM) {
			mainController.clientLogBox.appendText("Check email for info about your key to register!\n");
			setStatus(Status.CONFIRM);
		} else if (getStatus() == Status.CONFIRM && (localStatus == Status.ERROR || localStatus == Status.REGISTERED)) {
			if (localStatus == Status.REGISTERED) {
				mainController.clientLogBox.appendText("REGISTRATION SUCCESSFUL!\n");
				setStatus(Status.REGISTERED);
			} else {
				mainController.clientLogBox.appendText("SOMETHING WENT WRONG!\n");
			}
		} else if (localStatus == Status.GOTDATA){
			String realmessage = message.substring(7, message.length());
			int length = realmessage.length();
			String[] outputMessage1 = realmessage.split(Pattern.quote("|"));
			String outputMessage = "";
			for( int i = 0; i < outputMessage1.length; i++){
				outputMessage += outputMessage1[i] + "\n";
			}
			mainController.dataBox.appendText(outputMessage);
		} else if (localStatus == Status.BYE && getStatus() == Status.CONNECTED) {
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
		} else if (localStatus == Status.LOGGED){
			System.out.println("LOGGED");
			setStatus(Status.LOGGED);
			mainController.clientLogBox.appendText("Logged usr: " + mainController.loginBox.getText() +"!\n");
			mainController.loginBox.setDisable(true);
			mainController.passBox.setDisable(true);
			mainController.registerCheckBox.setDisable(true);
			mainController.emailBox.setDisable(true);
			mainController.loginButton.setDisable(true);
			mainController.keyBox.setDisable(true);
		} else if (localStatus == Status.MALFORMED) {
			mainController.clientLogBox
					.appendText("MALFORMED CONNECTION OR INTERNAL SERVER ERROR\nTURN OFF APPLICATION\n");
		}
		
	}

	@Override
	public void run() {
		mainController.clientLogBox.appendText("MyThread running...\n");
		secureConnection = new SecureCon();

		int portNumber = 4510;
		try {
			serverSocket = new ServerSocket(portNumber);
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
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			mainController.clientLogBox.appendText("Started listening on port: " + portNumber + "\n");
			setStatus(Status.CONNECTED);
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
		System.out.println("BEFORE");

		try {
			while (true) {
				boolean loopmein = secureConnection.EncryptHandShake(mainController, in, out);
				if (loopmein == false)
					break;
			}
			ReceiveDataFromServer();
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Closed thread");
	}

	public void closeConnection() {
		if (clientSocket != null) {
			if (clientSocket.isClosed() != true) {
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
package com.auth.server.main;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

public class MainController implements Initializable {

	@FXML
	private Button connectBox;

	@FXML
	private TextField statusBox;

	@FXML
	private TextArea textText;
	
	MyThread my;

	int countall = 0;

	List<Character> list;
	
	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out=null;
	BufferedReader in=null;

	private Desktop desktop = Desktop.getDesktop();
	final FileChooser fileChooser = new FileChooser();
	File file;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	private void openFile(File file) {
		try {
			desktop.open(file);
		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void connectBoxAction(ActionEvent event) {
		// Button was clicked, do something...
		statusBox.setText("STARTING...");
		my = new MyThread();
		my.start();
	}

	public void closeThread()
	{
		my.interrupt();
	}
	
	@FXML
	private void analyzeButtonAction(ActionEvent event) throws IOException {
		
	}

	private int searchList(List<Character> list, char letter) {
		
		return -1;
	}
	
	public class MyThread extends Thread {

		@Override
	    public void run(){
	       System.out.println("MyThread running");
	       
	       int portNumber = 4515;
	        try { 
	            serverSocket = new ServerSocket(portNumber);
	            clientSocket = serverSocket.accept();
	            out = new PrintWriter(clientSocket.getOutputStream(), true);
	            in = new BufferedReader(
	                new InputStreamReader(clientSocket.getInputStream()));
	        }catch (IOException e) {
	            System.out.println("Exception caught when trying to listen on port "
	                + portNumber + " or listening for a connection");
	            System.out.println(e.getMessage());
	        }
	        
	        String inputLine, outputLine;
	        
	        // Initiate conversation with client
	        KnockKnockProtocol kkp = new KnockKnockProtocol();
	        outputLine = kkp.processInput(null);
	        out.println(outputLine);

	        try {
	        	while(true){
					while ((inputLine = in.readLine()) != null) {
						System.out.println("\""+inputLine+"\"");
						if(inputLine.equals("Bye") ){
							statusBox.setText("CLOSED CONNECTION");
							out.println("Bye");
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
		            in = new BufferedReader(
		                new InputStreamReader(clientSocket.getInputStream()));
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
	
	public class KnockKnockProtocol {
	    private static final int WAITING = 0;
	    private static final int SENTKNOCKKNOCK = 1;
	    private static final int SENTCLUE = 2;
	    private static final int ANOTHER = 3;
	 
	    private static final int NUMJOKES = 5;
	 
	    public int state = WAITING;
	    private int currentJoke = 0;
	 
	    private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
	    private String[] answers = { "Turnip the heat, it's cold in here!",
	                                 "I didn't know you could yodel!",
	                                 "Bless you!",
	                                 "Is there an owl in here?",
	                                 "Is there an echo in here?" };
	 
	    public String processInput(String theInput) {
	        String theOutput = null;
	 
	        if (state == WAITING) {
	            theOutput = "1";
	            state = SENTKNOCKKNOCK;
	        } else if (state == SENTKNOCKKNOCK) {
	            if (theInput.equalsIgnoreCase("Who's there?")) {
	                theOutput = clues[currentJoke];
	                state = SENTCLUE;
	            } else {
	                theOutput = "You're supposed to say \"Who's there?\"! " +
	                "Try again. Knock! Knock!";
	            }
	        } else if (state == SENTCLUE) {
	            if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
	                theOutput = answers[currentJoke] + " Want another? (y/n)";
	                state = ANOTHER;
	            } else {
	                theOutput = "You're supposed to say \"" + 
	                clues[currentJoke] + 
	                " who?\"" + 
	                "! Try again. Knock! Knock!";
	                state = SENTKNOCKKNOCK;
	            }
	        } else if (state == ANOTHER) {
	            if (theInput.equalsIgnoreCase("y")) {
	                theOutput = "Knock! Knock!";
	                if (currentJoke == (NUMJOKES - 1))
	                    currentJoke = 0;
	                else
	                    currentJoke++;
	                state = SENTKNOCKKNOCK;
	            } else {
	                theOutput = "Bye.";
	                state = WAITING;
	            }
	        }
	        return theOutput;
	    }
	}
}

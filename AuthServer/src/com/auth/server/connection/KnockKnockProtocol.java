package com.auth.server.connection;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.UUID;

import com.auth.server.mail.MailSender;

import javafx.scene.control.TextArea;

public class KnockKnockProtocol {
	private static final int WAITING = 0;
	private static final int SENTCONFIRMATION = 1;
	private static final int SENTANSWER = 2;
	private static final int SENTKEY = 3;
	private static final int ANOTHER = 4;

	private static final int NUMJOKES = 5;

	public int state = WAITING;
	private int currentJoke = 0;

	MyConnection connection;
	
	private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
	private String[] answers = { "Turnip the heat, it's cold in here!", "I didn't know you could yodel!",
			"Bless you!", "Is there an owl in here?", "Is there an echo in here?" };

	TextArea databaseLog;
	
	public KnockKnockProtocol(TextArea databaseLog, MyConnection connection){
		this.databaseLog = databaseLog;
		this.connection = connection;
	}
	
	public String processInput(String theInput) {
		String theOutput = null;

		if (state == WAITING) {
			theOutput = "|5READY";
			state = SENTCONFIRMATION;
			System.out.println("SET STATE SENT");
			return theOutput;
		} 
		
		int count = Character.getNumericValue(theInput.charAt(1));
		String localInput = theInput.substring(2, 2 + count);
		System.out.println("INPUT: '"+ localInput +"'");
		if (theInput.charAt(0) != '|'){
            return "???";
		} else if (state == SENTCONFIRMATION) {
			System.out.println("IM HERE1");
			if (localInput.equalsIgnoreCase("LOGIN")) {
				System.out.println("IM LOGING");
				theOutput = "|5OKLOG";
				state = SENTANSWER;
			} else if(localInput.equalsIgnoreCase("REGISTER")) {
				System.out.println("IM REGISTERING");
				theOutput = "|3KEY";
				
				connection.uname = getUname(theInput.substring(2 + count + 1, theInput.length() ));
				connection.password = getPass(theInput.substring(2 + count + 1, theInput.length() ));
				
				UUID lol = UUID.randomUUID();
		        String hash = MD5("userReg201510251" + lol.toString());
		        
		        try {
					connection.insertKey(hash);
				} catch (SQLException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        databaseLog.appendText("Created HASH: " + hash + " " + "\n");
		        
		        String email = getEmailM(theInput.substring(2 + count + 1, theInput.length() ));
		        
		        try {
					MailSender.SendEmail(hash, email);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				state = SENTKEY;
			}
		} else if (state == SENTKEY){
			int count1 = Character.getNumericValue(theInput.charAt(1));
			String localInput1 = theInput.substring(5, theInput.length());
			System.out.println("LOOKING FOR A KEY:" + localInput1);
			
			String key = localInput1.substring(4, localInput1.length());
			if (localInput.equalsIgnoreCase("KEY")) {
				System.out.println("KEY: " + key);
				if(connection.findKey(key) == true){
					theOutput = "|5OKREG";
					try {
						connection.insertUsr(connection.uname, connection.password);
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return theOutput;
				} else{
					theOutput = "|5NOREG";
					return theOutput;
				}
			}
			
		} else if (state == SENTANSWER) {
			System.out.println("IM AWAITING ANSWER?");
			if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
				theOutput = answers[currentJoke] + " Want another? (y/n)";
				state = ANOTHER;
			} else {
				theOutput = "You're supposed to say \"" + clues[currentJoke] + " who?\""
						+ "! Try again. Knock! Knock!";
				state = SENTCONFIRMATION;
			}
		} else if (state == ANOTHER) {
			System.out.println("FUCK HAPPEND");
			if (theInput.equalsIgnoreCase("y")) {
				theOutput = "Knock! Knock!";
				if (currentJoke == (NUMJOKES - 1))
					currentJoke = 0;
				else
					currentJoke++;
				state = SENTCONFIRMATION;
			} else {
				theOutput = "Bye.";
				state = WAITING;
			}
		}
		System.out.println("Hell");
		return theOutput;
	}
	
	public String MD5(String md5) {
	   try {
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] array = md.digest(md5.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < array.length; ++i) {
	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	       }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException e) {
	    }
	    return null;
	}
	
	private String getEmailM(String msg){
		int count;
		//System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(5));
		String first = msg.substring(5 + count,msg.length());
		//System.out.println(first);
		count = Character.getNumericValue(first.charAt(7));
		first = first.substring(7 + count, first.length());
		//System.out.println(first);
		return first.substring(6, first.length());
	}
	
	private String getUname(String msg){
		int count;
		System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(0));
		String first = msg.substring(4, 4 + count);
		System.out.println("NAME: " + first);
		return first;
	}
	
	private String getPass(String msg){
		int count;
		System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(0));
		String first = msg.substring(4 + count,msg.length());
		System.out.println(first);
		count = Character.getNumericValue(first.charAt(1));
		first = first.substring(6, 6 + count);
		System.out.println("PASSWORD: " + first);
		return first;
	}
}

package com.auth.client.connection;

import javafx.scene.control.TextArea;

public class KnockKnockProtocol {
	private static final int AUTH = -1;
	private static final int WAITING = 0;
	private static final int SENTCONFIRMATION = 1;
	private static final int SENTANSWER = 2;
	private static final int SENTKEY = 3;
	private static final int ANOTHER = 4;

	private static final int NUMJOKES = 5;

	public int state = AUTH;
	private int count = 0;

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
		
		if(count < 2){
						
		}
		

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
				
				state = SENTKEY;
			}
		} else if (state == SENTKEY){
			
			
		} else if (state == SENTANSWER) {
			
		} else if (state == ANOTHER) {
			
		}
		System.out.println("Hell");
		return theOutput;
	}
}

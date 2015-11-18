package com.auth.server.connection;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.auth.server.mail.MailSender;

import javafx.scene.control.TextArea;

public class KnockKnockProtocol {
	private static final int WAITING = 0;
	private static final int SENTCONFIRMATION = 1;
	private static final int DIFFIEHELLMAN = 2;
	private static final int SENTANSWER = 3;
	private static final int SENTKEY = 4;
	private static final int ANOTHER = 5;
	private static final int SENTLOGCONFIRMATION = 6;

	PublicKey clientPublicKey;
	PublicKey serverPublicKey;
	PrivateKey serverPrivateKey;
	KeyPair keyPair;
	SecretKey key1;
	byte[] iv;

	public int state = WAITING;
	private int currentJoke = 0;
	public BigInteger p, g;

	Cipher c;

	private int encryptionCounter = 0;

	MyConnection connection;

	private String[] clues = { "Turnip", "Little Old Lady", "Atch", "Who", "Who" };
	private String[] answers = { "Turnip the heat, it's cold in here!", "I didn't know you could yodel!", "Bless you!",
			"Is there an owl in here?", "Is there an echo in here?" };

	TextArea databaseLog;

	public KnockKnockProtocol(TextArea databaseLog, MyConnection connection) {
		this.databaseLog = databaseLog;
		this.connection = connection;
	}

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
		System.out.println("BKEY: " + bkey);
		SecretKey desSpec = new SecretKeySpec(bkey, "AES");
		return desSpec;
	}

	public String processInput(String theInput) throws Exception {
		String theOutput = null;

		if (state == WAITING) {
			theOutput = "|5READY";
			state = DIFFIEHELLMAN;
			System.out.println("SET STATE SENT");
			return theOutput;
		}

		System.out.println("INPUT: '" + theInput + "'");
		if (encryptionCounter == 3) {
			System.out.println("HERE 3 ------------------------------------------------------------------------!");
			byte[] stringer = Base64.getDecoder().decode(theInput.getBytes(StandardCharsets.US_ASCII));
			System.out.println(stringer.length);
			System.out.println(theInput);

			// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob (g^X
			// mod p)^Y mod p

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
			DHParameterSpec param = new DHParameterSpec(p, g);
			kpg.initialize(param);
			keyPair = kpg.generateKeyPair();

			SecretKey key1 = agreeSecretKey(serverPrivateKey, clientPublicKey, true);
			System.out.println("SECRET KEY " + key1);
			System.out.println("MY PRIVATE KEY: " + serverPrivateKey);
			System.out.println("MY PUBLIC KEY: " + serverPublicKey);
			System.out.println("CLIENT PUBLIC KEY: " + clientPublicKey);
			System.out.println("G: " + g);
			System.out.println("P: " + p);
			// Instantiate the Cipher of algorithm "DES"
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			// Init the cipher with Alice's key1
			System.out.println(key1.getEncoded().length);
			// inits the encryptionMode
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			c.init(Cipher.DECRYPT_MODE, key1, ivSpec);
			// Decrypts and print
			byte[] pad = c.doFinal(stringer);
			encryptionCounter += 1;
			System.out.println("Decrypted: " + new String(pad, "ascii"));
			System.out.println("Done");
			state = SENTCONFIRMATION;
			return "|5READY";
		} else if (encryptionCounter == 2) {
			iv = theInput.getBytes();
			System.out.println("IV:" + iv);
			System.out.println("IV String: " + theInput);
			encryptionCounter += 1;
			return "|7OKREADY";
		} else if (encryptionCounter == 0) {
			System.out.println("HERE 1 ------------------------------------------------------------------------!");
			p = new BigInteger(theInput);

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
			kpg.initialize(512);
			keyPair = kpg.generateKeyPair();
			KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
			DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(keyPair.getPublic(), DHPublicKeySpec.class);
			System.out.println("G: " + kspec.getG());
			g = kspec.getG();
			encryptionCounter += 1;
			return kspec.getG().toString();
		} else if (encryptionCounter == 1) {
			System.out.println("HERE 2 ------------------------------------------------------------------------!");
			System.out.println(theInput);

			BigInteger byteKey = new BigInteger(theInput);
			DHPublicKeySpec dhspec = new DHPublicKeySpec(byteKey, p, g);
			KeyFactory keyFact = KeyFactory.getInstance("DH");
			clientPublicKey = keyFact.generatePublic(dhspec);
			System.out.println("GOT PUBLIC KEY: " + clientPublicKey);

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
			DHParameterSpec param = new DHParameterSpec(p, g);
			kpg.initialize(param);
			keyPair = kpg.generateKeyPair();
			encryptionCounter += 1;

			KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
			serverPublicKey = keyPair.getPublic();
			serverPrivateKey = keyPair.getPrivate();
			DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(serverPublicKey, DHPublicKeySpec.class);
			System.out.println("PUBLIC KEY : " + kspec.getY());
			return kspec.getY().toString();

		} else if (state == SENTLOGCONFIRMATION) {
			theInput = decryptMessage(theInput);
			int count = Character.getNumericValue(theInput.charAt(1));
			String localInput = theInput.substring(2, 2 + count);

			if (localInput.equalsIgnoreCase("GETCON")) {
				List<String> contractor = connection.findAllCon();
				if (contractor != null) {
					String message = "|4DATA";
					for (String con : contractor) {
						message += "|" + con;
					}
					return message;
				} else {
					return "|6NODATA";
				}
			}
		} else if (state == SENTCONFIRMATION) {
			theInput = decryptMessage(theInput);
			int count = Character.getNumericValue(theInput.charAt(1));
			System.out.println("\"" + theInput + "\"");
			String localInput = theInput.substring(2, 2 + count);
			System.out.println("\"" + localInput + "\"");
			System.out.println("IM HERE1");
			if (localInput.equalsIgnoreCase("LOGIN")) {
				System.out.println("IM LOGING");
				connection.uname = getUname(theInput.substring(2 + count + 1, theInput.length()));
				connection.password = getPass(theInput.substring(2 + count + 1, theInput.length()));

				if (localInput.equalsIgnoreCase("LOGIN")) {
					System.out.println("LOGGING: " + connection.uname + " " + connection.password);
					if (connection.findUser(connection.uname, connection.password) == true) {
						theOutput = "|5OKLOG";
						state = SENTLOGCONFIRMATION;
						return theOutput;
					} else {
						theOutput = "|5NOLOG";
						state = SENTCONFIRMATION;
						return theOutput;
					}
				}

				theOutput = "|5OKLOG";
				state = SENTANSWER;
			} else if (localInput.equalsIgnoreCase("REGISTER")) {
				System.out.println("IM REGISTERING");
				theOutput = "|3KEY";

				connection.uname = getUname(theInput.substring(2 + count + 1, theInput.length()));
				connection.password = getPass(theInput.substring(2 + count + 1, theInput.length()));

				UUID lol = UUID.randomUUID();
				String hash = MD5("userReg201510251" + lol.toString());

				try {
					connection.insertKey(hash);
				} catch (SQLException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				databaseLog.appendText("Created HASH: " + hash + " " + "\n");

				String email = getEmailM(theInput.substring(2 + count + 1, theInput.length()));

				try {
					MailSender.SendEmail("Your key: \n" + hash, email);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				state = SENTKEY;
			}
		} else if (state == SENTKEY) {
			theInput = decryptMessage(theInput);
			int count = Character.getNumericValue(theInput.charAt(1));
			String localInput = theInput.substring(2, 2 + count);
			int count1 = Character.getNumericValue(theInput.charAt(1));
			String localInput1 = theInput.substring(5, theInput.length());
			System.out.println("LOOKING FOR A KEY:" + localInput1);

			String key = localInput1.substring(4, localInput1.length());
			if (localInput.equalsIgnoreCase("KEY")) {
				System.out.println("KEY: " + key);
				if (connection.findKey(key) == true) {
					theOutput = "|5OKREG";
					try {
						connection.insertUsr(connection.uname, connection.password);
					} catch (ClassNotFoundException | SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					state = SENTCONFIRMATION;
					return theOutput;
				} else {
					theOutput = "|5NOREG";
					state = SENTCONFIRMATION;
					return theOutput;
				}
			}

		} else if (state == SENTANSWER) {
			theInput = decryptMessage(theInput);
			System.out.println("IM AWAITING ANSWER?");
			if (theInput.equalsIgnoreCase(clues[currentJoke] + " who?")) {
				theOutput = answers[currentJoke] + " Want another? (y/n)";
				state = ANOTHER;
			} else {
				theOutput = "You're supposed to say \"" + clues[currentJoke] + " who?\"" + "! Try again. Knock! Knock!";
				state = SENTCONFIRMATION;
			}
		} else if (state == ANOTHER) {
			theInput = decryptMessage(theInput);
			System.out.println("FUCK HAPPEND");
			if (theInput.equalsIgnoreCase("y")) {
			} else {
				theOutput = "Bye.";
				state = WAITING;
			}
		} else {
			return "???";
		}
		System.out.println("Hell");
		return theOutput;
	}

	public String encryptMessage(String message) throws Exception {
		c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Instantiate the Cipher of algorithm "DES"
		// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob
		// (g^X
		// mod p)^Y mod p
		key1 = agreeSecretKey(serverPrivateKey, clientPublicKey, true);
		// Instantiate the Cipher of algorithm "DES"

		// Init the cipher with Alice's key1
		System.out.println(key1.getEncoded().length);

		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		c.init(Cipher.ENCRYPT_MODE, key1, ivSpec);
		System.out.println("SECRET KEY: " + key1);
		System.out.println("MY PRIVATE KEY: " + serverPrivateKey);
		System.out.println("CLIENT PUBLIC KEY: " + clientPublicKey);
		System.out.println("MY PUBLIC KEY: " + serverPublicKey);
		System.out.println("G: " + g);
		System.out.println("P: " + p);
		// Compute the cipher text = E(key,plainText)
		byte[] ciphertext = c.doFinal(message.getBytes(StandardCharsets.US_ASCII));
		System.out.println("L: " + ciphertext.length);
		// prints ciphertext
		System.out.println("Encrypted: " + new String(ciphertext, "ascii"));
		System.out.println(ciphertext.length);
		return Base64.getEncoder().encodeToString(ciphertext);
	}

	public String decryptMessage(String message) throws Exception {
		byte[] stringer = Base64.getDecoder().decode(message.getBytes(StandardCharsets.US_ASCII));
		System.out.println(message);

		// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob (g^X
		// mod p)^Y mod p

		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
		DHParameterSpec param = new DHParameterSpec(p, g);
		kpg.initialize(param);
		keyPair = kpg.generateKeyPair();

		SecretKey key1 = agreeSecretKey(serverPrivateKey, clientPublicKey, true);
		System.out.println("SECRET KEY " + key1);
		System.out.println("MY PRIVATE KEY: " + serverPrivateKey);
		System.out.println("MY PUBLIC KEY: " + serverPublicKey);
		System.out.println("CLIENT PUBLIC KEY: " + clientPublicKey);
		System.out.println("G: " + g);
		System.out.println("P: " + p);
		// Instantiate the Cipher of algorithm "DES"
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Init the cipher with Alice's key1
		System.out.println(key1.getEncoded().length);
		// inits the encryptionMode
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		c.init(Cipher.DECRYPT_MODE, key1, ivSpec);
		// Decrypts and print
		byte[] pad = c.doFinal(stringer);
		System.out.println("Decrypted: \"" + new String(pad, "ascii") +"\"");
		return new String(pad, "ascii");
	}

	public String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}

	private String getEmailM(String msg) {
		int count;
		// System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(0));
		String first = msg.substring(4 + count, msg.length());
		System.out.println(first);
		count = Character.getNumericValue(first.charAt(1));
		first = first.substring(7 + count, first.length());
		System.out.println(first);
		return first.substring(5, first.length());
	}

	private String getUname(String msg) {
		int count;
		System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(0));
		String first = msg.substring(4, 4 + count);
		System.out.println("NAME: " + first);
		return first;
	}

	private String getPass(String msg) {
		int count;
		System.out.println(msg);
		count = Character.getNumericValue(msg.charAt(0));
		String first = msg.substring(4 + count, msg.length());
		System.out.println(first);
		count = Character.getNumericValue(first.charAt(1));
		first = first.substring(6, 6 + count);
		System.out.println("PASSWORD: " + first);
		return first;
	}
	
	public int getEncryptionCounter(){
		return encryptionCounter;
	}
	
	public void setEncryptionCounter(int set){
		encryptionCounter = set;
	}
}

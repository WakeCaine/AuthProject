package com.auth.client.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.auth.client.main.MainController;
/*
 * Class responsible for securing connection with Cryptography
 * FIXME Delete dependence on input data from BufferedReader and output data from PrintWriter
 */
public class SecureCon {
	
	int counterRaw = 0;
	/* 
	 * P prime and G prime
	 */
	public BigInteger p, g;
	
	/*
	 *  Note: 128-bit key is enough for AES, if you want more you need to override
	 *  Java Cipher restriction
	 */
	private static final int AES_KEY_SIZE = 128;
	
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
	
	public boolean EncryptHandShake(MainController mainController, BufferedReader in, PrintWriter out)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		String inputLine = null;
		
		inputLine = in.readLine();
		mainController.inputLogBox.appendText("INPUT: \"" + inputLine + "\"\n");
		System.out.println("HERE");
		mainController.clientLogBox.appendText("GOT P\n");
		if (counterRaw == 3) {
			try {
				c = Cipher.getInstance("AES/CBC/PKCS5Padding");
				// Instantiate the Cipher of algorithm "DES"
				// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob
				// (g^X
				// mod p)^Y mod p
				key1 = agreeSecretKey(clientPrivateKey, serverPublicKey, true);
				// Instantiate the Cipher of algorithm "DES"

				// Init the cipher with Alice's key1
				System.out.println(key1.getEncoded().length);

				IvParameterSpec ivSpec = new IvParameterSpec(secureRandom);
				c.init(Cipher.ENCRYPT_MODE, key1, ivSpec);
				System.out.println("SECRET KEY: " + key1);
				System.out.println("MY PRIVATE KEY: " + clientPrivateKey);
				System.out.println("SERVER PUBLIC KEY: " + serverPublicKey);
				System.out.println("MY PUBLIC KEY: " + clientPublicKey);
				System.out.println("G: " + g);
				System.out.println("P: " + p);
				// Compute the cipher text = E(key,plainText)
				byte[] ciphertext = c.doFinal("FUCKFUCKFUCKFUCK".getBytes());
				System.out.println("L: " + ciphertext.length);
				// prints ciphertext
				System.out.println("Encrypted: " + new String(ciphertext, "ascii"));
				System.out.println(ciphertext.length);
				out.println(Base64.getEncoder().encodeToString(ciphertext));
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (counterRaw == 2) {
			BigInteger byteKey = new BigInteger(inputLine);
			DHPublicKeySpec dhspec = new DHPublicKeySpec(byteKey, p, g);
			KeyFactory keyFact = KeyFactory.getInstance("DH");
			serverPublicKey = keyFact.generatePublic(dhspec);

			SecureRandom secureR = new SecureRandom();
			secureR.nextBytes(secureRandom);
			iv = secureRandom;
			System.out.println("IV: " + secureRandom);
			System.out.println("IV string: " + new String(secureRandom));
			out.println(new String(secureRandom));
			counterRaw += 1;
			return true;
		} else if (counterRaw == 1) {
			System.out.println("HERE 1 ------------------------------------------------------------!");
			g = new BigInteger(inputLine);
			KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
			keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
			DHParameterSpec param = new DHParameterSpec(p, g);
			keyPairGenerator.initialize(param);
			keyPair = keyPairGenerator.generateKeyPair();
			String publicKey = keyPair.getPublic().getEncoded().toString();
			System.out.println(keyPair.getPublic());
			clientPublicKey = keyPair.getPublic();
			clientPrivateKey = keyPair.getPrivate();
			DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(keyPair.getPublic(), DHPublicKeySpec.class);
			System.out.println(kspec.getY().toString());

			out.println(kspec.getY());
			counterRaw += 1;
			return true;
		} else if (counterRaw == 0) {
			System.out.println("HERE 2 ------------------------------------------------------------!");
			mainController.clientLogBox.appendText("NOTHING TO SEE HERE YET\n");

			KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

			kpg.initialize(512);
			KeyPair kp1 = kpg.generateKeyPair();
			KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
			DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp1.getPublic(), DHPublicKeySpec.class);
			System.out.println("G: " + kspec.getG());
			System.out.println("P: " + kspec.getP());
			/* System.out.println("Public key Alice :" + kp1.getPublic()); */
			p = kspec.getP();
			out.println(kspec.getP());
			counterRaw += 1;			
			return true;
		}
		return false;
	}
	
	public String encryptMessage(String message) throws Exception{
		c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Instantiate the Cipher of algorithm "DES"
		// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob
		// (g^X
		// mod p)^Y mod p
		key1 = agreeSecretKey(clientPrivateKey, serverPublicKey, true);
		// Instantiate the Cipher of algorithm "DES"

		// Init the cipher with Alice's key1
		System.out.println(key1.getEncoded().length);

		IvParameterSpec ivSpec = new IvParameterSpec(secureRandom);
		c.init(Cipher.ENCRYPT_MODE, key1, ivSpec);
		System.out.println("SECRET KEY: " + key1);
		System.out.println("MY PRIVATE KEY: " + clientPrivateKey);
		System.out.println("SERVER PUBLIC KEY: " + serverPublicKey);
		System.out.println("MY PUBLIC KEY: " + clientPublicKey);
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
	
	public String descryptMessage(String message) throws Exception{
	
		byte[] stringer = Base64.getDecoder().decode(message.getBytes(StandardCharsets.US_ASCII));
		System.out.println(message);

		// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob (g^X
		// mod p)^Y mod p
	
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
		DHParameterSpec param = new DHParameterSpec(p, g);
		kpg.initialize(param);
		keyPair = kpg.generateKeyPair();
	
		SecretKey key1 = agreeSecretKey(clientPrivateKey, serverPublicKey, true);
		System.out.println("SECRET KEY " + key1);
		System.out.println("MY PRIVATE KEY: " + clientPrivateKey);
		System.out.println("MY PUBLIC KEY: " + clientPublicKey);
		System.out.println("SERVER PUBLIC KEY: " + serverPublicKey);
		System.out.println("G: " + g);
		System.out.println("P: " + p);
		// Instantiate the Cipher of algorithm "DES"
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		// Init the cipher with Alice's key1
		System.out.println(key1.getEncoded().length);
		// inits the encryptionMode
		System.out.println("IV: " + iv);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		c.init(Cipher.DECRYPT_MODE, key1,ivSpec);
		// Decrypts and print
		byte[] pad = c.doFinal(stringer);
		System.out.println("Decrypted: " + new String(pad, "ascii"));
		return new String(pad, "ascii");
	}
	
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

	
}

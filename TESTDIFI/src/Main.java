import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Main {

	public final static int pValue = 47;

	public final static int gValue = 71;

	public final static int XaValue = 9;

	public final static int XbValue = 14;

	public static void main(String[] args) throws Exception {
		BigInteger p = new BigInteger(Integer.toString(pValue));
		BigInteger g = new BigInteger(Integer.toString(gValue));
		BigInteger Xa = new BigInteger(Integer.toString(XaValue));
		BigInteger Xb = new BigInteger(Integer.toString(XbValue));

		createKey();

		int bitLength = 512; // 512 bits
		SecureRandom rnd = new SecureRandom();
		p = BigInteger.probablePrime(bitLength, rnd);
		g = BigInteger.probablePrime(bitLength, rnd);

		createSpecificKey(p, g);
	}

	public static void createKey() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

		kpg.initialize(512);
		KeyPair kp1 = kpg.generateKeyPair();
		KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");
		DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp1.getPublic(), DHPublicKeySpec.class);
		System.out.println("G: " + kspec.getG());
		System.out.println("P: " + kspec.getP());
		System.out.println("Public key Alice :" + kp1.getPublic());
		System.out.println("Private key Alice :" + kp1.getPrivate().getEncoded());
		KeyPair kp2 = kpg.generateKeyPair();
		System.out.println("Public key Bob :" + kp2.getPublic());
		System.out.println("Private key Bob :" + kp2.getPrivate());
		// Gets the public key of Alice(g^X mod p) and Bob (g^Y mod p)
		PublicKey pbk1 = kp1.getPublic();
		PublicKey pbk2 = kp2.getPublic();
		// Gets the private key of Alice X and Bob Y
		PrivateKey prk1 = kp1.getPrivate();
		PrivateKey prk2 = kp2.getPrivate();
		try {
			// Computes secret keys for Alice (g^Y mod p)^X mod p == Bob (g^X
			// mod p)^Y mod p
			SecretKey key1 = agreeSecretKey(prk1, pbk2, true);
			SecretKey key2 = agreeSecretKey(prk2, pbk1, true);
			// Instantiate the Cipher of algorithm "DES"
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// Init the cipher with Alice's key1
			System.out.println(key1.getEncoded().length);
			c.init(Cipher.ENCRYPT_MODE, key1);
			// Compute the cipher text = E(key,plainText)
			byte[] ciphertext = c.doFinal("Stand and unfold yourself".getBytes());
			// prints ciphertext
			System.out.println("Encrypted: " + new String(ciphertext, "utf-8"));
			// inits the encryptionMode
			c.init(Cipher.DECRYPT_MODE, key2);
			// Decrypts and print
			System.out.println("Decrypted: " + new String(c.doFinal(ciphertext), "utf-8"));
			System.out.println("Done");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		SecretKey desSpec = new SecretKeySpec(bkey, "AES");
		return desSpec;
	}

	public static void createSpecificKey(BigInteger p, BigInteger g) throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");

		/*
		 * System.out.println("P: " + p); System.out.println("G: " + g);
		 */
		DHParameterSpec param = new DHParameterSpec(p, g);
		kpg.initialize(param);
		KeyPair kp = kpg.generateKeyPair();
		/*
		 * System.out.println("PUBLIC: " + kp.getPublic());
		 * 
		 * System.out.println("PRIVATE: " + kp.getPrivate());
		 */

		KeyFactory kfactory = KeyFactory.getInstance("DiffieHellman");

		DHPublicKeySpec kspec = (DHPublicKeySpec) kfactory.getKeySpec(kp.getPublic(), DHPublicKeySpec.class);
	}
}
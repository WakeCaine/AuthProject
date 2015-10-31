using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
<<<<<<< HEAD

=======
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Math;
>>>>>>> modifications

class Alice
{
    public static byte[] alicePublicKey;

<<<<<<< HEAD
    public static void Main(string[] args)
    {
        using (ECDiffieHellmanCng alice = new ECDiffieHellmanCng())
        {

            alice.KeyDerivationFunction = ECDiffieHellmanKeyDerivationFunction.Hash;
            alice.HashAlgorithm = CngAlgorithm.Sha256;
            alicePublicKey = alice.PublicKey.ToByteArray();
            Bob bob = new Bob();
            CngKey k = CngKey.Import(bob.bobPublicKey, CngKeyBlobFormat.EccPublicBlob);
            byte[] aliceKey = alice.DeriveKeyMaterial(CngKey.Import(bob.bobPublicKey, CngKeyBlobFormat.EccPublicBlob));
            byte[] encryptedMessage = null;
            byte[] iv = null;
            Send(aliceKey, "tirurururu", out encryptedMessage, out iv);
            bob.Receive(encryptedMessage, iv);


            int count = 0;
            foreach(byte b in aliceKey)
            {
                Console.Write(b);
                count++;
            }

            //Console.WriteLine("przerwa\n"); 


           /* foreach(byte b in encryptedMessage)
            {
                Console.Write("\n"+b);
                count++;
            }*/

            Console.WriteLine("\n"+count);

            byte[] kot;
            byte[] ivt;
            int ccc=0;
            using(Aes a = new AesCryptoServiceProvider())
            {
               kot = a.Key;
                ivt = a.IV;

                foreach(byte b in ivt)
                {
                    ccc++;
                }
                Console.WriteLine("ivv\n\n\n" + ccc);
            }
        }

=======

    public static DHParameters GenerateParameters()
    {
        var generator = new DHParametersGenerator();
        generator.Init(256, 30, new SecureRandom());
        return generator.GenerateParameters();
    }

    public string GetG(DHParameters parameters)
    {
        return parameters.G.ToString();
    }

    public static AsymmetricCipherKeyPair GenerateKeys(DHParameters parameters)
    {
        var keyGen = GeneratorUtilities.GetKeyPairGenerator("DH");
        var kgp = new DHKeyGenerationParameters(new SecureRandom(), parameters);
        keyGen.Init(kgp);
        return keyGen.GenerateKeyPair();
    }
    // This returns A
    public static string GetPublicKey(AsymmetricCipherKeyPair keyPair)
    {
        var dhPublicKeyParameters = keyPair.Public as DHPublicKeyParameters;
        if (dhPublicKeyParameters != null)
        {
            return dhPublicKeyParameters.Y.ToString();
        }
        throw new NullReferenceException("The key pair provided is not a valid DH keypair.");
    }

    // This returns a
    public static string GetPrivateKey(AsymmetricCipherKeyPair keyPair)
    {
        var dhPrivateKeyParameters = keyPair.Private as DHPrivateKeyParameters;
        if (dhPrivateKeyParameters != null)
        {
            return dhPrivateKeyParameters.X.ToString();
        }
        throw new NullReferenceException("The key pair provided is not a valid DH keypair.");
    }

    public static BigInteger ComputeSharedSecret(string A, AsymmetricKeyParameter bPrivateKey, DHParameters internalParameters)
    {
        var importedKey = new DHPublicKeyParameters(new BigInteger(A), internalParameters);
        var internalKeyAgree = AgreementUtilities.GetBasicAgreement("DH");
        internalKeyAgree.Init(bPrivateKey);
        return internalKeyAgree.CalculateAgreement(importedKey);
    }

    public static void Main(string[] args)
    {
        DHParameters dh = GenerateParameters();
        Console.WriteLine("G1 " + dh.G.ToByteArray().Length + " : " + dh.G.ToString());
        Console.WriteLine("P1 " + dh.P.ToByteArray().Length + " : " + dh.P.ToString());
        AsymmetricCipherKeyPair d1 = GenerateKeys(dh);
        Console.WriteLine("A1:" + GetPublicKey(d1));
        Console.WriteLine("a1:" + GetPrivateKey(d1));
        Console.WriteLine("------------------------------------------");
        Console.WriteLine("G2 " + dh.G.ToByteArray().Length + " : " + dh.G.ToString());
        Console.WriteLine("P2 " + dh.P.ToByteArray().Length + " : " + dh.P.ToString());
        AsymmetricCipherKeyPair d2 = GenerateKeys(dh);
        Console.WriteLine("A2:" + GetPublicKey(d2));
        Console.WriteLine("a2:" + GetPrivateKey(d2));
        Console.WriteLine("Shared1 : " + ComputeSharedSecret(GetPublicKey(d1), d2.Private, dh).ToByteArray().Length + " " + ComputeSharedSecret(GetPublicKey(d1), d2.Private, dh));
        Console.WriteLine("Shared2 : " + ComputeSharedSecret(GetPublicKey(d2), d1.Private, dh).ToByteArray().Length + " " + ComputeSharedSecret(GetPublicKey(d2), d1.Private, dh));
        BigInteger p, g;
        p = Console.ReadLine() as BigInteger;
        DHParameters dhdh = new DHParameters(,56661256350367679909263413006213938814695654402016867787335841210218888422489);
        AsymmetricCipherKeyPair d11 = GenerateKeys(dh);
        AsymmetricCipherKeyPair d22 = GenerateKeys(dh);

        byte[] encryptedMessage = null;
        byte[] key = ComputeSharedSecret("6283702420886854514301602857235445910702067563632198257494822857545019841892", d1.Private, dh).ToByteArray();
        byte[] iv = null;
        using (Aes aes = new AesCryptoServiceProvider())
        {
            Array.Resize(ref key, 32);
            aes.Key = key;
            iv = aes.IV;

            // Encrypt the message
            //uwaga uwaga szyfrowanie :D
            using (MemoryStream ciphertext = new MemoryStream())
            using (CryptoStream cs = new CryptoStream(ciphertext, aes.CreateEncryptor(), CryptoStreamMode.Write))
            {
                byte[] plaintextMessage = Encoding.UTF8.GetBytes("lol");
                cs.Write(plaintextMessage, 0, plaintextMessage.Length);
                cs.Close();
                encryptedMessage = ciphertext.ToArray();
            }
        }
        for(int i = 0; i < encryptedMessage.Length; i++)
        Console.Write(encryptedMessage[i]);
        Console.WriteLine();


        using (Aes aes = new AesCryptoServiceProvider())
        {
            aes.Key = key;
            aes.IV = iv;
            // Decrypt the message
            //deszyfrowanie
            using (MemoryStream plaintext = new MemoryStream())
            {
                using (CryptoStream cs = new CryptoStream(plaintext, aes.CreateDecryptor(), CryptoStreamMode.Write))
                {
                    cs.Write(encryptedMessage, 0, encryptedMessage.Length);
                    cs.Close();
                    string message = Encoding.UTF8.GetString(plaintext.ToArray()); //<----- odszyfrowana wiadomość
                    Console.WriteLine(message);
                }
            }
        }
        
>>>>>>> modifications
        Console.ReadLine();

    }

    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /* !-----------------------------TUTAJ JEST AES I SZYFROWANIE---------------------------*/
    private static void Send(byte[] key, string secretMessage, out byte[] encryptedMessage, out byte[] iv)
    {
        using (Aes aes = new AesCryptoServiceProvider())
        {
            aes.Key = key;
            iv = aes.IV;

            // Encrypt the message
            //uwaga uwaga szyfrowanie :D
            using (MemoryStream ciphertext = new MemoryStream())
            using (CryptoStream cs = new CryptoStream(ciphertext, aes.CreateEncryptor(), CryptoStreamMode.Write))
            {
                byte[] plaintextMessage = Encoding.UTF8.GetBytes(secretMessage);
                cs.Write(plaintextMessage, 0, plaintextMessage.Length);
                cs.Close();
                encryptedMessage = ciphertext.ToArray();
            }
        }
    }

}
public class Bob
{
    public byte[] bobPublicKey;
    private byte[] bobKey;
    public Bob()
    {
        using (ECDiffieHellmanCng bob = new ECDiffieHellmanCng())
        {

            bob.KeyDerivationFunction = ECDiffieHellmanKeyDerivationFunction.Hash;
            bob.HashAlgorithm = CngAlgorithm.Sha256;
            bobPublicKey = bob.PublicKey.ToByteArray();
            bobKey = bob.DeriveKeyMaterial(CngKey.Import(Alice.alicePublicKey, CngKeyBlobFormat.EccPublicBlob));

        }
    }

    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    /* !-----------------------------TUTAJ JEST AES I DESZYFROWANIE---------------------------*/

    public void Receive(byte[] encryptedMessage, byte[] iv)
    {

        using (Aes aes = new AesCryptoServiceProvider())
        {
            aes.Key = bobKey;
            aes.IV = iv;
            // Decrypt the message
            //deszyfrowanie
            using (MemoryStream plaintext = new MemoryStream())
            {
                using (CryptoStream cs = new CryptoStream(plaintext, aes.CreateDecryptor(), CryptoStreamMode.Write))
                {
                    cs.Write(encryptedMessage, 0, encryptedMessage.Length);
                    cs.Close();
                    string message = Encoding.UTF8.GetString(plaintext.ToArray()); //<----- odszyfrowana wiadomość
                    Console.WriteLine(message);
                }
            }
        }
    }

}
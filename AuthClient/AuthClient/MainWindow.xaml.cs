using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Org.BouncyCastle.Crypto;
using Org.BouncyCastle.Crypto.Generators;
using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.Security;
using Org.BouncyCastle.Math;

namespace AuthClient
{
    public partial class MainWindow : Window
    {
        // Receiving byte array  
        byte[] bytes = new byte[1024];
        Socket senderSock;

        int counterRaw = 0;

        //Information about current connection
        Connection con;
        DHParameters dh,dh1;
        BigInteger P, G;
        public MainWindow()
        {
            InitializeComponent();
            dataBox.Text = "";
        }

        private void Connect_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                // Create one SocketPermission for socket access restrictions 
                SocketPermission permission = new SocketPermission(
                    NetworkAccess.Connect,    // Connection permission 
                    TransportType.Tcp,        // Defines transport types 
                    "",                       // Gets the IP addresses 
                    SocketPermission.AllPorts // All ports 
                    );

                // Ensures the code to have permission to access a Socket 
                permission.Demand();

                // Resolves a host name to an IPHostEntry instance            
                IPHostEntry ipHost = Dns.GetHostEntry("");

                // Gets first IP address associated with a localhost 
                IPAddress ipAddr = IPAddress.Parse(ipBox.Text);

                // Creates a network endpoint 
                IPEndPoint ipEndPoint = new IPEndPoint(ipAddr, 4515);

                // Create one Socket object to setup Tcp connection 
                senderSock = new Socket(
                    ipAddr.AddressFamily,// Specifies the addressing scheme 
                    SocketType.Stream,   // The type of socket  
                    ProtocolType.Tcp     // Specifies the protocols  
                    );

                senderSock.NoDelay = false;   // Using the Nagle algorithm 

                // Establishes a connection to a remote host 
                senderSock.Connect(ipEndPoint);
                tbStatus.Text = "Socket connected to " + senderSock.RemoteEndPoint.ToString();

                Connect_Button.IsEnabled = false;
                Send_Button.IsEnabled = true;
            }
            catch (Exception exc) { MessageBox.Show(exc.ToString()); }
            con = new Connection();
            con.setStatus(Status.CONNECTED);
            ReceiveDataFromServer();
        }

        private void Send_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                // Sending message 
                //<Client Quit> is the sign for end of data 
                string theMessageToSend = tbMsg.Text;
                byte[] msg = Encoding.ASCII.GetBytes(theMessageToSend + "\n");

                // Sends data to a connected Socket. 
                int bytesSend = senderSock.Send(msg);

                ReceiveDataFromServer();

                Send_Button.IsEnabled = false;
                Disconnect_Button.IsEnabled = true;
            }
            catch (Exception exc) { MessageBox.Show(exc.ToString()); }
        }

        private void ReceiveDataFromServer()
        {
            try
            {
                // Receives data from a bound Socket. 
                int bytesRec = senderSock.Receive(bytes);

                // Converts byte array to string 
                String theMessageToReceive = Encoding.ASCII.GetString(bytes, 0, bytesRec);

                // Continues to read the data till data isn't available 
                while (senderSock.Available > 0)
                {
                    bytesRec = senderSock.Receive(bytes);
                    theMessageToReceive += Encoding.ASCII.GetString(bytes, 0, bytesRec);
                }

                tbReceivedMsg.Text = "The server reply: " + "\"" + theMessageToReceive.Substring(0, theMessageToReceive.Length - 2) + "\"";

                if (counterRaw > 2)
                {
                    analyzeMessage(theMessageToReceive.Substring(0, theMessageToReceive.Length - 2));
                }
                else
                {
                    tbReceivedMsg.Text = "GOT P";
                    byte[] msg = null;
                    if(counterRaw == 1)
                    {
                        dh = GenerateParameters();
                        G = new BigInteger(dh.G.ToString());
                        msg = Encoding.ASCII.GetBytes(dh.G.ToString() + "\n");
                    }
                    else
                    {
                        P = new BigInteger(theMessageToReceive);
                        dh1 = new DHParameters(P,G);
                        AsymmetricCipherKeyPair d1 = GenerateKeys(dh1);

                        msg = Encoding.ASCII.GetBytes(d1.Public.ToString() + "\n");
                    }

                    // Sends data to a connected Socket. 
                    int bytesSend = senderSock.Send(msg);

                    counterRaw = +1;
                    ReceiveDataFromServer();


                }

                //if (theMessageToReceive.Substring(0,theMessageToReceive.Length-2) == "BYE")
                //{
                //    // Disables sends and receives on a Socket. 
                //    senderSock.Shutdown(SocketShutdown.Both);

                //    //Closes the Socket connection and releases all resources 
                //    senderSock.Close();

                //    tbStatus.Text = "Disconnected";
                //    tbReceivedMsg.Text = "Disconnected";
                //    Disconnect_Button.IsEnabled = false;
                //    Connect_Button.IsEnabled = true;
                //    Send_Button.IsEnabled = false;
                //}
            }
            catch (Exception exc) { MessageBox.Show(exc.ToString()); }
        }

        private void Disconnect_Click(object sender, RoutedEventArgs e)
        {
            try
            {

                // Disables sends and receives on a Socket. 
                senderSock.Shutdown(SocketShutdown.Both);

                //Closes the Socket connection and releases all resources 
                senderSock.Close();

                Disconnect_Button.IsEnabled = false;
            }
            catch (Exception exc) { MessageBox.Show(exc.ToString()); }
        }

        static byte[] GetBytes(string str)
        {
            byte[] bytes = new byte[str.Length * sizeof(char)];
            System.Buffer.BlockCopy(str.ToCharArray(), 0, bytes, 0, bytes.Length);
            return bytes;
        }

        static string GetString(byte[] bytes)
        {
            char[] chars = new char[bytes.Length / sizeof(char)];
            System.Buffer.BlockCopy(bytes, 0, chars, 0, bytes.Length);
            return new string(chars);
        }

        public void analyzeMessage(String message)
        {
            Status localStatus = con.getAnswer(message);

            if (localStatus == Status.CONNECTED && con.getStatus() == Status.CONNECTED)
            {
                tbReceivedMsg.Text = "CONNECTED";
                loginBox.IsEnabled = true;
                passwordBox.IsEnabled = true;
                checkRegister.IsEnabled = true;
                emailBox.IsEnabled = true;
                loginButton.IsEnabled = true;
                keyBox.IsEnabled = true;
            }
            else if (localStatus == Status.CONFIRM)
            {
                dataBox.AppendText("Check email for info about your key to register!\n");
                con.setStatus(Status.CONFIRM);
            }
            else if (con.getStatus() == Status.CONFIRM && (localStatus == Status.ERROR || localStatus == Status.REGISTERED))
            {
                if(localStatus == Status.REGISTERED)
                {
                    dataBox.Text = "REGISTRATION SUCCESSFUL!";
                }
                else
                {
                    dataBox.Text = "SOMETHING WENT WRONG!";
                }
            }
            else if (localStatus == Status.BYE && con.getStatus() == Status.CONNECTED)
            {
                // Disables sends and receives on a Socket. 
                senderSock.Shutdown(SocketShutdown.Both);

                //Closes the Socket connection and releases all resources 
                senderSock.Close();

                tbStatus.Text = "Disconnected";
                tbReceivedMsg.Text = "Disconnected";
                Disconnect_Button.IsEnabled = false;
                Connect_Button.IsEnabled = true;
                Send_Button.IsEnabled = false;

                loginBox.IsEnabled = false;
                passwordBox.IsEnabled = false;
                checkRegister.IsEnabled = false;
                emailBox.IsEnabled = false;
                loginButton.IsEnabled = false;
                keyBox.IsEnabled = false;
            }
            else if (localStatus == Status.MALFORMED)
            {
                tbReceivedMsg.Text = "MALFORMED CONNECTION OR INTERNAL SERVER ERROR\nTURN OFF APPLICATION";
            }
        }

        private void Register_Button_Click(object sender, RoutedEventArgs e)
        {
            if (con.getStatus() == Status.CONFIRM)
            {
                String message = "|3KEY" + "|KEY" + keyBox.Text;
                byte[] msg = Encoding.ASCII.GetBytes(message + "\n");

                // Sends data to a connected Socket. 
                int bytesSend = senderSock.Send(msg);

                ReceiveDataFromServer();
            }
            else if (checkRegister.IsChecked == false)
            {
                String message = "|5LOGIN" + "|" + loginBox.Text.Length + "USR" + loginBox.Text + "|" + passwordBox.Text.Length + "PASS" + passwordBox.Text;
                byte[] msg = Encoding.ASCII.GetBytes(message + "\n");

                // Sends data to a connected Socket. 
                int bytesSend = senderSock.Send(msg);

                ReceiveDataFromServer();
            }
            else
            {
                String message = "|8REGISTER" + "|" + loginBox.Text.Length + "USR" + loginBox.Text + "|" + passwordBox.Text.Length + "PASS" + passwordBox.Text + "|EMAIL" + emailBox.Text;
                byte[] msg = Encoding.ASCII.GetBytes(message + "\n");

                // Sends data to a connected Socket. 
                int bytesSend = senderSock.Send(msg);

                ReceiveDataFromServer();
            }
        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            try
            {

                // Disables sends and receives on a Socket. 
                senderSock.Shutdown(SocketShutdown.Both);

                //Closes the Socket connection and releases all resources 
                senderSock.Close();
            }
            catch (Exception exc) { MessageBox.Show(exc.ToString()); }
        }

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

    }
}

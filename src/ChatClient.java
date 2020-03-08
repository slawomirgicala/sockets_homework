import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {

    private class UserInputHandler extends Thread{
        private PrintWriter out;
        private CancelableReader in;
        private DatagramSocket datagramSocket;
        private DatagramPacket datagramPacket;
        private ServerInputHandler serverInputHandler;
        private String nickname;


        public UserInputHandler(PrintWriter out, DatagramSocket datagramSocket, DatagramPacket datagramPacket,
                                String nickname){
            this.out = out;
            this.in = new CancelableReader(new InputStreamReader(System.in));
            this.datagramSocket = datagramSocket;
            this.datagramPacket = datagramPacket;
            this.nickname = nickname;
        }

        public void setServerInputHandler(ServerInputHandler serverInputHandler){
            this.serverInputHandler = serverInputHandler;
        }

        @Override
        public void run() {
            boolean handleInput = true;
            while (handleInput){
                String userInput = in.readLine();
                if (userInput == null || userInput.equals("q")){
                    if (userInput != null){
                        serverInputHandler.cancelReading();
                    }
                    System.out.println("Leaving chat");
                    handleInput = false;
                    userInput = "left chat";
                }else if (userInput.equals("U")){
                    try {
                        datagramSocket.send(datagramPacket);
                    } catch (IOException e) {
                        System.out.println("Datagram packet sending failed");
                        e.printStackTrace();
                    }
                }
                out.println(nickname + ": " + userInput);
            }
        }

        public void cancelReading(){
            in.cancelRead();
        }
    }

    private class ServerInputHandler extends Thread{
        private CancelableReader in;
        private UserInputHandler userInputHandler;

        public ServerInputHandler(CancelableReader in){
            this.in = in;
        }

        public void setUserInputHandler(UserInputHandler userInputHandler) {
            this.userInputHandler = userInputHandler;
        }

        @Override
        public void run() {
            boolean serverUp = true;
            while (serverUp){
                String serverInput = in.readLine();
                if (serverInput == null){
                    System.out.println("Connection with server lost");
                    serverUp = false;
                    userInputHandler.cancelReading();
                }else {
                    System.out.println(serverInput);
                }
            }
        }

        public void cancelReading(){
            in.cancelRead();
        }
    }

    private class UDPListener extends Thread{
        private int port;

        public UDPListener(int port){
            this.port = port;
        }

        @Override
        public void run() {
            DatagramSocket socket = null;

            try{
                socket = new DatagramSocket(port);
                byte[] receiveBuffer = new byte[1024];

                while(true) {
                    Arrays.fill(receiveBuffer, (byte)0);
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(receivePacket);
                    String msg = new String(receivePacket.getData());
                    System.out.println(msg);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }
    }



    public static void main(String[] args) throws IOException{

        System.out.println("JAVA TCP CLIENT");
        String hostName = "localhost";
        int portNumber = 12345;
        Socket socket = null;
        DatagramSocket datagramSocket = null;

        boolean retryConnection = true;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));


        while (retryConnection){
            try {
                // create socket
                socket = new Socket(hostName, portNumber);
                datagramSocket = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost");
                byte[] sendBuffer = (" ,_     _\n" +
                        " |\\\\_,-~/\n" +
                        " / _  _ |    ,--.\n" +
                        "(  @  @ )   / ,-'\n" +
                        " \\  _T_/-._( (\n" +
                        " /         `. \\\n" +
                        "|         _  \\ |\n" +
                        " \\ \\ ,  /      |\n" +
                        "  || |-_\\__   /\n" +
                        " ((_/`(____,-'\n").getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);

                // in & out streams
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                CancelableReader in = new CancelableReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Choose your nickname:");
                String nickname = bufferedReader.readLine();
                out.println(nickname);
                String nicknameStatus = in.readLine();
                if (nicknameStatus == null) throw new Exception(); //TODO
                while (nicknameStatus.equals("occupied")){
                    System.out.println("Nickname already taken, please choose another:");
                    nickname = bufferedReader.readLine();
                    out.println(nickname);
                    nicknameStatus =  in.readLine();
                    if (nicknameStatus == null) throw new Exception();
                }

                System.out.println("Hello " + nickname);

                UserInputHandler userInputHandler = new ChatClient().
                        new UserInputHandler(out, datagramSocket, datagramPacket, nickname);
                ServerInputHandler serverInputHandler = new ChatClient().new ServerInputHandler(in);

                userInputHandler.setServerInputHandler(serverInputHandler);
                serverInputHandler.setUserInputHandler(userInputHandler);

                userInputHandler.start();
                serverInputHandler.start();
                //new ChatClient().new UDPListener(portNumber).start();

                userInputHandler.join();
                serverInputHandler.join();

                System.out.println("Connection lost\nWould you like to retry?(yes)");
                String answer = bufferedReader.readLine();
                retryConnection = answer.equals("yes");
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted");
                e.printStackTrace();
            } catch (Exception e){
                System.out.println("There was a problem with connection! Probably server is down\n" +
                        "Would you like to retry?(yes)");
                String answer = bufferedReader.readLine();
                retryConnection = answer.equals("yes");
            } finally {
                if (socket != null){
                    socket.close();
                }

            }
        }

    }
}

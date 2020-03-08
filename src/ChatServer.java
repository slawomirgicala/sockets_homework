import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static Map<String, ClientServiceThread> clients = new ConcurrentHashMap<>();

    private class NicknameManagerThread extends Thread{
        private PrintWriter out;
        private BufferedReader in;

        public NicknameManagerThread(PrintWriter out, BufferedReader in){
            this.out = out;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String nickname = in.readLine();
                if (nickname == null) return;
                while (ChatServer.isNicknameOccupied(nickname)){
                    out.println("occupied");
                    nickname = in.readLine();
                    if (nickname == null) return;
                }
                out.println("free");
                ClientServiceThread client = new ClientServiceThread(out, in, nickname);
                ChatServer.addClient(nickname, client);
                client.start();
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP SERVER");
        ServerSocket serverSocket = null;
        DatagramSocket udpSocket = null;
        int portNumber = 12345;
        try {
            // create socket
            serverSocket = new ServerSocket(portNumber);
            udpSocket = new DatagramSocket(portNumber);
            while(true){

                // accept client
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connected");

                // in & out streams
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                new ChatServer().new NicknameManagerThread(out, in).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            if (serverSocket != null){
                serverSocket.close();
            }
        }
    }

    public static void sendTcpMessage(String message, String receivedFrom){
        for (String nickname : clients.keySet()){
            if (!nickname.equals(receivedFrom)){
                ClientServiceThread client = clients.get(nickname);
                if (client != null) client.sendTcpMessage(message);
            }
        }
    }

    public static void sendUdpMessage(String receivedFrom){

    }

    public static void addClient(String nickname, ClientServiceThread client){
        clients.put(nickname, client);
    }

    public static void removeClient(String nickname){
        clients.remove(nickname);
    }

    public static boolean isNicknameOccupied(String nickname){
        return clients.containsKey(nickname);
    }
}

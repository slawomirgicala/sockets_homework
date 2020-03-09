import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.UUID;

public class ClientServiceThread extends Thread {
    private PrintWriter out;
    private BufferedReader in;
    private UUID clientId;
    private String nickname;
    private InetAddress address;
    private int udpPort;
    private DatagramSocket datagramSocket;

    public ClientServiceThread(PrintWriter out, BufferedReader in, String nickname, String hostName, int udpPort) {
        this.out = out;
        this.in = in;
        this.nickname = nickname;
        try {
            this.address = InetAddress.getByName(hostName);
        } catch (UnknownHostException e) {
            System.out.println("Unknown client host name");
            e.printStackTrace();
        }
        this.udpPort = udpPort;
        try {
            this.datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Cannot make socket for client service");
            e.printStackTrace();
        }
    }

    public UUID getClientId(){
        return this.clientId;
    }

    @Override
    public void run() {
        while (true){
            try {
                String messageFromClient = in.readLine();
                if (messageFromClient == null) break;
                ChatServer.sendTcpMessage(messageFromClient, nickname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ChatServer.removeClient(nickname);
        System.out.println(nickname + " disconnected!");
    }

    public void sendTcpMessage(String message){
        out.println(message);
    }

    public void sendUdpMessage(byte[] message){
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, udpPort);
        try {
            datagramSocket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("Cannot send udp packet to client");
            e.printStackTrace();
        }
    }

}

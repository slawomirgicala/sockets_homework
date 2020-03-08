import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class ClientServiceThread extends Thread {
    private PrintWriter out;
    private BufferedReader in;
    private UUID clientId;
    private String nickname;

    public ClientServiceThread(PrintWriter out, BufferedReader in, String nickname){
        this.out = out;
        this.in = in;
        this.nickname = nickname;
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

    public void sendUdpMessage(){

    }

}

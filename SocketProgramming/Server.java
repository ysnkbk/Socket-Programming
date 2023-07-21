import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Server {

    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Yeni  bir client baglandi!");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {

        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            ServerSocket serverSocket = new ServerSocket(6000);
            Server server = new Server(serverSocket);
            System.out.println("Server dinliyor.........");
            server.startServer();
        } catch (Exception e) {
            System.out.println("Sunucuyu baslatma hatasi!");
        }
    }
}

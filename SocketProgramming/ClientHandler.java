import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<String> names = new ArrayList<String>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    public String clientUsername;

    /**
     * @param socket
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public ClientHandler(Socket socket) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            String publicKeyPemStr = bufferedReader.readLine();
            String path = "C:\\Users\\Yasin\\Desktop\\New folder\\JavaDeneme\\PublicKey\\" + clientUsername;

            try {
                byte[] byte_pubkey = Base64.getDecoder().decode(publicKeyPemStr);
                FileOutputStream keyfos = new FileOutputStream(path);
                keyfos.write(byte_pubkey);
                keyfos.close();

            } catch (Exception e) {
                System.out.println("Hata 12");
            }

            names.add(clientUsername);
            clientHandlers.add(this);
            ServerdanGidenMesaj("Server " + clientUsername + " sohbete katildi!\n");

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        getNames();
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                if (messageFromClient.charAt(0) == '|') {
                    String[] fullMessage = messageFromClient.split(" "); // 0-> | 1->receiverName 2->userName: 3->Rsa
                    sendPrivate(fullMessage[3], fullMessage[1]);
                } else
                    broadcastMessageEveryBody(messageFromClient);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                System.out.println("hata 19");
                break;
            }

        }
    }

    public void sendPrivate(String messageToSend, String receiverName) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.clientUsername.equals(receiverName)) {
                    System.out.println(
                            clientUsername + " tarafindan gonderilen sifrelenmis mesaj: " + messageToSend + "\n");
                    clientHandler.bufferedWriter.write("| " + clientUsername + " " + messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

    }

    public void broadcastMessageEveryBody(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    System.out.println(
                            clientUsername + "tarafindan gonderilen herkese acik mesaj: " + messageToSend + "\n");
                    clientHandler.bufferedWriter.write(clientUsername + ":" + messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

    }

    public void getNames() {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                String userNames = "Cevrim ici Kullanicilar: ";
                for (String name : names) {
                    if (name.equals(clientHandler.clientUsername)) {
                        continue;
                    }
                    userNames += name + ",";
                }
                if (userNames.equals("Cevrim ici Kullanicilar: ")) {
                    clientHandler.bufferedWriter.write("Aktif Kullanici yok\n");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } else {
                    clientHandler.bufferedWriter.write(userNames + "\n");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        ServerdanGidenMesaj("Server: " + clientUsername + " sohbetten ayrildi.\n");
        for (String nameString : names) {
            if (nameString == clientUsername) {
                names.remove(clientUsername);
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ServerdanGidenMesaj(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    System.out.println(messageToSend + "\n");
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }

    }

}
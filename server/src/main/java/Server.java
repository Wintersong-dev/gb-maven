import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;
    Connectable systemUser = new SysClient();

    final int PORT = 9876;

    Server() {
        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;
        authService = new SimpleAuth(this);
        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Новый клиент!");
                new ClientHandler(socket, this);
            }

        // Сервер упал
        } catch (IOException e) {
            broadcast(systemUser, "Неполадки на сервере, отключение...", systemUser);
            for (ClientHandler client : clients) {
                client.disconnect(false);
            }
            try {
                socket.close();
            } catch (Exception ignore) {}
            try {
                server.close();
            } catch (Exception ignore) {}

        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    void addClient(ClientHandler client) {
        clients.add(client);
        sendClientList();
    }

    void removeClient(ClientHandler client) {
        try {
            clients.remove(client);
        } catch (Exception ignore) {}
        sendClientList();
    }

    // Отправка на всех (не более 1 исключения, в качестве исключения во избежание NPE обычно передается псевдопользователь systemUser)
    void broadcast(Connectable sender, String msg, Connectable ignored) {
        for (ClientHandler client : clients) {
            try {
                if (!client.getNickname().equals(ignored.getNickname())) {
                    sendMsg(sender, client, msg);
                }
            } catch (NullPointerException ignore) {}

        }
    }

    // Оболочка для отправки сообщения, чтобы один клиент не "видел" другого
    private void sendMsg(Connectable sender, Connectable receiver, String msg) {
        receiver.sendMsg(sender.getNickname(), msg);
    }

    // Отправка ЛС
    void privateMessage(Connectable sender, Connectable receiver, String msg) {
        sender.sendMsg(sender.getNickname(), receiver.getNickname() + ", " + msg);
        receiver.sendMsg(sender.getNickname(),receiver.getNickname() + ", " + msg);
    }

    // Получаем клиента по его нику
    ClientHandler getClientByNick(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(nick)) {
                    return client;
            }
        }

        // Клиент не найден
        return null;
    }

    boolean isClientOnline(String login) {
        boolean res = false;

        for (ClientHandler client : clients) {
            if (client.getLogin() == login) {
                res = true;
                break;
            }
        }

        return res;
    }

    void sendClientList() {
        StringBuilder str = new StringBuilder();
        str.append("/clist");
        for (ClientHandler client : clients) {
            str.append(" " + client.getNickname());
        }
        broadcast(systemUser, str.toString(), systemUser);
    }
}

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientHandler implements Connectable {
    Server server;
    Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private String nick;
    String login;

    private final int TIMEOUT = 120000; // Срок жизни неактивного сокета

    private final int MSG_COMMON = 0;   // Обычное сообщение
    private final int MSG_AUTH = 1;     // Зарос на авторизацию
    private final int MSG_END = 2;      // Запрос на выход из чата
    private final int MSG_WHISPER = 3;  // Запрос на личное сообщение
    private final int MSG_REG = 4;      // Запрос на регистрацию
    private final int MSG_NICK = 5;     // Запрос на смену ника

    ClientHandler(Socket _socket, Server _server) {
        server = _server;
        socket = _socket;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            new Thread(() -> {

                String[] tokens;
                String str;
                try {
                    socket.setSoTimeout(TIMEOUT);
                    // Авторизация
                    while (true) {
                        str = in.readUTF();

                        // Ловим сообщение на авторизацию...
                        if (parseMessage(str) == MSG_AUTH) {
                            tokens = str.split(" ");
                            String newNick = server.getAuthService().getNickname(tokens[1], tokens[2]);

                            // Успех!
                            if (newNick != null) {

                                // Если логин "*" - клиент уже залогинен
                                if (!newNick.equals("*")) {
                                    authOk(tokens[1], newNick);
                                    break;
                                } else {
                                    sysMsg("Этот пользователь уже в сети!");
                                }

                            // Неудача!
                            } else {
                                sysMsg("Неверный логин/пароль");
                            }
                        } else if (parseMessage(str) == MSG_REG) {
                            tokens = str.split(" ", 4);
                            boolean regResult = server.getAuthService().register(tokens[1], tokens[2], tokens[3]);
                            if (!regResult) {
                                sysMsg("Такой логин или пароль или никнейм уже занят");
                            }
                        }

                    }

                    // Рабочий цикл чата
                    while (true) {
                        str = in.readUTF();

                        // Парсим пришедшее сообщшние
                        switch (parseMessage(str)) {
                            // Запрос на выход из чата
                            case MSG_END:
                                disconnect(false);
                                break;
                            // Запрос на личное сообщение
                            case MSG_WHISPER:
                                tokens = str.split(" ", 3);
                                if (tokens.length == 3) {
                                    server.privateMessage(this, server.getClientByNick(tokens[1]), tokens[2]);
                                }
                                break;
                            case MSG_NICK:
                                tokens = str.split(" ", 2);
                                boolean nickResult = server.getAuthService().changeNickname(nick, tokens[1]);

                                if (nickResult) {
                                    nickOk(tokens[1]);
                                } else {
                                    sysMsg("Не удалось изменить никнейм!");
                                }
                                break;
                            // Обычное сообщение
                            case MSG_COMMON:
                                server.broadcast(this, str, server.systemUser);
                                break;
                        }

                        // Проверяем, не вышли ли мы еще из чата
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Клиент вышел по таймауту");
                    sysMsg("Вы слишком долго были неактивны, отключение...");
                    disconnect(true);
                } catch (IOException e) {
                    disconnect(true);
                }
            }).start();

        } catch (IOException e) {
            disconnect(false);
        }
    }

    @Override
    public void sendMsg(String sender, String msg) {
        try {
            if (msg.startsWith("/")) {
                out.writeUTF(msg);
            } else {
                out.writeUTF(sender + ": " + msg);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sysMsg(String msg) {
        sendMsg(server.systemUser.getNickname(), msg);
    }

    void disconnect(boolean silent) {
        try {
            // Прощаемся
            if (!silent) {
                sysMsg("Отключение...");
            }

            // Уведомляем остальных
            server.broadcast(server.systemUser, nick + " вышел из чата", this);

            // Подчищаем за клиентом
            socket.close();
            server.removeClient(this);
            System.out.println(this.nick + " ушел");
        } catch (IOException ignore) {}
    }

    @Override
    public String getNickname() {
        return nick;
    }

    private int parseMessage(String msg) {
        int res = MSG_COMMON; // По умолчанию обычное сообщение

        if (msg.startsWith("/auth ")) { // Зарос на авторизацию
            res = MSG_AUTH;
        } else if (msg.equals("/end")){ // Запрос на выход из чата
            res = MSG_END;
        } else if (msg.startsWith("/w ")) { // Запрос на личное сообщение
            res = MSG_WHISPER;
        } else if (msg.startsWith("/reg ")) { // Запрос на регистрацию
            res = MSG_REG;
        } else if (msg.startsWith("/nick ")) { // Запрос на регистрацию
            res = MSG_NICK;
        }

        return res;
    }

    String getLogin() {
        return login;
    }

    private void authOk(String _login, String _nick) {
        nick = _nick;
        login = _login;

        // Отправляем клиенту его ник. Здесь отправитель роли не играет
        sendMsg(nick, "/authok " + nick);
        System.out.println("Клиент " + nick + " прошел аутентификацию");

        server.addClient(this);

        // Всем кроме себя сообщаем о своем входе в чат
        server.broadcast(server.systemUser, nick + " вошел в чат", this);

        // А потом отправляем себе приветствие
        sysMsg("Добро пожаловать в чат, " + nick);

        try {
            socket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void nickOk(String newNick) {
        // Cообщаем всем, что мы переименовались
        server.broadcast(server.systemUser, nick + " меняет никнейм на \"" + newNick + "\"!", this);

        // Себе любимому другой текст и команду на смену титула
        sysMsg("/nickok " + newNick);
        sysMsg("Ваш никнейм изменен на \"" + newNick + "\"!");

        // Обновляем здесь никнейм и обновляем списки у всех
        nick = newNick;
        server.sendClientList();
    }
}

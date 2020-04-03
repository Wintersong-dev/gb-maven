import java.util.ArrayList;
import java.util.List;

public class SimpleAuth implements AuthService {

    private class UserData {
        String login, pwd, nickname;
        UserData(String _login, String _pwd, String _nickname) {
            login = _login;
            pwd = _pwd;
            nickname = _nickname;
        }
    }

    List<UserData> users;
    Server server;

    SimpleAuth(Server _server) {
        users = new ArrayList<>();
        server = _server;

        for (int i = 0; i < 10; i++) {
            users.add(new UserData("login" + i, "password" + i, "nickname" + i));
        }

    }

    @Override
    public String getNickname(String login, String pwd) {
        for(UserData user : users) {
            if (user.login.equals(login) && user.pwd.equals(pwd)) {
                if (server.isClientOnline(login)) {
                    return "*";
                }
                return user.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean register(String _login, String _pwd, String _nick) {
        for(UserData user : users) {
            if (user.login.equals(_login) || user.nickname.equals(_nick)) {
                return false;
            }
        }

        users.add(new UserData(_login, _pwd, _nick));
        System.out.println("Пользователь зарегистрирован: " + _login + "/" + _pwd);
        return true;
    }
}

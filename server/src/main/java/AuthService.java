public interface AuthService {
    String getNickname(String login, String pwd);

    boolean register(String _login, String _pwd, String _nick);

    boolean changeNickname(String oldNick, String newNick);
}

import java.sql.*;
import java.util.List;

public class DBAuthService implements AuthService {

    private static Connection conn;
    private static Statement stmt;
    private static PreparedStatement ps;
    private Server server;

    DBAuthService(Server srv) {
        server = srv;

        try {
            connect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            // Если таблица users еще не создана (запрос выдаст ошибку Table or view does not exist), то надо ее создать
            exec("update users set username = username where 1 = 0");
        } catch (SQLException ex) {
            init();
        }
    }

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        conn = DriverManager.getConnection("jdbc:sqlite:sqlitedb.db");

        stmt = conn.createStatement();
    }

    private void init() {
        try {
            exec("create table users(id integer primary key autoincrement, username varchar2(50), password varchar2(50), nickname varchar2(50))");
            for (int i = 0; i < 5; i++) {
                exec("insert into users(username, password, nickname) values ('login"+i+"', 'pwd"+i+"', 'nickname"+i+"')");
            }

        } catch (SQLException ex) {
            System.out.println("При создании таблицы с пользователями что-то пошло не так");
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public String getNickname(String login, String pwd) {
        String nickname = runQueryWithStringResult("select nickname from users where username = ? and password = ?", login, pwd);
        if (server.isClientOnline(login)) {
            return "*";
        }
        return nickname;
    }

    @Override
    public boolean register(String _login, String _pwd, String _nick) {
        // Проверяем, есть ли такой юзер
        int cnt = runQueryWithIntResult("select count(*) from users where username = username = ? and nickname = ?", _login, _nick);
        if (cnt > 0) {
            return false;
        }

        // Регистрируем...
        exec("insert into users(username, password, nickname) values (?, ?, ?)", _login, _pwd, _nick);

        System.out.println("Пользователь зарегистрирован: " + _login + "/" + _pwd);
        return true;
    }

    @Override
    public boolean changeNickname(String oldNick, String newNick) {
        boolean res;

        int cnt = runQueryWithIntResult("select count(*) from users where nickname = ?", newNick);
        if (cnt > 0) {
            res = false;
        } else {
            exec("update users set nickname = ? where nickname = ?", newNick, oldNick);
            res = true;
        }

        return res;
    }

    private void exec(String query) throws SQLException {
        stmt.executeUpdate(query);
    }

    private void exec(String query, String... params)  {

        try {
            ps = conn.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("При выполнении запроса на изменение произошла ошибка: " + e.getMessage());
        }
    }

    private String runQueryWithStringResult(String query, String... params) {
        String returnVal = null;
        try {
            ResultSet res = runSelectQuery(query, params);

            boolean b = res.next();
            returnVal = res.getString(1);
            res.close();
        } catch (SQLException e) {
            System.out.println("SQL: No data found");
        }

        return returnVal;
    }

    private int runQueryWithIntResult(String query, String... params) {
        int returnVal = -9999;
        try {
            ResultSet res = runSelectQuery(query, params);

            boolean b = res.next();
            returnVal = res.getInt(1);
            res.close();
        } catch (SQLException e) {
            System.out.println("SQL: No data found");
        }

        return returnVal;
    }

    private ResultSet runSelectQuery(String query, String... params) throws SQLException {
        ps = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }

        ResultSet res = ps.executeQuery();
        return res;
    }
}

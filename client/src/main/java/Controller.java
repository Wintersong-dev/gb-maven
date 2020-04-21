import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    public Button changeNickname;
    @FXML
    private HBox authPanel;
    @FXML
    private HBox msgPanel;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ListView clientList;
    @FXML
    public TextArea textArea;
    @FXML
    private TextField msgField;

    private final String IP_ADDR = "localhost";
    private final int PORT = 9876;
    private final String NO_AUTH = "Chat lobby";
    private final int MSG_COMMON = 0;
    private final int MSG_AUTHOK = 1;
    private final int MSG_CLIST = 2;
    private final int MSG_NICKOK = 3;


    private boolean isAuth = false;
    private String nickname;
    private FileManager fm;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    Stage regStage;
    Stage nickStage;

    public void setAuth(boolean _auth) {
        isAuth = _auth;
        authPanel.setVisible(!_auth);
        authPanel.setManaged(!_auth);
        msgPanel.setVisible(_auth);
        msgPanel.setManaged(_auth);
        clientList.setVisible(_auth);
        clientList.setManaged(_auth);

        if (!_auth) {
            setNickname("");
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        fm.setNickname(nickname);
        setTitle("Chat: " + nickname);
    }

    public void auth() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF("/auth " + loginField.getText().trim() + " " + passwordField.getText().trim());
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg()  {
        try {
            sendMsgEx(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setTitle(NO_AUTH);
        fm = new FileManager();
        Platform.runLater(() -> {
            Window scene = textArea.getScene().getWindow();
            scene.setOnCloseRequest(windowEvent -> {
                try {
                    socket.close();
                    fm.closeFile();
                    setAuth(false);
                    regStage = null;
                    nickStage = null;
                } catch (IOException | NullPointerException ignore) {}
            });
        });


    }

    public void connect() {
        try {
            socket = new Socket(IP_ADDR, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                String str;
                try {
                    textArea.clear();
                    while (true) {
                        str = in.readUTF();
                        if (parseMsg(str) == MSG_AUTHOK) {
                            setNickname(str.split(" ")[1]);
                            setAuth(true);

                            break;
                        }
                        textArea.setText(str);
                    }

                    List<String> textFromFile = fm.readFile();
                    int len = textFromFile.size();

                    // Оставляем только последние 100
                    if (len > 100) {
                        for (int i = 0; i < (len - 100); i++) {
                            textFromFile.remove(0);
                        }
                    }

                    for (String elem : textFromFile) {
                        textArea.appendText(elem + "\n");
                    }

                    Platform.runLater(() -> {
                        msgField.requestFocus();
                    });

                    while (true) {
                        receiveMsg();
                    }
                } catch (IOException e) {
                    try {
                        socket.close();
                        setAuth(false);
                    } catch (IOException ignore) {}
                }

            }).start();


        } catch (IOException e) {
            setAuth(false);
            try {
                socket.close();
                setTitle(NO_AUTH);
                fm.closeFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void receiveMsg() throws IOException {

        String str = in.readUTF();
        switch (parseMsg(str)) {
            case MSG_CLIST:
                Platform.runLater(() -> {
                    clientList.getItems().clear();

                    String[] msg = str.split(" ");
                    for (int i = 0; i < msg.length; i++) {
                        if (i != 0) {
                            clientList.getItems().add(msg[i]);
                        }
                    }
                });
                break;
            case MSG_NICKOK:
                String[] msg = str.split(" ");
                setNickname(msg[1]);
                break;
            case MSG_COMMON:
                textArea.appendText(str + "\n");
                fm.append(str + "\n");
        }
    }

    void setTitle(String title) {
        Platform.runLater(() -> {
            ((Stage)textArea.getScene().getWindow()).setTitle(title);
        });
    }

    public void sendPM(MouseEvent mouseEvent) {
        msgField.setText("/w " + clientList.getSelectionModel().getSelectedItem().toString() + " ");
        msgField.requestFocus();
    }

    private Stage createRegWindow() {

        Parent root = null;
        Stage stage = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            root = loader.load();
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);

            RegController regController = loader.getController();
            regController.controller = this;

            stage.setTitle("Регистрация");
            stage.setScene(new Scene(root, 300, 275));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stage;
    }

    public void requestReg(ActionEvent actionEvent) {
        if (regStage == null) {
            regStage = createRegWindow();
        }
        regStage.show();
    }

    void tryReg(String login, String pwd, String nick) {
        String msg = String.format("/reg %s %s %s", login, pwd, nick);
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            sendMsgEx(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int parseMsg(String msg) {
        int res = MSG_COMMON;
        if (msg.startsWith("/authok ")) {
            res = MSG_AUTHOK;
        } else if (msg.startsWith("/clist ")) {
            res = MSG_CLIST;
        } else if (msg.startsWith("/nickok ")) {
            res = MSG_NICKOK;
        }

        return res;
    }

    private void sendMsgEx(String msg) throws IOException {
        out.writeUTF(msg);
        out.flush();
    }

    public void changeNick(ActionEvent actionEvent) {
        if (nickStage == null) {
            nickStage = createNickWindow();
        }
        nickStage.show();
    }

    private Stage createNickWindow() {

        Parent root = null;
        Stage stage = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nick.fxml"));
            root = loader.load();
            stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);

            NickController nickController = loader.getController();
            nickController.controller = this;

            stage.setTitle("Смена ника");
            stage.setScene(new Scene(root, 125, 100));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stage;
    }

    void tryChangeNick(String nick) {
        String msg = String.format("/nick %s", nick);
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            sendMsgEx(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

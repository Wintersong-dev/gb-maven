import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegController {
    Controller controller;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public PasswordField passwordField2;
    @FXML
    public TextField nickField;

    public void reg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String pwd = passwordField.getText().trim();
        String pwd2 = passwordField2.getText().trim();
        String nick = nickField.getText().trim();

        if (!pwd.equals(pwd2)) {
            controller.textArea.appendText("Пароли не совпадают!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }
        if (login.length() == 0) {
            controller.textArea.appendText("Логин не может быть пустым!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }
        if (pwd.length() == 0) {
            controller.textArea.appendText("Пароль не может быть пустым!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }
        if (nick.length() == 0) {
            controller.textArea.appendText("Никнейм не может быть пустым!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }
        if (login.startsWith("/")) {
            controller.textArea.appendText("Логин не может начинаться со знака \"\\\"!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }
        if (nick.startsWith("/")) {
            controller.textArea.appendText("Никнейм не может начинаться со знака \"\\\"!\n");
            ((Stage) loginField.getScene().getWindow()).close();
            return;
        }

        controller.tryReg(login, pwd, nick);
        ((Stage) loginField.getScene().getWindow()).close();
    }
}

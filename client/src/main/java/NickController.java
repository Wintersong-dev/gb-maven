import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NickController {
    Controller controller;

    @FXML
    public TextField newNickField;

    public void changeNick(ActionEvent actionEvent) {
        String nick = newNickField.getText().trim();

        if (nick.length() == 0) {
            controller.textArea.appendText("Никнейм не может быть пустым!\n");
            ((Stage) newNickField.getScene().getWindow()).close();
            return;
        }
        if (nick.startsWith("/")) {
            controller.textArea.appendText("Никнейм не может начинаться со знака \"\\\"!\n");
            ((Stage) newNickField.getScene().getWindow()).close();
            return;
        }

        controller.tryChangeNick(nick);
        ((Stage) newNickField.getScene().getWindow()).close();
    }
}

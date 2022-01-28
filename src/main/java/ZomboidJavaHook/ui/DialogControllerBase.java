package ZomboidJavaHook.ui;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public abstract class DialogControllerBase {
    double xInit;
    double yInit;

    protected abstract Window getWindow();

    @FXML
    void onMousePress(MouseEvent event) {
        xInit = getWindow().getX() - event.getScreenX();
        yInit = getWindow().getY() - event.getScreenY();
    }

    @FXML
    void onMouseMove(MouseEvent event) {
        getWindow().setX(xInit + event.getScreenX());
        getWindow().setY(yInit + event.getScreenY());
    }
}

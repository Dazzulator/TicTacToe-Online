package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Main.class.getResource("/sample/ticTacToe.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 300, 300);

        Controller controller = fxmlLoader.getController();

        primaryStage.setTitle("TicTacToe");
        primaryStage.resizableProperty().setValue(Boolean.FALSE);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((event -> {
            controller.disconnect();
            Platform.exit();
            System.exit(0);
        }));
    }


    public static void main(String[] args) {
        launch(args);
    }
}

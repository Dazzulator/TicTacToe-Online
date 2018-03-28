package sample;

import com.bojan.gameclient.ClientSideGameMessageListener;
import com.bojan.gameclient.GameClient;
import com.bojan.gameserver.GameServer;
import com.bojan.gameserver.ServerSideGameMessageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

public class Controller {
    private ImageView imageView = new ImageView(Controller.class.getResource("/tic-tac-toe-md.png").toString());
    private int[][] matrix = {{-1, -2, -3}, {-4, -5, -6}, {-7, -8, -9}};

    public Map<String, String> players = new HashMap<>();

    public GameServer<Message> gameServer;
    public GameClient<Message> gameClient;

    public enum State {WAIT, ACTION, END};

    public State currentState = State.WAIT;

    public String playerType;

    @FXML
    private StackPane stackPane;

    @FXML
    private Button hostButton;

    @FXML
    private Button connectButton;

    @FXML
    private TextField ipAddress;

    @FXML
    private ImageView iv00;

    @FXML
    private ImageView iv01;

    @FXML
    private ImageView iv02;

    @FXML
    private ImageView iv10;

    @FXML
    private ImageView iv11;

    @FXML
    private ImageView iv12;

    @FXML
    private ImageView iv20;

    @FXML
    private ImageView iv21;

    @FXML
    private ImageView iv22;

    @FXML
    private void initialize() {
        Pane pane = (Pane) stackPane.getChildren().get(1);
        pane.setVisible(true);
        pane.setDisable(false);
        Pane gridPane = (Pane) stackPane.getChildren().get(0);
        gridPane.setVisible(false);
        gridPane.setDisable(true);
    }

    @FXML
    public void clickHost() throws Exception {
        playerType = "host";
        goToGame();
        createServer();
        createClient();
    }

    @FXML
    public void clickConnect() throws Exception {
        playerType = "guest";
        goToGame();
        createClient();
    }

    @FXML
    public void click00() {
        setup(iv00);
    }

    @FXML
    public void click01() {
        setup(iv01);
    }

    @FXML
    public void click02() {
        setup(iv02);
    }

    @FXML
    public void click10() {
        setup(iv10);
    }

    @FXML
    public void click11() {
        setup(iv11);
    }

    @FXML
    public void click12() {
        setup(iv12);
    }

    @FXML
    public void click20() {
        setup(iv20);
    }

    @FXML
    public void click21() {
        setup(iv21);
    }

    @FXML
    public void click22() {
        setup(iv22);
    }

    private void createServer() {
        gameServer = new GameServer<>(8080, "", Message.class);
        gameServer.setGameMessageListener(new ServerSideGameMessageListener<Message>() {
            Object lock = new Object();
            int counter = 0;
            int turnNumber = 0;
            String host;
            @Override
            public void onConnect(String s) {
                synchronized (lock) {
                    counter++;
                    if (counter == 1) {
                        players.put(s, "iks");
                        host = s;
                    } else if (counter == 2) {
                        players.put(s, "oks");
                        Map<String, String> map = new HashMap<>();
                        map.put("turn", "");
                        Message message = new Message(MessageType.TURN_MSG, map);
                        gameServer.sendMessageToPlayer(host, message);
                    }
                }
            }

            @Override
            public void onDisconnect(String s, String s1) {
                synchronized (lock) {

                }
            }

            @Override
            public void onMessage(String s, Message message) {
                synchronized (lock) {
                    if (message.type == MessageType.MOVE_MSG) {
                        turnNumber++;
                        int i = Integer.valueOf(message.parameters.get("coord_i"));
                        int j = Integer.valueOf(message.parameters.get("coord_j"));
                        if (matrix[i][j] < 0) {
                            String sign = players.get(s);
                            if (sign.equals("iks")) {
                                matrix[i][j] = 0;
                                Map<String, String> drawMap = new HashMap<>();
                                drawMap.put("draw", "iks");
                                drawMap.put("coord_i", String.valueOf(i));
                                drawMap.put("coord_j", String.valueOf(j));
                                Message drawMessage = new Message(MessageType.DRAW_MSG, drawMap);
                                gameServer.sendMessageToAllPlayers(drawMessage);
                                Map<String, String> mapNext = new HashMap<>();
                                mapNext.put("turn", "");
                                Message turnMessage = new Message(MessageType.TURN_MSG, mapNext);
                                gameServer.sendMessageToAllOtherPlayers(s, turnMessage);
                                checkForGameOver(turnNumber);
                            } else if (sign.equals("oks")) {
                                matrix[i][j] = 1;
                                Map<String, String> drawMap = new HashMap<>();
                                drawMap.put("draw", "oks");
                                drawMap.put("coord_i", String.valueOf(i));
                                drawMap.put("coord_j", String.valueOf(j));
                                Message drawMessage = new Message(MessageType.DRAW_MSG, drawMap);
                                gameServer.sendMessageToAllPlayers(drawMessage);
                                Map<String, String> mapNext = new HashMap<>();
                                mapNext.put("turn", "");
                                Message turnMessage = new Message(MessageType.TURN_MSG, mapNext);
                                gameServer.sendMessageToAllOtherPlayers(s, turnMessage);
                                checkForGameOver(turnNumber);
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(String s, String s1) {
                synchronized (lock) {

                }
            }
        });
        gameServer.start();
    }

    private void goToGame() throws Exception {
        Pane pane = (Pane) stackPane.getChildren().get(1);
        pane.setVisible(false);
        pane.setDisable(true);
        Pane gridPane = (Pane) stackPane.getChildren().get(0);
        gridPane.setVisible(true);
        gridPane.setDisable(false);
    }

    private void createClient() {
        String address;

        if (playerType.equals("host")) {
            address = "localhost";
        } else {
            address = ipAddress.getText();
        }

        gameClient = new GameClient<>(address, 8080, "", Message.class);
        gameClient.setGameMessageListener(new ClientSideGameMessageListener<Message>() {
            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect(String s) {

            }

            @Override
            public void onMessage(Message message) {
                switch (message.type) {
                    case TURN_MSG:
                        currentState = State.ACTION;
                        break;
                    case DRAW_MSG:
                        drawSign(message.parameters.get("draw"), Integer.valueOf(message.parameters.get("coord_i")), Integer.valueOf(message.parameters.get("coord_j")));
                        break;
                    case END_MSG:
                        gameClient.disconnect();
                        Platform.exit();
                        System.exit(0);
                        break;
                }
            }

            @Override
            public void onError(String s) {

            }
        });
        gameClient.connect();
    }

    private void setup(ImageView view) {
        switch (currentState) {
            case WAIT:
                break;
            case ACTION:
                int i = -1;
                int j = -1;
                Map<String, String> map = new HashMap<>();
                if (view.equals(iv00)) {
                    i = 0;
                    j = 0;
                } else if (view.equals(iv01)) {
                    i = 0;
                    j = 1;
                } else if (view.equals(iv02)) {
                    i = 0;
                    j = 2;
                } else if (view.equals(iv10)) {
                    i = 1;
                    j = 0;
                } else if (view.equals(iv11)) {
                    i = 1;
                    j = 1;
                } else if (view.equals(iv12)) {
                    i = 1;
                    j = 2;
                } else if (view.equals(iv20)) {
                    i = 2;
                    j = 0;
                } else if (view.equals(iv21)) {
                    i = 2;
                    j = 1;
                } else if (view.equals(iv22)) {
                    i = 2;
                    j = 2;
                }
                map.put("coord_i", String.valueOf(i));
                map.put("coord_j", String.valueOf(j));
                Message message = new Message(MessageType.MOVE_MSG, map);
                gameClient.sendMessage(message);
                currentState = State.WAIT;
                break;
            case END:
                break;
        }
    }

    private void drawSign(String s, int i, int j) {
        if (s.equals("iks")) {
            if (i == 0 && j == 0) {
                iv00.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv00.setImage(imageView.getImage());
            } else if (i == 0 && j == 1) {
                iv01.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv01.setImage(imageView.getImage());
            } else if (i == 0 && j == 2) {
                iv02.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv02.setImage(imageView.getImage());
            } else if (i == 1 && j == 0) {
                iv10.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv10.setImage(imageView.getImage());
            } else if (i == 1 && j == 1) {
                iv11.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv11.setImage(imageView.getImage());
            } else if (i == 1 && j == 2) {
                iv12.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv12.setImage(imageView.getImage());
            } else if (i == 2 && j == 0) {
                iv20.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv20.setImage(imageView.getImage());
            } else if (i == 2 && j == 1) {
                iv21.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv21.setImage(imageView.getImage());
            } else if (i == 2 && j == 2) {
                iv22.setViewport(new Rectangle2D(150, 0, 150, 135));
                iv22.setImage(imageView.getImage());
            }
        } else if (s.equals("oks")) {
            if (i == 0 && j == 0) {
                iv00.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv00.setImage(imageView.getImage());
            } else if (i == 0 && j == 1) {
                iv01.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv01.setImage(imageView.getImage());
            } else if (i == 0 && j == 2) {
                iv02.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv02.setImage(imageView.getImage());
            } else if (i == 1 && j == 0) {
                iv10.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv10.setImage(imageView.getImage());
            } else if (i == 1 && j == 1) {
                iv11.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv11.setImage(imageView.getImage());
            } else if (i == 1 && j == 2) {
                iv12.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv12.setImage(imageView.getImage());
            } else if (i == 2 && j == 0) {
                iv20.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv20.setImage(imageView.getImage());
            } else if (i == 2 && j == 1) {
                iv21.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv21.setImage(imageView.getImage());
            } else if (i == 2 && j == 2) {
                iv22.setViewport(new Rectangle2D(0, 0, 150, 135));
                iv22.setImage(imageView.getImage());
            }
        }
    }

    private void checkForGameOver(int number) {
        if (matrix[0][0] == 0 && matrix[0][1] == 0 && matrix[0][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][0] == 1 && matrix[0][1] == 1 && matrix[0][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][0] == 0 && matrix[1][0] == 0 && matrix[2][0] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][0] == 1 && matrix[1][0] == 1 && matrix[2][0] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][0] == 0 && matrix[1][1] == 0 && matrix[2][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][0] == 1 && matrix[1][1] == 1 && matrix[2][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][1] == 0 && matrix[1][1] == 0 && matrix[2][1] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][1] == 1 && matrix[1][1] == 1 && matrix[2][1] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][2] == 0 && matrix[1][2] == 0 && matrix[2][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[0][2] == 1 && matrix[1][2] == 1 && matrix[2][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[2][0] == 0 && matrix[1][1] == 0 && matrix[0][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[2][0] == 1 && matrix[1][1] == 1 && matrix[0][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[1][0] == 0 && matrix[1][1] == 0 && matrix[1][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[1][0] == 1 && matrix[1][1] == 1 && matrix[1][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[2][0] == 0 && matrix[2][1] == 0 && matrix[2][2] == 0) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        } else if (matrix[2][0] == 1 && matrix[2][1] == 1 && matrix[2][2] == 1) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
        }

        if (number == 9) {
            Message endMessage = new Message(MessageType.END_MSG, null);
            gameServer.sendMessageToAllPlayers(endMessage);
            gameServer.stop();
        }
    }

    public void disconnect() {
        if (gameClient != null) {
            gameClient.disconnect();
        }
        if (gameServer != null) {
            gameServer.stop();
        }
    }
}

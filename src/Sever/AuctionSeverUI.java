// src/Sever/AuctionSeverUI.java
package Sever;

import Database.DatabaseManager;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

import com.google.gson.*;

/**
 * JavaFX replacement for the previous Swing server UI.
 * - Left 2/3: visualization (earth, nodes, links)
 * - Right 1/3: controls (create auction, view status) and summary labels
 *
 * This class implements AuctionResultListener so AuctionSession and AuctionServer
 * can call it for updates.
 */
public class AuctionSeverUI extends Application implements AuctionResultListener {

    // visualization
    private Canvas canvas;
    private GraphicsContext gc;
    private Map<String, NodeVisual> nodeVisuals = new LinkedHashMap<>();
    private Map<String, LinkVisual> linkVisuals = new LinkedHashMap<>();

    // controls / status
    private Label lbRevenue = new Label("Revenue: 0.0");
    private Label lbResources = new Label("Resources: 0.0/0.0");
    private Label lbCountdown = new Label("");
    private VBox detailsBox = new VBox(6);
    private Button btnCreateAuction = new Button("Create Auction");
    private Button btnViewStatus = new Button("View Status");

    // server marker
    private Circle serverDot;

    // timer
    private Timeline auctionCountdown;
    // per-node pulse animations
    private Map<String, Timeline> nodePulseTimelines = new HashMap<>();

    // simple concurrent state
    // set of connected node ids (reserved for future use)
    // private Set<String> connectedNodes = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        // ensure DB connection early
        DatabaseManager.getInstance().getConnection();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Auction Server (JavaFX)");

        BorderPane root = new BorderPane();

    // left: visualization with overlay pane for animations
    StackPane left = new StackPane();
    canvas = new Canvas(900, 700);
    gc = canvas.getGraphicsContext2D();
    Pane overlayPane = new Pane();
    overlayPane.setPickOnBounds(false); // allow mouse events reach canvas
    left.getChildren().addAll(canvas, overlayPane);

    // load nodes/links
    loadNodesAndLinks();
    drawScene();

    canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);

    root.setCenter(left);

        // right: controls and info
        VBox right = new VBox(10);
        right.setPadding(new Insets(12));
        right.setPrefWidth(360);

        lbRevenue.setFont(Font.font(16));
        lbResources.setFont(Font.font(16));
        lbCountdown.setFont(Font.font(18));

    HBox topSummary = new HBox(10, lbRevenue, lbResources);
        topSummary.setPadding(new Insets(6));

        btnCreateAuction.setMaxWidth(Double.MAX_VALUE);
        btnViewStatus.setMaxWidth(Double.MAX_VALUE);

        btnCreateAuction.setOnAction(e -> {
            AuctionServer.getInstance().setUi(this);
            new CreateAuctionDialogFX(primaryStage);
        });

        btnViewStatus.setOnAction(e -> new ShowStatusViewFX());

    right.getChildren().addAll(topSummary, lbCountdown, detailsBox, btnCreateAuction, btnViewStatus);

    detailsBox.setPadding(new Insets(8));
    detailsBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius:4; -fx-background-color: rgba(255,255,255,0.04);");
    detailsBox.getChildren().add(new Label("Hover a node to see details"));

    // server dot overlay (place on overlayPane)
    serverDot = new Circle(10, Color.ORANGE);
    serverDot.setOpacity(0.9);
    serverDot.setLayoutX(30);
    serverDot.setLayoutY(30);
    overlayPane.getChildren().add(serverDot);

        root.setRight(right);

    Scene scene = new Scene(root, 1300, 720);
        primaryStage.setScene(scene);
        primaryStage.show();

    // register this UI with server immediately so labels and events flow
    AuctionServer.getInstance().setUi(this);
    // update initial labels from server
    Update();

        // start TCP server in background thread (catch bind errors and report)
        new Thread(() -> {
            try {
                AuctionServer.getInstance().start();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Server failed to start: " + ex.getCause(), ButtonType.OK);
                    a.setHeaderText("Server start error");
                    a.showAndWait();
                });
            }
        }).start();
    }

    // load nodes.json and links.json and create visual objects
    private void loadNodesAndLinks() {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        try (FileReader r = new FileReader("src/Data/nodes.json")) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            Random rnd = new Random(12345);
            // random positions around a circle (earth)
            double centerX = 450, centerY = 350, radius = 220;
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String id = o.get("id").getAsString();
                double angle = rnd.nextDouble() * Math.PI * 2;
                double x = centerX + Math.cos(angle) * (radius + rnd.nextDouble() * 40);
                double y = centerY + Math.sin(angle) * (radius + rnd.nextDouble() * 40);
                NodeVisual nv = new NodeVisual(id, x, y, o);
                nodeVisuals.put(id, nv);
            }
            System.out.println("Loaded " + nodeVisuals.size() + " nodes for visualization.");
        } catch (Exception ex) {
            System.out.println("Failed to read src/Data/nodes.json directly: " + ex.getMessage());
            // fallback: try classpath locations
            InputStream is = AuctionSeverUI.class.getClassLoader().getResourceAsStream("Data/nodes.json");
            if (is == null) is = AuctionSeverUI.class.getClassLoader().getResourceAsStream("nodes.json");
            if (is != null) {
                try (java.io.InputStreamReader ir = new java.io.InputStreamReader(is)) {
                    JsonArray arr = JsonParser.parseReader(ir).getAsJsonArray();
                    Random rnd = new Random(12345);
                    double centerX = 450, centerY = 350, radius = 220;
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        String id = o.get("id").getAsString();
                        double angle = rnd.nextDouble() * Math.PI * 2;
                        double x = centerX + Math.cos(angle) * (radius + rnd.nextDouble() * 40);
                        double y = centerY + Math.sin(angle) * (radius + rnd.nextDouble() * 40);
                        NodeVisual nv = new NodeVisual(id, x, y, o);
                        nodeVisuals.put(id, nv);
                    }
                    System.out.println("Loaded " + nodeVisuals.size() + " nodes for visualization (from classpath).");
                } catch (Exception ex2) { ex2.printStackTrace(); }
            }
        }

        try (FileReader r = new FileReader("src/Data/links.json")) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject o = el.getAsJsonObject();
                String id = o.get("id").getAsString();
                String tx = o.get("tx").getAsString();
                String rx = o.get("rx").getAsString();
                NodeVisual a = nodeVisuals.get(tx);
                NodeVisual b = nodeVisuals.get(rx);
                if (a != null && b != null) {
                    LinkVisual lv = new LinkVisual(id, a, b);
                    linkVisuals.put(id, lv);
                }
            }
                System.out.println("Loaded " + linkVisuals.size() + " links for visualization.");
        } catch (Exception ex) {
            System.out.println("Failed to read src/Data/links.json directly: " + ex.getMessage());
            InputStream is = AuctionSeverUI.class.getClassLoader().getResourceAsStream("Data/links.json");
            if (is == null) is = AuctionSeverUI.class.getClassLoader().getResourceAsStream("links.json");
            if (is != null) {
                try (java.io.InputStreamReader ir = new java.io.InputStreamReader(is)) {
                    JsonArray arr = JsonParser.parseReader(ir).getAsJsonArray();
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        String id = o.get("id").getAsString();
                        String tx = o.get("tx").getAsString();
                        String rx = o.get("rx").getAsString();
                        NodeVisual a = nodeVisuals.get(tx);
                        NodeVisual b = nodeVisuals.get(rx);
                        if (a != null && b != null) {
                            LinkVisual lv = new LinkVisual(id, a, b);
                            linkVisuals.put(id, lv);
                        }
                    }
                    System.out.println("Loaded " + linkVisuals.size() + " links for visualization (from classpath).");
                } catch (Exception ex2) { ex2.printStackTrace(); }
            }
        }
    }

    private void drawScene() {
        Platform.runLater(() -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            // draw earth
            gc.setFill(Color.web("#0b3d91"));
            gc.fillOval(200, 120, 500, 500);

            // draw links
            for (LinkVisual l : linkVisuals.values()) l.draw(gc);

            // draw nodes
            for (NodeVisual n : nodeVisuals.values()) n.draw(gc);

            // draw server dot top-left of canvas
            gc.setFill(Color.ORANGE);
            gc.fillOval(20, 20, 14, 14);
        });
    }

    private void onMouseMoved(MouseEvent e) {
        // show tooltip-like details when hovering over node
        double mx = e.getX(), my = e.getY();
        Optional<NodeVisual> found = nodeVisuals.values().stream()
                .filter(n -> n.contains(mx, my)).findFirst();
        if (found.isPresent()) {
            NodeVisual n = found.get();
            String info = n.getInfo();
            lbCountdown.setText(info);
            // render all properties in detailsBox nicely
            Platform.runLater(() -> showNodeDetails(n));
        } else {
            // show countdown if active
            if (auctionCountdown != null && auctionCountdown.getStatus() == Animation.Status.RUNNING) {
                // keep countdown label
            } else lbCountdown.setText("");
            Platform.runLater(() -> clearNodeDetails());
        }
    }

    private void showNodeDetails(NodeVisual n) {
        detailsBox.getChildren().clear();
        Label title = new Label(n.id);
        title.setFont(Font.font(16));
        detailsBox.getChildren().add(title);
        // iterate over JSON properties and show key: value
        for (Map.Entry<String, JsonElement> e : n.props.entrySet()) {
            String key = e.getKey();
            String val = e.getValue().getAsString();
            Label row = new Label(String.format("%s: %s", key, val));
            detailsBox.getChildren().add(row);
        }
    }

    private void clearNodeDetails() {
        detailsBox.getChildren().clear();
        detailsBox.getChildren().add(new Label("Hover a node to see details"));
    }

    // AuctionResultListener implementations
    @Override
    public void Update() {
        Platform.runLater(() -> {
            lbResources.setText("Resources: " + AuctionServer.getInstance().getCurrentCapacity() + "/" + AuctionServer.getInstance().getTotalCapa());
            lbRevenue.setText("Revenue: " + AuctionServer.getInstance().getRevenue());
        });
    }

    @Override
    public void onAuctionStarted(String sessionId, int duration) {
        Platform.runLater(() -> startCountdown(duration));
    }

    @Override
    public void onAuctionEnded(String winnerId, double winningBid) {
        Platform.runLater(() -> {
            stopCountdown();
            // flash server dot to indicate end
            FadeTransition ft = new FadeTransition(Duration.seconds(0.6), serverDot);
            ft.setFromValue(1.0);
            ft.setToValue(0.2);
            ft.setAutoReverse(true);
            ft.setCycleCount(4);
            ft.play();
            // update labels
            Update();
            // animate resource sending if there is a winner
            if (winnerId != null) animateSendResourceTo(winnerId);
        });
    }

    @Override
    public void onNodeConnected(String nodeId) {
        NodeVisual nv = nodeVisuals.get(nodeId);
        if (nv != null) {
            nv.setActive(true);
            // mark connected and stop any pulse animation
            nv.connected = true;
            stopNodePulse(nodeId);
            drawScene();
        }
    }

    private void startNodePulse(String nodeId, NodeVisual nv) {
        // if already pulsing, do nothing
        if (nodePulseTimelines.containsKey(nodeId)) return;
        // create timeline toggling pulseScale between 1.0 and 2.0
        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, evt -> { nv.pulseScale = 1.0; drawScene(); }),
                new KeyFrame(Duration.seconds(0.5), evt -> { nv.pulseScale = 1.8; drawScene(); })
        );
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        nodePulseTimelines.put(nodeId, t);
        t.play();
    }

    private void stopNodePulse(String nodeId) {
        Timeline t = nodePulseTimelines.remove(nodeId);
        if (t != null) {
            t.stop();
            NodeVisual nv = nodeVisuals.get(nodeId);
            if (nv != null) nv.pulseScale = 1.0;
            drawScene();
        }
    }

    @Override
    public void onBidForLink(String nodeId, String linkId, String sessionId) {
        LinkVisual lv = linkVisuals.get(linkId);
        if (lv != null) {
            lv.highlight();
            drawScene();
        }
    }

    private void startCountdown(int seconds) {
        stopCountdown();
        final IntegerProperty timeSec = new SimpleIntegerProperty(seconds);
        lbCountdown.setText("Time remaining: " + seconds + "s");
        auctionCountdown = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
            int t = timeSec.get() - 1;
            timeSec.set(t);
            lbCountdown.setText("Time remaining: " + t + "s");
            if (t <= 0) {
                stopCountdown();
            }
        }));
        auctionCountdown.setCycleCount(seconds);
        auctionCountdown.play();
    }

    private void stopCountdown() {
        if (auctionCountdown != null) {
            auctionCountdown.stop();
            auctionCountdown = null;
            lbCountdown.setText("");
        }
    }

    // small helper classes for visuals
    private static class NodeVisual {
        final String id;
        double x, y;
        boolean active = false;
    boolean connected = false;
        JsonObject props;
        double pulseScale = 1.0;

        NodeVisual(String id, double x, double y, JsonObject props) {
            this.id = id; this.x = x; this.y = y; this.props = props;
        }

        void draw(GraphicsContext gc) {
            double base = 6 * pulseScale;
            if (connected) {
                double b = 6 * pulseScale;
                // connected nodes: solid blue with light halo
                gc.setFill(Color.rgb(30,144,255, 0.35));
                gc.fillOval(x - b*1.8, y - b*1.8, b*3.6, b*3.6);
                gc.setFill(Color.DEEPSKYBLUE);
                gc.fillOval(x - b, y - b, b*2, b*2);
                gc.setStroke(Color.LIGHTBLUE);
                gc.setLineWidth(1.0);
                gc.strokeOval(x - b, y - b, b*2, b*2);
            } else if (active) {
                // glow: larger translucent halo
                gc.setFill(Color.rgb(255,140,0, 0.35));
                gc.fillOval(x - base*1.8, y - base*1.8, base*3.6, base*3.6);
                gc.setFill(Color.ORANGE);
                gc.fillOval(x - base*1.2, y - base*1.2, base*2.4, base*2.4);
                gc.setFill(Color.RED.brighter());
                gc.fillOval(x - base, y - base, base*2, base*2);
                // bright border
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1.0);
                gc.strokeOval(x - base, y - base, base*2, base*2);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(x - base, y - base, base*2, base*2);
            }
        }

        boolean contains(double mx, double my) {
            double dx = mx - x, dy = my - y;
            return dx*dx + dy*dy <= 8*8;
        }

        String getInfo() {
            return String.format("%s Pt:%s Gt:%s Gr:%s currentBudget:%s",
                    id,
                    props.has("Pt")?props.get("Pt").getAsString():"-",
                    props.has("Gt")?props.get("Gt").getAsString():"-",
                    props.has("Gr")?props.get("Gr").getAsString():"-",
                    props.has("currentBudget")?props.get("currentBudget").getAsString():"-"
            );
        }

        void setActive(boolean v) { this.active = v; }

        void pulse() {
            // kept for compatibility; real animation is driven by Timeline in parent
        }
    }

    private static class LinkVisual {
        final String id;
        final NodeVisual a, b;
        boolean highlighted = false;

        LinkVisual(String id, NodeVisual a, NodeVisual b) { this.id = id; this.a = a; this.b = b; }

        void draw(GraphicsContext gc) {
            gc.setLineWidth(2);
            gc.setStroke(highlighted ? Color.LIME : Color.web("#7fbf7f"));
            gc.strokeLine(a.x, a.y, b.x, b.y);
            // small label
            double mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(10));
            gc.fillText(id, mx + 4, my + 4);
        }

        void highlight() {
            highlighted = true;
            // schedule fade out
            Timer t = new Timer(true);
            t.schedule(new TimerTask() {
                @Override public void run() { highlighted = false; }
            }, 2000);
        }
    }

    // Minimal JavaFX replacements for dialogs used earlier
    private static class CreateAuctionDialogFX {
        CreateAuctionDialogFX(Stage owner) {
            Dialog<Item> dialog = new Dialog<>();
            dialog.setTitle("Create Auction");
            ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            TextField resourceField = new TextField();
            TextField reserveField = new TextField();
            Button btnEstimate = new Button("Estimate Reserve");
            grid.add(new Label("Resource:"), 0, 0);
            grid.add(resourceField, 1, 0);
            grid.add(btnEstimate, 2, 0);
            grid.add(new Label("Reserve Price:"), 0, 1);
            grid.add(reserveField, 1, 1);
            dialog.getDialogPane().setContent(grid);

            btnEstimate.setOnAction(e -> {
                String resourceStr = resourceField.getText().trim();
                try {
                    double resource = Double.parseDouble(resourceStr);
                    if (resource <= 0) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Resource must be positive.");
                        a.showAndWait();
                        return;
                    } else if (resource > AuctionServer.getInstance().getCurrentCapacity()) {
                        Alert a = new Alert(Alert.AlertType.WARNING, "Not enough resources available.");
                        a.showAndWait();
                        return;
                    }
                    double avg_bid = DatabaseManager.getInstance().getAverageBidAmount();
                    if (avg_bid == 0.00) {
                        reserveField.setText("500");
                    } else {
                        reserveField.setText(String.format("%.2f", avg_bid * 0.8));
                    }
                } catch (NumberFormatException ex) {
                    Alert a = new Alert(Alert.AlertType.WARNING, "Invalid resource input.");
                    a.showAndWait();
                }
            });

            dialog.setResultConverter(btn -> {
                if (btn == createBtn) {
                    try {
                        double resource = Double.parseDouble(resourceField.getText().trim());
                        String reserve = reserveField.getText().trim();
                        Item item = new Item(resource, reserve);
                        String id = DatabaseManager.getInstance().generateId("item");
                        item.setId(id);
                        return item;
                    } catch (Exception ex) {
                        return null;
                    }
                }
                return null;
            });

            Optional<Item> result = dialog.showAndWait();
            result.ifPresent(item -> {
                if(!DatabaseManager.getInstance().insertItem(item)) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to create item in database.");
                    a.showAndWait();
                } else {
                    AuctionServer.getInstance().startAuctionSession(item);
                }
            });
        }
    }

        // animate a small circle from server dot (upper-left) to the node location
    private void animateSendResourceTo(String nodeId) {
        NodeVisual nv = nodeVisuals.get(nodeId);
        if (nv == null) return;
        Circle ball = new Circle(6, Color.GOLD);
        // overlay pane is stack pane child 1 (we added canvas then overlayPane)
        Pane overlay = (Pane) canvas.getParent().getChildrenUnmodifiable().get(1);
        if (overlay == null) return;
        ball.setLayoutX(30);
        ball.setLayoutY(30);
        Platform.runLater(() -> overlay.getChildren().add(ball));

        // animation from (30,30) to nv.x,nv.y
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.2), ball);
        tt.setToX(nv.x - 30);
        tt.setToY(nv.y - 30);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(ev -> {
            // small pulse on node
            Platform.runLater(() -> {
                overlay.getChildren().remove(ball);
                // visual effect: draw a quick ring on canvas
                gc.setStroke(Color.GOLD);
                gc.setLineWidth(2);
                gc.strokeOval(nv.x - 12, nv.y - 12, 24, 24);
                // restore after short delay
                new Timer(true).schedule(new TimerTask() {
                    @Override public void run() { drawScene(); }
                }, 400);
            });
        });
        tt.play();
    }

    private static class ShowStatusViewFX {
        ShowStatusViewFX() {
            // reuse existing Swing implementation ShowStatusView
            javax.swing.SwingUtilities.invokeLater(() -> new ShowStatusView());
        }
    }

}
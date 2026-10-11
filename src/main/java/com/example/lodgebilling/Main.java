package com.example.lodgebilling;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class Main extends Application {

    private static final double STANDARD_PRICE = 350.00;
    private static final double DELUXE_PRICE = 550.00;
    private static final double FAMILY_PRICE = 750.00;

    private static final double DISCOUNT_RATE = 0.02;
    private static final int DISCOUNT_MIN_NIGHTS = 3;

    // Demo login credentials for the practical
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    private static final String[] FONT_FILES = {
            "Poppins-Regular.ttf", "Poppins-Bold.ttf", "Lora-Variable.ttf"
    };
    private static final String[] PHOTOS = {"photo1.png", "photo2.png", "photo3.png"};

    private static final String HINT = "status-hint";
    private static final String ERROR = "status-error";
    private static final String OK = "status-ok";

    private static final DecimalFormat MONEY =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter RECEIPT_DATE =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private Stage stage;
    private String stylesheet;

    // Form
    private TextField guestNameField;
    private ComboBox<String> roomTypeComboBox;
    private TextField nightsField;
    private TextField extrasField;
    private TextField amountPaidField;
    private TextArea notesArea;

    // Live summary
    private Label sumRoomName;
    private Label sumRoomValue;
    private Label sumDiscountValue;
    private Label sumExtrasValue;
    private Label sumTotalValue;
    private Label sumPaidValue;
    private Label sumBalanceName;
    private Label sumBalanceValue;
    private Label statusLabel;

    // Totals strip
    private Label statCountValue;
    private Label statBilledValue;
    private Label statOwingValue;

    private TableView<Bill> billTable;

    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int nextBillId = 1;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        loadFonts();

        var url = Main.class.getResource("styles.css");
        stylesheet = (url == null) ? null : url.toExternalForm();

        showLoginScreen();
    }

    // ---------------------------------------------------------
    // SCENE HELPERS
    // ---------------------------------------------------------

    private void showScene(Scene scene, String title, double minWidth, double minHeight) {
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet);
        }
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private void styleDialog(DialogPane pane) {
        if (stylesheet != null) {
            pane.getStylesheets().add(stylesheet);
        }
        pane.getStyleClass().add("app-dialog");
    }

    private Alert infoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setGraphic(null);
        styleDialog(alert.getDialogPane());
        return alert;
    }

    private boolean confirm(
            String title, String header, String content,
            String actionText, boolean destructive) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setGraphic(null);

        ButtonType action = new ButtonType(
                actionText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(
                "Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(action, cancel);

        styleDialog(alert.getDialogPane());
        if (destructive) {
            alert.getDialogPane().getStyleClass().add("danger");
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == action;
    }

    private static VBox field(String labelText, Node control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return new VBox(6, label, control);
    }

    private static void autoHide(Label label) {
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.textProperty().isNotEmpty());
    }

    private static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static Region brandMark() {
        Region mark = new Region();
        mark.getStyleClass().add("brand-mark");
        return mark;
    }

    private void loadFonts() {
        for (String file : FONT_FILES) {
            try (InputStream in = Main.class.getResourceAsStream("fonts/" + file)) {
                if (in != null) {
                    Font.loadFont(in, 12);
                }
            } catch (IOException e) {
                // Fall back to the system fonts named in styles.css
            }
        }
    }

    /** Nested triangle outlines and a diamond: the geometric accent behind the screens. */
    private static Pane pattern(double apexX, double apexY, int levels) {
        Pane pane = new Pane();
        pane.setMouseTransparent(true);
        Color lineColor = Color.web("#855F48");

        for (int k = 1; k <= levels; k++) {
            Polyline triangle = new Polyline(
                    apexX - 40 * k, apexY + 66 * k,
                    apexX, apexY,
                    apexX + 40 * k, apexY + 66 * k
            );
            triangle.setFill(Color.TRANSPARENT);
            triangle.setStroke(lineColor);
            triangle.setStrokeWidth(2.2);
            pane.getChildren().add(triangle);
        }

        double dx = apexX - 84;
        double dy = apexY - 20;
        Polygon diamond = new Polygon(
                dx, dy - 11, dx + 11, dy, dx, dy + 11, dx - 11, dy);
        diamond.setFill(Color.TRANSPARENT);
        diamond.setStroke(lineColor);
        diamond.setStrokeWidth(2.2);
        pane.getChildren().add(diamond);

        return pane;
    }

    /** A circular photo with a tan ring, or null if the image is missing. */
    private static Node photoBubble(String file, double size) {
        try (InputStream in = Main.class.getResourceAsStream("images/" + file)) {
            if (in == null) {
                return null;
            }

            ImageView view = new ImageView(
                    new Image(in, size * 2, size * 2, true, true));
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setClip(new Circle(size / 2, size / 2, size / 2));

            Circle ring = new Circle(size / 2 - 1.25);
            ring.setFill(Color.TRANSPARENT);
            ring.setStroke(Color.web("#DCC0AA"));
            ring.setStrokeWidth(2.5);

            StackPane bubble = new StackPane(view, ring);
            bubble.setMinSize(size, size);
            bubble.setPrefSize(size, size);
            bubble.setMaxSize(size, size);
            return bubble;
        } catch (IOException e) {
            return null;
        }
    }

    /** Divider line plus three round photos. Returns null if no photos are available. */
    private HBox createPhotoColumn() {
        VBox photos = new VBox(22);
        photos.setAlignment(Pos.TOP_CENTER);

        for (String file : PHOTOS) {
            Node bubble = photoBubble(file, 128);
            if (bubble != null) {
                photos.getChildren().add(bubble);
            }
        }
        if (photos.getChildren().isEmpty()) {
            return null;
        }

        Region divider = new Region();
        divider.getStyleClass().add("photo-divider");

        HBox column = new HBox(22, divider, photos);
        column.setMinWidth(Region.USE_PREF_SIZE);
        return column;
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    private void showLoginScreen() {
        // Left: brand panel
        Label brandName = new Label("Lodge Billing Lab");
        brandName.getStyleClass().add("brand-name");

        HBox brandRow = new HBox(10, brandMark(), brandName);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Label headline = new Label(
                "Guest bills, discounts and receipts in one place.");
        headline.getStyleClass().add("login-headline");
        headline.setWrapText(true);

        Label support = new Label("Totals and balances update as you type.");
        support.getStyleClass().add("login-support");

        HBox photos = new HBox(14);
        for (String file : PHOTOS) {
            Node bubble = photoBubble(file, 84);
            if (bubble != null) {
                photos.getChildren().add(bubble);
            }
        }

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        VBox brandContent = new VBox(
                14, brandRow, grow, headline, support, photos);
        brandContent.setPadding(new Insets(36));

        StackPane brandPane = new StackPane(
                pattern(150, 120, 2), brandContent);
        brandPane.getStyleClass().add("login-brand");
        brandPane.setMinWidth(400);
        brandPane.setPrefWidth(400);
        brandPane.setMaxWidth(400);

        // Right: form
        Label title = new Label("Log in");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label(
                "Enter your username and password to open the billing desk.");
        subtitle.getStyleClass().add("page-sub");
        subtitle.setWrapText(true);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label message = new Label();
        message.getStyleClass().add("login-error");
        message.setWrapText(true);
        autoHide(message);

        Button loginButton = new Button("Log in");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e ->
                authenticate(usernameField, passwordField, message));
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e ->
                authenticate(usernameField, passwordField, message));

        Label hint = new Label("Demo account: admin / admin123");
        hint.getStyleClass().add("muted-small");

        VBox form = new VBox(
                16,
                title,
                subtitle,
                field("Username", usernameField),
                field("Password", passwordField),
                message,
                loginButton,
                hint
        );
        form.setPrefWidth(320);
        form.setMaxWidth(320);

        StackPane formPane = new StackPane(form);
        formPane.getStyleClass().add("login-form-pane");
        formPane.setPadding(new Insets(30));
        HBox.setHgrow(formPane, Priority.ALWAYS);

        HBox root = new HBox(brandPane, formPane);

        showScene(new Scene(root, 900, 560),
                "Lodge Billing Lab - Log in", 800, 520);

        Platform.runLater(usernameField::requestFocus);
    }

    private void authenticate(
            TextField usernameField,
            PasswordField passwordField,
            Label message) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            showMainApplication(username);
        } else {
            message.setText("Invalid username or password.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    // ---------------------------------------------------------
    // MAIN APPLICATION
    // ---------------------------------------------------------

    private void showMainApplication(String loggedInUser) {
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());

        ScrollPane scroll = new ScrollPane(createContent(loggedInUser));
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");
        root.setCenter(scroll);

        showScene(new Scene(root, 1200, 780),
                "Lodge Billing Lab - " + loggedInUser, 1040, 640);

        refreshSummary(false);
        updateStats();
        Platform.runLater(guestNameField::requestFocus);
    }

    // ---------------------------------------------------------
    // TOP AREA
    // ---------------------------------------------------------

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem newGuestItem = new MenuItem("New guest");
        newGuestItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newGuestItem.setOnAction(e -> clearForm());

        MenuItem saveItem = new MenuItem("Save bill");
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveItem.setOnAction(e -> saveBill());

        MenuItem logoutItem = new MenuItem("Log out");
        logoutItem.setOnAction(e -> logout());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(
                newGuestItem,
                saveItem,
                new SeparatorMenuItem(),
                logoutItem,
                exitItem
        );

        Menu helpMenu = new Menu("Help");

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAbout());

        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private HBox createPageHeader(String loggedInUser) {
        Label title = new Label("Lodge Billing Lab");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label(
                "ICT261 Lecture 3   |   JavaFX Practical Lab   |   Mulungushi University");
        subtitle.getStyleClass().add("app-sub");

        VBox titleBox = new VBox(4, title, subtitle);

        Circle dot = new Circle(5, Color.web("#9AE5AD"));
        Label userLabel = new Label(loggedInUser);

        HBox userChip = new HBox(9, dot, userLabel);
        userChip.setAlignment(Pos.CENTER_LEFT);
        userChip.getStyleClass().add("chip");

        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("chip-button");
        logoutButton.setOnAction(e -> logout());

        HBox actions = new HBox(10, userChip, logoutButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(12, titleBox, spacer(), actions);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ---------------------------------------------------------
    // PAGE CONTENT
    // ---------------------------------------------------------

    private StackPane createContent(String loggedInUser) {
        VBox formCard = createFormCard();
        HBox.setHgrow(formCard, Priority.ALWAYS);

        HBox workArea = new HBox(20, formCard, createSideColumn());

        VBox left = new VBox(
                20,
                createPageHeader(loggedInUser),
                createStatStrip(),
                workArea,
                createBillsCard()
        );
        HBox.setHgrow(left, Priority.ALWAYS);

        HBox row = new HBox(28, left);
        HBox photoColumn = createPhotoColumn();
        if (photoColumn != null) {
            row.getChildren().add(photoColumn);
        }
        row.setPadding(new Insets(26, 28, 28, 28));

        return new StackPane(pattern(70, 100, 3), row);
    }

    private HBox createStatStrip() {
        statCountValue = new Label("0");
        statBilledValue = new Label("K 0.00");
        statOwingValue = new Label("K 0.00");

        HBox strip = new HBox(
                stat("Bills saved", statCountValue, false),
                stat("Total billed", statBilledValue, false),
                stat("Outstanding", statOwingValue, true)
        );
        strip.getStyleClass().addAll("card", "stat-strip");
        return strip;
    }

    private VBox stat(String labelText, Label value, boolean last) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");
        value.getStyleClass().add("stat-value");

        VBox box = new VBox(4, label, value);
        box.getStyleClass().add("stat");
        if (last) {
            box.getStyleClass().add("stat-last");
        }
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void updateStats() {
        double billed = bills.stream()
                .mapToDouble(b -> b.calc().total()).sum();
        double owing = bills.stream()
                .mapToDouble(b -> Math.max(0, b.calc().balance())).sum();

        statCountValue.setText(String.valueOf(bills.size()));
        statBilledValue.setText(money(billed));
        statOwingValue.setText(money(owing));

        statOwingValue.getStyleClass().remove("stat-warn");
        if (owing > 0) {
            statOwingValue.getStyleClass().add("stat-warn");
        }
    }

    // ---------------------------------------------------------
    // FORM CARD
    // ---------------------------------------------------------

    private VBox createFormCard() {
        guestNameField = new TextField();
        guestNameField.setPromptText("Enter guest name");

        roomTypeComboBox = new ComboBox<>();
        roomTypeComboBox.getItems().addAll("Standard", "Deluxe", "Family");
        roomTypeComboBox.setValue("Standard");

        nightsField = new TextField();
        nightsField.setPromptText("e.g. 3");
        nightsField.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,3}") ? change : null));

        extrasField = new TextField("0");
        extrasField.setTextFormatter(moneyFormatter());
        selectOnFocus(extrasField);

        amountPaidField = new TextField("0");
        amountPaidField.setTextFormatter(moneyFormatter());
        selectOnFocus(amountPaidField);

        notesArea = new TextArea();
        notesArea.setPrefRowCount(3);
        notesArea.setPromptText("Optional notes");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(50);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(50);
        grid.getColumnConstraints().addAll(left, right);

        grid.add(field("Guest name", guestNameField), 0, 0, 2, 1);
        grid.add(field("Room type", roomTypeComboBox), 0, 1);
        grid.add(field("Nights", nightsField), 1, 1);
        grid.add(field("Extras (K)", extrasField), 0, 2);
        grid.add(field("Amount paid (K)", amountPaidField), 1, 2);
        grid.add(field("Notes", notesArea), 0, 3, 2, 1);

        Button saveButton = new Button("Save bill");
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setOnAction(e -> saveBill());

        Button calculateButton = new Button("Calculate");
        calculateButton.getStyleClass().add("btn-secondary");
        calculateButton.setOnAction(e -> refreshSummary(true));

        Button newGuestButton = new Button("New guest");
        newGuestButton.getStyleClass().add("btn-ghost");
        newGuestButton.setOnAction(e -> clearForm());

        HBox buttons = new HBox(
                10, saveButton, calculateButton, spacer(), newGuestButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Guest and stay");
        title.getStyleClass().add("card-title");

        Label note = new Label("The summary updates as you type.");
        note.getStyleClass().add("msg-muted");

        VBox card = new VBox(18, new VBox(3, title, note), grid, buttons);
        card.getStyleClass().add("card");

        // Keep the summary in step with the form
        nightsField.textProperty().addListener((o, a, b) -> refreshSummary(false));
        extrasField.textProperty().addListener((o, a, b) -> refreshSummary(false));
        amountPaidField.textProperty().addListener((o, a, b) -> refreshSummary(false));
        roomTypeComboBox.valueProperty().addListener((o, a, b) -> refreshSummary(false));

        return card;
    }

    private static TextFormatter<String> moneyFormatter() {
        return new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d{0,9}(\\.\\d{0,2})?")
                        ? change : null);
    }

    private static void selectOnFocus(TextField field) {
        field.focusedProperty().addListener((o, was, is) -> {
            if (is) {
                Platform.runLater(field::selectAll);
            }
        });
    }

    // ---------------------------------------------------------
    // SIDE COLUMN (summary + rates)
    // ---------------------------------------------------------

    private VBox createSideColumn() {
        VBox side = new VBox(16, createSummaryCard(), createRateCard());
        side.setMinWidth(330);
        side.setPrefWidth(340);
        side.setMaxWidth(340);
        return side;
    }

    private VBox createSummaryCard() {
        Label title = new Label("Bill summary");
        title.getStyleClass().add("card-title");

        sumRoomName = new Label("Room charge");
        sumRoomValue = new Label();
        sumDiscountValue = new Label();
        sumExtrasValue = new Label();
        sumTotalValue = new Label();
        sumPaidValue = new Label();
        sumBalanceName = new Label("Balance");
        sumBalanceValue = new Label();

        sumTotalValue.getStyleClass().add("sum-total-value");
        sumBalanceValue.getStyleClass().add("sum-balance-value");
        sumDiscountValue.getStyleClass().addAll("sum-value", "sum-discount");
        sumRoomValue.getStyleClass().add("sum-value");
        sumExtrasValue.getStyleClass().add("sum-value");
        sumPaidValue.getStyleClass().add("sum-value");

        sumRoomName.getStyleClass().add("sum-name");
        sumBalanceName.getStyleClass().add("sum-total-name");

        Label discountName = new Label("Discount (2%)");
        discountName.getStyleClass().add("sum-name");
        Label extrasName = new Label("Extras");
        extrasName.getStyleClass().add("sum-name");
        Label totalName = new Label("Total");
        totalName.getStyleClass().add("sum-total-name");
        Label paidName = new Label("Amount paid");
        paidName.getStyleClass().add("sum-name");

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        autoHide(statusLabel);

        VBox card = new VBox(
                12,
                title,
                summaryRow(sumRoomName, sumRoomValue),
                summaryRow(discountName, sumDiscountValue),
                summaryRow(extrasName, sumExtrasValue),
                divider(),
                summaryRow(totalName, sumTotalValue),
                summaryRow(paidName, sumPaidValue),
                divider(),
                summaryRow(sumBalanceName, sumBalanceValue),
                statusLabel
        );
        card.getStyleClass().add("summary-card");
        return card;
    }

    private static HBox summaryRow(Label name, Label value) {
        HBox row = new HBox(8, name, spacer(), value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Region divider() {
        Region line = new Region();
        line.getStyleClass().add("sum-divider");
        return line;
    }

    private VBox createRateCard() {
        Label title = new Label("Rates per night");
        title.getStyleClass().add("card-title");

        Label discount = new Label("3 nights or more: 2% off the room charge.");
        Label extras = new Label("Extras get no discount. No tax in this lab.");
        Label saved = new Label("Saved bills last only until you close the app.");
        for (Label note : new Label[]{discount, extras, saved}) {
            note.getStyleClass().add("note");
            note.setWrapText(true);
        }

        VBox card = new VBox(
                10,
                title,
                rateRow("Standard", STANDARD_PRICE),
                rateRow("Deluxe", DELUXE_PRICE),
                rateRow("Family", FAMILY_PRICE),
                new Separator(),
                discount, extras, saved
        );
        card.getStyleClass().add("card");
        return card;
    }

    private HBox rateRow(String name, double price) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("rate-name");
        Label priceLabel = new Label(money(price));
        priceLabel.getStyleClass().add("rate-value");

        HBox row = new HBox(8, nameLabel, spacer(), priceLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ---------------------------------------------------------
    // SAVED BILLS TABLE
    // ---------------------------------------------------------

    private VBox createBillsCard() {
        Label title = new Label("Saved bills");
        title.getStyleClass().add("card-title");

        Label hint = new Label(
                "Select a bill to view its receipt or delete it. "
                + "Double-click a row to open the receipt.");
        hint.getStyleClass().add("msg-muted");

        billTable = new TableView<>(bills);
        billTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        billTable.setPlaceholder(
                new Label("No bills yet. Save a bill and it will appear here."));
        billTable.setPrefHeight(280);
        billTable.setMinHeight(200);

        TableColumn<Bill, String> idColumn =
                column("Bill", 70, b -> String.format("#%03d", b.id()));
        TableColumn<Bill, String> guestColumn =
                column("Guest", 200, Bill::guest);
        TableColumn<Bill, String> roomColumn =
                column("Room", 100, b -> b.calc().room());
        TableColumn<Bill, String> nightsColumn =
                column("Nights", 70, b -> String.valueOf(b.calc().nights()));
        TableColumn<Bill, String> totalColumn =
                column("Total", 120, b -> money(b.calc().total()));
        TableColumn<Bill, String> paidColumn =
                column("Paid", 120, b -> money(b.calc().paid()));
        TableColumn<Bill, Bill> balanceColumn =
                column("Balance", 120, b -> b);
        TableColumn<Bill, String> statusColumn =
                column("Status", 110, b -> b.calc().status());

        java.util.List<TableColumn<Bill, ?>> numericColumns =
                java.util.List.of(nightsColumn, totalColumn, paidColumn, balanceColumn);
        for (TableColumn<Bill, ?> numeric : numericColumns) {
            numeric.setStyle("-fx-alignment: CENTER-RIGHT;");
            numeric.getStyleClass().add("numeric");
        }

        balanceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Bill bill, boolean empty) {
                super.updateItem(bill, empty);
                getStyleClass().removeAll("balance-owing", "balance-clear");
                if (empty || bill == null) {
                    setText(null);
                    return;
                }
                setText(money(bill.calc().balance()));
                getStyleClass().add(
                        bill.calc().balance() > 0 ? "balance-owing" : "balance-clear");
            }
        });

        statusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label pill = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                pill.setText(status);
                pill.getStyleClass().setAll("pill", "pill-" + status.toLowerCase());
                setGraphic(pill);
            }
        });

        billTable.getColumns().addAll(
                idColumn, guestColumn, roomColumn, nightsColumn,
                totalColumn, paidColumn, balanceColumn, statusColumn
        );

        billTable.setRowFactory(tv -> {
            TableRow<Bill> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showReceipt(row.getItem());
                }
            });
            return row;
        });

        bills.addListener((ListChangeListener<Bill>) change -> updateStats());

        Button viewButton = new Button("View receipt");
        viewButton.getStyleClass().add("btn-secondary");
        viewButton.setOnAction(e -> viewReceipt());

        Button deleteButton = new Button("Delete bill");
        deleteButton.getStyleClass().add("btn-danger");
        deleteButton.setOnAction(e -> deleteSelectedBill());

        var noSelection =
                billTable.getSelectionModel().selectedItemProperty().isNull();
        viewButton.disableProperty().bind(noSelection);
        deleteButton.disableProperty().bind(noSelection);

        VBox titleBox = new VBox(3, title, hint);
        HBox header = new HBox(10, titleBox, spacer(), viewButton, deleteButton);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, header, billTable);
        card.getStyleClass().add("card");
        VBox.setVgrow(billTable, Priority.ALWAYS);
        return card;
    }

    private static <T> TableColumn<Bill, T> column(
            String title, double width, Function<Bill, T> value) {

        TableColumn<Bill, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        column.setSortable(false);
        return column;
    }

    // ---------------------------------------------------------
    // BILL CALCULATION
    // ---------------------------------------------------------

    private Calc computeBill() {
        String room = roomTypeComboBox.getValue();
        int nights = parseNights(nightsField.getText());
        double extras = parseAmount(extrasField.getText(), "Extras");
        double paid = parseAmount(amountPaidField.getText(), "Amount paid");

        double rate = getRoomPrice(room);
        double roomCharge = round2(rate * nights);
        double discount = nights >= DISCOUNT_MIN_NIGHTS
                ? round2(roomCharge * DISCOUNT_RATE)
                : 0;

        // Extras do not receive the discount
        double total = round2(roomCharge - discount + extras);
        double balance = round2(total - paid);

        return new Calc(room, rate, nights, roomCharge, discount,
                extras, total, paid, balance);
    }

    private int parseNights(String text) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Enter the number of nights.");
        }

        int nights;
        try {
            nights = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Nights must be a whole number.");
        }

        if (nights <= 0) {
            throw new IllegalArgumentException("Nights must be greater than zero.");
        }
        return nights;
    }

    private double parseAmount(String text, String label) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            return 0;
        }

        double amount;
        try {
            amount = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number.");
        }

        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException(label + " must be a valid amount.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException(label + " cannot be negative.");
        }
        return amount;
    }

    private double getRoomPrice(String room) {
        return switch (room) {
            case "Standard" -> STANDARD_PRICE;
            case "Deluxe" -> DELUXE_PRICE;
            case "Family" -> FAMILY_PRICE;
            default -> throw new IllegalArgumentException(
                    "Invalid room type."
            );
        };
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static String money(double value) {
        String sign = value < -0.004 ? "\u2212" : "";
        return sign + "K " + MONEY.format(Math.abs(value));
    }

    // ---------------------------------------------------------
    // SUMMARY
    // ---------------------------------------------------------

    private void refreshSummary(boolean explicit) {
        try {
            Calc calc = computeBill();
            showCalc(calc);
            setStatus("Preview only. Use Save bill to keep this bill.", HINT);
        } catch (IllegalArgumentException e) {
            clearSummary();
            setStatus(e.getMessage(), explicit ? ERROR : HINT);
        }
    }

    private void showCalc(Calc calc) {
        sumRoomName.setText(
                calc.room() + " \u00D7 " + calc.nights()
                + (calc.nights() == 1 ? " night" : " nights"));
        sumRoomValue.setText(money(calc.roomCharge()));
        sumDiscountValue.setText(
                calc.discount() > 0 ? "\u2212 " + money(calc.discount()) : "\u2014");
        sumExtrasValue.setText(money(calc.extras()));
        sumTotalValue.setText(money(calc.total()));
        sumPaidValue.setText(money(calc.paid()));

        sumBalanceName.setText(switch (calc.status()) {
            case "Owing" -> "Balance due";
            case "Overpaid" -> "Overpaid by";
            default -> "Balance";
        });
        sumBalanceValue.setText(money(Math.abs(calc.balance())));
        setBalanceStyle(calc.balance() > 0 ? "sum-owing" : "sum-clear");
    }

    private void clearSummary() {
        sumRoomName.setText("Room charge");
        sumRoomValue.setText("\u2014");
        sumDiscountValue.setText("\u2014");
        sumExtrasValue.setText("\u2014");
        sumTotalValue.setText("\u2014");
        sumPaidValue.setText("\u2014");
        sumBalanceName.setText("Balance");
        sumBalanceValue.setText("\u2014");
        setBalanceStyle(null);
    }

    private void setBalanceStyle(String styleClass) {
        sumBalanceValue.getStyleClass().removeAll("sum-owing", "sum-clear");
        if (styleClass != null) {
            sumBalanceValue.getStyleClass().add(styleClass);
        }
    }

    private void setStatus(String message, String kind) {
        statusLabel.getStyleClass().removeAll(HINT, ERROR, OK);
        statusLabel.getStyleClass().add(kind);
        statusLabel.setText(message);
    }

    // ---------------------------------------------------------
    // SAVE BILL
    // ---------------------------------------------------------

    private void saveBill() {
        try {
            String guest = guestNameField.getText().trim();

            if (guest.isEmpty()) {
                throw new IllegalArgumentException("Enter the guest name.");
            }

            Calc calc = computeBill();

            Bill bill = new Bill(
                    nextBillId,
                    guest,
                    notesArea.getText().trim(),
                    calc,
                    LocalDateTime.now()
            );

            bills.add(bill);
            billTable.getSelectionModel().select(bill);
            billTable.scrollTo(bill);

            showCalc(calc);
            setStatus(String.format("Saved bill #%03d for %s.", nextBillId, guest), OK);

            nextBillId++;

        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), ERROR);
        }
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    private void logout() {
        boolean confirmed = confirm(
                "Log out",
                "Log out of Lodge Billing Lab?",
                "You will need to log in again to access the system.",
                "Log out",
                false
        );

        if (confirmed) {
            showLoginScreen();
        }
    }

    // ---------------------------------------------------------
    // FORM CONTROLS AND TABLE ACTIONS
    // ---------------------------------------------------------

    private void clearForm() {
        guestNameField.clear();
        roomTypeComboBox.setValue("Standard");
        nightsField.clear();
        extrasField.setText("0");
        amountPaidField.setText("0");
        notesArea.clear();
        refreshSummary(false);
        guestNameField.requestFocus();
    }

    private void deleteSelectedBill() {
        Bill selected = billTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        boolean confirmed = confirm(
                "Delete bill",
                String.format("Delete bill #%03d?", selected.id()),
                "The bill for " + selected.guest()
                        + " will be removed from this session.",
                "Delete",
                true
        );

        if (confirmed) {
            bills.remove(selected);
        }
    }

    private void viewReceipt() {
        Bill selected = billTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showReceipt(selected);
        }
    }

    // ---------------------------------------------------------
    // RECEIPT
    // ---------------------------------------------------------

    private void showReceipt(Bill bill) {
        Calc calc = bill.calc();

        Region mark = brandMark();
        mark.getStyleClass().add("brand-mark-dark");

        Label title = new Label("Lodge Billing Lab");
        title.getStyleClass().add("receipt-title");

        HBox brandRow = new HBox(10, mark, title);
        brandRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(
                String.format("Receipt #%03d   %s",
                        bill.id(), bill.issuedAt().format(RECEIPT_DATE)));
        meta.getStyleClass().add("receipt-meta");

        HBox balanceRow = receiptRow(
                switch (calc.status()) {
                    case "Owing" -> "Balance due";
                    case "Overpaid" -> "Overpaid by";
                    default -> "Balance";
                },
                money(Math.abs(calc.balance())),
                true);
        ((Label) balanceRow.getChildren().get(2)).getStyleClass().add(
                calc.balance() > 0 ? "row-owing" : "row-clear");

        Label pill = new Label(calc.status());
        pill.getStyleClass().addAll("pill", "pill-" + calc.status().toLowerCase());
        HBox pillRow = new HBox(pill);
        pillRow.setAlignment(Pos.CENTER_RIGHT);

        VBox sheet = new VBox(10,
                brandRow,
                meta,
                dash(),
                receiptRow("Guest", bill.guest(), false),
                receiptRow("Room", calc.room() + ", " + calc.nights()
                        + (calc.nights() == 1 ? " night" : " nights"), false),
                dash(),
                receiptRow("Room charge (" + money(calc.rate()) + " per night)",
                        money(calc.roomCharge()), false),
                receiptRow("Discount (2%)",
                        calc.discount() > 0 ? "\u2212 " + money(calc.discount()) : "\u2014",
                        false),
                receiptRow("Extras", money(calc.extras()), false),
                dash(),
                receiptRow("Total", money(calc.total()), true),
                receiptRow("Amount paid", money(calc.paid()), false),
                balanceRow,
                pillRow
        );

        if (!bill.notes().isBlank()) {
            Label notesTitle = new Label("Notes");
            notesTitle.getStyleClass().add("field-label");
            Label notes = new Label(bill.notes());
            notes.getStyleClass().add("receipt-notes");
            notes.setWrapText(true);
            sheet.getChildren().addAll(dash(), new VBox(4, notesTitle, notes));
        }

        Label footer = new Label("Thank you for staying with us.");
        footer.getStyleClass().add("receipt-footer");
        sheet.getChildren().addAll(dash(), footer);

        sheet.getStyleClass().add("receipt");
        sheet.setPrefWidth(380);

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.setTitle("Receipt #" + String.format("%03d", bill.id()));
        dialog.getDialogPane().setContent(sheet);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        styleDialog(dialog.getDialogPane());
        dialog.showAndWait();
    }

    private HBox receiptRow(String left, String right, boolean strong) {
        Label leftLabel = new Label(left);
        leftLabel.getStyleClass().add("row-label");
        leftLabel.setWrapText(true);

        Label rightLabel = new Label(right);
        rightLabel.getStyleClass().add("row-value");

        if (strong) {
            leftLabel.getStyleClass().add("row-strong");
            rightLabel.getStyleClass().add("row-strong");
        }

        HBox row = new HBox(12, leftLabel, spacer(), rightLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static Region dash() {
        Region line = new Region();
        line.getStyleClass().add("receipt-dash");
        return line;
    }

    private void showAbout() {
        infoDialog(
                "About",
                "Lodge Billing Lab",
                "ICT261 Lecture 3 | JavaFX lab\n" +
                "Lodge billing application\n\n" +
                "Java 21 + JavaFX\n" +
                "Includes login and logout security."
        ).showAndWait();
    }

    // ---------------------------------------------------------
    // MODEL
    // ---------------------------------------------------------

    /** The numbers behind one bill. All amounts are in kwacha. */
    private record Calc(
            String room,
            double rate,
            int nights,
            double roomCharge,
            double discount,
            double extras,
            double total,
            double paid,
            double balance) {

        String status() {
            if (balance > 0) {
                return "Owing";
            }
            return balance < 0 ? "Overpaid" : "Paid";
        }
    }

    private record Bill(
            int id,
            String guest,
            String notes,
            Calc calc,
            LocalDateTime issuedAt) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}

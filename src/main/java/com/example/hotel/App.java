package com.example.hotel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main JavaFX Application class for the Hotel Booking System.
 */
public class App extends Application {

    private static Stage primaryStage;
    private static final String CSS_PATH = "/styles/application.css";
    private static final String FXML_BASE_PATH = "/com/example/hotel/gui/fxml/";

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Hotel Booking System");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        showDashboard();
        primaryStage.show();
    }

    /**
     * Loads an FXML view and applies CSS styling.
     *
     * @param viewName The name of the FXML file (without extension)
     * @return The loaded Parent node
     */
    public static Parent loadView(String viewName) {
        try {
            String fxmlPath = FXML_BASE_PATH + viewName + ".fxml";
            URL fxmlUrl = App.class.getResource(fxmlPath);

            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Apply CSS
            URL cssUrl = App.class.getResource(CSS_PATH);
            if (cssUrl != null) {
                root.getStylesheets().add(cssUrl.toExternalForm());
            }

            return root;
        } catch (IOException e) {
            System.err.println("Error loading view: " + viewName);
            e.printStackTrace();
            throw new RuntimeException("Failed to load view: " + viewName, e);
        }
    }

    /**
     * Sets the scene with the given root node.
     */
    private static void setScene(Parent root) {
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
    }

    /**
     * Shows the Dashboard view.
     */
    public static void showDashboard() {
        Parent root = loadView("Dashboard");
        setScene(root);
    }

    /**
     * Shows the Room Management view.
     */
    public static void showRoomManagement() {
        try {
            Parent root = loadView("RoomManagement");
            setScene(root);
        } catch (RuntimeException e) {
            System.err.println("Room Management view not yet implemented");
        }
    }

    /**
     * Shows the Guest Management view.
     */
    public static void showGuestManagement() {
        try {
            Parent root = loadView("GuestManagement");
            setScene(root);
        } catch (RuntimeException e) {
            System.err.println("Guest Management view not yet implemented");
        }
    }

    /**
     * Shows the Booking view.
     */
    public static void showBooking() {
        try {
            Parent root = loadView("Booking");
            setScene(root);
        } catch (RuntimeException e) {
            System.err.println("Booking view not yet implemented");
        }
    }

    /**
     * Shows the Booking List view.
     */
    public static void showBookingList() {
        try {
            Parent root = loadView("BookingList");
            setScene(root);
        } catch (RuntimeException e) {
            System.err.println("Booking List view not yet implemented");
        }
    }

    /**
     * Shows the Invoice view.
     * The booking is passed via BookingListController.getSelectedBookingForInvoice().
     */
    public static void showInvoice() {
        try {
            Parent root = loadView("Invoice");
            setScene(root);
        } catch (RuntimeException e) {
            System.err.println("Invoice view not yet implemented");
        }
    }

    /**
     * Returns the primary stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

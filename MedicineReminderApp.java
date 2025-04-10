import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MedicineReminderApp extends Application {

    private final List<Medicine> medicineList = new ArrayList<>();
    private final TextArea displayArea = new TextArea();

    // Medicine class to hold name and time
    static class Medicine {
        String name;
        LocalTime time;

        Medicine(String name, String time) {
            this.name = name;
            this.time = LocalTime.parse(time); // Format: HH:MM
        }

        @Override
        public String toString() {
            return name + " at " + time;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Medicine Reminder");

        TextField medNameField = new TextField();
        medNameField.setPromptText("Medicine name");

        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM (24hr format)");

        Button addBtn = new Button("Add Reminder");

        displayArea.setEditable(false);
        displayArea.setPrefHeight(200);

        addBtn.setOnAction(e -> {
            try {
                String name = medNameField.getText();
                String time = timeField.getText();

                medicineList.add(new Medicine(name, time));
                displayArea.appendText("Added: " + name + " at " + time + "\n");

                medNameField.clear();
                timeField.clear();
            } catch (Exception ex) {
                displayArea.appendText("❌ Invalid time format. Use HH:MM\n");
            }
        });

        VBox layout = new VBox(10, medNameField, timeField, addBtn, displayArea);
        layout.setPadding(new javafx.geometry.Insets(15));

        primaryStage.setScene(new Scene(layout, 400, 350));
        primaryStage.show();

        // 🔁 Start real-time reminder checker
        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> checkReminders()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // ⏱️ First call immediately (don't wait a minute for the first check)
        checkReminders();
    }

    // 🔔 Check if any medicine is due now and notify
    private void checkReminders() {
        LocalTime now = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        boolean found = false;
    
        for (Medicine med : medicineList) {
            if (med.time.equals(now)) {
                String message = "🔔 It's time to take: " + med.name + "\n";
                displayArea.appendText(message);
                showSystemNotification("Medicine Reminder", "Time to take: " + med.name);
                found = true;
            }
        }
        if (!found) {
            displayArea.appendText("Checked at " + now + ": No medicines due now.\n");
        }
    }    

    // 🖥️ Show a system tray notification
    private void showSystemNotification(String title, String message) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported !");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
            // add icon to project resources
            // No icon or set a valid path
            TrayIcon trayIcon = new TrayIcon(image, "Medicine Reminder");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Medicine Reminder");

            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);

            // Optional: Remove the tray icon after a short delay to avoid clutter
            Thread remover = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Show for 5 seconds
                    tray.remove(trayIcon);
                } catch (Exception ignored) {}
            });
            remover.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

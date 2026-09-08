package com.eventify;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("Eventify");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("Event Management & Coordination System — Setup Complete");
        subtitleLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subtitleLabel.setStyle("-fx-text-fill: #475569;");

        VBox root = new VBox(12, titleLabel, subtitleLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f8fafc; -fx-padding: 40;");

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setTitle("Eventify — Event Management & Coordination System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

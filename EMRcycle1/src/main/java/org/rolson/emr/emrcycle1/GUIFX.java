package org.rolson.emr.emrcycle1;


import javafx.animation.Animation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;

import javafx.stage.Stage;
import javafx.util.Duration;

public class GUIFX extends Application {

	
	public void start(Stage stage) {

		ClusterCoordinator coordinator = new ClusterCoordinator();
        GUILayout gui = new GUILayout(coordinator,stage);
//        Timeline timeline = new Timeline(
//        	    new KeyFrame(Duration.seconds(10), e -> gui.updateStatusLabel()),
//        	    new KeyFrame(Duration.seconds(10), e -> coordinator.updateAll())
//        	);
//        	timeline.setCycleCount(Animation.INDEFINITE);
//        	timeline.play();
    }
    public static void main(String[] args) {

        launch(args);
    }
    
}
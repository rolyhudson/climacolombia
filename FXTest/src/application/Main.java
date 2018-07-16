package application;
	
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
	public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {

        StackPane root = new StackPane();

        TabPane tabPane = new TabPane();
       
        Tab tab1 = new Tab();
        tab1.setText("Rectangle");
        //tab1.setContent(new Rectangle(100, 100, Color.LIGHTSTEELBLUE));
       
        Tab tab2 = new Tab();
        tab2.setText("Line");
        //tab2.setContent(new Line(0, 0, 100, 100)); 
       
        Tab tab3 = new Tab();
        tab3.setText("Circle");
        //tab3.setContent(new Circle(0, 0, 50, Color.LIGHTGREEN));        
       
        tabPane.getSelectionModel().select(1);
        tabPane.getTabs().addAll(tab1, tab2, tab3);
       
        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 300, 250);

        stage.setTitle("TabPane");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

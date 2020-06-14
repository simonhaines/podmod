package scalardata.podmod;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scalardata.podmod.audio.Hardware;
import scalardata.podmod.mix.Mixer;
import scalardata.podmod.ui.MixerController;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
    	final var microphone = Hardware.getMicrophone();
    	final var headphones = Hardware.getHeadphones();
    	
    	final var mixer = new Mixer(microphone.getFormat(), microphone, headphones);
    	final var controller = new MixerController(mixer);
        
    	try {
            scene = new Scene(controller.getView(), 640, 480);
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
            stage.setOnHidden(we -> controller.cleanUp());
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    }

    public static void main(String[] args) {
        launch();
    }
}
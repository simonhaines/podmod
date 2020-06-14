package scalardata.podmod.ui.controls;

import java.io.IOException;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

public class LevelMeter extends Pane {
	
	@FXML
	private Line line;

	final private ObjectProperty<Paint> colour = new SimpleObjectProperty<>(Color.DARKRED);

	private AnimationTimer timer;
	private float level = 0f;
	
	public LevelMeter() {
		super();
		
		try {
			// Load child nodes from template
			var loader = new FXMLLoader(getClass().getResource("LevelMeter.fxml"));
			loader.setController(this);
			getChildren().add(loader.load());

			// Bind values
			line.strokeProperty().bind(colour);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public float getLevel() {
		return this.level;
	}

	public void setLevel(float level) {
		this.level = level;
	}

	public final ObjectProperty<Paint> colourProperty() {
		return this.colour;
	}
	
	public final Paint getColour() {
		return this.colourProperty().get();
	}
	
	public final void setColour(final Paint colour) {
		this.colourProperty().set(colour);
	}
	
	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
	}
	
	@FXML
	public void initialize() {
		timer = new AnimationTimer() {
			float renderedLevel = 0f;

			@Override
			public void handle(long now) {
				if (level != renderedLevel) {
					line.setEndX(getWidth() * level);
					renderedLevel = level;
				}
			}
		};
		timer.start();
	}
}

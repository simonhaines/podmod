package scalardata.podmod.ui.controls;

import java.util.Random;

import javafx.geometry.Orientation;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class LevelMeterPane extends Region {
	private static final int LEVEL_COUNT = 64;
	private static final double MIN_LEVEL_WIDTH = 10.0;
	private static final double MIN_WIDTH = MIN_LEVEL_WIDTH * LEVEL_COUNT;

	private final Line[] levels = new Line[LEVEL_COUNT];
	private final float[] values = new float[LEVEL_COUNT];

	
	public LevelMeterPane() { 
		// Init values
		final Random random = new Random();
		for (var idx = 0; idx < LEVEL_COUNT; ++idx) {
			levels[idx] = new Line();
			values[idx] = random.nextFloat();
		}
		
		// Init graphics
		this.setBackground(new Background(new BackgroundFill(Color.DARKGREEN, null, null)));
		this.getChildren().addAll(levels);
	}
	
	@Override protected double computeMinWidth(double height) {
		return MIN_WIDTH;
	}
	
	@Override protected double computePrefWidth(double height) {
		return this.snappedLeftInset() + MIN_WIDTH + this.snappedRightInset();
	}
	
	@Override protected double computeMinHeight(double width) {
		return width / LEVEL_COUNT * 2;
	}

	@Override protected double computePrefHeight(double width) {
		return width / 2;
	}
	
	@Override public Orientation getContentBias() {
		// Ensures width is passed to computePrefHeight
		return Orientation.HORIZONTAL;
	}

	@Override protected void layoutChildren() {
		final double x = snappedLeftInset();
		final double y = snappedTopInset();
		final double w = getWidth() - (snappedLeftInset() + snappedRightInset());
		final double h = getHeight() - (snappedTopInset() + snappedBottomInset());
		
		final double levelWidth = w / LEVEL_COUNT;
		for (var idx = 0; idx < LEVEL_COUNT; ++idx) {
			final Line level = levels[idx];
			final float value = values[idx];
			final double height = (h - levelWidth) * value;
			level.setStartX(x);
			level.setEndX(x);
			level.setStartY(y);
			level.setEndY(height);
			level.setStrokeWidth(levelWidth);
			level.relocate(idx * levelWidth, h - height - levelWidth);
		}

	}
}

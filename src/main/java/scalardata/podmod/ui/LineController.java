package scalardata.podmod.ui;

import java.io.IOException;
import java.time.Duration;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import scalardata.podmod.mix.LineState;
import scalardata.podmod.mix.Mixer;
import scalardata.podmod.mix.MixerListener;

public class LineController {

	final StringProperty cursor = new SimpleStringProperty();
	final BooleanProperty canPlay = new SimpleBooleanProperty();
	final StringProperty playPauseLabel = new SimpleStringProperty();
	final BooleanProperty canMix = new SimpleBooleanProperty();
	final StringProperty mixPauseLabel = new SimpleStringProperty();
	final BooleanProperty canSkip = new SimpleBooleanProperty();
	
	@FXML
	final String name;
	final Mixer mixer;
	final int id;
	
	private Pane view;
	private LineState state;

	public LineController(Mixer mixer, int id, String name) {
		this.mixer = mixer;
		this.id = id;
		this.name = name;

		this.mixer.addListener(new MixerListener() {
			@Override
			public void stateChanged(int line, LineState state) {
				if (line == id) {
					Platform.runLater(() -> handleStateChange(line, state));
				}
			}
			@Override
			public void positionUpdated(int line, Duration position) {
				if (line == id) {
					Platform.runLater(() ->
						LineController.this.cursor.set(durationToString(position)));
				}
			}
		});
	}
	
	public Pane getView() throws IOException {
		if (view == null) {
			var loader = new FXMLLoader(getClass().getResource("Mixer.fxml"));
			loader.setController(this);
			view = loader.load();
		}
		return view;
	}
	
	@FXML
	public void initialize() {
		// Set initial state
		state = LineState.IDLE;
		canPlay.set(true);
		playPauseLabel.set("PLAY");
		canMix.set(false);
		mixPauseLabel.set("MIX");
		canSkip.set(true);

		try {
			cursor.set(durationToString(mixer.getPosition(id)));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			onError();
		}
	}
	
	public void onSkipForward() {
	}
	
	public void onSkipBackward() {
	}
	
	public void onPlayPause(ActionEvent e) {
		if (state == LineState.PLAYING) {
			mixer.stopPlaying(id);
		} else {
			mixer.startPlaying(id);
		}
	}
	
	public void onMixPause(ActionEvent e) {
		if (state == LineState.MIXING) {
			// TODO mixer.stopMixing(id);
		} else {
			// TODO mixer.startMixing(id);
		}
	}
	
	private void onError() {
		state = LineState.ERROR;
		cursor.set("ERROR");
		canPlay.set(false);
		canMix.set(false);
		canSkip.set(false);
	}
	
	private void handleStateChange(int line, LineState newState) {
		if (line == id) {
			// State change to this line
			state = newState;
			switch (state) {
			case IDLE:
				canPlay.set(true);
				canMix.set(true);
				canSkip.set(true);
				break;
			case RECORDING:
				canPlay.set(false);
				canMix.set(true);
				canSkip.set(true);
				break;
			case MIXING:
				canPlay.set(true);
				canMix.set(false);
				canSkip.set(false);
				break;
			case PLAYING:
				canPlay.set(true);
				canMix.set(false);
				canSkip.set(true);
				break;
			case ERROR:
				onError();
				break;
			}
		}
		// TODO handle changes to other lines:
		// master starts mixing, master starts playing, etc
	}

	private String durationToString(Duration duration) {
		var seconds = duration.getSeconds();
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}

	public final StringProperty cursorProperty() {
		return this.cursor;
	}
	

	public final String getCursor() {
		return this.cursorProperty().get();
	}
	

	public final void setCursor(final String cursor) {
		this.cursorProperty().set(cursor);
	}
	

	public final BooleanProperty canPlayProperty() {
		return this.canPlay;
	}
	

	public final boolean isCanPlay() {
		return this.canPlayProperty().get();
	}
	

	public final void setCanPlay(final boolean canPlay) {
		this.canPlayProperty().set(canPlay);
	}
	

	public final StringProperty playPauseLabelProperty() {
		return this.playPauseLabel;
	}
	

	public final String getPlayPauseLabel() {
		return this.playPauseLabelProperty().get();
	}
	

	public final void setPlayPauseLabel(final String playPauseLabel) {
		this.playPauseLabelProperty().set(playPauseLabel);
	}
	

	public final BooleanProperty canMixProperty() {
		return this.canMix;
	}
	

	public final boolean isCanMix() {
		return this.canMixProperty().get();
	}
	

	public final void setCanMix(final boolean canMix) {
		this.canMixProperty().set(canMix);
	}
	

	public final StringProperty mixPauseLabelProperty() {
		return this.mixPauseLabel;
	}
	

	public final String getMixPauseLabel() {
		return this.mixPauseLabelProperty().get();
	}
	

	public final void setMixPauseLabel(final String mixPauseLabel) {
		this.mixPauseLabelProperty().set(mixPauseLabel);
	}
	

	public final BooleanProperty canSkipProperty() {
		return this.canSkip;
	}
	

	public final boolean isCanSkip() {
		return this.canSkipProperty().get();
	}
	

	public final void setCanSkip(final boolean canSkip) {
		this.canSkipProperty().set(canSkip);
	}
	
}

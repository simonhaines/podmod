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
	private boolean isPlaying;
	private boolean isMixing;

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
		isPlaying = false;
		setCanPlay(true);
		setPlayPauseLabel("PLAY");
		isMixing = false;
		setCanMix(false);
		setMixPauseLabel("MIX");
		setCanSkip(true);

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
		if (isPlaying) {
			mixer.stopPlaying(id);
		} else {
			mixer.startPlaying(id);
		}
	}
	
	public void onMixPause(ActionEvent e) {
		if (isMixing) {
			// TODO mixer.stopMixing(id);
		} else {
			// TODO mixer.startMixing(id);
		}
	}
	
	private void onError() {
		cursor.set("ERROR");
		canPlay.set(false);
		canMix.set(false);
		canSkip.set(false);
	}
	
	private void handleStateChange(int line, LineState newState) {
		switch (newState) {
		case IDLE:
			// The mixer is going idle
			isPlaying = false;
			setCanPlay(true);
			setPlayPauseLabel("PLAY");
			isMixing = false;
			setCanMix(false);
			setMixPauseLabel("MIX");
			setCanSkip(true);
			break;
		case RECORDING:
			if (line == id) {
				// This channel is being recorded (spliced)
				isPlaying = true;
				setCanPlay(true);
				setPlayPauseLabel("PAUSE");
				isMixing = false;
				setCanMix(false);
				setMixPauseLabel("MIX");
				setCanSkip(false);
			} else {
				// Some other channel is being recorded (spliced)
				isPlaying = false;
				setCanPlay(false);
				setPlayPauseLabel("PLAY");
				isMixing = false;
				setCanMix(false);
				setMixPauseLabel("MIX");
				setCanSkip(true);
			}
			break;
		case MIXING:
			if (line == id) {
				// This channel is being mixed
				isPlaying = false;
				setCanPlay(false);
				setPlayPauseLabel("PLAY");
				isMixing = true;
				setCanMix(true);
				setMixPauseLabel("PAUSE");
				setCanSkip(false);
			} else {
				// Some other channel is being mixed
				isPlaying = false;
				setCanPlay(false);
				setPlayPauseLabel("PLAY");
				isMixing = false;
				setCanMix(false);
				setMixPauseLabel("MIX");
				setCanSkip(false);
			}
			break;
		case PLAYING:
			if (line == id) {
				// This channel is being played
				isPlaying = true;
				setCanPlay(true);
				setPlayPauseLabel("PAUSE");
				isMixing = false;
				setCanMix(false);
				setMixPauseLabel("MIX");
				setCanSkip(true);
			} else {
				// Some other channel is being played
				isPlaying = false;
				setCanPlay(true);
				setPlayPauseLabel("PLAY");
				isMixing = false;
				setCanMix(false);
				setMixPauseLabel("MIX");
				setCanSkip(true);
			}
			break;
		default:
			onError();
			break;
		}
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

package scalardata.podmod.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import scalardata.podmod.mix.LineState;
import scalardata.podmod.mix.Mixer;
import scalardata.podmod.mix.MixerListener;

public class MixerController {
	
	final StringProperty cursor = new SimpleStringProperty();
	final BooleanProperty canPlay = new SimpleBooleanProperty();
	final StringProperty playPauseLabel = new SimpleStringProperty();
	final BooleanProperty canRecord = new SimpleBooleanProperty();
	final StringProperty recordPauseLabel = new SimpleStringProperty();
	final BooleanProperty canSkip = new SimpleBooleanProperty();
	private Pane view;

	final Mixer mixer;
	final List<LineController> lines = new ArrayList<>();

	LineState state;

	public MixerController(Mixer mixer) {
		this.mixer = mixer;
		this.mixer.addListener(new MixerListener() {
			@Override
			public void stateChanged(int line, LineState state) {
				if (line == 0) {
					Platform.runLater(() -> MixerController.this.handleStateChange(line, state));
				}
			}
			@Override
			public void positionUpdated(int line, Duration position) {
				if (line == 0) {
					Platform.runLater(() ->
						MixerController.this.setCursor(durationToString(position)));
				}
			}
		});

	}
	
	@FXML
	public void initialize() {
		// Set initial state
		state = LineState.IDLE;
		canPlay.set(true);
		playPauseLabel.set("PLAY");
		canRecord.set(true);
		recordPauseLabel.set("REC");
		canSkip.set(true);
		cursor.set("0:00");
		
		// FIXME just open some recording
		try {
			var recording = Path.of("recording.pcm");
			mixer.open(recording);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void cleanUp() {
		try {
			mixer.close();
		} catch (IOException e) {
			// Noting else can really be done here
			e.printStackTrace();
		}
	}
	
	public Pane getView() throws IOException {
		if (view == null) {
			var loader = new FXMLLoader(getClass().getResource("Mixer.fxml"));
			loader.setController(this);
			view = loader.load();
		}
		return view;
	}
	
	public void openLine(Path path) throws IOException {
		try {
			// Construct a controller
			final var id = mixer.addClip(path);
			final var name = path.getFileName().toString();
			final var controller = new LineController(mixer, id, name);
			lines.add(controller);
			getView().getChildren().add(controller.getView());
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}
	
	@FXML
	private void onPlayPause() {
		if (state == LineState.IDLE)
			mixer.startPlaying(0);
		else if (state == LineState.PLAYING)
			mixer.stopPlaying(0);
	}
	
	@FXML
	private void onRecordPause() {
		if (state == LineState.IDLE)
			mixer.startRecording();
		else if (state == LineState.RECORDING)
			mixer.stopRecording();
	}
	
	private void handleStateChange(int id, LineState newState) {
		if (id == 0 && newState == LineState.PLAYING) {
			state = LineState.PLAYING;
			setPlayPauseLabel("STOP");
			setCanPlay(true);
			setCanRecord(false);
		} else if (id == 0 && newState == LineState.IDLE) {
			state = LineState.IDLE;
			setPlayPauseLabel("PLAY");
			setCanPlay(true);
			setCanRecord(true);
			setCanSkip(true);
		} else if (id == 0 && newState == LineState.RECORDING) {
			state = LineState.RECORDING;
			setRecordPauseLabel("STOP");
			setCanPlay(false);
			setCanRecord(true);
			setCanSkip(true);
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
	

	public final BooleanProperty canRecordProperty() {
		return this.canRecord;
	}
	

	public final boolean isCanRecord() {
		return this.canRecordProperty().get();
	}
	

	public final void setCanRecord(final boolean canRecord) {
		this.canRecordProperty().set(canRecord);
	}
	

	public final StringProperty recordPauseLabelProperty() {
		return this.recordPauseLabel;
	}
	

	public final String getRecordPauseLabel() {
		return this.recordPauseLabelProperty().get();
	}
	

	public final void setRecordPauseLabel(final String recordPauseLabel) {
		this.recordPauseLabelProperty().set(recordPauseLabel);
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

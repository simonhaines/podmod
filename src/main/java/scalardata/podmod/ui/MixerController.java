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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import scalardata.podmod.mix.LineState;
import scalardata.podmod.mix.Mixer;
import scalardata.podmod.mix.MixerListener;

public class MixerController {
	
	final StringProperty title = new SimpleStringProperty();
	final StringProperty cursor = new SimpleStringProperty();
	final BooleanProperty canPlay = new SimpleBooleanProperty();
	final StringProperty playPauseLabel = new SimpleStringProperty();
	final BooleanProperty canRecord = new SimpleBooleanProperty();
	final StringProperty recordPauseLabel = new SimpleStringProperty();
	final BooleanProperty canSkip = new SimpleBooleanProperty();
	private Pane view;

	final Mixer mixer;
	final List<LineController> lines = new ArrayList<>();
	
	private boolean isPlaying;
	private boolean isRecording;

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
		isPlaying = false;
		setCanPlay(true);
		setPlayPauseLabel("PLAY");
		isRecording = false;
		setCanRecord(true);
		setRecordPauseLabel("REC");
		setCanSkip(true);
		setCursor("0:00");

		// Hotkeys
		
		
		// FIXME just open some recording
		try {
			var recording = Path.of("recording.pcm");
			mixer.open(recording);
			setCursor(durationToString(mixer.getLength(0)));
			setTitle(recording.getFileName().toString());
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

			// Add a listener to register accelerators when the scene is set
			view.sceneProperty().addListener((observer, oldScene, newScene) -> {
				if (oldScene != newScene) {
					final var accelerators = newScene.getAccelerators();
					accelerators.put(new KeyCodeCombination(KeyCode.P), () -> onPunchIn(true));
					accelerators.put(new KeyCodeCombination(KeyCode.P, KeyCombination.SHIFT_DOWN),
							() -> onPunchIn(false));
				}
			});
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
	
	private void onPunchIn(boolean preview) {
		// No button wired up for this yet, but there may be one in future
		mixer.startPunchIn(preview);
	}
	
	@FXML
	private void onPlayPause() {
		if (isPlaying) {
			mixer.stopPlaying(0);
		} else {
			mixer.startPlaying(0);
		}
	}
	
	@FXML
	private void onRecordPause() {
		if (isRecording) {
			mixer.stopRecording();
		} else {
			mixer.startRecording();
		}
	}
	
	@FXML
	private void onSkipForward() {
		mixer.skipForward(0, Duration.ofSeconds(1));
	}
	
	@FXML
	private void onSkipBackward() {
		mixer.skipBackward(0, Duration.ofSeconds(1));
	}
	
	private void handleStateChange(int id, LineState newState) {
		switch (newState) {
		case IDLE:
			isPlaying = false;
			setCanPlay(true);
			setPlayPauseLabel("PLAY");
			isRecording = false;
			setCanRecord(true);
			setRecordPauseLabel("REC");
			setCanSkip(true);
			break;
		case PLAYING:
			if (id == 0) {
				// Master is playing
				isPlaying = true;
				setCanPlay(true);
				setPlayPauseLabel("STOP");
				isRecording = false;
				setCanRecord(false);
				setRecordPauseLabel("REC");
				setCanSkip(true);
			} else {
				// Some other channel is playing
				isPlaying = false;
				setCanPlay(false);
				setPlayPauseLabel("PLAY");
				isRecording = false;
				setCanRecord(false);
				setRecordPauseLabel("REC");
				setCanSkip(true);
			}
			break;
		case RECORDING:
			isPlaying = false;
			setCanPlay(false);
			setPlayPauseLabel("PLAY");
			isRecording = true;
			setCanRecord(true);
			setRecordPauseLabel("STOP");
			setCanSkip(false);
			break;
		case MIXING:
			isPlaying = false;
			setCanPlay(false);
			setPlayPauseLabel("PLAY");
			isRecording = true;
			setCanRecord(true);
			setRecordPauseLabel("STOP");
			setCanSkip(false);
			break;
		default:
			isPlaying = false;
			setCanPlay(false);
			setPlayPauseLabel("PLAY");
			isRecording = false;
			setCanRecord(false);
			setRecordPauseLabel("REC");
			setCursor("ERROR");
			break;
		}
	}
	
	private String durationToString(Duration duration) {
		var seconds = duration.getSeconds();
		var tenths = duration.getNano() / 1e8;
		return String.format("%d:%02d.%d", seconds / 60, seconds % 60, (int)tenths);
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

	public final StringProperty titleProperty() {
		return this.title;
	}
	

	public final String getTitle() {
		return this.titleProperty().get();
	}
	

	public final void setTitle(final String title) {
		this.titleProperty().set(title);
	}
	
	
}

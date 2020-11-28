package scalardata.podmod.mix;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import scalardata.podmod.audio.AudioFormats;

public class Mixer implements Closeable, AutoCloseable {
	

	final AudioFormat format;
	final TargetDataLine microphone;
	final SourceDataLine headphones;
	final ArrayList<RandomAccessFile> lines = new ArrayList<>();
	
	private final ArrayList<MixerListener> listeners = new ArrayList<>();
	private ExecutorService executor;
	private MixerState currentState;

	public Mixer(AudioFormat format, TargetDataLine microphone, SourceDataLine headphones) {
		this.format = format;
		this.microphone = microphone;
		this.headphones = headphones;
		currentState = new IdleState(this, 0);
	}
	
	public void addListener(MixerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MixerListener listener) {
		listeners.remove(listener);
	}
	
	public void open(Path path) throws IOException {
		final var master = new RandomAccessFile(path.toString(), "rw");
		lines.clear();
		lines.add(master);
		
		// Start the recording/mixing loop
		executor = Executors.newSingleThreadExecutor();
		executor.submit(this::processor);
	}
	
	public int addClip(Path path) throws FileNotFoundException {
		final var line = new RandomAccessFile(path.toString(), "r");
		final var id = lines.size();
		lines.set(id, line);
		return id;
	}
	
	public Duration getLength(int id) throws IOException {
		final var length = lines.get(id).length();
		return AudioFormats.getDuration(format, length);
	}
	
	public Duration getPosition(int id) throws IOException {
		final var pointer = lines.get(id).getFilePointer();
		return AudioFormats.getDuration(format, pointer);
	}
	
	public void startRecording() {
		currentState.record();
	}
	
	public void stopRecording() {
		currentState.stop(0);
	}
	
	public void startPlaying(int channel) {
		currentState.play(channel);
	}
	
	public void stopPlaying(int channel) {
		currentState.stop(channel);
	}
	
	public void startMixing(int channel) {
		currentState.mix(channel);
	}
	
	public void stopMixing(int channel) {
		currentState.stop(channel);
	}
	
	public void startPunchIn(boolean preview) {
		currentState.punchIn(preview);
	}
	
	public void skipForward(int channel, Duration amount) {
		final var length = AudioFormats.getLength(format, amount);
		currentState.skipForward(channel, length);
	}
	
	public void skipBackward(int channel, Duration amount) {
		final var length = AudioFormats.getLength(format, amount);
		currentState.skipBackward(channel, length);
	}
	
	void notifyStateChanged(int line, LineState state) {
		listeners.stream().forEach(l -> l.stateChanged(line, state));
	}
	
	void notifyPositionUpdated(int line, long position) {
		final var duration = AudioFormats.getDuration(format, position);
		listeners.stream().forEach(l -> l.positionUpdated(line, duration));
	}
	
	void processor() {
		while (!(currentState instanceof ErrorState)) {
			currentState = currentState.process();
			if (currentState == null)
				return;
		}
	}

	@Override
	public void close() throws IOException {
		// Terminate the current mixer state
		currentState.terminate();
		
		// Shut down the executor and wait for mixing states to exit
		try {
			executor.shutdown();
			executor.awaitTermination(200, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		headphones.close();
		microphone.close();
		for (var line : lines) {
			line.close();
		}
	}

	/** Default implementation */
	void skipForward(int channel, long length) {
		try {
			final var line = lines.get(channel);
			var pos = line.getFilePointer() + length;
			if (pos > line.length())
				pos = line.length();
			line.seek(pos);
			notifyPositionUpdated(channel, pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Default implementation */
	void skipBackward(int channel, long length) {
		try {
			final var line = lines.get(channel);
			var pos = line.getFilePointer() - length;
			if (pos < 0)
				pos = 0;
			line.seek(pos);
			notifyPositionUpdated(channel, pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

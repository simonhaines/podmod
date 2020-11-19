package scalardata.podmod.mix;

import java.io.IOException;
import java.time.Duration;

import scalardata.podmod.audio.AudioFormats;

public class MixingState implements MixerState {
	
	private final Mixer mixer;
	private final int channel;

	private byte[] frame;
	private MixerState nextState;
	
	MixingState(Mixer mixer, int channel) {
		this.mixer = mixer;
		this.channel = channel;
		
		// Read frames of 200ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(2000));
		frame = new byte[(int)frameLength];
	}
	
	@Override
	public void start() {
		mixer.notifyStateChanged(channel, LineState.MIXING);
	}

	@Override
	public MixerState tick() {
		if (nextState != null) {
			mixer.headphones.drain();
			mixer.headphones.stop();
			return nextState;
		}
		
		if (!mixer.headphones.isActive()) {
			mixer.headphones.flush();
			mixer.headphones.start();
		}
		
		try {
			final var line = mixer.lines.get(channel);
			final var remaining = line.length() - line.getFilePointer();
			if (remaining == 0) {
				return new RecordingState(mixer);
			}

			// Read from line
			final var length = (int)Math.min(remaining, frame.length);
			line.read(frame, 0, length);

			// Write to headphones and master
			mixer.headphones.write(frame, 0, length);
			mixer.lines.get(0).write(frame, 0, length);
			return this;
		} catch (IOException ioe) {
			return new ErrorState(ioe);
		}
	}

	@Override
	public void record() {
		// Do nothing
	}

	@Override
	public void play(int channel) {
		// Do nothing
	}

	@Override
	public void mix(int channel) {
		// Do nothing
	}

	@Override
	public void stop(int channel) {
		if (channel == this.channel) {
			nextState = new RecordingState(mixer);
		}
	}

	@Override
	public void skipForward(int channel, long length) {
		if (channel != this.channel)
			mixer.skipForward(channel, length);
	}

	@Override
	public void skipBackward(int channel, long length) {
		if (channel != this.channel)
			mixer.skipBackward(channel, length);
	}

	@Override
	public void punchIn(boolean preview) {
		// Do nothing
	}

}

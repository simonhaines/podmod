package scalardata.podmod.mix;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;

import scalardata.podmod.audio.AudioFormats;

public class PlayingState implements MixerState {
	
	private final Mixer mixer;
	private final int channel;
	private final byte[] frame;

	private RandomAccessFile source;
	private MixerState nextState;
	private long savedPosition;
	private long nextPosition;
	
	PlayingState(Mixer mixer, int channel) {
		this.mixer = mixer;
		this.channel = channel;

		// Read frames of 200ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(200));
		frame = new byte[(int)frameLength];
	}

	@Override
	public MixerState tick() {
		mixer.notifyStateChanged(channel, LineState.PLAYING);
		
		try {
			// Engage the headphones
			if (!mixer.headphones.isActive()) {
				mixer.headphones.start();
			}

			// Save the current position for replaying
			source = mixer.lines.get(channel);
			savedPosition = source.getFilePointer();
			nextPosition = -1;
			
			while (nextState == null) {
				// Skip to a new position if required
				if (nextPosition != -1) {
					source.seek(nextPosition);
					nextPosition = -1;
				}

				// Go idle if finished
				final var remaining = source.length() - source.getFilePointer();
				final var amount = Math.min(remaining, frame.length);
				if (amount == 0) {
					return new IdleState(mixer, channel);
				} else {
					try {
						source.read(frame, 0, (int)amount);
						mixer.headphones.write(frame, 0, (int)amount);
						mixer.headphones.drain();
						mixer.notifyPositionUpdated(channel, source.getFilePointer());
					} catch (IOException ioe) {
						mixer.notifyStateChanged(channel, LineState.ERROR);
						return new ErrorState(ioe);
					}
				}
			}
			return nextState;

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
		if (channel == this.channel) {
			// Restart
			nextPosition = savedPosition;
		} else {
			// Play another channel
			nextState = new PlayingState(mixer, channel);
		}
	}

	@Override
	public void mix(int channel) {
		// Do nothing
	}

	@Override
	public void stop(int channel) {
		if (this.channel == channel)
			nextState = new IdleState(mixer, this.channel);
	}

}

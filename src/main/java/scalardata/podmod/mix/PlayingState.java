package scalardata.podmod.mix;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;

import scalardata.podmod.audio.AudioFormats;

public class PlayingState implements MixerState {
	// See https://github.com/philfrei/AudioCue
		
	private final Mixer mixer;
	private final int channel;
	private final byte[] frame;

	private RandomAccessFile source;
	private MixerState nextState;
	private long savedPosition;
	private long nextPosition;
	private boolean terminating = false;
	
	PlayingState(Mixer mixer, int channel) {
		this.mixer = mixer;
		this.channel = channel;

		// Read frames of 200ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(100));
		frame = new byte[(int)frameLength];
	}

	@Override
	public MixerState process() {
		mixer.notifyStateChanged(channel, LineState.PLAYING);

		// Engage the headphones
		if (!mixer.headphones.isActive()) {
			mixer.headphones.start();
		}

		// Save the current position for replaying
		try {
			source = mixer.lines.get(channel);
			savedPosition = source.getFilePointer();
		} catch (IOException e) {
			return new ErrorState(e);
		}
		nextPosition = -1; 

		try {
			// TODO get a better algorithm for this (Duration)
			final var threshold = mixer.headphones.getBufferSize() - 800;
			
			while (nextState == null) {
				// Exit if terminating
				if (terminating) return null;
				
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
						final int amountRead = source.read(frame, 0, (int)amount);
						mixer.headphones.write(frame, 0, amountRead);
						
						//mixer.headphones.drain();
						while (mixer.headphones.available() < threshold) {
							Thread.yield();
						}
						
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

	@Override
	public void skipForward(int channel, long length) {
		try {
			if (channel == this.channel) {
				var pos = source.getFilePointer() + length;
				if (pos > source.length())
					pos = source.length();
				nextPosition = pos;
			} else {
				mixer.skipForward(channel, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void skipBackward(int channel, long length) {
		try {
			if (channel == this.channel) {
				var pos = source.getFilePointer() - length;
				if (pos < 0)
					pos = 0;
				nextPosition = pos;
			} else {
				mixer.skipBackward(channel, length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void punchIn(boolean preview) {
		// Do nothing
	}

	@Override
	public void terminate() {
		terminating = true;
	}
}

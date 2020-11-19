package scalardata.podmod.mix;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;

import scalardata.podmod.audio.AudioFormats;

public class PunchInState implements MixerState {
	
	private final Mixer mixer;
	private final boolean preview;
	private final RandomAccessFile master;
	private final byte[] frame;
	private MixerState nextState;

	PunchInState(Mixer mixer, boolean preview) {
		this.mixer = mixer;
		this.preview = preview;
		master = mixer.lines.get(0);
		
		// Read frames of 100ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(100));
		frame = new byte[(int)frameLength];
		
		nextState = null;
	}

	@Override
	public void start() {
		mixer.notifyStateChanged(0, LineState.PLAYING);
	}

	@Override
	public MixerState tick() {
		try {
			// Take the current pointer in the master file and
			// rewind 3s or to the start, whichever is less
			final long punchPoint = master.getFilePointer();
			// TODO ensure the duration is a direct multiple of frames
			var pos = punchPoint - AudioFormats.getLength(mixer.format, Duration.ofSeconds(3));
			if (pos < 0) pos = 0;
			master.seek(pos);
			
			// Start the playing loop
			while (nextState == null) {
				// Start recording or go idle if we've reached the punch point
				final var remaining = punchPoint - master.getFilePointer();
				final var amount = Math.min(remaining, frame.length);
				if (amount == 0) {
					if (preview)
						return new IdleState(mixer, 0);
					else
						return new RecordingState(mixer);
				} else {
					final int amountRead = master.read(frame, 0, (int)amount);
					mixer.headphones.write(frame, 0, amountRead);
					mixer.notifyPositionUpdated(0, master.getFilePointer());
				}
			}
			
			// We've been interrupted (probably by going idle)
			// restore the master position before returning
			master.seek(punchPoint);
			return nextState;
		} catch (IOException ioe) {
			mixer.notifyStateChanged(0, LineState.ERROR);
			return new ErrorState(ioe);
		}
	}

	@Override
	public void record() {
		// TODO Auto-generated method stub

	}

	@Override
	public void play(int channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mix(int channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(int channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skipForward(int channel, long length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skipBackward(int channel, long length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void punchIn(boolean preview) {
		// Already punching in, do nothing
	}

}

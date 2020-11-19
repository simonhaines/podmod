package scalardata.podmod.mix;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;

import javax.sound.sampled.AudioInputStream;

import scalardata.podmod.audio.AudioFormats;

public class RecordingState implements MixerState {
	
	private final Mixer mixer;
	private final byte[] frame;

	private final AudioInputStream source;
	private final RandomAccessFile master;
	private MixerState nextState;
	
	RecordingState(Mixer mixer) {
		this.mixer = mixer;
		source = new AudioInputStream(mixer.microphone);
		master = mixer.lines.get(0);
		
		// Read frames of 200ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(100));
		frame = new byte[(int)frameLength];

		
		// Don't buffer
		//masterBuffer = master.getChannel().map(MapMode.READ_WRITE, 0, newLength);
		//masterBuffer.position(position);
	}
	
	@Override
	public void start() {
		// Start the microphone
		mixer.microphone.flush();
		if (!mixer.microphone.isActive()) {
			mixer.microphone.start();
		}

		// Trim the master file to the current location
		try {
			master.setLength(master.getFilePointer());
			mixer.notifyPositionUpdated(0, master.getFilePointer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mixer.notifyStateChanged(0, LineState.RECORDING);
	}

	@Override
	public MixerState tick() {
		if (nextState != null) {
			if (nextState instanceof IdleState) {
				mixer.microphone.stop();
				try {
					// Trim the master file to get rid of excess capacity
					mixer.lines.get(0).setLength(master.getFilePointer());
				} catch (IOException ioe) {
					return new ErrorState(ioe);
				}
			}
			return nextState;
		}
		
		try {
			final var length = source.read(frame);
 			ensureCapacity(length);
			master.write(frame, 0, length);
			mixer.notifyPositionUpdated(0, master.getFilePointer());
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
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(int channel) {
		// Go idle
		nextState = new IdleState(mixer, 0);
	}
	
	void ensureCapacity(int length) throws IOException {
		final var position = master.getFilePointer();
		if (position + length > master.length()) {
			// Enlarge the file to fit extra frames
			final var newLength = master.length() + (frame.length * 10);
			master.setLength(newLength);
		}
	}

	@Override
	public void skipForward(int channel, long length) {
		if (channel != 0)
			mixer.skipForward(channel, length);
	}

	@Override
	public void skipBackward(int channel, long length) {
		if (channel != 0) 
			mixer.skipBackward(channel, length);
	}

	@Override
	public void punchIn(boolean preview) {
		// Do nothing
	}

}

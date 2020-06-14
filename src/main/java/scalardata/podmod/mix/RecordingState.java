package scalardata.podmod.mix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.time.Duration;

import javax.sound.sampled.AudioInputStream;

import scalardata.podmod.audio.AudioFormats;

public class RecordingState implements MixerState {
	
	private final Mixer mixer;
	private final byte[] frame;

	private ByteBuffer masterBuffer;
	private AudioInputStream source;
	private MixerState nextState;
	
	
	RecordingState(Mixer mixer) {
		this.mixer = mixer;
		
		// Read frames of 200ms
		final long frameLength = AudioFormats.getLength(mixer.format, Duration.ofMillis(200));
		frame = new byte[(int)frameLength];
	}

	@Override
	public MixerState tick() {
		if (nextState != null) {
			if (nextState instanceof IdleState) {
				mixer.microphone.stop();
				try {
					// Trim the master file to get rid of excess capacity
					mixer.lines.get(0).setLength(masterBuffer.position());
				} catch (IOException ioe) {
					return new ErrorState(ioe);
				}
			}
			return nextState;
		}
		
		try {
			if (!mixer.microphone.isActive()) {
				mixer.microphone.flush();
				mixer.microphone.start();
				source = new AudioInputStream(mixer.microphone);

				// Position the master track at the end
				final var master = mixer.lines.get(0);
				master.seek(master.length());
			}

			final var length = source.read(frame);
			ensureCapacity(length);
			masterBuffer.put(frame, 0, length);
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
		final var position = masterBuffer.position();
		if (position + length > masterBuffer.capacity()) {
			// Enlarge the file to fit extra frames
			final var newLength = masterBuffer.capacity() + (frame.length * 10);
			final var master = mixer.lines.get(0);
			master.setLength(newLength);
			masterBuffer = master.getChannel().map(MapMode.READ_WRITE, 0, newLength);
			masterBuffer.position(position);
		}
	}

}

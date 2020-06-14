package scalardata.podmod.mix;

public class IdleState implements MixerState {
	
	private final Mixer mixer;
	private final int channel;
	private MixerState nextState;

	public IdleState(Mixer mixer, int channel) {
		this.mixer = mixer;
		this.channel = channel;
	}

	@Override
	public MixerState tick() {
		mixer.notifyStateChanged(channel, LineState.IDLE);
		synchronized (this) {
			try {
				wait();
				return nextState;
			} catch (InterruptedException ie) {
				return new ErrorState(ie);
			}
		}
	}

	@Override
	public void record() {
		synchronized (this) {
			nextState = new RecordingState(mixer);
			notify();
		}
	}

	@Override
	public void play(int channel) {
		synchronized (this) {
			nextState = new PlayingState(mixer, channel);
			notify();
		}
	}

	@Override
	public void mix(int channel) {
		// Do nothing
	}

	@Override
	public void stop(int channel) {
		// Do nothing
	}

}

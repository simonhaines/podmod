package scalardata.podmod.mix;

public class ErrorState implements MixerState {
	
	private final Exception exception;
	
	ErrorState(Exception exception) {
		this.exception = exception;
	}
	
	@Override
	public void terminate() {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public MixerState process() {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void record() {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void play(int channel) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void mix(int channel) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void stop(int channel) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void skipForward(int channel, long length) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void skipBackward(int channel, long length) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

	@Override
	public void punchIn(boolean preview) {
		throw new RuntimeException(String.format("Error: %s", exception.getMessage()));
	}

}

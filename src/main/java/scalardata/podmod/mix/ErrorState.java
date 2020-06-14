package scalardata.podmod.mix;

public class ErrorState implements MixerState {
	
	private final Exception exception;
	
	ErrorState(Exception exception) {
		this.exception = exception;
	}

	@Override
	public MixerState tick() {
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

}

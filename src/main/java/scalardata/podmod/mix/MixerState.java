package scalardata.podmod.mix;

public interface MixerState {
	/**
	 * Process the current state.
	 * @return The next state to process.
	 */
	MixerState tick();

	/**
	 * Start recording to the master track.
	 */
	void record();

	/**
	 * Start playing an audio channel.
	 * @param channel the channel to play
	 */
	void play(int channel);
	
	/**
	 * Start mixing an audio channel.
	 * @param channel the channel to mix
	 */
	void mix(int channel);
	
	/**
	 * Stop playing an audio channel.
	 * @param channel the channel to stop
	 */
	void stop(int channel);
}

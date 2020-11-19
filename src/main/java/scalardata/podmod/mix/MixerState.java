package scalardata.podmod.mix;

public interface MixerState {
	/**
	 * Start the mixer state.
	 */
	void start();

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

	/**
	 * Advance the cursor for a channel.
	 * @param channel the channel
	 * @param length the amount to advance
	 */
	void skipForward(int channel, long length);

	/**
	 * Wind back the cursor for a channel.
	 * @param channel the channel
	 * @param length the amount to move the cursor back
	 */
	void skipBackward(int channel, long length);

	/**
	 * Start a punch-and-roll session. If preview is true, it
	 * will not start recording, but only play the lead-in.
	 * @param preview
	 */
	void punchIn(boolean preview);
}

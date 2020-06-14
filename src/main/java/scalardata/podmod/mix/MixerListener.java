package scalardata.podmod.mix;

import java.time.Duration;

public interface MixerListener {
	/**
	 * Notification that a line has changed (e.g. from playing to idle).
	 * @param line the line index (0 is master)
	 * @param state the new state of the line
	 */
	public void stateChanged(int line, LineState state);

	/**
	 * Notification that a line's position has changed (e.g. from playing).
	 * @param line the line index (0 is master)
	 * @param position the new position
	 */
	public void positionUpdated(int line, Duration position);
}

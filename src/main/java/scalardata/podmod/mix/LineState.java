package scalardata.podmod.mix;

public enum LineState {
	IDLE,     // channel going idle
	RECORDING,  // source channel (0 for mic)
	MIXING,
	PLAYING,   // source channel
	ERROR
}

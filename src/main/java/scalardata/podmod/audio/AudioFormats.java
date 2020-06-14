package scalardata.podmod.audio;

import java.time.Duration;

import javax.sound.sampled.AudioFormat;

public class AudioFormats {
	
	public static long getLength(AudioFormat format, Duration duration) {
		final double bytesPerSecond = format.getSampleRate() *
    			((format.getSampleSizeInBits() + 7) / 8) *
    			format.getChannels();

		return (long)((duration.getSeconds() * bytesPerSecond)
				+ (bytesPerSecond * duration.getNano() * 1e-9));
	}
	
	public static Duration getDuration(AudioFormat format, long length) {
		final double bytesPerSecond = format.getSampleRate() *
    			((format.getSampleSizeInBits() + 7) / 8) *
    			format.getChannels();
		final var div = length / bytesPerSecond;
		final var mod = length % bytesPerSecond;
		return Duration.ofSeconds((long)Math.floor(div), (long)Math.floor(mod * 1e-9));
	}
}

package scalardata.podmod.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Hardware {
	
	public static TargetDataLine getMicrophone() {
        try {
        	var fmt = getAudioFormat();
            var info = new DataLine.Info(TargetDataLine.class, fmt);
            if (AudioSystem.isLineSupported(info)) {
            	var line = (TargetDataLine) AudioSystem.getLine(info);
            	line.open();
            	return line;
            }
        } catch (LineUnavailableException ignored) { }
        return null;
	}
	
	public static SourceDataLine getHeadphones() {
        try {
        	var fmt = getAudioFormat();
            var info = new DataLine.Info(SourceDataLine.class, fmt);
            if (AudioSystem.isLineSupported(info)) {
            	var line = (SourceDataLine) AudioSystem.getLine(info);
            	line.open();
            	return line;
            }
        } catch (LineUnavailableException ignored) { }
        return null;
	}

	private static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
}

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

            // Use the 'CODEC' mixer
            for (var mi : AudioSystem.getMixerInfo()) {
            	if (mi.getName().startsWith("CODEC")) {
            		var mixer = AudioSystem.getMixer(mi);
            		if (mixer.isLineSupported(info)) {
            			var line = (TargetDataLine) mixer.getLine(info);
            			line.open();
            			return line;
            		}
            	}
            }
        } catch (LineUnavailableException ignored) { }
        return null;
	}
	
	public static SourceDataLine getHeadphones() {
        try {
        	// The headphones aren't attached to the default mixer, so
        	// to capture the 'CODEC' mixer
        	var fmt = getAudioFormat();
            var info = new DataLine.Info(SourceDataLine.class, fmt);
            for (var mi : AudioSystem.getMixerInfo()) {
            	if (mi.getName().startsWith("CODEC")) {
            		var mixer = AudioSystem.getMixer(mi);
            		if (mixer.isLineSupported(info)) {
            			var line = (SourceDataLine) mixer.getLine(info);
            			line.open();
            			return line;
            		}
            	}
            }
        } catch (LineUnavailableException ignored) { }
        return null;
	}
	
	public static void listSourceLines() {
		var fmt = getAudioFormat();
        var info = new DataLine.Info(SourceDataLine.class, fmt);
        for (var mi : AudioSystem.getMixerInfo()) {
        	var m = AudioSystem.getMixer(mi);
        	if (m.isLineSupported(info)) {
            	System.out.println(String.format("%s|%s|%s|%s", mi.getName(), mi.getDescription(), mi.getVendor(), mi.getVersion()));
        	}
        }
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

package me.SoundCreator.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class ExportInfo {

	private File outFile;
	private int outSampleLen;
	private AudioFormat audioFormat;

	ExportInfo() {
	}

	public boolean isComplete() {
		return getOutFile() != null && getAudioFormat() != null && getOutSampleLen() > 0;
	}

	public void save(byte[] bytes) throws IOException {
		int byteSampleLen = bytes.length / (getAudioFormat().getSampleSizeInBits() / 8);
		if (byteSampleLen > getOutSampleLen()) {
			System.out.println("The input functions result in a longer sound than the specified export length.");
			System.out.println("This will make the sound seem like it was cut off.");
		} else if (byteSampleLen < getOutSampleLen()) { // repetition needed
			if (getOutSampleLen() % byteSampleLen != 0) {
				System.out.println("The export length is not a multiple of the input functions result.");
				System.out.println(
						"This would make the sound seem like it was cut off, extending export length to the next multiple.");
				setOutSampleLen((getOutSampleLen() / byteSampleLen + 1) * byteSampleLen);
			}
			byte[] newBytes = new byte[getOutSampleLen() * (getAudioFormat().getSampleSizeInBits() / 8)];
			for (int i = 0; i < getOutSampleLen(); i += byteSampleLen) {
				System.arraycopy(bytes, 0, newBytes, i, byteSampleLen);
			}
			bytes = newBytes;
		}
		AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(bytes), getAudioFormat(), bytes.length),
				Type.WAVE, getOutFile());
	}

	public AudioFormat getAudioFormat() {
		return audioFormat;
	}

	void setAudioFormat(AudioFormat audioFormat) {
		this.audioFormat = audioFormat;
	}

	public int getOutSampleLen() {
		return outSampleLen;
	}

	void setOutSampleLen(int outSampleLen) {
		this.outSampleLen = outSampleLen;
	}

	public File getOutFile() {
		return outFile;
	}

	void setOutFile(File outFile) {
		this.outFile = outFile;
	}

}

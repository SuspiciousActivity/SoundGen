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

	private boolean persistentTime;

	ExportInfo() {
	}

	public boolean isComplete() {
		return getOutFile() != null && getAudioFormat() != null && getOutSampleLen() > 0;
	}

	public boolean redirectToStdout() {
		return outFile != null && outFile.getName().equals("-");
	}

	public void save(byte[] bytes) throws IOException {
		int byteSampleLen = bytes.length;
		if (byteSampleLen > getOutSampleLen()) {
			log("The input functions result in a longer sound than the specified export length.");
			log("Extending export length to fit everything in. ("
					+ (byteSampleLen / getAudioFormat().getSampleRate() / (getAudioFormat().getSampleSizeInBits() / 8))
					+ "s)");
			setOutSampleLen(byteSampleLen);
		} else if (byteSampleLen < getOutSampleLen()) { // repetition needed
			if (getOutSampleLen() % byteSampleLen != 0) {
				setOutSampleLen((getOutSampleLen() / byteSampleLen + 1) * byteSampleLen);
				log("The export length is not a multiple of the input functions result.");
				log("This would make the sound seem like it was cut off, extending export length to the next multiple. ("
						+ (getOutSampleLen() / getAudioFormat().getSampleRate()
								/ (getAudioFormat().getSampleSizeInBits() / 8))
						+ "s)");
			}
			byte[] newBytes = new byte[getOutSampleLen()];
			for (int i = 0; i < getOutSampleLen(); i += byteSampleLen) {
				System.arraycopy(bytes, 0, newBytes, i, byteSampleLen);
			}
			bytes = newBytes;
		}
		if (redirectToStdout())
			AudioSystem.write(
					new AudioInputStream(new ByteArrayInputStream(bytes), getAudioFormat(), getOutSampleLen()),
					Type.WAVE, System.out);
		else
			AudioSystem.write(
					new AudioInputStream(new ByteArrayInputStream(bytes), getAudioFormat(), getOutSampleLen()),
					Type.WAVE, getOutFile());
		log("Saved!");
	}

	public void log(String s) {
		if (!redirectToStdout())
			System.out.println(s);
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

	public boolean isPersistentTime() {
		return persistentTime;
	}

	void setPersistentTime(boolean persistentTime) {
		this.persistentTime = persistentTime;
	}

}

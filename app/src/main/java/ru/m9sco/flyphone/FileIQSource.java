package ru.m9sco.flyphone;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileIQSource implements IQSourceInterface {
	private Callback callback = null;
	private boolean repeat = false;
	private int sampleRate = 0;
	private long frequency = 0;
	private int packetSize = 0;
	private int sleepTime = 0;			// min. time (in ms) between two getPacket() calls to simulate the sample rate
	private long lastAccessTime = 0;	// timestamp of the last getPacket() call
	private byte[] buffer = null;
	private File file = null;
	private String filename = null;
	private BufferedInputStream bufferedInputStream = null;
	private IQConverter iqConverter;
	private int fileFormat;
	private static final String LOGTAG = "FileIQSource";
	public static final int FILE_FORMAT_8BIT_SIGNED = 0;
	public static final int FILE_FORMAT_8BIT_UNSIGNED = 1;

	public FileIQSource(String filename, int sampleRate, long frequency, int packetSize, boolean repeat, int fileFormat) {
		this.filename = filename;
		this.file = new File(filename);
		this.repeat = repeat;
		this.fileFormat = fileFormat;
		this.sampleRate = sampleRate;
		this.frequency = frequency;
		this.packetSize = packetSize;
		this.buffer = new byte[packetSize];
		this.sleepTime = (int)((packetSize/2)/(float)sampleRate * 1000); // note: half packet size because of I and Q samples

		switch (fileFormat) {
			case FILE_FORMAT_8BIT_SIGNED:
				iqConverter = new Signed8BitIQConverter();
				break;
			case FILE_FORMAT_8BIT_UNSIGNED:
				iqConverter = new Unsigned8BitIQConverter();
				break;
			default:
				Log.e(LOGTAG, "constructor: Invalid file format: " + fileFormat);
				break;
		}
		iqConverter.setFrequency(frequency);
		iqConverter.setSampleRate(sampleRate);
	}

	private void reportError(String msg) {
		if(callback != null)
			callback.onIQSourceError(this,msg);
		else
			Log.e(LOGTAG,"Callback is null when reporting Error (" + msg + ")");
	}

	@Override
	public boolean open(Context context, Callback callback) {
		this.callback = callback;
		// open the file
		try {
			this.bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
			callback.onIQSourceReady(this);
			return true;
		}catch (IOException e) {
			Log.e(LOGTAG, "open: Error while opening file: " + e.getMessage());
			reportError("Error while opening file: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean isOpen() {
		if(bufferedInputStream == null)
			return false;
		try {
			if(bufferedInputStream.available() > 0)
				return true;
		} catch (IOException e) {

		}
		return false;
	}

	@Override
	public boolean close() {
		// close the file
		try {
			if(bufferedInputStream != null)
				bufferedInputStream.close();
			return true;
		} catch (IOException e) {
			Log.e(LOGTAG, "stopSampling: Error while closing file: " + e.getMessage());
			reportError("Unexpected error while closing file: " + e.getMessage());
			return false;
		}
	}

	@Override
	public String getName() {
		return "IQ-File: " + file.getName();
	}

	/**
	 * @return the file name of the file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return true if repeat is enabled; false if not
	 */
	public boolean isRepeat() {
		return repeat;
	}

	/**
	 * @return the format of the file: FILE_FORMAT_8BIT_SIGNED, ...
	 */
	public int getFileFormat() {
		return fileFormat;
	}

	@Override
	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public void setSampleRate(int sampleRate) {
		Log.e(LOGTAG,"Setting the sample rate is not supported on a file source");
	}

	@Override
	public long getFrequency() {
		return frequency;
	}

	@Override
	public void setFrequency(long frequency) {
		Log.e(LOGTAG,"Setting the frequency is not supported on a file source");
	}

	@Override
	public long getMaxFrequency() {
		return frequency;
	}

	@Override
	public long getMinFrequency() {
		return frequency;
	}

	@Override
	public int getMaxSampleRate() {
		return sampleRate;
	}

	@Override
	public int getMinSampleRate() {
		return sampleRate;
	}

	@Override
	public int getNextHigherOptimalSampleRate(int sampleRate) {
		return this.sampleRate;
	}

	@Override
	public int getNextLowerOptimalSampleRate(int sampleRate) {
		return this.sampleRate;
	}

	@Override
	public int[] getSupportedSampleRates() {
		return new int[] {this.sampleRate};
	}

	@Override
	public int getPacketSize() {
		return packetSize;
	}

	@Override
	public byte[] getPacket(int timeout) {
		if(bufferedInputStream == null)
			return null;

		try {
			// Simulate sample rate of real hardware:
			int sleep = Math.min(sleepTime-(int)(System.currentTimeMillis()-lastAccessTime), timeout);
			if(sleep > 0)
				Thread.sleep(sleep);

			// Read the samples.
			if(bufferedInputStream.read(buffer, 0 , buffer.length) != buffer.length) {
				if (repeat) {
					// rewind and try again:
					Log.i(LOGTAG,"getPacket: End of File. Rewind!");
					bufferedInputStream.close();
					this.bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
					if (bufferedInputStream.read(buffer, 0, buffer.length) != buffer.length)
						return null;
					else {
						lastAccessTime = System.currentTimeMillis();
						return buffer;
					}
				} else {
					Log.i(LOGTAG, "getPacket: End of File");
					reportError("End of File");
					return null;
				}
			}
		} catch (IOException e) {
			Log.e(LOGTAG, "getPacket: Error while reading from file: " + e.getMessage());
			reportError("Unexpected error while reading file: " + e.getMessage());
			return null;
		} catch (InterruptedException e) {
			Log.w(LOGTAG, "getPacket: Interrupted while sleeping!");
			return null;
		}

		lastAccessTime = System.currentTimeMillis();
		return buffer;
	}

	@Override
	public void returnPacket(byte[] buffer) {
		// do nothing
	}

	@Override
	public void startSampling() {
		// nothing to do here...
	}

	@Override
	public void stopSampling() {
		// nothing to do here...
	}

	@Override
	public int fillPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket) {
		return this.iqConverter.fillPacketIntoSamplePacket(packet, samplePacket);
	}

	public int mixPacketIntoSamplePacket(byte[] packet, SamplePacket samplePacket, long channelFrequency) {
		return this.iqConverter.mixPacketIntoSamplePacket(packet, samplePacket, channelFrequency);
	}
}

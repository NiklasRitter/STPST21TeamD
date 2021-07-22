package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import javafx.scene.control.ProgressBar;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

public class Recorder implements Runnable{

    private Thread worker;
    final ProgressBar bar;
    TargetDataLine line;
    private Editor editor;

    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

    int channels = 1;
    int sampleSize = 16;
    float bitRate = 48000.0f;
    boolean bigEndian = false;
    AudioFormat audioFormat = new AudioFormat(encoding, bitRate, sampleSize, channels,
            (sampleSize / 8) * channels, bitRate, bigEndian);

    public Recorder(final ProgressBar bar, Editor editor) {
        this.bar = bar;
        this.editor = editor;
    }

    public void start(){
        worker = new Thread(this);
        worker.start();
    }

    public void stop(){
        cleanup();
        worker.stop();
    }

    private void cleanup(){
        if (line.isRunning()) {
            line.stop();
            line.flush();
        }
        if (line.isOpen()) {
            line.close();
        }

    }

    @Override
    public void run() {
        try {
            line = AudioSystem.getTargetDataLine(audioFormat);
            line.open(audioFormat);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        byte[] buf = new byte[2048];
        float[] samples = new float[1024];

        line.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            // convert bytes to samples here
            for(int i = 0, s = 0; i < b;) {
                int sample = 0;

                sample |= buf[i++] & 0xFF; // (reverse these two lines
                sample |= buf[i++] << 8;   //  if the format is big endian)

                // normalize to range of +/-1.0f
                samples[s++] = sample / 32768f;
            }
            float rms = 0f;
            for(float sample : samples) {
                rms += sample * sample;
            }

            rms = (float)Math.sqrt(rms / samples.length);
            bar.setProgress(rms*1.5);


        }
    }

}

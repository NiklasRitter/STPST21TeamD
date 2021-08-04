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
    private final Editor editor;

    AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

    int channels = 1;
    int sampleSize = 16;
    float bitRate = 48000.0f;
    boolean bigEndian = false;
    AudioFormat audioFormat = new AudioFormat(encoding, bitRate, sampleSize, channels,
            (sampleSize / 8) * channels, bitRate, bigEndian);

    public Recorder(ProgressBar bar, Editor editor) {
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
        if(line != null) {
            if (line.isRunning()) {
                line.stop();
                line.flush();
            }
            if (line.isOpen()) {
                line.close();
            }
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

        line.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            double rms = editor.calculateRMS(buf,b);
            if(bar != null) bar.setProgress(rms);

        }
    }

}

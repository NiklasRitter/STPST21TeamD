package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import javafx.scene.control.ProgressBar;

import javax.sound.sampled.*;

public class Recorder implements Runnable{

    private Thread worker;
    final ProgressBar bar;
    private TargetDataLine line;
    private SourceDataLine sourceDataLine;
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
        if(sourceDataLine != null) {
            if (sourceDataLine.isRunning()) {
                sourceDataLine.stop();
                sourceDataLine.flush();
            }
            if (sourceDataLine.isOpen()) {
                sourceDataLine.close();
            }
        }

    }

    @Override
    public void run() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            DataLine.Info sourceDataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            Mixer.Info inputDevice = editor.getStageManager().getPrefManager().loadInputDevice();
            Mixer.Info outputDevice = editor.getStageManager().getPrefManager().loadOutputDevice();
            if(inputDevice != null){
                line = (TargetDataLine) AudioSystem.getMixer(inputDevice).getLine(info);
            }else{
                line = (TargetDataLine) AudioSystem.getLine(info);
            }

            if(outputDevice != null){
                sourceDataLine = (SourceDataLine) AudioSystem.getMixer(outputDevice).getLine(sourceDataLineInfo);
            }else{
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceDataLineInfo);
            }

            sourceDataLine.open(audioFormat);
            line.open(audioFormat);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        byte[] buf = new byte[2048];

        line.start();
        sourceDataLine.start();
        for(int b; (b = line.read(buf, 0, buf.length)) > -1;) {

            double rms = editor.calculateRMS(buf,b);
            if(rms > editor.getAccordClient().getOptions().getAudioRootMeanSquare())sourceDataLine.write(buf,0,buf.length);
            else sourceDataLine.flush();
            if(bar != null) bar.setProgress(rms);

        }
    }

}

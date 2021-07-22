package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.CHANNEL;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioSend extends Thread {

    private final DatagramSocket sendSocket;

    private final LocalUser localUser;
    private final Channel channel;
    private final String address;
    private final int port;
    AtomicBoolean shouldSend;
    private TargetDataLine line;



    public AudioSend(LocalUser localUser, Channel channel, DatagramSocket sendSocket, String address, int port) {
        this.localUser = localUser;
        this.channel = channel;
        this.sendSocket = sendSocket;
        this.shouldSend = new AtomicBoolean();
        this.shouldSend.set(true);
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        byte[] metaData = createMetaData(localUser, channel);

        // default IPv6, but UPD multicasting not available at IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");

        DatagramPacket datagramPacket;

        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

        int channels = 1;
        int sampleSize = 16;
        float bitRate = 48000.0f;
        boolean bigEndian = false;
        AudioFormat audioFormat = new AudioFormat(encoding, bitRate, sampleSize, channels,
                (sampleSize / 8) * channels, bitRate, bigEndian);

        //DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        //if (!AudioSystem.isLineSupported(audioFormat)) {
        //    System.err.println("Data Line not supported");
        //}

        try {
            line = AudioSystem.getTargetDataLine(audioFormat);
            line.open(audioFormat);
            byte[] readData = new byte[1279];
            System.arraycopy(metaData, 0, readData, 0, 255);

            line.start();
            InetAddress inetAddress = InetAddress.getByName(address);

            while (shouldSend.get()) {
                line.read(readData, 255, 1024);

                //System.out.println(Arrays.toString(readData));
                //System.out.println(getRMS(readData,audioFormat,255));
                //calculatePeakAndRms(Arrays.copyOfRange(readData,255,readData.length));

                datagramPacket = new DatagramPacket(readData, readData.length, inetAddress, port);


                if (line.isRunning()) {
                    this.sendSocket.send(datagramPacket);
                }
            }
            if (line.isRunning()) {
                line.stop();
                line.flush();
            }
            if (line.isOpen()) {
                line.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] createMetaData(LocalUser localUser, Channel channel) {
        JsonObject metaData = Json.createObjectBuilder()
                .add(CHANNEL, channel.getId())
                .add(NAME, localUser.getName())
                .build();

        byte[] metaDataByte = new byte[255];
        System.arraycopy(metaData.toString().getBytes(), 0, metaDataByte, 0, metaData.toString().getBytes().length);

        return metaDataByte;
    }

    public void setShouldSend(boolean value) {
        this.shouldSend.set(value);
    }

    public void stopSending() {
        if (line != null) {
            this.line.stop();
            this.line.flush();
        }
    }

    public void startSending() {
        if (line != null) {
            this.line.start();
        }
    }

    public double getRMS(byte[] audioData, AudioFormat format,int offset) {

        int[] samples = convertByteArray(Arrays.copyOfRange(audioData, offset, audioData.length), format);

        long sumOfSquares = Arrays.stream(samples).mapToLong(i -> (long) i * i).sum();
        System.out.println(sumOfSquares + ", " + samples.length);
        return 10*Math.log10((long)(sumOfSquares / samples.length));
    }

    private int[] convertByteArray(byte[] audioData, AudioFormat format) {

        if (format.getFrameSize() == 2) {
            IntBuffer samples;
            if (format.isBigEndian()) {
                samples = ByteBuffer.wrap(audioData).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
            } else {
                samples = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
            }
            int[] array = new int[samples.remaining()];
            samples.get(array);
            return array;
        } else {
            System.err.println("unsupported frame size: " + format.getFrameSize());
            return new int[0];
        }

    }

    public void calculatePeakAndRms(byte[] audioData) {
        ShortBuffer sBuffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        double sumOfSampleSq = 0.0;    // sum of square of normalized samples.
        double peakSample = 0.0;     // peak sample.

        short[] samples = new short[sBuffer.remaining()];
        sBuffer.get(samples);


        for (short sample : samples) {
            double normSample = (double) sample / 32767;  // normalized the sample with maximum value.
            sumOfSampleSq += Math.abs(normSample * normSample);
            if (Math.abs(sample) > peakSample) {
                peakSample = Math.abs(sample);
            }
        }

        double rms = Math.sqrt(sumOfSampleSq / samples.length);
        System.out.println("rms: " + rms);
        double dB = 20*Math.log10(peakSample / 32767);
        System.out.println("dB : " + dB);
    }
}

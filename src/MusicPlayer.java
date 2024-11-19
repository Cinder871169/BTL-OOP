import javax.sound.sampled.*;
import java.net.URL;

public class MusicPlayer {
    private Clip clip;

    public MusicPlayer(String path) {
        try {
            URL url = getClass().getResource(path); // Update the path if needed
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-7.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.setMicrosecondPosition(0); // Restart the clip if it is already playing
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }
}

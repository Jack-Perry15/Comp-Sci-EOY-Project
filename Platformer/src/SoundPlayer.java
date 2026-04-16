import java.io.File;
import javax.sound.sampled.*;

public class SoundPlayer {
    public static void play(String fileName) {
        try {
            File file = new File(fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Could not play sound: " + fileName);
        }
    }
}
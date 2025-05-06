package globalFunc;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Sound_Func {
    // MediaPlayer for background music (needed for continuous playback)
    private static MediaPlayer backgroundMusicPlayer;
    
    // Game action sounds
    public static void playEatingSound() {
        playSound("../sound/eating.mp3");
    }
    
    public static void playDefeatSound() {
        playSound("../sound/Lose.mp3");
    }
    
    public static void playVictorySound() {
        playSound("../sound/Victory.mp3");
    }
    
    public static void playNewHighScoreSound() {
        playSound("../sound/Player beats up new score.mp3");
    }
    
    // Background music control
    public static void playPacmanSong() {
        stopBackgroundMusic(); // Stop any existing background music
        try {
            Media sound = new Media(Sound_Func.class.getResource("../sound/Pacman Music.mp3").toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(sound);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
            backgroundMusicPlayer.setVolume(volume);
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing Pacman background music");
            e.printStackTrace();
        }
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer = null;
        }
    }
    
    // Other music methods (updated to use MediaPlayer)
    public static void playBackgroundSong() {
        stopBackgroundMusic();
        try {
            Media sound = new Media(Sound_Func.class.getResource("../sound/song.mp3").toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(sound);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(volume);
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing background song");
            e.printStackTrace();
        }
    }
    
    public static void playMainMusic() {
        stopBackgroundMusic();
        try {
            Media sound = new Media(Sound_Func.class.getResource("../sound/Music.mp3").toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(sound);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(volume);
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing main music");
            e.printStackTrace();
        }
    }
    
    // UI sounds (keep using AudioClip for short sounds)
    public static void playProfileOpenedSound() {
        playSound("../sound/Profile_Opened.mp3");
    }
    
    public static void playBackground() {
        playSound("../sound/Background.mp3");
    }
    
    public static void playLoadingMusic() {
        playSound("../sound/Loading.mp3");
    }
    
    // Volume control
    private static double volume = 1.0; // Default volume (1.0 = 100%)
    
    public static void setVolume(double newVolume) {
        volume = Math.max(0, Math.min(1.0, newVolume)); // Clamp between 0 and 1
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(volume);
        }
    }
    
    public static double getVolume() {
        return volume;
    }
    
    // Core sound playing method for short sounds
    private static void playSound(String soundFileName) {
        try {
            AudioClip sound = new AudioClip(Sound_Func.class.getResource(soundFileName).toExternalForm());
            sound.setVolume(volume);
            sound.play();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + soundFileName);
            e.printStackTrace();
        }
    }
}
package jgame.gradle;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Sonido {
    public static void reproducir(String archivo) {
        try {
            URL url = Sonido.class.getClassLoader().getResource("musica/" + archivo);
            if (url != null) {
                AudioClip clip = Applet.newAudioClip(url);
                clip.play();
            } else {
                System.out.println("No se encontró el sonido: " + archivo);
            }
        } catch (Exception e) {
            System.out.println("Error al reproducir sonido: " + e.getMessage());
        }
    }

    public static void reproducirLoop(String archivo) {
    try {
        AudioInputStream audio = AudioSystem.getAudioInputStream(
                Sonido.class.getClassLoader().getResource("musica/" + archivo));
        Clip clip = AudioSystem.getClip();
        clip.open(audio);
        clip.loop(Clip.LOOP_CONTINUOUSLY); // Lo hace sonar en bucle
        clip.start();
    } catch (Exception e) {
        System.out.println("Error al reproducir música: " + e.getMessage());
    }
}

}

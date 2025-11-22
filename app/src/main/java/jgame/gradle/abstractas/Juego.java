package jgame.gradle.abstractas;

import java.awt.Graphics2D;
import com.entropyinteractive.Keyboard; // Importa esto
import com.entropyinteractive.Mouse; // Importa esto

public interface Juego {
    // CAMBIA ESTA LÍNEA
    void iniciar(Keyboard k, Mouse m); // Ya no es iniciar()
    
    void actualizar(double delta);
    void dibujar(Graphics2D g);
    void finalizar();
    boolean estaTerminado();
}
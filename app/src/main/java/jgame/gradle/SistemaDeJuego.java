package jgame.gradle;

import com.entropyinteractive.JGame;
import java.awt.Graphics2D;

// Importa tu interfaz
import jgame.gradle.abstractas.Juego; 

// Importa los controles para poder pasarlos
import com.entropyinteractive.Keyboard;
import com.entropyinteractive.Mouse;

/**
 * Esta clase es el "Adaptador" entre la librería JGame y nuestra
 * interfaz abstracta "Juego".
 * Hereda de JGame (es el motor de ventana) pero CONTIENE un Juego (la lógica).
 */
public class SistemaDeJuego extends JGame {

    // TIENE-UN juego (Composición sobre Herencia)
    private Juego juegoActual; 

    public SistemaDeJuego(Juego juegoInicial, int ancho, int alto) {
        // Llama al constructor de JGame (crea la ventana)
        super(juegoInicial.getClass().getSimpleName(), ancho, alto);
        this.juegoActual = juegoInicial;
    }

    /**
     * Se llama una vez al iniciar.
     * Aquí es donde le "inyectamos" los controles a nuestro juego.
     */
    @Override
    public void gameStartup() {
        // Delega la inicialización al juego real, pasándole los controles
        juegoActual.iniciar(this.getKeyboard(), this.getMouse()); 
    }

    /**
     * Se llama en cada fotograma (lógica).
     */
    @Override
    public void gameUpdate(double delta) {
        // Delega la lógica de actualización al juego real
        juegoActual.actualizar(delta);
        
        if (juegoActual.estaTerminado()) {
            this.stop(); // Si el juego dice que terminó, paramos el sistema
        }
    }

    /**
     * Se llama en cada fotograma (dibujado).
     */
    @Override
    public void gameDraw(Graphics2D g) {
        // Delega el dibujado al juego real
        juegoActual.dibujar(g);
    }

    /**
     * Se llama al cerrar la ventana.
     */
    @Override
    public void gameShutdown() {
        // Delega la finalización/limpieza al juego real
        juegoActual.finalizar();
    }

    // El main() FUE BORRADO. El único main() ahora está en LanzadorJuegos.java
}
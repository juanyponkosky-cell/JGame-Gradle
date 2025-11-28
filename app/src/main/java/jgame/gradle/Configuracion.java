package jgame.gradle;

import java.awt.*;
import java.awt.event.KeyEvent;
import com.entropyinteractive.Keyboard; 

/**
 * Clase de configuración del juego.
 * Compatible con Java 5+ para soportar la librería JGame antigua.
 */
public class Configuracion {

    private boolean musicaActivada = true;
    private boolean efectosActivados = true;
    private boolean pantallaCompleta = false;

    // -------------------------
    //      GETTERS
    // -------------------------
    public boolean isMusicaActivada() {
        return musicaActivada;
    }

    public boolean isEfectosActivados() {
        return efectosActivados;
    }

    public boolean isPantallaCompleta() {
        return pantallaCompleta;
    }

    // -------------------------
    //      MÉTODOS TOGGLE
    // -------------------------
    public void alternarMusica() {
        musicaActivada = !musicaActivada;
    }

    public void alternarEfectos() {
        efectosActivados = !efectosActivados;
    }

    public void alternarPantallaCompleta() {
        pantallaCompleta = !pantallaCompleta;
    }

    // ---------------------------------------------------
    //           LEER TECLADO Y ACTUALIZAR CONFIGS
    // ---------------------------------------------------
    public void actualizar(Keyboard teclado) {

        for (KeyEvent e : teclado.getEvents()) {

            if (e.getID() != KeyEvent.KEY_PRESSED) {
                continue;
            }

            switch (e.getKeyCode()) {

                case KeyEvent.VK_M:
                    alternarMusica();
                    break;

                case KeyEvent.VK_S:
                    alternarEfectos();
                    break;

                case KeyEvent.VK_F:
                    alternarPantallaCompleta();
                    break;

                default:
                    break;
            }
        }
    }

    // ---------------------------------------------------
    //                DIBUJO DEL MENÚ
    // ---------------------------------------------------
    public void dibujar(Graphics2D g, int width, int height) {

        // Fondo
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, width, height);

        // Título
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("CONFIGURACIÓN DEL JUEGO", width / 2 - 200, 100);

        // Opciones
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("M - Música: " + (musicaActivada ? "✔ Activada" : "✘ Desactivada"), width / 2 - 160, 170);
        g.drawString("S - Efectos: " + (efectosActivados ? "✔ Activados" : "✘ Desactivados"), width / 2 - 160, 210);
        g.drawString("F - Pantalla Completa: " + (pantallaCompleta ? "✔ Activada" : "✘ Desactivada"), width / 2 - 160, 250);

        // Texto inferior
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Presioná ENTER para continuar", width / 2 - 140, 320);
    }
}

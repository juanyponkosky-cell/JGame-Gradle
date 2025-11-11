package jgame.gradle;

import java.awt.*;
import java.awt.event.KeyEvent;
import com.entropyinteractive.Keyboard;

public class Configuracion {

    private boolean musicaActivada = true;
    public  boolean efectosActivados = true;
    public boolean pantallaCompleta = false;

    public boolean isMusicaActivada() {
        return musicaActivada;
    }

    public boolean isEfectosActivados() {
        return efectosActivados;
    }

    public void alternarMusica() {
        musicaActivada = !musicaActivada;
    }

    public void alternarEfectos() {
        efectosActivados = !efectosActivados;
    }

    public void actualizarConfiguracion(Keyboard teclado) {
        // Leemos las teclas presionadas
        for (KeyEvent e : teclado.getEvents()) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_M:
                        musicaActivada = !musicaActivada;
                        break;
                    case KeyEvent.VK_S:
                        efectosActivados = !efectosActivados;
                        break;
                    case KeyEvent.VK_F:
                        pantallaCompleta = !pantallaCompleta;
                        break;
                }
            }
        }
    }

    public void dibujarPantalla(Graphics2D g) {
        // Fondo
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, 800, 600);

        // Título
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(22f));
        g.drawString("CONFIGURACIÓN DEL JUEGO", 240, 100);

        // Cuadro
        g.setColor(new Color(50, 50, 50));
        g.fillRoundRect(200, 140, 400, 300, 20, 20);
        g.setColor(Color.GRAY);
        g.drawRoundRect(200, 140, 400, 300, 20, 20);

        // Opciones
        g.setFont(g.getFont().deriveFont(18f));
        g.setColor(Color.WHITE);
        g.drawString("M - Música: " + (musicaActivada ? "✔ Activada" : "✘ Desactivada"), 240, 190);
        g.drawString("S - Efectos: " + (efectosActivados ? "✔ Activados" : "✘ Desactivados"), 240, 230);
        g.drawString("F - Pantalla completa: " + (pantallaCompleta ? "✔ Activada" : "✘ Desactivada"), 240, 270);

        g.setFont(g.getFont().deriveFont(16f));
        g.drawString("Presioná ENTER para continuar", 260, 330);
    }

}

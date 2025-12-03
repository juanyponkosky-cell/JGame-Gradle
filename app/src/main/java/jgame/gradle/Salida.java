package jgame.gradle;

import java.awt.*;
import java.util.List;

public class Salida {

    private Rectangle zona;
    private int salvados = 0;
    private int objetivo;

    private ObjetoGrafico spriteSalida;
    private Configuracion configuracion;

    public Salida(int x, int y, int ancho, int alto, int objetivo, Configuracion configuracion) {
        this.zona = new Rectangle(x, y, ancho, alto);
        this.objetivo = objetivo;
        this.configuracion = configuracion;

        // Carga reutilizable usando tu ObjetoGrafico
        spriteSalida = new ObjetoGrafico("imagenes/salida.png"); // carga la imagen desde ObjetoGrafico
    }

    public void actualizar(List<Lemming> lemmings) {
        for (int i = 0; i < lemmings.size(); i++) {
            Lemming l = lemmings.get(i);

            if (l.getEstado() != Lemming.Estado.MUERTO &&
                zona.contains(l.getX(), l.getY())) {

                lemmings.remove(i);
                i--;

                if (configuracion.isEfectosActivados()) {
                    Sonido.reproducir("YIPPEE.wav");
                }

                salvados++;
            }
        }
    }

    public boolean seGano() {
        return salvados >= objetivo;
    }

    public int getSalvados() {
        return salvados;
    }

    public int getObjetivo() {
        return objetivo;
    }

    public void dibujar(Graphics2D g) {

        // AJUSTE VISUAL
        // Probamos con 16 píxeles (medio bloque).
        // Si sigue muy abajo, aumenta este número (ej. 20).
        // Si queda flotando, disminúyelo (ej. 10).
        int offsetLevantar = 16; 

        // Restamos el offset a la Y para que se dibuje más arriba
        spriteSalida.setPosition(zona.x, zona.y - offsetLevantar);
        spriteSalida.display(g);

        // El texto lo dejamos donde estaba o lo subimos también si tapa el dibujo
        g.setColor(Color.BLACK);
        // Ajusté un poco la posición del texto para que acompañe
        g.drawString("Salvados: " + salvados + " / " + objetivo, zona.x - 10, zona.y - offsetLevantar - 5);
    }
}

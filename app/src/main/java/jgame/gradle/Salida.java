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

        // Dibuja la salida gráfica
        spriteSalida.setPosition(zona.x, zona.y);
        spriteSalida.display(g);

        // Dibuja el texto
        g.setColor(Color.BLACK);
        g.drawString("Salvados: " + salvados + " / " + objetivo, zona.x - 20, zona.y - 10);
    }
}

package jgame.gradle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;

public class Salida {

    private Rectangle zona;
    private int salvados = 0;
    private int objetivo;
    private BufferedImage imagen;
   // private Configuracion configuracion = new Configuracion();
    public Salida(int x, int y, int ancho, int alto, int objetivo, Configuracion configuracion) {
        this.zona = new Rectangle(x, y, ancho, alto);
        this.objetivo = objetivo;
        this.configuracion = configuracion;
        try {
            imagen = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/salida.png"));
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen de salida: " + e.getMessage());
        }
    }
    private Configuracion configuracion;

    public void actualizar(List<Lemming> lemmings) {
        for (int i = 0; i < lemmings.size(); i++) {
            Lemming l = lemmings.get(i);
            if (l.getEstado() != Lemming.Estado.MUERTO
                    && zona.contains(l.getX(), l.getY())) {
                lemmings.remove(i);
                if (configuracion.isEfectosActivados()) {
                    Sonido.reproducir("YIPPEE.wav");
                }

                
                salvados++;
                i--;
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
        if (imagen != null) {
            g.drawImage(imagen, zona.x, zona.y, zona.width, zona.height, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(zona.x, zona.y, zona.width, zona.height);
            g.setColor(Color.BLACK);
            g.drawRect(zona.x, zona.y, zona.width, zona.height);
        }

        g.setColor(Color.BLACK);
        g.drawString("Salvados: " + salvados + " / " + objetivo, zona.x - 20, zona.y - 10);
    }
}

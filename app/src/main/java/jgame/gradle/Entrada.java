package jgame.gradle;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Entrada {

    private int x, y;
    private double intervalo;         // Tiempo entre lemmings
    private double tiempoAcumulado;   // Tiempo desde el último lemming
    private int maxLemmings;
    private int lanzados;
    private BufferedImage imagen;
    private Configuracion configuracion;

    public Entrada(int x, int y, double intervalo, int maxLemmings, Configuracion configuracion) {
        this.x = x;
        this.y = y;
        this.intervalo = intervalo;
        this.maxLemmings = maxLemmings;
        this.tiempoAcumulado = 0;
        this.lanzados = 0;
         this.configuracion = configuracion;
        try {
            imagen = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/entrada.png"));
        } catch (IOException e) {
            System.out.println("No se pudo cargar la imagen de entrada: " + e.getMessage());
        }
    }

    public void actualizar(double delta, List<Lemming> lemmings, TerrenoMatriz terreno) {
        if (lanzados >= maxLemmings) {
            return;
        }

        tiempoAcumulado += delta; //va auemntando el tiempo acumulado 

        if (tiempoAcumulado >= intervalo) {
            lemmings.add(new Lemming(x, y, terreno, configuracion));//
            lanzados++;
            tiempoAcumulado = 0;
        }
    }

    public void dibujar(Graphics2D g) {
        if (imagen != null) {
            g.drawImage(imagen, x, y, 32, 48, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, 32, 48);
        }
    }
}

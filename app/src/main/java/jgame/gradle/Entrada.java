package jgame.gradle;

import java.awt.Graphics2D;
//import java.io.IOException;
import java.util.List;
//import javax.imageio.ImageIO;

public class Entrada extends ObjetoGrafico {

    private double intervalo;         
    private double tiempoAcumulado;   
    private int maxLemmings;
    private int lanzados;
    private Configuracion configuracion;

    public Entrada(int x, int y, double intervalo, int maxLemmings, Configuracion configuracion) {
        super("imagenes/entrada.png");  // carga la imagen desde ObjetoGrafico

        this.positionX = x;
        this.positionY = y;

        this.intervalo = intervalo;
        this.maxLemmings = maxLemmings;
        this.tiempoAcumulado = 0;
        this.lanzados = 0;
        this.configuracion = configuracion;
    }

    public void actualizar(double delta, List<Lemming> lemmings, TerrenoMatriz terreno) {
        if (lanzados >= maxLemmings) {
            return;
        }

        tiempoAcumulado += delta;

        if (tiempoAcumulado >= intervalo) {
            lemmings.add(new Lemming((int)positionX, (int)positionY, terreno, configuracion));
            lanzados++;
            tiempoAcumulado = 0;
        }
    }

    @Override
    public void display(Graphics2D g) {
        g.drawImage(imagen, (int)positionX, (int)positionY, getWidth(), getHeight(), null);
    }
}

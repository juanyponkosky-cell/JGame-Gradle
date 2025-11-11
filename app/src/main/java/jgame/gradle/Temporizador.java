package jgame.gradle;

import java.awt.*;

public class Temporizador {

    private double tiempoRestante; // en segundos

    public Temporizador(double tiempoInicialSegundos) {
        this.tiempoRestante = tiempoInicialSegundos;
    }

    public void actualizar(double delta) {
        if (tiempoRestante > 0) {
            tiempoRestante -= delta;
        }
    }

    public int getMinutos() {
        return (int) (tiempoRestante / 60);
    }

    public int getSegundos() {
        return (int) (tiempoRestante % 60);
    }

    public boolean termino() {
        return tiempoRestante <= 0;
    }

    public void dibujar(Graphics2D g) {
        g.setColor(Color.RED); // Usá un color que se vea sí o sí
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Tiempo: " + getMinutos() + ":" + String.format("%02d", getSegundos()), 650, 585);
    }

}

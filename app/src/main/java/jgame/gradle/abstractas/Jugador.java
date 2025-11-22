package jgame.gradle.abstractas;

public abstract class Jugador {
    protected String nombre;
    protected int puntaje;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.puntaje = 0;
    }

    public void sumarPuntos(int puntos) {
        this.puntaje += puntos;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getNombre() {
        return nombre;
    }
}
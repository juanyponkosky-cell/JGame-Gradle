package jgame.gradle.abstractas;

public abstract class Jugador {

    protected String nombre;
    protected int puntaje;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.puntaje = 0;
    }

    // ============================================================
    //   FACTORY METHOD → PERMITE CREAR JUGADORES SIN SUBCLASES
    // ============================================================
    public static Jugador crear(String nombre) {
        return new JugadorConcreto(nombre);
    }

    // Implementación interna y encapsulada
    private static class JugadorConcreto extends Jugador {
        public JugadorConcreto(String nombre) {
            super(nombre);
        }
    }

    // ============================================================
    //   MÉTODOS
    // ============================================================
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

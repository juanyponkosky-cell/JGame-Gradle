package jgame.gradle;

import java.awt.Graphics2D;
import java.io.*;
import java.util.*;

public class TerrenoMatriz {

    public static final int TAM = 32;
    public static final int BLOQUE_VACIO = 0;
    public static final int BLOQUE_SOLIDO = 1;
    public static final int BLOQUE_SALIDA = 3;
    public static final int BLOQUE_ENTRADA = 2;

    private int[][] mapa;

    private ObjetoGrafico bloqueSolido;

    // NUEVO:
    private Entrada entrada;
    private Salida salida;

    private Configuracion configuracion;

    public TerrenoMatriz(Configuracion configuracion) {
        this.configuracion = configuracion;
        bloqueSolido = new ObjetoGrafico("imagenes/terreno.png");
    }

    public void cargarDesdeArchivo(String ruta) {
        List<int[]> filas = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(ruta)) {

            if (is == null) {
                System.out.println("❌ No se encontró el archivo: " + ruta);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String linea;

            while ((linea = br.readLine()) != null) {
                int[] fila = linea.chars().map(c -> c - '0').toArray();
                filas.add(fila);
            }

            mapa = filas.toArray(new int[0][]);

            detectarZonasEspeciales();

        } catch (Exception e) {
            System.out.println("Error cargando el mapa: " + e.getMessage());
        }
    }

    // NUEVO: detectar salida y entrada según el mapa
    private void detectarZonasEspeciales() {
        entrada = null;
        salida = null;

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length; col++) {

                int valor = mapa[fila][col];

                if (valor == BLOQUE_ENTRADA) {
                    entrada = new Entrada(col * TAM, fila * TAM, 2.0, 10, configuracion);
                    mapa[fila][col] = BLOQUE_VACIO; // ya no es un bloque sólido
                }

                if (valor == BLOQUE_SALIDA) {
                    salida = new Salida(col * TAM, fila * TAM, TAM, TAM * 1, 5, configuracion);
                    mapa[fila][col] = BLOQUE_VACIO;
                }
            }
        }
    }

    public Entrada getEntrada() {
        return entrada;
    }

    public Salida getSalida() {
        return salida;
    }

    public boolean esSolido(int px, int py) {
        int fila = py / TAM;
        int col  = px / TAM;

        if (fila < 0 || fila >= mapa.length || col < 0 || col >= mapa[0].length) {
            return false;
        }
        return mapa[fila][col] == BLOQUE_SOLIDO;
    }

    public void eliminarBloque(int px, int py) {
        int fila = py / TAM;
        int col  = px / TAM;

        if (fila >= 0 && fila < mapa.length && col >= 0 && col < mapa[0].length) {
            mapa[fila][col] = BLOQUE_VACIO;
        }
    }

    public void dibujar(Graphics2D g) {
        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length; col++) {

                if (mapa[fila][col] == BLOQUE_SOLIDO) {
                    bloqueSolido.setPosition(col * TAM, fila * TAM);
                    bloqueSolido.display(g);
                }
            }
        }

        // dibujar objetos especiales
        if (entrada != null) entrada.display(g);
        if (salida != null) salida.dibujar(g);
    }
}

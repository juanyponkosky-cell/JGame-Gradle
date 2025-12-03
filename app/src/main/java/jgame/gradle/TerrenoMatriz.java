package jgame.gradle;

import java.awt.Graphics2D;
import java.io.*;
import java.util.*;
import java.awt.Color;

public class TerrenoMatriz {

    public static final int TAM = 32;
    public static final int BLOQUE_VACIO = 0;
    public static final int BLOQUE_SOLIDO = 1;
    public static final int BLOQUE_SALIDA = 3;
    public static final int BLOQUE_ENTRADA = 2;

    private int[][] mapa;

    private ObjetoGrafico bloqueSolido;

    private Entrada entrada;
    private Salida salida;

    private Configuracion configuracion;

    public TerrenoMatriz(Configuracion configuracion) {
        this.configuracion = configuracion;
        this.bloqueSolido = new ObjetoGrafico("imagenes/terreno.png");
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

    private void detectarZonasEspeciales() {
        entrada = null;
        salida = null;

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length; col++) {

                int valor = mapa[fila][col];

                if (valor == BLOQUE_ENTRADA) {
                    entrada = new Entrada(col * TAM, fila * TAM, 2.0, 10, configuracion);
                    mapa[fila][col] = BLOQUE_VACIO;
                }

                if (valor == BLOQUE_SALIDA) {
                    salida = new Salida(col * TAM, fila * TAM, TAM, TAM, 5, configuracion);
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

        // COLORES PARA EL FOG-OF-SHADOW
        Color sombraFuerte = new Color(0, 0, 0, 140);
        Color sombraMedia  = new Color(0, 0, 0, 90);
        Color sombraSuave  = new Color(0, 0, 0, 50);

        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[fila].length; col++) {

                if (mapa[fila][col] != BLOQUE_SOLIDO)
                    continue;

                int x = col * TAM;
                int y = fila * TAM;

                // --- BLOQUE ---
                g.drawImage(bloqueSolido.getImagen(), x, y, TAM, TAM, null);

                // ====================================================================================
                //                           OUTLINE RETRO NEGRO
                // ====================================================================================
                g.setColor(Color.BLACK);

                // izquierda
                if (col - 1 < 0 || mapa[fila][col - 1] != BLOQUE_SOLIDO)
                    g.fillRect(x, y, 2, TAM);

                // derecha
                if (col + 1 >= mapa[fila].length || mapa[fila][col + 1] != BLOQUE_SOLIDO)
                    g.fillRect(x + TAM - 2, y, 2, TAM);

                // arriba
                if (fila - 1 < 0 || mapa[fila - 1][col] != BLOQUE_SOLIDO)
                    g.fillRect(x, y, TAM, 2);

                // abajo
                if (fila + 1 >= mapa.length || mapa[fila + 1][col] != BLOQUE_SOLIDO)
                    g.fillRect(x, y + TAM - 2, TAM, 2);

                // ====================================================================================
                //                           FOG-OF-SHADOW (3 niveles)
                // ====================================================================================

                // sombra derecha
                if (col + 1 >= mapa[fila].length || mapa[fila][col + 1] == BLOQUE_VACIO) {
                    g.setColor(sombraFuerte);
                    g.fillRect(x + 28, y + 2, 4, 28);

                    g.setColor(sombraMedia);
                    g.fillRect(x + 24, y + 2, 4, 28);

                    g.setColor(sombraSuave);
                    g.fillRect(x + 20, y + 2, 4, 28);
                }

                // sombra abajo
                if (fila + 1 >= mapa.length || mapa[fila + 1][col] == BLOQUE_VACIO) {
                    g.setColor(sombraFuerte);
                    g.fillRect(x + 2, y + 28, 28, 4);

                    g.setColor(sombraMedia);
                    g.fillRect(x + 2, y + 24, 28, 4);

                    g.setColor(sombraSuave);
                    g.fillRect(x + 2, y + 20, 28, 4);
                }

                // sombra en esquina inferior derecha
                if ((col + 1 >= mapa[fila].length || mapa[fila][col + 1] == BLOQUE_VACIO) &&
                    (fila + 1 >= mapa.length || mapa[fila + 1][col] == BLOQUE_VACIO)) {

                    g.setColor(sombraFuerte);
                    g.fillRect(x + 24, y + 24, 8, 8);
                }
            }
        }

        // DIBUJAR OBJETOS ESPECIALES
        if (entrada != null) entrada.display(g);
        if (salida != null) salida.dibujar(g);
    }
}

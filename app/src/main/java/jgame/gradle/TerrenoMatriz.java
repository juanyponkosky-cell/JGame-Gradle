package jgame.gradle;

import java.awt.Graphics2D;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class TerrenoMatriz {

    private int[][] mapa;
    private BufferedImage bloqueSolido;

    public TerrenoMatriz() {
        try {
            bloqueSolido = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/terreno.png"));
        } catch (IOException e) {
            System.out.println("No se pudo cargar la imagen del bloque: " + e.getMessage());
        }
    }

    public void cargarDesdeArchivo(String rutaRelativa) {
        List<int[]> filas = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(rutaRelativa)) {

            if (is == null) {
                System.out.println("❌ No se encontró el archivo: " + rutaRelativa);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String linea;
            while ((linea = br.readLine()) != null) {
                int[] fila = linea.chars()
                        .map(c -> c - '0')
                        .toArray();
                filas.add(fila);
            }
            mapa = filas.toArray(new int[0][]);

        } catch (Exception e) {
            System.out.println("Error al cargar el mapa: " + e.getMessage());
        }
    }

    public boolean esSolido(int px, int py) {
        int fila = py / 32;
        int col = px / 32;
        if (fila < 0 || fila >= mapa.length || col < 0 || col >= mapa[0].length) {
            return false;
        }
        return mapa[fila][col] == 1;
    }

    public void dibujar(Graphics2D g) {
        for (int fila = 0; fila < mapa.length; fila++) {
            for (int col = 0; col < mapa[0].length; col++) {
                if (mapa[fila][col] == 1) {
                    g.drawImage(bloqueSolido, col * 32, fila * 32, 32, 32, null);
                }
            }
        }
    }

    public void eliminarBloque(int px, int py) {
        int fila = py / 32;
        int col = px / 32;
        if (fila >= 0 && fila < mapa.length && col >= 0 && col < mapa[0].length) {
            mapa[fila][col] = 0;
        }
    }
}

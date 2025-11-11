package jgame.gradle;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranking {

    private static final String ARCHIVO = "datos/ranking.txt";

    public static void guardar(String nombre, int nivel, int puntaje) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO, true))) {
            String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            pw.println(nombre + "," + nivel + "," + puntaje + "," + fecha);
        } catch (IOException e) {
            System.out.println("Error al guardar el ranking: " + e.getMessage());
        }
    }

    public static ArrayList<EntradaRanking> obtenerTop(int cantidad) {
        ArrayList<EntradaRanking> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length >= 4) {
                    String nombre = partes[0];
                    int nivel = Integer.parseInt(partes[1]);
                    int puntaje = Integer.parseInt(partes[2]);
                    String fecha = partes[3];
                    lista.add(new EntradaRanking(nombre, nivel, puntaje, fecha));
                }
            }
        } catch (IOException e) {
            // archivo no existe
        }

        lista.sort((a, b) -> Integer.compare(b.puntaje, a.puntaje));
        return new ArrayList<>(lista.subList(0, Math.min(cantidad, lista.size())));
    }

    // 🔽 Clase interna para entrada del ranking
    public static class EntradaRanking {
        public final String nombre;
        public final int nivel;
        public final int puntaje;
        public final String fecha;

        public EntradaRanking(String nombre, int nivel, int puntaje, String fecha) {
            this.nombre = nombre;
            this.nivel = nivel;
            this.puntaje = puntaje;
            this.fecha = fecha;
        }
    }
}

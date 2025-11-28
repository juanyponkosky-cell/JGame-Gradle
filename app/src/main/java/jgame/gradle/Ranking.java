package jgame.gradle;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranking {

    private static final String CARPETA = "datos";
    private static final String ARCHIVO = CARPETA + "/ranking.txt";

    public static void guardar(String nombre, int nivel, int puntaje) {
        try {
            // 1. Crear carpeta si no existe
            File dir = new File(CARPETA);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. Crear el archivo si no existe
            File file = new File(ARCHIVO);
            if (!file.exists()) {
                file.createNewFile();
            }

            // 3. Escribir datos
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                pw.println(nombre + "," + nivel + "," + puntaje + "," + fecha);
            }

        } catch (IOException e) {
            System.out.println("❌ Error al guardar el ranking: " + e.getMessage());
        }
    }

    public static ArrayList<EntradaRanking> obtenerTop(int cantidad) {
        ArrayList<EntradaRanking> lista = new ArrayList<>();

        File file = new File(ARCHIVO);
        if (!file.exists()) {
            return lista;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
            System.out.println("❌ Error al leer ranking: " + e.getMessage());
        }

        lista.sort((a, b) -> Integer.compare(b.puntaje, a.puntaje));

        return new ArrayList<>(lista.subList(0, Math.min(cantidad, lista.size())));
    }

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

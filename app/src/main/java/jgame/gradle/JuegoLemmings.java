package jgame.gradle;

import jgame.gradle.abstractas.Juego;
import com.entropyinteractive.Keyboard;
import com.entropyinteractive.Mouse;
import com.entropyinteractive.Log;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class JuegoLemmings implements Juego {

    // -------------------------------
    // ENUMS DE ESTADO Y HABILIDAD
    // -------------------------------
    private enum Habilidad {
        BLOCKER, FLOATER, EXCAVADOR, BOMBER
    }

    private enum EstadoJuego {
        CONFIGURACION,
        INGRESANDO_NOMBRE,
        JUGANDO,
        NIVEL_GANADO,
        NIVEL_PERDIDO,
        FINALIZADO
    }

    // -------------------------------
    // ATRIBUTOS PRINCIPALES
    // -------------------------------

    private EstadoJuego estado = EstadoJuego.CONFIGURACION;
    private Habilidad habilidadSeleccionada = Habilidad.BLOCKER;

    private Entrada entrada;
    private Salida salida;
    private TerrenoMatriz terreno;
    private Temporizador temporizador;
    private ArrayList<Lemming> lemmings;

    private int nivelActual = 1;
    private boolean mousePresionado = false;

    private String nombreJugador = "";
    private StringBuilder nombreTemporal = new StringBuilder();

    private Configuracion configuracion = new Configuracion();

    // Controladores del motor JGame
    private Keyboard keyboard;
    private Mouse mouse;

    private boolean terminado = false;

    public JuegoLemmings() {
    }

    // --------------------------------
    // INICIAR
    // --------------------------------
    @Override
    public void iniciar(Keyboard k, Mouse m) {
        this.keyboard = k;
        this.mouse = m;
        lemmings = new ArrayList<>();
        // NO crear entrada/salida aquí
    }

    // --------------------------------
    // ACTUALIZAR
    // --------------------------------
    @Override
    public void actualizar(double delta) {

        // -------- CONFIGURACIÓN --------
        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.actualizar(keyboard);

            if (keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                estado = EstadoJuego.INGRESANDO_NOMBRE;
            }
            return;
        }

        // -------- INGRESO NOMBRE --------
        if (estado == EstadoJuego.INGRESANDO_NOMBRE) {
            manejarIngresoNombre();
            return;
        }

        // -------- JUGANDO --------
        if (estado == EstadoJuego.JUGANDO) {
            manejarHabilidades();

            if (entrada != null && salida != null) {
                for (Lemming l : lemmings)
                    l.actualizar(delta);

                entrada.actualizar(delta, lemmings, terreno);
                salida.actualizar(lemmings);
            }

            temporizador.actualizar(delta);

            if (temporizador.termino()) {
                estado = EstadoJuego.NIVEL_PERDIDO;
                guardarPuntaje();
            }

            if (salida.seGano()) {
                estado = EstadoJuego.NIVEL_GANADO;
                guardarPuntaje();
            }

            manejarMouse();
            detectarRebotes();
        }

        manejarFinDeNivel();

        if (keyboard.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            terminado = true;
        }
    }

    // --------------------------------
    // DIBUJAR
    // --------------------------------
    @Override
    public void dibujar(Graphics2D g) {

        // CONFIGURACIÓN
        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.dibujar(g, 800, 600);
            return;
        }

        // INGRESO DE NOMBRE
        if (estado == EstadoJuego.INGRESANDO_NOMBRE) {
            dibujarPantallaNombre(g);
            return;
        }

        // NIVEL
        terreno.dibujar(g);

        if (entrada != null)
            entrada.display(g);
        if (salida != null)
            salida.dibujar(g);

        for (Lemming l : lemmings)
            l.display(g);

        dibujarHUD(g);

        // PANTALLAS DE FIN
        if (estado == EstadoJuego.NIVEL_GANADO ||
                estado == EstadoJuego.NIVEL_PERDIDO ||
                estado == EstadoJuego.FINALIZADO) {
            dibujarFinNivel(g);
        }
    }

    @Override
    public void finalizar() {
        Log.info(getClass().getSimpleName(), "Shutting down game");
    }

    @Override
    public boolean estaTerminado() {
        return terminado;
    }

    // --------------------------------
    // MÉTODOS PRIVADOS
    // --------------------------------

    private void cargarNivel(int numero) {
        try {
            terreno = new TerrenoMatriz(configuracion);
            terreno.cargarDesdeArchivo("niveles/nivel" + numero + ".txt");

            lemmings = new ArrayList<>();

            entrada = terreno.getEntrada();
            salida = terreno.getSalida();

            if (entrada == null || salida == null) {
                throw new RuntimeException("El nivel no tiene entrada o salida en el archivo.");
            }

            temporizador = new Temporizador(60);
            estado = EstadoJuego.JUGANDO;

        } catch (Exception e) {
            System.out.println("Error al cargar nivel: " + e.getMessage());
            terminado = true;
        }
    }

    private void manejarMouse() {
        if (mouse.isLeftButtonPressed()) {
            if (!mousePresionado) {

                int mx = mouse.getX();
                int my = mouse.getY();

                for (Lemming l : lemmings) {
                    if (l.estaDebajo(mx, my)) {

                        switch (habilidadSeleccionada) {
                            case BLOCKER:
                                l.setEstadoBloqueador();
                                break;
                            case FLOATER:
                                l.setEstadoFloater();
                                break;
                            case EXCAVADOR:
                                l.setEstadoExcavador();
                                break;
                            case BOMBER:
                                l.setEstadoBomber();
                                break;
                        }

                        if (configuracion.isEfectosActivados())
                            Sonido.reproducir("TING.wav");

                        break;
                    }
                }

                mousePresionado = true;
            }
        } else {
            mousePresionado = false;
        }
    }

    private void manejarIngresoNombre() {
        LinkedList<KeyEvent> eventos = keyboard.getEvents();

        for (KeyEvent e : eventos) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                char c = e.getKeyChar();
                if (Character.isLetterOrDigit(c) || c == ' ') {
                    nombreTemporal.append(c);
                }
            }

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nombreTemporal.length() > 0) {
                    nombreTemporal.deleteCharAt(nombreTemporal.length() - 1);
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER && nombreTemporal.length() > 0) {
                    nombreJugador = nombreTemporal.toString();
                    cargarNivel(nivelActual);
                    estado = EstadoJuego.JUGANDO;

                    if (configuracion.isMusicaActivada())
                        Sonido.reproducirLoop("spanish_flea.wav");
                }
            }
        }
    }

    private void manejarHabilidades() {
        if (keyboard.isKeyPressed(KeyEvent.VK_1))
            habilidadSeleccionada = Habilidad.BLOCKER;
        if (keyboard.isKeyPressed(KeyEvent.VK_2))
            habilidadSeleccionada = Habilidad.FLOATER;
        if (keyboard.isKeyPressed(KeyEvent.VK_3))
            habilidadSeleccionada = Habilidad.EXCAVADOR;
        if (keyboard.isKeyPressed(KeyEvent.VK_4))
            habilidadSeleccionada = Habilidad.BOMBER;
    }

    private void guardarPuntaje() {
        int puntaje = salida.getSalvados() * 100 - contarMuertos() * 50;
        Ranking.guardar(nombreJugador, nivelActual, puntaje);
    }

    private void manejarFinDeNivel() {
        LinkedList<KeyEvent> eventos = keyboard.getEvents();
        for (KeyEvent e : eventos) {

            if (e.getID() == KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == KeyEvent.VK_ENTER) {

                if (estado == EstadoJuego.NIVEL_GANADO) {
                    nivelActual++;
                    cargarNivel(nivelActual);
                }

                if (estado == EstadoJuego.NIVEL_PERDIDO) {
                    cargarNivel(nivelActual);
                }
            }
        }
    }

    private void detectarRebotes() {
        for (Lemming l : lemmings) {
            if (l.puedeRebotar()) {
                for (Lemming otro : lemmings) {
                    if (otro != l &&
                            otro.getEstado() == Lemming.Estado.BLOCKER &&
                            l.chocaCon(otro)) {
                        l.rebote();
                    }
                }
            }
        }
    }

    private int contarMuertos() {
        int muertos = 0;
        for (Lemming l : lemmings)
            if (l.getEstado() == Lemming.Estado.MUERTO)
                muertos++;
        return muertos;
    }

    private void dibujarPantallaNombre(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        g.setColor(Color.WHITE);
        g.drawString("Ingrese su nombre:", 300, 250);
        g.drawRect(290, 270, 220, 30);
        g.drawString(nombreTemporal.toString(), 300, 290);
        g.drawString("ENTER para continuar", 280, 330);
    }

    private void dibujarHUD(Graphics2D g) {

        // Fondo elegante del HUD
        g.setColor(new Color(245, 245, 245, 220)); // Blanco suave con un poco de transparencia
        g.fillRoundRect(0, 550, 800, 50, 20, 20);

        // Borde fino
        g.setColor(new Color(180, 180, 180));
        g.drawRoundRect(0, 550, 800, 50, 20, 20);

        // Título "Habilidad"
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(40, 40, 40));
        g.drawString("Habilidad Seleccionada:", 10, 570);

        // Nombre de habilidad resaltado
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(30, 100, 200)); // Azul suave
        g.drawString(habilidadSeleccionada.toString(), 180, 570);

        // Lista de teclas
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(new Color(60, 60, 60));
        g.drawString("1 BLOCKER  |  2 FLOATER  |  3 EXCAVADOR  |  4 BOMBER", 10, 590);

        // Timer alineado a la derecha
        temporizador.dibujar(g);
    }

    private void dibujarFinNivel(Graphics2D g) {

        Color fondo;
        String titulo;

        if (estado == EstadoJuego.NIVEL_GANADO) {
            fondo = new Color(0, 255, 0, 180);
            titulo = "¡Nivel Completado!";
        } else if (estado == EstadoJuego.NIVEL_PERDIDO) {
            fondo = new Color(255, 0, 0, 180);
            titulo = "¡Nivel Fallido!";
        } else {
            fondo = new Color(100, 100, 100, 180);
            titulo = "¡Juego Terminado!";
        }

        g.setColor(fondo);
        g.fillRect(200, 200, 400, 280);

        g.setColor(Color.BLACK);
        g.drawRect(200, 200, 400, 280);

        g.drawString(titulo, 280, 230);
        g.drawString("Lemmings salvados: " + salida.getSalvados(), 250, 260);
        g.drawString("Lemmings muertos: " + contarMuertos(), 250, 280);
        g.drawString("Objetivo: " + salida.getObjetivo(), 250, 300);
        g.drawString("ENTER para continuar", 250, 330);

        g.drawString("Top 5 puntajes:", 250, 360);
        ArrayList<Ranking.EntradaRanking> top = Ranking.obtenerTop(5);

        int y = 380;
        for (Ranking.EntradaRanking r : top) {
            g.drawString(r.nombre + " - Nivel " + r.nivel + " - " + r.puntaje, 250, y);
            y += 18;
        }
    }
}

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
    //      ENUMS DE ESTADO Y HABILIDAD
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
    //        ATRIBUTOS PRINCIPALES
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

    // Nueva clase de configuración integrada
    private Configuracion configuracion = new Configuracion();

    // Controladores del motor JGame
    private Keyboard keyboard;
    private Mouse mouse;

    private boolean terminado = false; // Indica al motor que debe parar

    // -------------------------------
    //           CONSTRUCTOR
    // -------------------------------
    public JuegoLemmings() {
        // El SistemaDeJuego administra ventana, loop, etc.
    }

    // -------------------------------
    //            INICIAR
    // -------------------------------
    @Override
    public void iniciar(Keyboard k, Mouse m) {
        this.keyboard = k;
        this.mouse = m;

        try {
            lemmings = new ArrayList<>();
            entrada = new Entrada(50, 50, 2.0, 10, this.configuracion);
        } catch (Exception e) {
            System.out.println("Error en iniciar(): " + e.getMessage());
        }
    }

    // -------------------------------
    //            ACTUALIZAR
    // -------------------------------
    @Override
    public void actualizar(double delta) {

        // ----------- CONFIGURACIÓN ----------
        if (estado == EstadoJuego.CONFIGURACION) {

            configuracion.actualizar(this.keyboard);

            if (this.keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                estado = EstadoJuego.INGRESANDO_NOMBRE;
            }
            return;
        }

        // ----------- INGRESO DE NOMBRE ----------
        if (estado == EstadoJuego.INGRESANDO_NOMBRE) {

            LinkedList<KeyEvent> eventos = this.keyboard.getEvents();

            for (KeyEvent e : eventos) {

                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char c = e.getKeyChar();
                    if (Character.isLetterOrDigit(c) || c == ' ')
                        nombreTemporal.append(c);
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
            return;
        }

        // ----------- JUGANDO ----------
        if (estado == EstadoJuego.JUGANDO) {

            // Cambiar de habilidad
            if (keyboard.isKeyPressed(KeyEvent.VK_1)) habilidadSeleccionada = Habilidad.BLOCKER;
            if (keyboard.isKeyPressed(KeyEvent.VK_2)) habilidadSeleccionada = Habilidad.FLOATER;
            if (keyboard.isKeyPressed(KeyEvent.VK_3)) habilidadSeleccionada = Habilidad.EXCAVADOR;
            if (keyboard.isKeyPressed(KeyEvent.VK_4)) habilidadSeleccionada = Habilidad.BOMBER;

            // Actualizar objetos
            for (Lemming l : lemmings) l.actualizar(delta);
            entrada.actualizar(delta, lemmings, terreno);
            salida.actualizar(lemmings);
            temporizador.actualizar(delta);

            // Fin por tiempo
            if (temporizador.termino()) {
                estado = EstadoJuego.NIVEL_PERDIDO;
                int puntaje = salida.getSalvados()*100 - contarMuertos()*50;
                Ranking.guardar(nombreJugador, nivelActual, puntaje);
            }

            // Nivel ganado
            if (salida.seGano()) {
                estado = EstadoJuego.NIVEL_GANADO;
                int puntaje = salida.getSalvados()*100 - contarMuertos()*50;
                Ranking.guardar(nombreJugador, nivelActual, puntaje);
            }

            manejarMouse();
            detectarRebotes();
        }

        // ----------- NIVEL GANADO / PERDIDO ----------
        LinkedList<KeyEvent> eventosTecla = this.keyboard.getEvents();
        for (KeyEvent e : eventosTecla) {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {

                if (estado == EstadoJuego.NIVEL_GANADO) {
                    nivelActual++;
                    cargarNivel(nivelActual);
                }

                if (estado == EstadoJuego.NIVEL_PERDIDO) {
                    cargarNivel(nivelActual);
                }
            }
        }

        // ----------- SALIR ----------
        if (keyboard.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            terminado = true;
        }
    }

    // -------------------------------
    //              DIBUJAR
    // -------------------------------
    @Override
    public void dibujar(Graphics2D g) {

        // ----------- CONFIGURACIÓN ----------
        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.dibujar(g, 800, 600);
            return;
        }

        // ----------- INGRESO DE NOMBRE ----------
        if (estado == EstadoJuego.INGRESANDO_NOMBRE) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 800, 600);
            g.setColor(Color.WHITE);
            g.drawString("Ingrese su nombre:", 300, 250);
            g.drawRect(290, 270, 220, 30);
            g.drawString(nombreTemporal.toString(), 300, 290);
            g.drawString("Presione ENTER para continuar", 280, 330);
            return;
        }

        // ----------- DIBUJADO DEL NIVEL ----------
        terreno.dibujar(g);
        entrada.display(g);
        salida.dibujar(g);
        for (Lemming l : lemmings) l.display(g);


        g.setColor(Color.WHITE);
        g.fillRect(0, 560, 800, 40);
        g.setColor(Color.BLACK);
        g.drawString("Habilidad: " + habilidadSeleccionada, 10, 580);
        g.drawString("1=BLOCKER | 2=FLOATER | 3=EXCAVADOR | 4=BOMBER", 10, 595);
        temporizador.dibujar(g);

        // ----------- FIN DE NIVEL ----------
        if (estado == EstadoJuego.NIVEL_GANADO ||
            estado == EstadoJuego.NIVEL_PERDIDO ||
            estado == EstadoJuego.FINALIZADO) {

            Color fondo;
            String titulo;

            if (estado == EstadoJuego.NIVEL_GANADO) {
                fondo = new Color(0, 255, 0, 180);
                titulo = "¡Nivel completado!";
            } else if (estado == EstadoJuego.NIVEL_PERDIDO) {
                fondo = new Color(255, 0, 0, 180);
                titulo = "¡Nivel fallido!";
            } else {
                fondo = new Color(100, 100, 100, 180);
                titulo = "¡Juego terminado!";
            }

            g.setColor(fondo);
            g.fillRect(200, 200, 400, 280);

            g.setColor(Color.BLACK);
            g.drawRect(200, 200, 400, 280);
            g.drawString(titulo, 280, 230);
            g.drawString("Lemmings salvados: " + salida.getSalvados(), 250, 260);
            g.drawString("Lemmings muertos: " + contarMuertos(), 250, 280);
            g.drawString("Objetivo: " + salida.getObjetivo(), 250, 300);
            g.drawString("Presione ENTER para continuar", 250, 330);

            g.drawString("Top 5 puntajes:", 250, 360);
            ArrayList<Ranking.EntradaRanking> top = Ranking.obtenerTop(5);

            int y = 380;
            for (Ranking.EntradaRanking r : top) {
                g.drawString(r.nombre + " - Nivel: " + r.nivel + " - Puntaje: " + r.puntaje, 250, y);
                y += 18;
            }
        }
    }

    // -------------------------------
    //           FINALIZAR
    // -------------------------------
    @Override
    public void finalizar() {
        Log.info(getClass().getSimpleName(), "Shutting down game");
    }

    @Override
    public boolean estaTerminado() {
        return terminado;
    }

    // -------------------------------
    //    MÉTODOS PRIVADOS AUXILIARES
    // -------------------------------
    private void cargarNivel(int numero) {
        try {
            terreno = new TerrenoMatriz();
            terreno.cargarDesdeArchivo("niveles/nivel" + numero + ".txt");

            lemmings = new ArrayList<>();
            entrada = new Entrada(50, 50, 2.0, 10, configuracion);
            salida = new Salida(700, 448, 32, 48, 5, configuracion);
            temporizador = new Temporizador(60);

            estado = EstadoJuego.JUGANDO;

        } catch (Exception e) {
            System.out.println("Error al cargar nivel " + numero + ": " + e.getMessage());
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
                            case BLOCKER:   l.setEstadoBloqueador(); break;
                            case FLOATER:   l.setEstadoFloater(); break;
                            case EXCAVADOR: l.setEstadoExcavador(); break;
                            case BOMBER:    l.setEstadoBomber(); break;
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
}

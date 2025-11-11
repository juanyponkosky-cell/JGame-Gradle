package jgame.gradle;

import com.entropyinteractive.JGame;
import com.entropyinteractive.Keyboard;
import com.entropyinteractive.Log;
import com.entropyinteractive.Mouse;

import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


public class JuegoLemmings extends JGame {

    private enum Habilidad { // enum para guardar habilidad
        BLOCKER, FLOATER, EXCAVADOR, BOMBER
    }

    private enum EstadoJuego { //enum estado juego 
        CONFIGURACION,
        INGRESANDO_NOMBRE,
        JUGANDO,
        NIVEL_GANADO,
        NIVEL_PERDIDO,
        FINALIZADO
    }

    private EstadoJuego estado = EstadoJuego.CONFIGURACION; //ASI ARRANCA EL JUEGO 
    private Habilidad habilidadSeleccionada = Habilidad.BLOCKER;
    //BufferedImage fondo; // no usamos fondo pero podria ponerse una imagen tambien 
    Entrada entrada;
    Salida salida;
    TerrenoMatriz terreno;
    Temporizador temporizador;
    ArrayList<Lemming> lemmings; // array de lemmings 
    private int nivelActual = 1;
    private boolean mousePresionado = false;
    private String nombreJugador = "";//nombre para el ranking
    private StringBuilder nombreTemporal = new StringBuilder();
    private Configuracion configuracion = new Configuracion();

    public JuegoLemmings() {
        super("Lemmings", 800, 600); // llama al constructor de la clase padre JGame
    }

    public void gameStartup() {
        try {
            // El juego empieza en estado INGRESANDO_NOMBRE, así que NO cargamos el nivel aún
            lemmings = new ArrayList<>();  // para evitar null pointer cuando dibuja en el estado inicial
            entrada = new Entrada(50, 50, 2.0, 10, this.configuracion);
            salida = new Salida(700, 448, 32, 48, 5, this.configuracion);
            terreno = new TerrenoMatriz(); // inicial vacío hasta cargar nivel real
            temporizador = new Temporizador(60); // cantidad de segundos que va a durar el nivel

            // fondo = ImageIO.read(...) en caso de quereer poner un fondo 
        } catch (Exception e) {
            System.out.println("Error al iniciar juego: " + e.getMessage()); // por si falla algun recurso
        }
    }

    public void gameUpdate(double delta) { // se llama en cada fotograma del juego 
        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.actualizarConfiguracion(this.getKeyboard());
            if (this.getKeyboard().isKeyPressed(KeyEvent.VK_ENTER)) {
                estado = EstadoJuego.INGRESANDO_NOMBRE;
                /* if (configuracion.pantallaCompleta) {
                    //this.setFullScreen(true); // solo si tu framework lo soporta
                }*/
            }
            return;
        }

        if (estado == EstadoJuego.INGRESANDO_NOMBRE) { // pide nombre al usuario mediante el teclado
            LinkedList<KeyEvent> eventosTecla = this.getKeyboard().getEvents();
            // Obtenemos todos los eventos de teclado que ocurrieron desde la última actualización.
            // Un LinkedList se usa para almacenar los eventos de teclado pendientes.
            for (KeyEvent e : eventosTecla) {
                if (e.getID() == KeyEvent.KEY_TYPED) { // Verificamos si el tipo de evento es una tecla 'tipificada' (KEY_TYPED)
                    char c = e.getKeyChar();
                     // Obtenemos el carácter de la tecla que fue tipificada.
                    if (Character.isLetterOrDigit(c) || c == ' ') {
                        nombreTemporal.append(c);
                    }
                } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nombreTemporal.length() > 0) {
                        nombreTemporal.deleteCharAt(nombreTemporal.length() - 1);
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER && nombreTemporal.length() > 0) {
                        nombreJugador = nombreTemporal.toString(); // Convertimos el StringBuilder a String y lo asignamos a nombreJugador.
                        cargarNivel(nivelActual);
                        estado = EstadoJuego.JUGANDO;
                        if (configuracion.isMusicaActivada()) {
                            Sonido.reproducirLoop("spanish_flea.wav");
                        }
                    }
                }
            }
            return; // No sigue actualizando nada más
        }

        if (estado == EstadoJuego.JUGANDO) {
            Keyboard keyboard = this.getKeyboard();

            if (keyboard.isKeyPressed(KeyEvent.VK_1)) {
                habilidadSeleccionada = Habilidad.BLOCKER;
            }
            if (keyboard.isKeyPressed(KeyEvent.VK_2)) {
                habilidadSeleccionada = Habilidad.FLOATER;
            }
            if (keyboard.isKeyPressed(KeyEvent.VK_3)) {
                habilidadSeleccionada = Habilidad.EXCAVADOR;
            }
            if (keyboard.isKeyPressed(KeyEvent.VK_4)) {
                habilidadSeleccionada = Habilidad.BOMBER;
            }

            for (Lemming l : lemmings) {
                l.actualizar(delta);
            }

            entrada.actualizar(delta, lemmings, terreno);
            salida.actualizar(lemmings);
            temporizador.actualizar(delta);

            if (temporizador.termino()) {
                System.out.println("Tiempo agotado. Game Over!");
                estado = EstadoJuego.NIVEL_PERDIDO;
                int puntaje = salida.getSalvados() * 100 - contarMuertos() * 50;
                Ranking.guardar(nombreJugador, nivelActual, puntaje); //guarda el puntaje del jugador 
            }

            if (salida.seGano()) {
                System.out.println("¡Ganaste!");
                estado = EstadoJuego.NIVEL_GANADO;
                int puntaje = salida.getSalvados() * 100 - contarMuertos() * 50;
                Ranking.guardar(nombreJugador, nivelActual, puntaje);
            }

            manejarMouse();
            detectarRebotes();
        }
        

        LinkedList<KeyEvent> eventosTecla = this.getKeyboard().getEvents();
        for (KeyEvent e : eventosTecla) {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (estado == EstadoJuego.NIVEL_GANADO) {
                    nivelActual++;
                    cargarNivel(nivelActual);
                } else if (estado == EstadoJuego.NIVEL_PERDIDO) {
                    cargarNivel(nivelActual);
                }
            }
        }

    }

    private void cargarNivel(int numero) {
        try {
            terreno = new TerrenoMatriz();
            terreno.cargarDesdeArchivo("niveles/nivel" + numero + ".txt");

            lemmings = new ArrayList<>();
            entrada = new Entrada(50, 50, 2.0, 10, this.configuracion);
            salida = new Salida(700, 448, 32, 48, 5, this.configuracion);

            temporizador = new Temporizador(60); // podés variar según nivel
            estado = EstadoJuego.JUGANDO;
        } catch (Exception e) {
            System.out.println("Error al cargar el nivel " + numero + ": " + e.getMessage());
            stop();
        }
    }

    private void manejarMouse() {
        Mouse mouse = this.getMouse();

        if (mouse.isLeftButtonPressed()) {
            if (!mousePresionado) {
                int mouseX = mouse.getX();
                int mouseY = mouse.getY();

                for (Lemming l : lemmings) {
                    if (l.estaDebajo(mouseX, mouseY)) {
                        switch (habilidadSeleccionada) {
                            case BLOCKER:
                                l.setEstadoBloqueador();
                                if (configuracion.isEfectosActivados()) {
                                    Sonido.reproducir("TING.wav");
                                }
                                break;
                            case FLOATER:
                                l.setEstadoFloater();
                                if (configuracion.isEfectosActivados()) {
                                    Sonido.reproducir("TING.wav");
                                }
                                break;
                            case EXCAVADOR:
                                l.setEstadoExcavador();
                                if (configuracion.isEfectosActivados()) {
                                    Sonido.reproducir("TING.wav");
                                }
                                break;
                            case BOMBER:
                                l.setEstadoBomber();
                                if (configuracion.isEfectosActivados()) {
                                    Sonido.reproducir("TING.wav");
                                }
                                break;
                        }
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
                    if (otro != l
                            && otro.getEstado() == Lemming.Estado.BLOCKER
                            && l.chocaCon(otro)) {
                        l.rebote();
                    }
                }
            }
        }
    }

    public void gameDraw(Graphics2D g) {
        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.dibujarPantalla(g);
            return;
        }

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

        terreno.dibujar(g);
        entrada.dibujar(g);
        salida.dibujar(g);

        for (Lemming l : lemmings) {
            l.dibujar(g);
        }

        // HUD
        g.setColor(Color.WHITE);
        g.fillRect(0, 560, 800, 40);
        g.setColor(Color.BLACK);
        g.drawString("Habilidad seleccionada: " + habilidadSeleccionada, 10, 580);
        g.drawString("Presioná 1 = BLOCKER | 2 = FLOATER | 3 = EXCAVADOR | 4 = BOMBER", 10, 595);

        temporizador.dibujar(g);
        //Color fondo;
        if (estado == EstadoJuego.NIVEL_GANADO || estado == EstadoJuego.NIVEL_PERDIDO || estado == EstadoJuego.FINALIZADO) {

            // 1. Fondo semitransparente
            Color fondo;
            String titulo;

            if (estado == EstadoJuego.NIVEL_GANADO) {
                fondo = new Color(0, 255, 0, 180);
                titulo = " ¡Nivel completado!";
            } else if (estado == EstadoJuego.NIVEL_PERDIDO) {
                fondo = new Color(255, 0, 0, 180);
                titulo = " ¡Nivel fallido!";
            } else {
                fondo = new Color(100, 100, 100, 180);
                titulo = " ¡Juego terminado!";
            }

            // 2. Dibujo del fondo del cartel
            g.setColor(fondo);
            g.fillRect(200, 200, 400, 280);  // altura aumentada para mostrar ranking

            g.setColor(Color.BLACK);
            g.drawRect(200, 200, 400, 280);

            // 3. Texto principal
            g.drawString(titulo, 280, 230);
            g.drawString("Lemmings salvados: " + salida.getSalvados(), 250, 260);
            g.drawString("Lemmings muertos: " + contarMuertos(), 250, 280);
            g.drawString("Objetivo: " + salida.getObjetivo(), 250, 300);
            g.drawString("Presioná ENTER para continuar", 240, 320);

            // 4. Ranking
            g.drawString("Top 5 puntajes:", 250, 350);
            ArrayList<Ranking.EntradaRanking> top = Ranking.obtenerTop(5);
            int y = 370;
            for (Ranking.EntradaRanking entrada : top) {
                g.drawString(entrada.nombre + " - Nivel: " + entrada.nivel + " - Puntaje: " + entrada.puntaje, 250, y);
                y += 18;
            }
        }

    }

    private int contarMuertos() {
        int muertos = 0;
        for (Lemming l : lemmings) {
            if (l.getEstado() == Lemming.Estado.MUERTO) {
                muertos++;
            }
        }
        return muertos;
    }

    public static void main(String[] args) {
        JuegoLemmings juego = new JuegoLemmings();
        juego.run(1.0 / 60.0);
        System.exit(0);
    }

    public void gameShutdown() {
        Log.info(getClass().getSimpleName(), "Shutting down game");
    }

}

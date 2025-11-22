package jgame.gradle;

// Importa tus clases nuevas
import jgame.gradle.abstractas.Juego;

// NO importas JGame, pero SÍ los controles y la interfaz
import com.entropyinteractive.Keyboard;
import com.entropyinteractive.Mouse;
import com.entropyinteractive.Log;

import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import java.util.Scanner;


// FIJATE AQUÍ: "implements Juego" en vez de "extends JGame"
public class JuegoLemmings implements Juego {

    // ... (Todos tus Enums Habilidad y EstadoJuego van aquí igual) ...
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
    
    // ... (Todos tus atributos privados van aquí igual) ...
    private EstadoJuego estado = EstadoJuego.CONFIGURACION;
    private Habilidad habilidadSeleccionada = Habilidad.BLOCKER;
    Entrada entrada;
    Salida salida;
    TerrenoMatriz terreno;
    Temporizador temporizador;
    ArrayList<Lemming> lemmings; 
    private int nivelActual = 1;
    private boolean mousePresionado = false;
    private String nombreJugador = "";
    private StringBuilder nombreTemporal = new StringBuilder();
    private Configuracion configuracion = new Configuracion();

    // --- Atributos nuevos ---
    // Necesitamos guardar referencias a los controles
    private Keyboard keyboard;
    private Mouse mouse;
    private boolean terminado = false; // Para avisar al SistemaDeJuego que pare

    // El constructor cambia. Ya no llama a super()
    public JuegoLemmings() {
        // Constructor vacío. El SistemaDeJuego se encarga de la ventana.
    }

@Override
    // CAMBIA LA FIRMA DE ESTE MÉTODO
    public void iniciar(Keyboard k, Mouse m) { 
        // Y AÑADE ESTAS DOS LÍNEAS
        this.keyboard = k;
        this.mouse = m;
        
        // El resto de tu código de iniciar() va aquí
        try {
            lemmings = new ArrayList<>();  
            entrada = new Entrada(50, 50, 2.0, 10, this.configuracion);
            // ... etc ...
        } catch (Exception e) {
            // ...
        }
    }

    // --- Métodos de la Interfaz "Juego" ---

    @Override
    public void actualizar(double delta) {
        // Este es tu viejo "gameUpdate()"
        // IMPORTANTE: Cambia "this.getKeyboard()" por "this.keyboard"
        // y "this.getMouse()" por "this.mouse" en TODO este método.

        if (estado == EstadoJuego.CONFIGURACION) {
            configuracion.actualizarConfiguracion(this.keyboard); // usa this.keyboard
            if (this.keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                estado = EstadoJuego.INGRESANDO_NOMBRE;
            }
            return;
        }

        if (estado == EstadoJuego.INGRESANDO_NOMBRE) { 
            LinkedList<KeyEvent> eventosTecla = this.keyboard.getEvents(); // usa this.keyboard
            for (KeyEvent e : eventosTecla) {
                 // ... (lógica de ingreso de nombre sin cambios) ...
                 if (e.getID() == KeyEvent.KEY_TYPED) { 
                    char c = e.getKeyChar();
                    if (Character.isLetterOrDigit(c) || c == ' ') {
                        nombreTemporal.append(c);
                    }
                } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nombreTemporal.length() > 0) {
                        nombreTemporal.deleteCharAt(nombreTemporal.length() - 1);
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER && nombreTemporal.length() > 0) {
                        nombreJugador = nombreTemporal.toString(); 
                        cargarNivel(nivelActual);
                        estado = EstadoJuego.JUGANDO;
                        if (configuracion.isMusicaActivada()) {
                            Sonido.reproducirLoop("spanish_flea.wav");
                        }
                    }
                }
            }
            return; 
        }

        if (estado == EstadoJuego.JUGANDO) {
            // Ya no haces "Keyboard keyboard = this.getKeyboard();"
            
            if (this.keyboard.isKeyPressed(KeyEvent.VK_1)) { // usa this.keyboard
                habilidadSeleccionada = Habilidad.BLOCKER;
            }
            if (this.keyboard.isKeyPressed(KeyEvent.VK_2)) {
                habilidadSeleccionada = Habilidad.FLOATER;
            }
            if (this.keyboard.isKeyPressed(KeyEvent.VK_3)) {
                habilidadSeleccionada = Habilidad.EXCAVADOR;
            }
            if (this.keyboard.isKeyPressed(KeyEvent.VK_4)) {
                habilidadSeleccionada = Habilidad.BOMBER;
            }
            
            // ... (resto de tu lógica de JUGANDO sin cambios) ...
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
                Ranking.guardar(nombreJugador, nivelActual, puntaje); 
            }
            if (salida.seGano()) {
                System.out.println("¡Ganaste!");
                estado = EstadoJuego.NIVEL_GANADO;
                int puntaje = salida.getSalvados() * 100 - contarMuertos() * 50;
                Ranking.guardar(nombreJugador, nivelActual, puntaje);
            }

            manejarMouse(); // Este método ahora usará this.mouse
            detectarRebotes();
        }
        
        // ... (lógica de ENTER para NIVEL_GANADO / NIVEL_PERDIDO) ...
        LinkedList<KeyEvent> eventosTecla = this.keyboard.getEvents(); // usa this.keyboard
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
        
        // Para salir del juego
        if (this.keyboard.isKeyPressed(KeyEvent.VK_ESCAPE)) {
             this.terminado = true; // Avisa al SistemaDeJuego que debe parar
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        // Este es tu viejo "gameDraw()"
        // TODO TU CÓDIGO de gameDraw() va aquí EXACTAMENTE IGUAL 
        // No hay que cambiarle nada.
        
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
        
        // ... (el resto de tu código de dibujado) ...
         g.setColor(Color.WHITE);
         g.fillRect(0, 560, 800, 40);
         g.setColor(Color.BLACK);
         g.drawString("Habilidad seleccionada: " + habilidadSeleccionada, 10, 580);
         g.drawString("Presioná 1 = BLOCKER | 2 = FLOATER | 3 = EXCAVADOR | 4 = BOMBER", 10, 595);
         temporizador.dibujar(g);
         
         if (estado == EstadoJuego.NIVEL_GANADO || estado == EstadoJuego.NIVEL_PERDIDO || estado == EstadoJuego.FINALIZADO) {
            Color fondo;
            String titulo;
            // ... (todo el resto de gameDraw) ...
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
             g.setColor(fondo);
             g.fillRect(200, 200, 400, 280); 
             g.setColor(Color.BLACK);
             g.drawRect(200, 200, 400, 280);
             g.drawString(titulo, 280, 230);
             g.drawString("Lemmings salvados: " + salida.getSalvados(), 250, 260);
             g.drawString("Lemmings muertos: " + contarMuertos(), 250, 280);
             g.drawString("Objetivo: " + salida.getObjetivo(), 250, 300);
             g.drawString("Presioná ENTER para continuar", 240, 320);
             g.drawString("Top 5 puntajes:", 250, 350);
             ArrayList<Ranking.EntradaRanking> top = Ranking.obtenerTop(5);
             int y = 370;
             for (Ranking.EntradaRanking entrada : top) {
                 g.drawString(entrada.nombre + " - Nivel: " + entrada.nivel + " - Puntaje: " + entrada.puntaje, 250, y);
                 y += 18;
             }
         }
    }

    @Override
    public void finalizar() {
        // Este es tu viejo "gameShutdown()"
        Log.info(getClass().getSimpleName(), "Shutting down game");
    }
    
    @Override
    public boolean estaTerminado() {
        return this.terminado;
    }

    // --- MÉTODOS PRIVADOS ---
    // (Todo tu código privado va aquí igual)

    private void cargarNivel(int numero) {
        try {
            terreno = new TerrenoMatriz();
            terreno.cargarDesdeArchivo("niveles/nivel" + numero + ".txt");
            lemmings = new ArrayList<>();
            entrada = new Entrada(50, 50, 2.0, 10, this.configuracion);
            salida = new Salida(700, 448, 32, 48, 5, this.configuracion);
            temporizador = new Temporizador(60); 
            estado = EstadoJuego.JUGANDO;
        } catch (Exception e) {
            System.out.println("Error al cargar el nivel " + numero + ": " + e.getMessage());
            // En vez de stop(), avisamos al sistema que termine
            this.terminado = true; 
        }
    }

    private void manejarMouse() {
        // IMPORTANTE: Cambia "this.getMouse()" por "this.mouse"
        // ya no haces Mouse mouse = this.getMouse();
        
        if (this.mouse.isLeftButtonPressed()) { // usa this.mouse
            if (!mousePresionado) {
                int mouseX = this.mouse.getX();
                int mouseY = this.mouse.getY();

                for (Lemming l : lemmings) {
                    if (l.estaDebajo(mouseX, mouseY)) {
                        switch (habilidadSeleccionada) {
                             // ... (lógica del switch sin cambios) ...
                            case BLOCKER:
                                l.setEstadoBloqueador();
                                if (configuracion.isEfectosActivados()) Sonido.reproducir("TING.wav");
                                break;
                            case FLOATER:
                                l.setEstadoFloater();
                                if (configuracion.isEfectosActivados()) Sonido.reproducir("TING.wav");
                                break;
                            case EXCAVADOR:
                                l.setEstadoExcavador();
                                if (configuracion.isEfectosActivados()) Sonido.reproducir("TING.wav");
                                break;
                            case BOMBER:
                                l.setEstadoBomber();
                                if (configuracion.isEfectosActivados()) Sonido.reproducir("TING.wav");
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
        // ... (código sin cambios) ...
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

    private int contarMuertos() {
        // ... (código sin cambios) ...
        int muertos = 0;
        for (Lemming l : lemmings) {
            if (l.getEstado() == Lemming.Estado.MUERTO) {
                muertos++;
            }
        }
        return muertos;
    }

    // EL MAIN YA NO VA AQUÍ. Lo movimos a SistemaDeJuego.
}
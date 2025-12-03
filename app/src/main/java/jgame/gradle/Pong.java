package jgame.gradle;

// Importamos lo que SÍ necesitamos de la librería
import com.entropyinteractive.Keyboard;
import com.entropyinteractive.Mouse;
import com.entropyinteractive.Log;

// Importamos nuestra interfaz abstracta
import jgame.gradle.abstractas.Juego;

import java.awt.*;
import java.awt.event.*; //eventos
import java.awt.image.*; //imagenes
import javax.imageio.*; //imagenes
import java.util.*;
import java.io.*;

// CAMBIO 1: Ya no "extends JGame", ahora "implements Juego"
public class Pong implements Juego {

    // --- Constantes y Atributos (Sin cambios) ---
    private final double VELOCIDAD = 7.0;
    private final int ESTADO_INICIO = 0;
    private final int ESTADO_JUGANDO = 1;
    private final int ESTADO_ESPERANDO = 2;
    private final int ESTADO_FIN = 3;
    private final int ESTADO_CONFIG = -1;
    private final static int HEIGHT = 600;
    private final static int WIDTH = 800;

    private Configuracion configuracion = new Configuracion();

    private Paleta paleta1 = new Paleta(20, 120, WIDTH, HEIGHT);
    private Paleta paleta2 = new Paleta(20, 120, WIDTH, HEIGHT);

    private double cooldownRebote = 0;

    private PelotaPong pelota = new PelotaPong("imagenes/pelota.png", WIDTH, HEIGHT);
    private BufferedImage pong = null;
    private Rectangle play = new Rectangle(190, 80);
    
    private int contador1 = 0;
    private int contador2 = 0;
    private int estado;

    private Keyboard keyboard;
    private Mouse mouse;
    private boolean terminado = false;

    public Pong() {
        try {
            // red =
            // ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/red.png"));
            pong = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/pong0.jpg"));
        } catch (IOException e) {
            System.out.println("ZAS! en ObjectoGrafico " + e);
        }
    }

    // CAMBIO 4: Renombramos "gameStartup" a "iniciar" y guardamos los controles
    @Override
    public void iniciar(Keyboard k, Mouse m) {
        this.keyboard = k;
        this.mouse = m;

        System.out.println("iniciar() de Pong");
        cargarTTF();

        try {
            paleta1.setPosition(50, HEIGHT / 2 - paleta1.getHeight() / 2);
            paleta2.setPosition(WIDTH - 50, HEIGHT / 2 - paleta2.getHeight() / 2);
            pelota.setPelotaAlCentro();
        } catch (Exception e) {
            System.out.println(e);
        }

        // Primero mostramos la pantalla de configuración
        estado = ESTADO_CONFIG;
    }

    // (Este método privado no se toca)
    private void cargarTTF() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            InputStream is = getClass().getClassLoader().getResourceAsStream("Pong-Game.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            ge.registerFont(font);
        } catch (Exception e) {
            System.out.println("ZAS! Problema al cargar tipo de letra");
        }
    }

    // CAMBIO 5: Renombramos "gameUpdate" a "actualizar"
    @Override
    public void actualizar(double delta) {
        // Ya no hacemos "this.getKeyboard()", usamos las variables guardadas
        // --- ESTADO CONFIGURACIÓN ---

        if (cooldownRebote > 0) // Rebote cooldown timer, evita spamear el sonido de rebote
            cooldownRebote -= delta;

        if (estado == ESTADO_CONFIG) {
            configuracion.actualizar(keyboard); // M/S/F como en Lemmings

            // ENTER para continuar al menú de inicio de Pong
            if (keyboard.isKeyPressed(KeyEvent.VK_ENTER)) {
                estado = ESTADO_INICIO;
            }
            return; // no seguir con el resto de la lógica
        }

        // Procesar teclas de direccion
        if (this.keyboard.isKeyPressed(KeyEvent.VK_UP)) {
            paleta2.positionY -= VELOCIDAD;
        }
        if (this.keyboard.isKeyPressed(KeyEvent.VK_DOWN)) {
            paleta2.positionY += VELOCIDAD;
        }
        if (this.keyboard.isKeyPressed(KeyEvent.VK_W)) {
            paleta1.positionY -= VELOCIDAD;
        }
        if (this.keyboard.isKeyPressed(KeyEvent.VK_S)) {
            paleta1.positionY += VELOCIDAD;
        }

        // Esc fin del juego
        LinkedList<KeyEvent> keyEvents = this.keyboard.getEvents();
        for (KeyEvent event : keyEvents) {
            if ((event.getID() == KeyEvent.KEY_PRESSED)
                    && (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                // CAMBIO 6: "stop()" se reemplaza por "this.terminado = true"
                this.terminado = true;
            }
            if ((event.getID() == KeyEvent.KEY_PRESSED)
                    && (event.getKeyCode() == KeyEvent.VK_SPACE)
                    && estado == ESTADO_ESPERANDO) {
                pelota.startVelocity();
                estado = ESTADO_JUGANDO;
            }
            if ((event.getID() == KeyEvent.KEY_PRESSED)
                    && (event.getKeyCode() == KeyEvent.VK_P)
                    && estado == ESTADO_INICIO) {
                estado = ESTADO_ESPERANDO;
            }
        }

        if (pelota.getBordes().intersects(paleta1.getBordes())) {
            double centroPaleta = paleta1.positionY + paleta1.getHeight() / 2;
            double desplazamiento = (pelota.positionY + pelota.getHeight() / 2) - centroPaleta;
            pelota.invertVelocityX();
            pelota.setVelocityY(desplazamiento * 0.15);

            if (cooldownRebote <= 0 && configuracion.isEfectosActivados()) {
                Sonido.reproducir("sonidopong.wav"); // ← usa tu clase Sonido
                cooldownRebote = 0.08; // evita que suene repetido
            }
        }

        if (pelota.getBordes().intersects(paleta2.getBordes())) {
            double centroPaleta = paleta2.positionY + paleta2.getHeight() / 2;
            double desplazamiento = (pelota.positionY + pelota.getHeight() / 2) - centroPaleta;
            pelota.invertVelocityX();
            pelota.setVelocityY(desplazamiento * 0.15);

            if (cooldownRebote <= 0 && configuracion.isEfectosActivados()) {
                Sonido.reproducir("sonidopong.wav");
                cooldownRebote = 0.08;
            }
        }

        // Gol para jugador 2
        if (pelota.positionX < 0) {
            contador2++;
            pelota.setPelotaAlCentro();
            estado = ESTADO_ESPERANDO;
        }

        // Gol para jugador 1
        if (pelota.positionX > WIDTH - pelota.getSize()) {
            contador1++;
            pelota.setPelotaAlCentro();
            estado = ESTADO_ESPERANDO;
        }

        // CAMBIO 7: Lógica del mouse ahora usa "this.mouse"
        // Si alguien gana
        if (contador1 >= 10 || contador2 >= 10) {

            estado = ESTADO_FIN;

            // Reinicio con click
            if (mouse.isLeftButtonPressed()) {
                contador1 = 0;
                contador2 = 0;
                pelota.setPelotaAlCentro();
                estado = ESTADO_ESPERANDO;
            }
        }

        if (estado == ESTADO_INICIO) {
            play.setLocation(WIDTH / 2 - 100, 385);
            if (play.contains(this.mouse.getX(), this.mouse.getY()) && this.mouse.isLeftButtonPressed()) {
                estado = ESTADO_ESPERANDO;
            }
        }

        paleta1.update(delta);
        paleta2.update(delta);
        pelota.update(delta);
    }

    // CAMBIO 8: Renombramos "gameDraw" a "dibujar"
    @Override
    public void dibujar(Graphics2D g) {

        // --- PANTALLA DE CONFIGURACIÓN ---
        if (estado == ESTADO_CONFIG) {
            configuracion.dibujar(g, WIDTH, HEIGHT); // mismo estilo que en Lemmings
            return;
        }

        // (TODA TU LÓGICA DE DIBUJADO ES PERFECTA Y NO CAMBIA)
        if (estado == ESTADO_INICIO) {
            g.setColor(Color.WHITE);
            g.drawImage(pong, 200, 100, 400, 225, null);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 100));
            g.drawString("PLAY", WIDTH / 2 - 100, 500);
        }

        if (estado == ESTADO_ESPERANDO || estado == ESTADO_JUGANDO) {
            g.setColor(Color.WHITE);
            // ...
            paleta1.display(g);
            paleta2.display(g);
            pelota.display(g);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 60));
            g.drawString(String.valueOf(contador1), WIDTH / 2 - 80, 80);
            g.drawString(String.valueOf(contador2), WIDTH / 2 + 40, 80);

        }

        if (estado == ESTADO_FIN) {

            g.setColor(Color.WHITE);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 80));

            if (contador1 >= 10) {
                g.drawString("GANA JUGADOR 1", WIDTH / 2 - 300, HEIGHT / 2 - 50);
            }

            if (contador2 >= 10) {
                g.drawString("GANA JUGADOR 2", WIDTH / 2 - 300, HEIGHT / 2 - 50);
            }

            g.setFont(new Font("Pong-Game", Font.PLAIN, 50));
            g.drawString("Click para reiniciar", WIDTH / 2 - 200, HEIGHT / 2 + 80);
        }

    }

    // CAMBIO 9: Renombramos "gameShutdown" a "finalizar"
    @Override
    public void finalizar() {
        Log.info(getClass().getSimpleName(), "Shutting down game");
    }

    // CAMBIO 10: Agregamos el método que faltaba de la interfaz
    @Override
    public boolean estaTerminado() {
        return this.terminado;
    }
}

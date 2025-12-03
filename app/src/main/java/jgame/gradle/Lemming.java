package jgame.gradle;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Lemming extends ObjetoGrafico implements ObjetoMovible {

    enum Estado {
        CAMINANDO,
        BLOCKER,
        FLOATER,
        MUERTO,
        EXCAVADOR,
        BOMBER,
        ESCALADOR
    }

    private double velocidad = 50;
    private double gravedad = 200;
    private double velocidadY = 0;
    private boolean haciaDerecha = true;

    private TerrenoMatriz terreno;
    private Estado estado = Estado.CAMINANDO;

    private double alturaCaida = 0;
    private double tiempoBomber = 0;

    private boolean quiereEscalar = false;

    private Configuracion configuracion;

    private BufferedImage spriteWalker;
    private BufferedImage spriteBlocker;
    private BufferedImage spriteFloater;
    private BufferedImage spriteExcavador;
    private BufferedImage spriteBomber;
    private BufferedImage spriteEscalador;

    public Lemming(double x, double y, TerrenoMatriz terreno, Configuracion configuracion) {
        super("imagenes/lemming.png");

        this.positionX = x;
        this.positionY = y;
        this.terreno = terreno;
        this.configuracion = configuracion;

        try {
            spriteWalker = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/lemming.png"));
            spriteBlocker = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/blocker.png"));
            spriteFloater = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/floater.png"));
            spriteExcavador = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/minner.png"));
            spriteBomber = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/bomber.png"));
            spriteEscalador = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/escalador.png"));
        } catch (Exception e) {
            System.out.println("Error cargando sprites de lemming: " + e.getMessage());
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================
    @Override
    public void update(double delta) {
        actualizar(delta);
    }

    @Override
    public double getX() {
        return positionX;
    }

    @Override
    public double getY() {
        return positionY;
    }

    // =========================================================
    // LÓGICA PRINCIPAL
    // =========================================================
    public void actualizar(double delta) {

        if (estado == Estado.BLOCKER || estado == Estado.MUERTO)
            return;

        // =========================================================
        // GRAVEDAD (si NO está escalando)
        // =========================================================
        if (estado != Estado.ESCALADOR) {
            velocidadY += gravedad * delta;
        }

        double nuevaY = positionY + velocidadY * delta;

        // Suelo
        int sueloY = (int) (nuevaY + 32);
        if (terreno.esSolido((int) (positionX + 16), sueloY)) {

            if (estado != Estado.FLOATER && alturaCaida > 100) {
                if (configuracion.isEfectosActivados())
                    Sonido.reproducir("SPLAT.wav");
                estado = Estado.MUERTO;
                return;
            }

            velocidadY = 0;
            nuevaY = Math.floor(nuevaY / 32) * 32;
            alturaCaida = 0;

        } else {
            alturaCaida += velocidadY * delta;
        }

        // EXCAVADOR
        if (estado == Estado.EXCAVADOR) {
            int cx = (int) (positionX + 16);
            int abajo = (int) (positionY + 32);

            if (terreno.esSolido(cx, abajo)) {
                terreno.eliminarBloque(cx, abajo);
                positionY += 1;
            } else {
                estado = Estado.CAMINANDO;
            }
            return;
        }

        positionY = nuevaY;
        // ... dentro de tu update o loop principal ...
        int frenteX = (int) (positionX + (haciaDerecha ? 32 : -1));
        int centroY = (int) (positionY + 16);
        if (estado == Estado.ESCALADOR) {

            // Actualizamos sensores
            // Nota: A veces conviene chequear un poco más arriba de 'centroY' para
            // anticipar el borde
            boolean paredFrente = terreno.esSolido(frenteX, centroY);

            if (paredFrente) {
                // -------------------------------------------------
                // CASO 1: TODAVÍA HAY PARED -> SEGUIR SUBIENDO
                // -------------------------------------------------
                positionY -= velocidad * delta * 1.2;

                // Opcional: Chequear si se golpea la cabeza con un techo real (bloque
                // impasable)
                // int arribaY = (int) positionY;
                // if (terreno.esSolido(positionX, arribaY)) { ... lógica para caer o darse la
                // vuelta ... }

            } else {
                // -------------------------------------------------
                // CASO 2: SE ACABÓ LA PARED -> ES LA CIMA (LEDGE)
                // -------------------------------------------------
                // Aquí es donde hacemos la magia. Detectamos que ya no hay pared,
                // así que lo "teletransportamos" arriba y adelante.

                // 1. Ajuste Vertical: Subimos un poco para que los pies queden a nivel del
                // suelo
                positionY -= 14; // Ajusta este valor según el tamaño de tu sprite (aprox medio sprite)

                // 2. Empuje Horizontal: Lo metemos dentro de la plataforma
                if (haciaDerecha)
                    positionX += 10; // Ajusta según el ancho del sprite
                else
                    positionX -= 10;

                // 3. Cambiamos estado
                estado = Estado.CAMINANDO;
                quiereEscalar = false;
                velocidadY = 0;
            }
        }
        /*
         * // =========================================================
         * // LÓGICA DE ESCALADOR
         * // =========================================================
         * 
         * // Coordenadas delante del lemming
         * int frenteX = (int) (positionX + (haciaDerecha ? 32 : -1));
         * int centroY = (int) (positionY + 16);
         * 
         * // Si quiere escalar pero aún no está escalando…
         * if (quiereEscalar && estado != Estado.ESCALADOR) {
         * 
         * // ¿Está tocando pared?
         * if (terreno.esSolido(frenteX, centroY)) {
         * estado = Estado.ESCALADOR;
         * velocidadY = 0; // cancelar gravedad
         * alturaCaida = 0;
         * }
         * }
         * 
         * // Está escalando…
         * if (estado == Estado.ESCALADOR) {
         * 
         * // int frenteX = (int) (positionX + (haciaDerecha ? 32 : -1));
         * // int centroY = (int) (positionY + 16);
         * 
         * // ¿Hay pared frente?
         * boolean paredFrente = terreno.esSolido(frenteX, centroY);
         * 
         * if (paredFrente) {
         * 
         * // Subir verticalmente
         * positionY -= velocidad * delta * 1.2;
         * 
         * // Sensor techo justo arriba de la cabeza
         * int arribaX = (int) (positionX + 16);
         * int arribaY = (int) (positionY);
         * 
         * boolean techo = terreno.esSolido(arribaX, arribaY);
         * 
         * if (techo) {
         * // --- PULL UP (subir el borde) ---
         * // empuje hacia adelante
         * if (haciaDerecha)
         * positionX += 6;
         * else
         * positionX -= 6;
         * 
         * // pequeña bajada para quedar apoyado
         * positionY += 4;
         * 
         * // volver a caminar
         * estado = Estado.CAMINANDO;
         * quiereEscalar = false;
         * velocidadY = 0;
         * return;
         * }
         * 
         * // Sensor para bordes (cuando la pared empieza a desaparecer)
         * int bordeX = (int) (positionX + (haciaDerecha ? 20 : -4));
         * int bordeY = (int) (positionY - 4); // ligeramente arriba
         * 
         * boolean borde = !terreno.esSolido(bordeX, bordeY);
         * 
         * if (!paredFrente && borde) {
         * // Mini salto para subir escalón
         * positionY -= 10;
         * return;
         * }
         * 
         * return;
         * }
         * 
         * // Si ya no hay pared, deja de escalar
         * estado = Estado.CAMINANDO;
         * quiereEscalar = false;
         * }
         */

        // MOVIMIENTO NORMAL
        // =========================================================
        // Solo nos movemos horizontalmente si NO estamos escalando
        if (estado != Estado.ESCALADOR) {

            if (haciaDerecha) {
                // Verificar colisión derecha
                if (!terreno.esSolido((int) (positionX + 32), (int) (positionY + 16))) {
                    positionX += velocidad * delta;
                } else {
                    // --- AQUÍ ESTABA EL ERROR ---
                    // Chocó con pared derecha. ¿Quiere escalar?
                    if (quiereEscalar) {
                        estado = Estado.ESCALADOR;
                        velocidadY = 0; // Detener gravedad
                        alturaCaida = 0;
                    } else {
                        // Si no quiere escalar, rebota
                        haciaDerecha = false;
                    }
                }

            } else {
                // Verificar colisión izquierda
                if (!terreno.esSolido((int) (positionX - 1), (int) (positionY + 16))) {
                    positionX -= velocidad * delta;
                } else {
    
                        haciaDerecha = true;
                }
            }
        }
        // =========================================================
        // BOMBER
        // =========================================================
        if (estado == Estado.BOMBER) {

            tiempoBomber -= delta;

            if (tiempoBomber <= 0) {
                explotar();
                if (configuracion.isEfectosActivados())
                    Sonido.reproducir("DIE.wav");
                estado = Estado.MUERTO;
            }
        }
    }

    // =========================================================
    // DRAW
    // =========================================================
    @Override
    public void display(Graphics2D g) {

        if (estado == Estado.MUERTO)
            return;

        BufferedImage img;

        switch (estado) {
            case BLOCKER:
                img = spriteBlocker;
                break;
            case FLOATER:
                img = spriteFloater;
                break;
            case EXCAVADOR:
                img = spriteExcavador;
                break;
            case BOMBER:
                img = spriteBomber;
                break;
            case ESCALADOR:
                img = spriteEscalador;
                break;
            default:
                img = spriteWalker;
                break;
        }

        g.drawImage(img, (int) positionX, (int) positionY, 32, 32, null);
    }

    // =========================================================
    // SETTERS
    // =========================================================
    public Estado getEstado() {
        return estado;
    }

    public void setEstadoBloqueador() {
        estado = Estado.BLOCKER;
    }

    public void setEstadoFloater() {
        estado = Estado.FLOATER;
    }

    public void setEstadoExcavador() {
        estado = Estado.EXCAVADOR;
    }

    public void setEstadoBomber() {
        estado = Estado.BOMBER;
        tiempoBomber = 1.5;
    }

    public void setEstadoEscalador() {
        quiereEscalar = true;
    }

    // =========================================================
    // COLISIONES
    // =========================================================
    public boolean estaDebajo(int mx, int my) {
        return mx >= positionX && mx <= positionX + 32 &&
                my >= positionY - 20 && my <= positionY + 32;
    }

    public boolean chocaCon(Lemming otro) {
        return Math.abs(this.positionX - otro.positionX) < 32 &&
                Math.abs(this.positionY - otro.positionY) < 32;
    }

    public boolean puedeRebotar() {
        return estado == Estado.CAMINANDO ||
                estado == Estado.FLOATER ||
                estado == Estado.BOMBER ||
                estado == Estado.ESCALADOR;
    }

    private void explotar() {
        int cx = (int) (positionX + 16);
        int cy = (int) (positionY + 16);

        for (int dx = -32; dx <= 32; dx += 32)
            for (int dy = -32; dy <= 32; dy += 32)
                terreno.eliminarBloque(cx + dx, cy + dy);
    }

    public void rebote() {
        haciaDerecha = !haciaDerecha;
    }
}

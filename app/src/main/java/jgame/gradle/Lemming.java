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
        BOMBER
    }

    private double velocidad = 50;
    private double gravedad = 200;
    private double velocidadY = 0;
    private boolean haciaDerecha = true;
    private TerrenoMatriz terreno;
    private Estado estado = Estado.CAMINANDO;

    private double alturaCaida = 0;
    private double tiempoBomber = 0;

    private Configuracion configuracion;

    private BufferedImage spriteWalker;
    private BufferedImage spriteBlocker;
    private BufferedImage spriteFloater;
    private BufferedImage spriteExcavador;
    private BufferedImage spriteBomber;

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
        } catch (Exception e) {
            System.out.println("Error cargando sprites de lemming: " + e.getMessage());
        }
    }

    // =========================================================
    //   IMPLEMENTACIÓN DE LA INTERFAZ ObjetoMovible
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
    //   LÓGICA PRINCIPAL DEL LEMMING
    // =========================================================
    public void actualizar(double delta) {

        if (estado == Estado.BLOCKER || estado == Estado.MUERTO)
            return;

        // === Gravedad ===
        velocidadY += gravedad * delta;
        double nuevaY = positionY + velocidadY * delta;

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

        // === Excavador ===
        if (estado == Estado.EXCAVADOR) {

            int centroX = (int) (positionX + 16);
            int abajoY = (int) (positionY + 32);

            if (terreno.esSolido(centroX, abajoY)) {
                terreno.eliminarBloque(centroX, abajoY);
                positionY += 1;
            } else {
                estado = Estado.CAMINANDO;
            }
            return;
        }

        if (terreno.esSolido((int) (positionX + 16), (int) (nuevaY + 32))) {
            velocidadY = 0;
            nuevaY = Math.floor(nuevaY / 32) * 32;
        }

        positionY = nuevaY;

        // === Movimiento lateral ===
        if (haciaDerecha) {
            if (!terreno.esSolido((int) (positionX + 32), (int) (positionY + 16)) && positionX + 32 < 800) {
                positionX += velocidad * delta;
            } else {
                haciaDerecha = false;
            }
        } else {
            if (!terreno.esSolido((int) (positionX - 1), (int) (positionY + 16)) && positionX > 0) {
                positionX -= velocidad * delta;
            } else {
                haciaDerecha = true;
            }
        }

        // === Bomber ===
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
    //   DIBUJADO
    // =========================================================    
    @Override
    public void display(Graphics2D g) {
        if (estado == Estado.MUERTO)
            return;

        BufferedImage img;

        switch (estado) {
            case BLOCKER: img = spriteBlocker; break;
            case FLOATER: img = spriteFloater; break;
            case EXCAVADOR: img = spriteExcavador; break;
            case BOMBER: img = spriteBomber; break;
            default: img = spriteWalker; break;
        }

        g.drawImage(img, (int) positionX, (int) positionY, 32, 32, null);
    }

    // =========================================================
    //   GETTERS / SETTERS Y COLISIONES
    // =========================================================
    public Estado getEstado() { return estado; }

    public void setEstadoBloqueador() { estado = Estado.BLOCKER; }

    public void setEstadoFloater() { estado = Estado.FLOATER; }

    public void setEstadoExcavador() { estado = Estado.EXCAVADOR; }

    public void setEstadoBomber() {
        estado = Estado.BOMBER;
        tiempoBomber = 1.5;
    }

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
               estado == Estado.BOMBER;
    }

    private void explotar() {
        int cx = (int) positionX + 16;
        int cy = (int) positionY + 16;

        for (int dx = -32; dx <= 32; dx += 32)
            for (int dy = -32; dy <= 32; dy += 32)
                terreno.eliminarBloque(cx + dx, cy + dy);
    }
    public void rebote() {
    haciaDerecha = !haciaDerecha;
}

}

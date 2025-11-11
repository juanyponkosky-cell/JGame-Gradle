package jgame.gradle;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class Lemming {

    enum Estado {
        CAMINANDO,
        BLOCKER,
        FLOATER,
        MUERTO,
        EXCAVADOR,
        BOMBER
    }

    private double x, y;
    private double velocidad = 50;
    private double gravedad = 200;
    private double velocidadY = 0;
    private boolean haciaDerecha = true;
    private TerrenoMatriz terreno;
    private Estado estado = Estado.CAMINANDO;
    private double alturaCaida = 0;
    private double tiempoBomber = 0;
   // private Configuracion configuracion = new Configuracion();
    private BufferedImage spriteWalker;
    private BufferedImage spriteBlocker;
    private BufferedImage spriteFloater;
    private BufferedImage spriteExcavador;
    private BufferedImage spriteBomber;

    public Lemming(double x, double y, TerrenoMatriz terreno, Configuracion configuracion) {
        this.x = x;
        this.y = y;
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

    private Configuracion configuracion;

    public void actualizar(double delta) {
        if (estado == Estado.BLOCKER || estado == Estado.MUERTO) {
            return;
        }
        // Aplicar gravedad
        velocidadY += gravedad * delta;
        double nuevaY = y + velocidadY * delta;
        //aplicar gravedad
        int sueloY = (int) (nuevaY + 32);
        if (terreno.esSolido((int) (x + 16), sueloY)) {
            if (estado != Estado.FLOATER && alturaCaida > 100) {
                if (configuracion.isEfectosActivados()) {
                    Sonido.reproducir("SPLAT.wav");
                }
                estado = Estado.MUERTO;
                System.out.println("Lemming murio por caida");
                return;
            }

            velocidadY = 0;
            nuevaY = Math.floor(nuevaY / 32) * 32;
            alturaCaida = 0;
        } else {
            alturaCaida += velocidadY * delta;
        }

        if (estado == Estado.EXCAVADOR) {
            int centroX = (int) (x + 16);
            int abajoY = (int) (y + 32);

            if (terreno.esSolido(centroX, abajoY)) {
                terreno.eliminarBloque(centroX, abajoY); // método que ya tenemos
                y += 1; // baja lentamente
            } else {
                estado = Estado.CAMINANDO; // si ya no hay nada para excavar
            }
            return;
        }

        if (terreno.esSolido((int) (x + 16), (int) (nuevaY + 32))) {
            velocidadY = 0;
            nuevaY = Math.floor(nuevaY / 32) * 32;
        }

        y = nuevaY;

        if (haciaDerecha) {
            if (!terreno.esSolido((int) (x + 32), (int) (y + 16)) && x + 32 < 800) {
                x += velocidad * delta;
            } else {
                haciaDerecha = false; // Rebota
            }
        } else {
            if (!terreno.esSolido((int) (x - 1), (int) (y + 16)) && x > 0) {
                x -= velocidad * delta;
            } else {
                haciaDerecha = true; // Rebota
            }
        }

        if (estado == Estado.BOMBER) {
            tiempoBomber -= delta;
            if (tiempoBomber <= 0) {
                explotar();
                 if (configuracion.isEfectosActivados()) {
                    Sonido.reproducir("DIE.wav");
                }
                estado = Estado.MUERTO;
            }
            return;
        }

    }

    public void dibujar(Graphics2D g) {
        if (estado == Estado.MUERTO) {
            return;
        }
        BufferedImage img;  // por defecto
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
            default:
                img = spriteWalker;
                break;
        }
        if (img != null) {
            g.drawImage(img, (int) x, (int) y, 32, 32, null);
        }
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstadoBloqueador() {
        estado = Estado.BLOCKER;
    }

    public void setEstadoFloater() {
        System.out.println(" Lemming ahora es FLOATER");
        estado = Estado.FLOATER;
    }

    public void setEstadoExcavador() {
        estado = Estado.EXCAVADOR;
    }

    public boolean estaDebajo(int mx, int my) {
        return mx >= x && mx <= x + 32 && my >= y - 20 && my <= y + 32;
    }

    public boolean chocaCon(Lemming otro) {
        return Math.abs(this.x - otro.x) < 32 && Math.abs(this.y - otro.y) < 32;
    }

    public void rebote() {
        haciaDerecha = !haciaDerecha;
    }

    public int getCentroX() {
        return (int) (x + 16); // centro horizontal de 32x32
    }

    public int getCentroY() {
        return (int) (y + 16); // centro vertical de 32x32
    }

    public boolean puedeRebotar() {
        return estado == Estado.CAMINANDO || estado == Estado.FLOATER || estado == Estado.BOMBER;
    }

    public void setEstadoBomber() {
        estado = Estado.BOMBER;
        tiempoBomber = 1.5; // segundos antes de explotar
    }

    private void explotar() {
        int centroX = (int) x + 16;
        int centroY = (int) y + 16;

        // Rompe bloques en un área de 3x3
        for (int dx = -32; dx <= 32; dx += 32) {
            for (int dy = -32; dy <= 32; dy += 32) {
                terreno.eliminarBloque(centroX + dx, centroY + dy);
            }
        }
        System.out.println("Lemming explotó");
    }

}

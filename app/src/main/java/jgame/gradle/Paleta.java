package jgame.gradle;

import java.awt.*;

public class Paleta extends ObjetoGrafico implements ObjetoMovible {

    private int ancho;
    private int alto;
    private int maxWidth;
    private int maxHeight;

    public Paleta(int ancho, int alto, int maxWidth, int maxHeight) {
        super(null); // No usamos imagen
        this.ancho = ancho;
        this.alto = alto;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public void update(double delta) {
        // Limite inferior
        if (positionY + alto > maxHeight - 10) {
            positionY = maxHeight - 10 - alto;
        }
        // Limite superior
        if (positionY < 30) {
            positionY = 30;
        }
    }

    @Override
    public void display(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(
            (int) positionX,
            (int) positionY,
            ancho,
            alto
        );
    }

    // Ahora los bordes son correctos para colisiones
    @Override
    public Rectangle getBordes() {
        return new Rectangle(
            (int) positionX,
            (int) positionY,
            ancho,
            alto
        );
    }
}

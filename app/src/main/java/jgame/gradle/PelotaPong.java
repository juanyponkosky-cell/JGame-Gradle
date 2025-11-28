package jgame.gradle;
import java.awt.*;

public class PelotaPong extends ObjetoGrafico implements ObjetoMovible {

    protected double velocityY = 0.0;
    protected double velocityX = 0.0;

    private final int maxWidth;
    private final int maxHeight;

    private final int size = 16; // tamaño fijo de la pelota

    public PelotaPong(String filename, int maxWidth, int maxHeight) {
        super(null); // no usamos imagen
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public int getSize() {
        return size;
    }

    // Centro y detengo la pelota
    public void setPelotaAlCentro() {
        positionX = maxWidth / 2 - size / 2;
        positionY = maxHeight / 2 - size / 2;
        velocityX = 0;
        velocityY = 0;
    }

    public void startVelocity() {
        velocityX = 8.0;
        velocityY = 0;
    }

    public void invertVelocityX() {
        velocityX *= -1;
    }

    public void setVelocityY(double vy) {
        velocityY = vy;
    }

    @Override
    public void update(double delta) {
        positionX += velocityX;
        positionY += velocityY;

        // Bordes superior e inferior
        if (positionY < 30) {
            positionY = 30;
            velocityY *= -1;
        }

        if (positionY + size > maxHeight - 10) {
            positionY = maxHeight - 10 - size;
            velocityY *= -1;
        }
    }

    @Override
    public Rectangle getBordes() {
        return new Rectangle((int) positionX, (int) positionY, size, size);
    }

    public void display(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect((int) positionX, (int) positionY, size, size);
    }
}

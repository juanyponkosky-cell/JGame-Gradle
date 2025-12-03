package jgame.gradle;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class ObjetoGrafico {
    protected BufferedImage imagen = null;

    double positionX = 0;
    double positionY = 0;
    
    public ObjetoGrafico(String filename) {
        // Hacemos el path más robusto, asegurándonos de que
        // siempre empiece con una barra /
        String imagePath = filename;
        if (imagePath == null || imagePath.isEmpty()) {
             System.err.println("¡ERROR! Se intentó cargar un ObjetoGrafico con nombre de archivo vacío.");
             imagen = null;
             return;
        }

        if (!imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }

        try {
            // Usamos getClass().getResource() que es más estándar
            java.net.URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl == null) {
                // Si no se encuentra, imprimimos un error claro
                System.err.println("¡ERROR! No se pudo encontrar el recurso: " + imagePath);
                imagen = null; // Nos aseguramos que sea null
            } else {
                imagen = ImageIO.read(imgUrl);
                // Extra: Verificar si la imagen cargada tiene dimensiones válidas
                if (imagen != null && (imagen.getWidth() <= 0 || imagen.getHeight() <= 0)) {
                    System.err.println("¡ADVERTENCIA! Imagen cargada con dimensiones 0 o negativas: " + imagePath);
                    // Podríamos setearla a null aquí si quisiéramos, pero la dejaremos para el display
                }
            }
        } catch (IOException e) {
            System.err.println("ZAS! Error de IO al cargar " + imagePath + ": " + e.getMessage());
            imagen = null; // Nos aseguramos que sea null
        }
    }

    public int getWidth(){
        if (imagen == null || imagen.getWidth() <= 0) { // Agregamos chequeo de 0
            return 10; // Devuelve un valor por defecto para evitar divisiones por cero y tener un tamaño mínimo
        }
        return imagen.getWidth();
    }
    
    public int getHeight(){
        if (imagen == null || imagen.getHeight() <= 0) { // Agregamos chequeo de 0
            return 10; // Devuelve un valor por defecto
        }
        return imagen.getHeight();
    }

    public void setPosition(int x,int y){
        this.positionX = x;
        this.positionY = y;
    }

    public Rectangle getBordes(){
        return new Rectangle((int) this.positionX, (int) this.positionY, this.getWidth(), this.getHeight());
    }

    // *** CAMBIO PRINCIPAL: Dibuja un cuadrado rojo si la imagen no es válida ***
    public void display(Graphics2D g2) {
        if (imagen != null && imagen.getWidth() > 0 && imagen.getHeight() > 0) {
            g2.drawImage(imagen,(int) this.positionX,(int) this.positionY,null);
        } else {
            // Si la imagen es null o tiene dimensiones 0, dibuja un cuadrado rojo.
            // Usamos el getWidth/getHeight que devuelve 10 si la imagen es inválida.
            g2.setColor(Color.RED);
            g2.fillRect((int)this.positionX, (int)this.positionY, getWidth(), getHeight()); 
            // Opcional: Para debugging, podemos imprimir cuando esto sucede.
            // System.err.println("Dibujando marcador rojo para objeto sin imagen válida en " + this.positionX + "," + this.positionY);
        }
    }

    public double getX(){
        return positionX;
    }

    public double getY(){
        return positionY;
    }

    public BufferedImage getImagen(){
        return imagen;
    }
}
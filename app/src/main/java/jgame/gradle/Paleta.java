package jgame.gradle;
import java.awt.*;

public class Paleta extends ObjetoGrafico implements ObjetoMovible{

    private int alto;
    private int ancho;

    public Paleta(String filename,int ancho, int alto) {
        super(filename);
        this.ancho=ancho;
        this.alto=alto;
    }

    @Override
    public void update(double delta) {

        // Si choca contra el borde de abajo: 
        if ((positionY+ (this.getHeight())) > this.alto+10) {
			positionY = this.alto +10 - (this.getHeight());
		}

        // Si choca contra el borde de arriba
        if ((positionY) < 30) {
			positionY = 30;
		}
    }
    
    public void display(Graphics2D g2) {
       g2.drawImage(imagen,(int) this.positionX,(int) this.positionY,null);
    }
}

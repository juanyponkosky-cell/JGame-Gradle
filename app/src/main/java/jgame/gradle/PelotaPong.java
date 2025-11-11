package jgame.gradle;
import java.awt.*;

public class PelotaPong extends ObjetoGrafico implements ObjetoMovible{

    protected double velocityY = 0.0;
    protected double velocityX = 0.0;
    private int alto;
    private int ancho;

    public PelotaPong(String filename,int ancho, int alto) {
        super(filename);
        this.ancho=ancho;
        this.alto=alto;
    }

    //Centro y detengo la paleta
    public void setPelotaAlCentro(){
        positionX=this.ancho/2-this.getWidth()/2;
        positionY=this.alto/2-this.getHeight()/2;
        velocityX=0.0;
        velocityY=0.0;
    }

    public void startVelocity(){
        velocityX=9.0;
    }

    public void invertVelocityX(){
        velocityX*=-1;
    }

    public void setVelocityY(double x){
        velocityY=x;
    }

    @Override
    public void update(double delta) {
        positionY += velocityY;
        positionX += velocityX;

        // Si choca contra el borde de abajo
        if ((positionY+ (this.getHeight())) > this.alto+10) {
            velocityY*=-1;
		}

        // Si choca contra el borde de arriba
        if ((positionY) < 30) {
			velocityY*=-1;
		}
    }

    public void display(Graphics2D g2) {
        g2.drawImage(imagen,(int) this.positionX,(int) this.positionY,null);
        g2.setColor(Color.RED);
    }
    
}

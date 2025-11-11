package jgame.gradle;
import com.entropyinteractive.*;  //jgame
 
import java.awt.*;
import java.awt.event.*; //eventos

import java.awt.image.*;  //imagenes
import javax.imageio.*; //imagenes

import java.util.*;
//import java.text.*;

import java.io.*;

public class Pong extends JGame {


    private final double VELOCIDAD=7.0;

    private final int ESTADO_INICIO=0;
    private final int ESTADO_JUGANDO=1;
    private final int ESTADO_ESPERANDO=2;
    private final int ESTADO_FIN=3;

    private final static int HEIGHT=600;
    private final static int WIDTH=800;
    
    //Paleta recibe el ancho del juego
    private Paleta paleta1=new Paleta("imagenes/paleta.png",WIDTH,HEIGHT);
    private Paleta paleta2=new Paleta("imagenes/paleta.png",WIDTH,HEIGHT);
    private PelotaPong pelota=new PelotaPong("imagenes/pelota.png",WIDTH,HEIGHT);
    private BufferedImage red = null;
    private BufferedImage pong = null;
    private Rectangle play = new Rectangle(190,80);
    private Rectangle replay = new Rectangle(153,37);

    private int contador1 = 0;
    private int contador2 = 0;
    private int estado;

    public static void main(String[] args) {

        Pong game = new Pong();
        game.run(1.0 / 60.0); //60 FPS
        System.exit(0);
    }



    public Pong() {
        super("Pong", WIDTH, HEIGHT);

        System.out.println(appProperties.stringPropertyNames());

        try {
            red = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/red.png"));
            pong = ImageIO.read(getClass().getClassLoader().getResourceAsStream("imagenes/pong0.jpg"));
        } catch (IOException e) {
            System.out.println("ZAS! en ObjectoGrafico "+e);
        }
    }

    public void gameStartup() {
        System.out.println("gameStartup");
        cargarTTF();
        try{
            paleta1.setPosition(50 ,HEIGHT / 2 - paleta1.getHeight()/2);
            paleta2.setPosition(WIDTH-50 ,HEIGHT / 2 - paleta2.getHeight()/2);
            pelota.setPelotaAlCentro();
        }
        catch(Exception e){
            System.out.println(e);
        }

        estado=ESTADO_INICIO;
       
    }


//FUENTES
private void cargarTTF(){
    try{
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        InputStream is =getClass().getClassLoader().getResourceAsStream("Pong-Game.ttf");
        Font font = Font.createFont(Font.TRUETYPE_FONT, is);
        ge.registerFont(font);  
    }catch(Exception e){
        System.out.println("ZAS! Problema al cargar tipo de letra");
    }
}

public void gameUpdate(double delta) {
        Keyboard keyboard = this.getKeyboard();
        Mouse mouse = this.getMouse();
         
        // Procesar teclas de direccion
        if (keyboard.isKeyPressed(KeyEvent.VK_UP)){
            paleta2.positionY -= VELOCIDAD;
        }

        if (keyboard.isKeyPressed(KeyEvent.VK_DOWN)){
            paleta2.positionY += VELOCIDAD;
        }

        if (keyboard.isKeyPressed(KeyEvent.VK_W)){
            paleta1.positionY -= VELOCIDAD;
        }

        if (keyboard.isKeyPressed(KeyEvent.VK_S)){
            paleta1.positionY += VELOCIDAD;
        }

        // Esc fin del juego
        LinkedList < KeyEvent > keyEvents = keyboard.getEvents();
        for (KeyEvent event: keyEvents) {
            if ((event.getID() == KeyEvent.KEY_PRESSED) &&
                (event.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                stop();
            }

            if((event.getID() == KeyEvent.KEY_PRESSED) && 
               (event.getKeyCode() == KeyEvent.VK_SPACE) && 
                estado==ESTADO_ESPERANDO){
                pelota.startVelocity();
                estado=ESTADO_JUGANDO;
            }

            if((event.getID() == KeyEvent.KEY_PRESSED) && 
               (event.getKeyCode() == KeyEvent.VK_P) && 
                estado==ESTADO_INICIO){
                    estado=ESTADO_ESPERANDO;
            }
        }

        //COLISIONES
        if(pelota.getBordes().intersects(paleta1.getBordes())){
            double centroPaleta = paleta1.positionY + paleta1.getHeight() / 2;
            double desplazamiento = (pelota.positionY + pelota.getHeight()/2) - centroPaleta;
            pelota.invertVelocityX();
            pelota.setVelocityY(desplazamiento*0.15);
        }

        if(pelota.getBordes().intersects(paleta2.getBordes())){
            double centroPaleta = paleta2.positionY + paleta2.getHeight() / 2;
            double desplazamiento = (pelota.positionY + pelota.getHeight()/2) - centroPaleta;
            pelota.invertVelocityX();
            pelota.setVelocityY(desplazamiento*0.15);
        }

        if(pelota.positionX < 0){
            contador2++;
            paleta1.setPosition(50 ,HEIGHT / 2 - paleta1.getHeight()/2);
            paleta2.setPosition(WIDTH-50 ,HEIGHT / 2 - paleta2.getHeight()/2);
            pelota.setPelotaAlCentro();
            estado=ESTADO_ESPERANDO;
        }

        if(pelota.positionX > WIDTH-pelota.getWidth() ){
            contador1++;
            paleta1.setPosition(50 ,HEIGHT / 2 - paleta1.getHeight()/2);
            paleta2.setPosition(WIDTH-50 ,HEIGHT / 2 - paleta2.getHeight()/2);
            pelota.setPelotaAlCentro();
            estado=ESTADO_ESPERANDO;
        }

        if(contador1>=10 || contador2>=10){
            estado=ESTADO_FIN;
            if(contador1>=10){
                replay.setLocation(117, 330);
            }
            if(contador2>=10){
                replay.setLocation(517, 330);
            }
            if(replay.contains(mouse.getX(),mouse.getY())){
                System.out.println(".");
            }
            if(replay.contains(mouse.getX(),mouse.getY()) && mouse.isLeftButtonPressed()){
                estado=ESTADO_ESPERANDO;
                contador1=0;
                contador2=0;
            }
        }
        
        if(estado == ESTADO_INICIO){
            play.setLocation(WIDTH/2-100, 385);
            if(play.contains(mouse.getX(),mouse.getY()) && mouse.isLeftButtonPressed()){
                estado=ESTADO_ESPERANDO;
            }
        }

        paleta1.update(delta);
        paleta2.update(delta);
        pelota.update(delta);

    }

    public void gameDraw(Graphics2D g) {

        if(estado==ESTADO_INICIO){
            g.setColor(Color.WHITE);
            g.drawImage(pong, 200, 100, 400, 225, null);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 100));
            g.drawString("PLAY", WIDTH/2-100, 500);
        }
        
        if(estado == ESTADO_ESPERANDO || estado == ESTADO_JUGANDO){
            g.setColor(Color.WHITE);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 100));
            g.drawString(""+this.contador1,WIDTH/2-130,110);
            g.drawString(""+this.contador2,WIDTH/2+70,110);
            g.drawImage(red, WIDTH/2-1, 0,null);
         
            paleta1.display(g);
            paleta2.display(g);
            pelota.display(g);
        }

        if(estado == ESTADO_FIN){
            g.setColor(Color.WHITE);
            g.setFont(new Font("Pong-Game", Font.PLAIN, 100));
            g.drawString(""+this.contador1,WIDTH/2-130,110);
            g.drawString(""+this.contador2,WIDTH/2+70,110);
            g.drawImage(red, WIDTH/2-1, 0,null);
         
            paleta1.display(g);
            paleta2.display(g);

            if(this.contador2>=10){
                g.setColor(Color.WHITE);
                g.setFont(new Font("Pong-Game", Font.PLAIN, 70));
                g.drawString("GANADOR",470,250);
                g.setFont(new Font("Pong-Game", Font.PLAIN, 40));
                g.drawString("REINICIAR", 517, 400);
            }
            
            if(this.contador1>=10){
                g.setColor(Color.WHITE);
                g.setFont(new Font("Pong-Game", Font.PLAIN, 70));
                g.drawString("GANADOR",70,250);
                g.setFont(new Font("Pong-Game", Font.PLAIN, 40));
                g.drawString("REINICIAR", 117, 400);
            }
        }
    }

    public void gameShutdown() {
       Log.info(getClass().getSimpleName(), "Shutting down game");
    }
}

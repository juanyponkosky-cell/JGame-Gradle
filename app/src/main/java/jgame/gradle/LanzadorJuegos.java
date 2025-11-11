
package jgame.gradle;
import java.awt.*;

import java.awt.event.*;
import javax.swing.*;
import com.entropyinteractive.JGame;

public class LanzadorJuegos extends JPanel {

    JGame juego;
    Thread t;

    public LanzadorJuegos() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(750, 300));

        // Fondo con gradiente
        setOpaque(false);

        // Título
        JLabel titulo = new JLabel("🎮 Lanzador de Juegos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(Color.white);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        add(titulo, BorderLayout.NORTH);

        // Panel de tarjetas de juegos
        JPanel panelJuegos = new JPanel(new GridLayout(1, 3, 20, 20));
        panelJuegos.setOpaque(false);
        panelJuegos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelJuegos.add(crearTarjetaJuego("imagenes/PortadaLemmings.png", "Lemmings", "Rescatá a los lemmings"));
        panelJuegos.add(crearTarjetaJuego("imagenes/PortadaPong.png", "Pong", "Partido de pong"));
        panelJuegos.add(crearTarjetaJuego("imagenes/PortadaWarzone.jpg", "Warzone", "Batalla"));

        add(panelJuegos, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaJuego(String rutaImagen, String nombreJuego, String descripcion) {
        JPanel tarjeta = new JPanel(new BorderLayout()) {
            // Fondo transparente para permitir bordes redondeados
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        java.net.URL imgUrl = getClass().getResource("/" + rutaImagen);
        if (imgUrl == null) {
            System.err.println("Imagen no encontrada: " + rutaImagen);
            tarjeta.add(new JLabel("Sin imagen"), BorderLayout.CENTER);
            return tarjeta;
        }

        ImageIcon originalIcon = new ImageIcon(imgUrl);
        Image imagenEscalada = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon iconoEscalado = new ImageIcon(imagenEscalada);

        JLabel imagen = new JLabel(iconoEscalado);
        imagen.setHorizontalAlignment(SwingConstants.CENTER);
        imagen.setToolTipText(descripcion);
        imagen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Efecto hover
        imagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (nombreJuego == "Warzone")
                    tarjeta.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                else {
                    tarjeta.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                lanzarJuego(nombreJuego);
            }
        });

        tarjeta.add(imagen, BorderLayout.CENTER);
        return tarjeta;
    }

    private void lanzarJuego(String nombreJuego) {
        switch (nombreJuego) {
            case "Lemmings":
                juego = new JuegoLemmings();
                break;
            case "Pong":
                juego = new Pong();
                break;
            case "JuegoConDB":
                // juego = new JuegoConDB();
                break;
            default:
                System.out.println("Juego en proceso.");
                return;
        }

        t = new Thread(() -> juego.run(1.0 / 60.0));
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Fondo con degradado vertical
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 60), 0, getHeight(), new Color(10, 10, 30));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    public static void main(String[] args) {
        
        JFrame f = new JFrame("Lanzador de Juegos");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(new LanzadorJuegos());
        f.pack();
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}


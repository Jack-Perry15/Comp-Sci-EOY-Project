import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Game extends JPanel implements Runnable, KeyListener {

    private BufferedImage back;
    private boolean[] keys;

    private Player p1;
    private Player p2;

    private ArrayList<Rectangle> platforms;

    // 2 second cooldown at ~60 FPS
    private int tagCooldown;

    public Game() {
        keys = new boolean[256];

        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        p1 = new Player(100, 650, 50, 50, Color.RED, true);
        p2 = new Player(300, 650, 50, 50, Color.BLUE, false);

        platforms = new ArrayList<>();

        // Lower platforms
        platforms.add(new Rectangle(0, 800, 1800, 60)); // ground
        platforms.add(new Rectangle(200, 700, 250, 20));
        platforms.add(new Rectangle(600, 650, 250, 20));
        platforms.add(new Rectangle(1000, 600, 250, 20));
        platforms.add(new Rectangle(1400, 700, 200, 20));

        tagCooldown = 0;

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                updateGame();
                repaint();
                Thread.sleep(16); // about 60 FPS
            }
        } catch (Exception e) {
        }
    }

    public void updateGame() {
        handleInput();

        p1.applyGravity();
        p2.applyGravity();

        p1.move(platforms);
        p2.move(platforms);

        if (tagCooldown > 0) {
            tagCooldown--;
        }

        checkTag();
    }

    public void handleInput() {
        p1.vx = 0;
        if (keys[KeyEvent.VK_A]) {
            p1.vx = -5;
        }
        if (keys[KeyEvent.VK_D]) {
            p1.vx = 5;
        }
        if (keys[KeyEvent.VK_W] && p1.onGround) {
            p1.vy = -14;
            p1.onGround = false;
        }

        p2.vx = 0;
        if (keys[KeyEvent.VK_LEFT]) {
            p2.vx = -5;
        }
        if (keys[KeyEvent.VK_RIGHT]) {
            p2.vx = 5;
        }
        if (keys[KeyEvent.VK_UP] && p2.onGround) {
            p2.vy = -14;
            p2.onGround = false;
        }
    }

    public void checkTag() {
        Rectangle r1 = p1.getRect();
        Rectangle r2 = p2.getRect();

        if (tagCooldown == 0 && r1.intersects(r2)) {
            if (p1.isIt) {
                p1.isIt = false;
                p2.isIt = true;
            } else {
                p2.isIt = false;
                p1.isIt = true;
            }

            tagCooldown = 120; // about 2 seconds
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D twoDgraph = (Graphics2D) g;

        if (back == null) {
            back = (BufferedImage) (createImage(getWidth(), getHeight()));
        }

        Graphics g2d = back.createGraphics();
        g2d.clearRect(0, 0, getWidth(), getHeight());

        g2d.setColor(new Color(180, 220, 255));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.DARK_GRAY);
        for (Rectangle r : platforms) {
            g2d.fillRect(r.x, r.y, r.width, r.height);
        }

        p1.draw(g2d);
        p2.draw(g2d);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("Player 1 (Red): " + (p1.isIt ? "IT" : "Runner"), 40, 40);
        g2d.drawString("Player 2 (Blue): " + (p2.isIt ? "IT" : "Runner"), 40, 80);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("P1: A D W", 40, 120);
        g2d.drawString("P2: LEFT RIGHT UP", 40, 150);

        twoDgraph.drawImage(back, null, 0, 0);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) {
            keys[code] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) {
            keys[code] = false;
        }
    }
}
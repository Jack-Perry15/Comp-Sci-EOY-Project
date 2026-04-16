import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Game extends JPanel implements Runnable, KeyListener, ComponentListener {

    private BufferedImage back;
    private BufferedImage bg;
    private boolean[] keys;

    private Player p1;
    private Player p2;

    private ArrayList<Rectangle> platforms;
    private ArrayList<MovingPlatform> movingPlatforms;
    private ArrayList<Particle> particles;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Map> maps;

    private int currentMapIndex;
    private int tagCooldown;

    private int roundTimer;
    private int roundLength;
    private int roundNumber;
    private int maxRounds;

    private int powerUpSpawnTimer;

    public static double scale = 1.0;
    private final int ORIG_WIDTH = 1800;
    private final int ORIG_HEIGHT = 900;

    private final Random rand = new Random();

    public Game() {
        keys = new boolean[256];

        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        addComponentListener(this);

        scale = 1.0;

        particles = new ArrayList<>();
        powerUps = new ArrayList<>();
        maps = new ArrayList<>();

        roundLength = 60 * 60;
        roundTimer = roundLength;
        roundNumber = 1;
        maxRounds = 5;
        powerUpSpawnTimer = 300;

        buildMaps();
        rebuildGameElements();

        tagCooldown = 0;

        try {
            bg = ImageIO.read(new File("Platformer/Background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    public void run() {
        try {
            while (true) {
                updateGame();
                repaint();
                Thread.sleep(16);
            }
        } catch (Exception e) {
        }
    }

    public void updateGame() {
        updateMovingPlatforms();
        handleInput();

        p1.updateTimers();
        p2.updateTimers();

        p1.applyGravity(1 * scale, 15);
        p2.applyGravity(1 * scale, 15);

        p1.move(platforms, movingPlatforms, getWorldWidth(), getWorldHeight(), (int)(650 * scale));
        p2.move(platforms, movingPlatforms, getWorldWidth(), getWorldHeight(), (int)(650 * scale));

        updateParticles();
        updatePowerUps();
        handlePowerUpPickups();

        if (tagCooldown > 0) tagCooldown--;

        checkTag();
        updateRoundTimer();
    }

    private int getWorldWidth() {
        return (int)(ORIG_WIDTH * scale);
    }

    private int getWorldHeight() {
        return (int)(ORIG_HEIGHT * scale);
    }

    public void handleInput() {
        int baseSpeed = (int)(5 * scale);
        int baseJump = (int)(14 * scale);

        p1.vx = 0;
        if (p1.canMove()) {
            int p1Speed = p1.getMoveSpeed(baseSpeed);
            if (keys[KeyEvent.VK_A]) p1.vx = -p1Speed;
            if (keys[KeyEvent.VK_D]) p1.vx = p1Speed;

            if (keys[KeyEvent.VK_W]) {
                p1.jumpBufferFrames = 6;
            }

            if (p1.jumpBufferFrames > 0 && (p1.onGround || p1.coyoteFrames > 0)) {
                p1.vy = -p1.getJumpPower(baseJump);
                p1.onGround = false;
                p1.coyoteFrames = 0;
                p1.jumpBufferFrames = 0;
                spawnJumpParticles(p1);
                SoundPlayer.play("jump.wav");
            }
        }

        p2.vx = 0;
        if (p2.canMove()) {
            int p2Speed = p2.getMoveSpeed(baseSpeed);
            if (keys[KeyEvent.VK_LEFT]) p2.vx = -p2Speed;
            if (keys[KeyEvent.VK_RIGHT]) p2.vx = p2Speed;

            if (keys[KeyEvent.VK_UP]) {
                p2.jumpBufferFrames = 6;
            }

            if (p2.jumpBufferFrames > 0 && (p2.onGround || p2.coyoteFrames > 0)) {
                p2.vy = -p2.getJumpPower(baseJump);
                p2.onGround = false;
                p2.coyoteFrames = 0;
                p2.jumpBufferFrames = 0;
                spawnJumpParticles(p2);
                SoundPlayer.play("jump.wav");
            }
        }
    }

    public void checkTag() {
        Rectangle r1 = p1.getRect();
        Rectangle r2 = p2.getRect();

        if (tagCooldown == 0 && r1.intersects(r2)) {
            spawnTagParticles((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
            SoundPlayer.play("tag.wav");

            if (p1.isIt) {
                p1.isIt = false;
                p2.isIt = true;
                p1.tagsThisRound++;
            } else {
                p2.isIt = false;
                p1.isIt = true;
                p2.tagsThisRound++;
            }

            tagCooldown = 120;
        }
    }

    private void updateRoundTimer() {
        roundTimer--;
        if (roundTimer <= 0) {
            endRound();
        }
    }

    private void endRound() {
        Player winner;
        Player loser;

        if (p1.tagsThisRound > p2.tagsThisRound) {
            winner = p1;
            loser = p2;
        } else if (p2.tagsThisRound > p1.tagsThisRound) {
            winner = p2;
            loser = p1;
        } else {
            if (p1.isIt) {
                winner = p2;
                loser = p1;
            } else {
                winner = p1;
                loser = p2;
            }
        }

        winner.roundsWon++;
        winner.winStreak++;
        loser.winStreak = 0;

        roundNumber++;

        if (roundNumber > maxRounds) {
            resetMatch();
            return;
        }

        resetRound();
    }

    private void resetRound() {
        roundTimer = roundLength;
        powerUps.clear();
        particles.clear();
        currentMapIndex = (currentMapIndex + 1) % maps.size();
        rebuildGameElements();
        p1.tagsThisRound = 0;
        p2.tagsThisRound = 0;
    }

    private void resetMatch() {
        p1.roundsWon = 0;
        p2.roundsWon = 0;
        p1.winStreak = 0;
        p2.winStreak = 0;
        roundNumber = 1;
        resetRound();
    }

    private void updateMovingPlatforms() {
        for (MovingPlatform mp : movingPlatforms) {
            mp.update();
        }
    }

    private void updatePowerUps() {
        powerUpSpawnTimer--;
        if (powerUpSpawnTimer <= 0) {
            spawnPowerUp();
            powerUpSpawnTimer = 420;
        }
    }

    private void spawnPowerUp() {
        String[] types = {"speed", "jump", "freeze"};
        String type = types[rand.nextInt(types.length)];

        if (platforms.size() > 1) {
            Rectangle plat = platforms.get(1 + rand.nextInt(platforms.size() - 1));
            int px = plat.x + rand.nextInt(Math.max(1, plat.width - 30));
            int py = plat.y - 30;
            powerUps.add(new PowerUp(px, py, type));
        }
    }

    private void handlePowerUpPickups() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp pu = powerUps.get(i);

            if (p1.getRect().intersects(pu.getRect())) {
                applyPowerUp(p1, p2, pu);
                SoundPlayer.play("powerup.wav");
                powerUps.remove(i);
            } else if (p2.getRect().intersects(pu.getRect())) {
                applyPowerUp(p2, p1, pu);
                SoundPlayer.play("powerup.wav");
                powerUps.remove(i);
            }
        }
    }

    private void applyPowerUp(Player collector, Player opponent, PowerUp pu) {
        if (pu.type.equals("speed")) {
            collector.speedBoostTimer = pu.duration;
        } else if (pu.type.equals("jump")) {
            collector.jumpBoostTimer = pu.duration;
        } else if (pu.type.equals("freeze")) {
            opponent.freezeTimer = pu.duration;
        }

        for (int i = 0; i < 12; i++) {
            particles.add(new Particle(
                    collector.x + collector.width / 2,
                    collector.y + collector.height / 2,
                    rand.nextDouble() * 8 - 4,
                    rand.nextDouble() * 8 - 4,
                    6,
                    20,
                    pu.color
            ));
        }
    }

    private void spawnJumpParticles(Player p) {
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle(
                    p.x + p.width / 2,
                    p.y + p.height,
                    rand.nextDouble() * 4 - 2,
                    rand.nextDouble() * -2,
                    5,
                    16,
                    Color.LIGHT_GRAY
            ));
        }
    }

    private void spawnTagParticles(int x, int y) {
        for (int i = 0; i < 20; i++) {
            particles.add(new Particle(
                    x,
                    y,
                    rand.nextDouble() * 10 - 5,
                    rand.nextDouble() * 10 - 5,
                    6,
                    20,
                    Color.YELLOW
            ));
        }
    }

    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            particles.get(i).update();
            if (particles.get(i).isDead()) {
                particles.remove(i);
            }
        }
    }

    private void buildMaps() {
        maps.clear();

        Map map1 = new Map("Classic");
        map1.staticPlatforms.add(new Rectangle(0, 800, 1800, 60));
        map1.staticPlatforms.add(new Rectangle(200, 700, 250, 20));
        map1.staticPlatforms.add(new Rectangle(600, 650, 250, 20));
        map1.staticPlatforms.add(new Rectangle(1000, 600, 250, 20));
        map1.staticPlatforms.add(new Rectangle(1400, 700, 200, 20));
        maps.add(map1);

        Map map2 = new Map("Vertical");
        map2.staticPlatforms.add(new Rectangle(0, 800, 1800, 60));
        map2.staticPlatforms.add(new Rectangle(250, 720, 180, 20));
        map2.staticPlatforms.add(new Rectangle(500, 620, 180, 20));
        map2.staticPlatforms.add(new Rectangle(780, 520, 180, 20));
        map2.staticPlatforms.add(new Rectangle(1080, 420, 180, 20));
        map2.staticPlatforms.add(new Rectangle(1380, 320, 180, 20));
        maps.add(map2);

        Map map3 = new Map("Moving");
        map3.staticPlatforms.add(new Rectangle(0, 800, 1800, 60));
        map3.staticPlatforms.add(new Rectangle(250, 650, 200, 20));
        map3.staticPlatforms.add(new Rectangle(1350, 650, 200, 20));
        map3.movingPlatforms.add(new MovingPlatform(650, 560, 220, 20, 300, 3));
        map3.movingPlatforms.add(new MovingPlatform(900, 420, 220, 20, -250, 2));
        maps.add(map3);

        currentMapIndex = 0;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D twoDgraph = (Graphics2D) g;

        if (back == null || back.getWidth() != getWidth() || back.getHeight() != getHeight()) {
            back = (BufferedImage)(createImage(getWidth(), getHeight()));
        }

        Graphics g2d = back.createGraphics();
        g2d.clearRect(0, 0, getWidth(), getHeight());

        if (bg != null) {
            g2d.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(new Color(180, 220, 255));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.setColor(Color.DARK_GRAY);
        for (Rectangle r : platforms) {
            g2d.fillRect(r.x, r.y, r.width, r.height);
        }

        for (MovingPlatform mp : movingPlatforms) {
            mp.draw(g2d);
        }

        for (PowerUp pu : powerUps) {
            pu.draw(g2d);
        }

        for (Particle p : particles) {
            p.draw(g2d);
        }

        p1.draw(g2d);
        p2.draw(g2d);

        drawUI(g2d);

        twoDgraph.drawImage(back, null, 0, 0);
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("Round " + roundNumber + "/" + maxRounds, 40, 40);
        g.drawString("Map: " + maps.get(currentMapIndex).name, 40, 75);

        g.drawString("P1 Wins: " + p1.roundsWon + "   Tags: " + p1.tagsThisRound, 40, 120);
        g.drawString("P2 Wins: " + p2.roundsWon + "   Tags: " + p2.tagsThisRound, 40, 155);

        int seconds = roundTimer / 60;
        g.drawString("Time: " + seconds, 40, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("P1: A D W", 40, 240);
        g.drawString("P2: LEFT RIGHT UP", 40, 270);

        int barWidth = 220;
        int barHeight = 18;

        g.setColor(Color.GRAY);
        g.fillRect(40, 300, barWidth, barHeight);
        g.setColor(Color.GREEN);
        g.fillRect(40, 300, (int)(barWidth * (roundTimer / (double) roundLength)), barHeight);
        g.setColor(Color.BLACK);
        g.drawRect(40, 300, barWidth, barHeight);

        g.drawString("Power-Ups:", 40, 350);
        g.drawString("Speed = Green, Jump = Pink, Freeze = Cyan", 40, 380);

        if (p1.freezeTimer > 0) g.drawString("P1 FROZEN", 300, 120);
        if (p2.freezeTimer > 0) g.drawString("P2 FROZEN", 300, 155);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

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

    @Override
    public void componentResized(ComponentEvent e) {
        scale = Math.min((double)getWidth() / ORIG_WIDTH, (double)getHeight() / ORIG_HEIGHT);
        rebuildGameElements();
        back = null;
        requestFocusInWindow();
    }

    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}

    private void rebuildGameElements() {
        Map map = maps.get(currentMapIndex);

        int sPlayerSize = (int)(50 * scale);
        int sPlayerX1 = (int)(100 * scale);
        int sPlayerX2 = (int)(300 * scale);
        int sGroundY = (int)(800 * scale);
        int sPlayerY = sGroundY - sPlayerSize;

        Player oldP1 = p1;
        Player oldP2 = p2;

        p1 = new Player(sPlayerX1, sPlayerY, sPlayerSize, sPlayerSize, Color.RED, true);
        p2 = new Player(sPlayerX2, sPlayerY, sPlayerSize, sPlayerSize, Color.BLUE, false);

        if (oldP1 != null && oldP2 != null) {
            p1.roundsWon = oldP1.roundsWon;
            p2.roundsWon = oldP2.roundsWon;
            p1.winStreak = oldP1.winStreak;
            p2.winStreak = oldP2.winStreak;
            p1.tagsThisRound = oldP1.tagsThisRound;
            p2.tagsThisRound = oldP2.tagsThisRound;
            p1.isIt = oldP1.isIt;
            p2.isIt = oldP2.isIt;
        }

        p1.onGround = true;
        p2.onGround = true;

        platforms = new ArrayList<>();
        for (Rectangle r : map.staticPlatforms) {
            platforms.add(new Rectangle(
                    (int)(r.x * scale),
                    (int)(r.y * scale),
                    (int)(r.width * scale),
                    (int)(r.height * scale)
            ));
        }

        movingPlatforms = new ArrayList<>();
        for (MovingPlatform mp : map.movingPlatforms) {
            movingPlatforms.add(new MovingPlatform(
                    (int)(mp.x * scale),
                    (int)(mp.y * scale),
                    (int)(mp.width * scale),
                    (int)(mp.height * scale),
                    (int)((mp.endX - mp.startX) * scale),
                    Math.max(1, (int)(mp.speed * scale))
            ));
        }

        if (oldP1 == null && oldP2 == null) {
            p1.isIt = true;
            p2.isIt = false;
        }
    }
}
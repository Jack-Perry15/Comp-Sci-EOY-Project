import java.awt.*;

public class Particle {
    double x, y;
    double vx, vy;
    int size;
    int life;
    Color color;

    public Particle(double x, double y, double vx, double vy, int size, int life, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.life = life;
        this.color = color;
    }

    public void update() {
        x += vx;
        y += vy;
        vy += 0.2;
        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval((int)x, (int)y, size, size);
    }
}
import java.awt.*;

public class MovingPlatform {
    int x, y, width, height;
    int minX, maxX;
    int speed;
    int dir = 1;
    private int dx = 0;

    public MovingPlatform(int x, int y, int width, int height, int moveDistance, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = Math.max(1, Math.abs(speed));

        int endX = x + moveDistance;
        minX = Math.min(x, endX);
        maxX = Math.max(x, endX);

        if (endX < x) {
            dir = -1;
        }
    }

    public void update() {
        dx = speed * dir;
        x += dx;

        if (x <= minX) {
            x = minX;
            dir = 1;
            dx = 0;
        } else if (x >= maxX) {
            x = maxX;
            dir = -1;
            dx = 0;
        }
    }

    public int getDx() {
        return dx;
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(new Color(90, 90, 90));
        g.fillRect(x, y, width, height);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }
}
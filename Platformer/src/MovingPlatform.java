import java.awt.*;

public class MovingPlatform {
    int x, y, width, height;
    int startX, endX;
    int speed;
    int dir = 1;

    public MovingPlatform(int x, int y, int width, int height, int moveDistance, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startX = x;
        this.endX = x + moveDistance;
        this.speed = speed;
    }

    public void update() {
        x += speed * dir;
        if (x <= startX || x >= endX) {
            dir *= -1;
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(new Color(90, 90, 90));
        g.fillRect(x, y, width, height);
    }
}
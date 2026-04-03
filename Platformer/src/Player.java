import java.awt.*;
import java.util.ArrayList;

public class Player {
    int x, y, width, height;
    int vx, vy;

    boolean onGround;
    boolean isIt;

    Color color;

    public Player(int x, int y, int width, int height, Color color, boolean isIt) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.isIt = isIt;

        vx = 0;
        vy = 0;
        onGround = false;
    }

    public void applyGravity() {
        vy += 1; // gravity
        if (vy > 15) {
            vy = 15;
        }
    }

    public void move(ArrayList<Rectangle> platforms) {
        // move horizontally
        x += vx;
        Rectangle playerRect = getRect();

        for (Rectangle platform : platforms) {
            if (playerRect.intersects(platform)) {
                if (vx > 0) {
                    x = platform.x - width;
                } else if (vx < 0) {
                    x = platform.x + platform.width;
                }
                playerRect = getRect();
            }
        }

        // move vertically
        y += vy;
        onGround = false;
        playerRect = getRect();

        for (Rectangle platform : platforms) {
            if (playerRect.intersects(platform)) {
                if (vy > 0) { // falling
                    y = platform.y - height;
                    vy = 0;
                    onGround = true;
                } else if (vy < 0) { // jumping upward
                    y = platform.y + platform.height;
                    vy = 0;
                }
                playerRect = getRect();
            }
        }

        // keep inside screen
        if (x < 0) x = 0;
        if (x + width > 1800) x = 1800 - width;
        if (y > 1600) {
            y = 500;
            vy = 0;
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);

        if (isIt) {
            g.setColor(Color.YELLOW);
            g.drawRect(x - 3, y - 3, width + 6, height + 6);
            g.drawRect(x - 4, y - 4, width + 8, height + 8);
        }
    }
}
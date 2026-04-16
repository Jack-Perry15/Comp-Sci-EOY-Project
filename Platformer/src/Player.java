import java.awt.*;
import java.util.ArrayList;

public class Player {
    int x, y, width, height;
    int vx, vy;

    boolean onGround;
    boolean isIt;

    Color color;

    int coyoteFrames;
    int jumpBufferFrames;

    int speedBoostTimer;
    int jumpBoostTimer;
    int freezeTimer;

    int tagsThisRound;
    int roundsWon;
    int winStreak;

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

        coyoteFrames = 0;
        jumpBufferFrames = 0;

        speedBoostTimer = 0;
        jumpBoostTimer = 0;
        freezeTimer = 0;

        tagsThisRound = 0;
        roundsWon = 0;
        winStreak = 0;
    }

    public void applyGravity(double gravity, int maxVy) {
        vy += gravity;
        if (vy > maxVy) {
            vy = maxVy;
        }
    }

    public void updateTimers() {
        if (coyoteFrames > 0) coyoteFrames--;
        if (jumpBufferFrames > 0) jumpBufferFrames--;

        if (speedBoostTimer > 0) speedBoostTimer--;
        if (jumpBoostTimer > 0) jumpBoostTimer--;
        if (freezeTimer > 0) freezeTimer--;
    }

    public boolean canMove() {
        return freezeTimer <= 0;
    }

    public int getMoveSpeed(int baseSpeed) {
        if (speedBoostTimer > 0) return baseSpeed + 3;
        return baseSpeed;
    }

    public int getJumpPower(int baseJump) {
        if (jumpBoostTimer > 0) return baseJump + 4;
        return baseJump;
    }

    public void move(ArrayList<Rectangle> platforms, ArrayList<MovingPlatform> movingPlatforms, int worldWidth, int worldHeight, int respawnY) {
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

        for (MovingPlatform platform : movingPlatforms) {
            Rectangle r = platform.getRect();
            if (playerRect.intersects(r)) {
                if (vx > 0) {
                    x = r.x - width;
                } else if (vx < 0) {
                    x = r.x + r.width;
                }
                playerRect = getRect();
            }
        }

        y += vy;
        boolean wasOnGround = onGround;
        onGround = false;
        playerRect = getRect();

        for (Rectangle platform : platforms) {
            if (playerRect.intersects(platform)) {
                if (vy > 0) {
                    y = platform.y - height;
                    vy = 0;
                    onGround = true;
                } else if (vy < 0) {
                    y = platform.y + platform.height;
                    vy = 0;
                }
                playerRect = getRect();
            }
        }

        for (MovingPlatform platform : movingPlatforms) {
            Rectangle r = platform.getRect();
            if (playerRect.intersects(r)) {
                if (vy > 0) {
                    y = r.y - height;
                    vy = 0;
                    onGround = true;
                    x += platform.speed * platform.dir;
                } else if (vy < 0) {
                    y = r.y + r.height;
                    vy = 0;
                }
                playerRect = getRect();
            }
        }

        if (onGround) {
            coyoteFrames = 6;
        } else if (wasOnGround) {
            coyoteFrames = 6;
        }

        if (x < 0) x = 0;
        if (x + width > worldWidth) x = worldWidth - width;

        if (y > worldHeight) {
            y = respawnY;
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
            int border1 = 3;
            g.drawRect(x - border1, y - border1, width + 2 * border1, height + 2 * border1);
            g.drawRect(x - border1 - 1, y - border1 - 1, width + 2 * (border1 + 1), height + 2 * (border1 + 1));
        }

        drawCrownStack(g);
    }

    private void drawCrownStack(Graphics g) {
        if (winStreak <= 0) return;

        int crownWidth = width;
        int crownHeight = 10;
        int startY = y - 14;

        for (int i = 0; i < winStreak; i++) {
            int crownY = startY - i * 12;
            g.setColor(new Color(255, 215, 0));
            int[] xs = {x, x + 8, x + width / 2, x + width - 8, x + width};
            int[] ys = {crownY + crownHeight, crownY, crownY + crownHeight, crownY, crownY + crownHeight};
            g.fillPolygon(xs, ys, 5);

            g.setColor(Color.BLACK);
            g.drawPolygon(xs, ys, 5);
        }
    }
}
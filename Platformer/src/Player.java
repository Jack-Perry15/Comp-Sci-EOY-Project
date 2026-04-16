import java.awt.*;
import java.util.ArrayList;

public class Player {
    int x, y, width, height;
    double vx, vy;

    boolean onGround;
    boolean isIt;
    boolean facingRight;

    Color color;

    int jumpBufferFrames;

    int speedBoostTimer;
    int jumpBoostTimer;
    int freezeTimer;
    int coyotePowerTimer;
    int coyoteFramesLeft;

    int tagsThisRound;
    int roundsWon;
    int winStreak;

    boolean jumpHeld;
    int jumpHoldFrames;
    int maxJumpHoldFrames;

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
        facingRight = true;

        jumpBufferFrames = 0;

        speedBoostTimer = 0;
        jumpBoostTimer = 0;
        freezeTimer = 0;
        coyotePowerTimer = 0;
        coyoteFramesLeft = 0;

        tagsThisRound = 0;
        roundsWon = 0;
        winStreak = 0;

        jumpHeld = false;
        jumpHoldFrames = 0;
        maxJumpHoldFrames = 18;
    }

    public void applyGravity(double gravity, int maxVy) {
        vy += gravity;
        if (vy > maxVy) {
            vy = maxVy;
        }
    }

    public void updateTimers() {
        if (jumpBufferFrames > 0) jumpBufferFrames--;

        if (speedBoostTimer > 0) speedBoostTimer--;
        if (jumpBoostTimer > 0) jumpBoostTimer--;
        if (freezeTimer > 0) freezeTimer--;
        if (coyotePowerTimer > 0) coyotePowerTimer--;

        if (coyoteFramesLeft > 0) coyoteFramesLeft--;
    }

    public boolean canMove() {
        return freezeTimer <= 0;
    }

    public int getMoveSpeed(int baseSpeed) {
        if (speedBoostTimer > 0) return baseSpeed + 3;
        return baseSpeed;
    }

    public int getJumpPower(int baseJump) {
        if (jumpBoostTimer > 0) return baseJump + 3;
        return baseJump;
    }

    public boolean canUseCoyoteJump() {
        return coyotePowerTimer > 0 && coyoteFramesLeft > 0;
    }

    public void move(ArrayList<Rectangle> platforms, ArrayList<MovingPlatform> movingPlatforms, int worldWidth, int worldHeight, int respawnY) {
        x += (int)Math.round(vx);
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

        boolean wasOnGround = onGround;

        y += (int)Math.round(vy);
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
                    x += platform.getDx();
                } else if (vy < 0) {
                    y = r.y + r.height;
                    vy = 0;
                }
                playerRect = getRect();
            }
        }

        if (coyotePowerTimer > 0) {
            if (onGround) {
                coyoteFramesLeft = 6;
            } else if (wasOnGround) {
                coyoteFramesLeft = 6;
            }
        } else {
            coyoteFramesLeft = 0;
        }

        if (onGround) {
            jumpHoldFrames = 0;
        }

        if (x < 0) x = 0;
        if (x + width > worldWidth) x = worldWidth - width;

        if (y > worldHeight) {
            y = respawnY;
            vy = 0;
            jumpHoldFrames = 0;
            jumpHeld = false;
            coyoteFramesLeft = 0;
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);

        drawFace(g);

        if (isIt) {
            g.setColor(Color.YELLOW);
            int border1 = 3;
            g.drawRect(x - border1, y - border1, width + 2 * border1, height + 2 * border1);
            g.drawRect(x - border1 - 1, y - border1 - 1, width + 2 * (border1 + 1), height + 2 * (border1 + 1));
        }

        drawCrownStack(g);
    }

    private void drawFace(Graphics g) {
        g.setColor(Color.BLACK);

        double maxVisualSpeedX = 8.0;
        double maxVisualSpeedY = 12.0;

        double normalizedX = Math.max(-1, Math.min(1, vx / maxVisualSpeedX));
        double normalizedY = Math.max(-1, Math.min(1, vy / maxVisualSpeedY));

        int maxShiftX = width / 4;
        int maxShiftY = height / 4;

        int shiftX = (int)Math.round(normalizedX * maxShiftX);
        int shiftY = (int)Math.round(normalizedY * maxShiftY);

        int faceCenterX = x + width / 2 + shiftX;
        int faceCenterY = y + height / 2 + shiftY;

        int eyeSize = Math.max(4, width / 10);
        int eyeSpacing = width / 6;

        int leftEyeX = faceCenterX - eyeSpacing - eyeSize / 2;
        int rightEyeX = faceCenterX + eyeSpacing - eyeSize / 2;
        int eyeY = faceCenterY - height / 8;

        g.fillOval(leftEyeX, eyeY, eyeSize, eyeSize);
        g.fillOval(rightEyeX, eyeY, eyeSize, eyeSize);

        Graphics2D g2 = (Graphics2D) g;
        int mouthWidth = width / 4;
        int mouthHeight = height / 7;
        int mouthX = faceCenterX - mouthWidth / 2;
        int mouthY = faceCenterY + height / 14;

        g2.drawArc(mouthX, mouthY, mouthWidth, mouthHeight, 200, 140);
    }

    private void drawCrownStack(Graphics g) {
        if (winStreak <= 0) return;

        int startY = y - 14;

        for (int i = 0; i < winStreak; i++) {
            int crownY = startY - i * 12;

            int[] xs = {
                x,
                x + width / 6,
                x + width / 3,
                x + width / 2,
                x + (2 * width) / 3,
                x + (5 * width) / 6,
                x + width
            };

            int[] ys = {
                crownY + 10,
                crownY,
                crownY + 10,
                crownY - 2,
                crownY + 10,
                crownY,
                crownY + 10
            };

            g.setColor(new Color(255, 215, 0));
            g.fillPolygon(xs, ys, 7);

            g.setColor(Color.BLACK);
            g.drawPolygon(xs, ys, 7);

            g.setColor(new Color(255, 235, 120));
            g.fillOval(x + width / 2 - 2, crownY + 1, 5, 5);
        }
    }
}
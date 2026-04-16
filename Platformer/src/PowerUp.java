import java.awt.*;

public class PowerUp {
    int x, y, width, height;
    String type;
    Color color;
    int duration;

    public PowerUp(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.width = 28;
        this.height = 28;
        this.type = type;

        if (type.equals("speed")) {
            color = Color.GREEN;
            duration = 300;
        } else if (type.equals("jump")) {
            color = Color.MAGENTA;
            duration = 300;
        } else if (type.equals("freeze")) {
            color = Color.CYAN;
            duration = 180;
        } else {
            color = Color.ORANGE; // coyote / wolf time
            duration = 360;
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, width, height);

        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);

        if (type.equals("coyote")) {
            g.drawString("W", x + 8, y + 18);
        }
    }
}
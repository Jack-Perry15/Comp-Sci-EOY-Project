import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private static final int WIDTH = 1800;
    private static final int HEIGHT = 900;

    public Main() {
        super("Comp Sci EOY Project");

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Game play = new Game();
        play.setFocusable(true);

        getContentPane().add(play);
        setVisible(true);

        play.requestFocusInWindow();
    }

    public static void main(String[] args) {
        new Main();
    }
}
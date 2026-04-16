import java.awt.*;
import javax.swing.*;

public class Main extends JFrame {
    private static final int WIDTH = 1800;
    private static final int HEIGHT = 900;

    public Main() {
        super("Comp Sci EOY Project");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        Game play = new Game();
        play.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        play.setFocusable(true);

        getContentPane().add(play);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(play::requestFocusInWindow);
    }

    public static void main(String[] args) {
        new Main();
    }
}
package red.felnull.ikenaichip8test;

import red.felnull.ikenaichip8.Chip8;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Test {
    public static void main(String[] args) throws Exception {
        Chip8 chip8 = new Chip8();
        C8Frame frame = new C8Frame(chip8);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 64 * 10 + 15;
        int h = 32 * 10 + 37;
        frame.setBounds(d.width / 2 - w / 2, d.height / 2 - h / 2, w, h);
        chip8.setCycleSpeed(10);
        C8Canvas canvas = new C8Canvas(chip8);
        frame.add(canvas);
        canvas.setSize(w, h);
        //  chip8.setNonThrow(true);
        byte[] data;
        URL url = new URL("https://github.com/kripod/chip8-roms/blob/master/games/Cave.ch8?raw=true");
        data = inputStreamToByteArray(url.openStream());
        chip8.loadROM(data);
        chip8.runAsync();

        chip8.addCycleListener(c8 -> canvas.repaint());
        chip8.addCycleListener(n -> {
            if (n.isBeeping())
                java.awt.Toolkit.getDefaultToolkit().beep();
        });

    }

    public static byte[] inputStreamToByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int len = stream.read(buffer);
            if (len < 0) {
                break;
            }
            bout.write(buffer, 0, len);
        }
        return bout.toByteArray();
    }

    private static class C8Frame extends JFrame implements KeyListener, MouseListener {
        private final Chip8 chip8;

        private C8Frame(Chip8 chip8) {
            this.chip8 = chip8;
            this.addKeyListener(this);
            this.addMouseListener(this);
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            char key = e.getKeyChar();
            if (key == '1') chip8.pressKey(0);
            else if (key == '2') chip8.pressKey(1);
            else if (key == '3') chip8.pressKey(2);
            else if (key == '4') chip8.pressKey(3);

            else if (key == 'q') chip8.pressKey(4);
            else if (key == 'w') chip8.pressKey(5);
            else if (key == 'e') chip8.pressKey(6);
            else if (key == 'r') chip8.pressKey(7);

            else if (key == 'a') chip8.pressKey(8);
            else if (key == 's') chip8.pressKey(9);
            else if (key == 'd') chip8.pressKey(10);
            else if (key == 'f') chip8.pressKey(11);

            else if (key == 'z') chip8.pressKey(12);
            else if (key == 'x') chip8.pressKey(13);
            else if (key == 'c') chip8.pressKey(14);
            else if (key == 'v') chip8.pressKey(15);
            else if (key == 'h') chip8.stop();
            else if (key == 'j') chip8.runAsync();
            else if (key == 'k') chip8.reset();
            else if (key == 'i') chip8.setCycleSpeed(chip8.getCycleSpeed() / 2);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            char key = e.getKeyChar();
            if (key == '1') chip8.releaseKey(0);
            else if (key == '2') chip8.releaseKey(1);
            else if (key == '3') chip8.releaseKey(2);
            else if (key == '4') chip8.releaseKey(3);

            else if (key == 'q') chip8.releaseKey(4);
            else if (key == 'w') chip8.releaseKey(5);
            else if (key == 'e') chip8.releaseKey(6);
            else if (key == 'r') chip8.releaseKey(7);

            else if (key == 'a') chip8.releaseKey(8);
            else if (key == 's') chip8.releaseKey(9);
            else if (key == 'd') chip8.releaseKey(10);
            else if (key == 'f') chip8.releaseKey(11);

            else if (key == 'z') chip8.releaseKey(12);
            else if (key == 'x') chip8.releaseKey(13);
            else if (key == 'c') chip8.releaseKey(14);
            else if (key == 'v') chip8.releaseKey(15);
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            //    int xp = getWidth() / 64 + 1;
            //    int yp = getHeight() / 32 + 1;

            float xScale = (float) getWidth() / 640f;
            float yScale = (float) getHeight() / 320f;
            int x = (e.getX() - 10) / (int) (10 * xScale);
            int y = (e.getY() - 30) / (int) (10 * yScale);

            chip8.graphic[(y * 64) + x] = e.getButton() != 1;
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    private static class C8Canvas extends JPanel {
        private final Chip8 chip8;

        private C8Canvas(Chip8 chip8) {
            this.chip8 = chip8;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            int xp = getWidth() / 64 + 1;
            int yp = getHeight() / 32 + 1;

            for (int y = 0; y < 32; ++y) {
                for (int x = 0; x < 64; ++x) {
                    if (chip8.graphic[(y * 64) + x]) {
                        g.fillRect(x * xp, y * yp, xp, yp);
                    }
                }
            }
        }
    }
}

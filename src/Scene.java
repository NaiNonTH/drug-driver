import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

public class Scene extends JPanel {
    int offset = 0;

    boolean gameStarted = false;
    boolean gamePaused = false;
    boolean gameOver = false;
    
    Thread paint = new Paint();
    Thread entityModifier = new EntityModifier();
    Thread spawnObstacles = new SpawnObstacles();
    Thread countdown = new Countdown();
    
    Truck truck = new Truck();
    
    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    public Scene() {
        setFocusable(true);

        addMouseMotionListener(new MovingTruck());
        addMouseListener(new StartAndPauseGame());
    }

    int waterSize = 96;
    int roadSize = 128;
    
    URL waterUrl = getClass().getResource("assets/textures/water.png");

    public void drawWater(Graphics g) {
        Image waterImage = new ImageIcon(waterUrl).getImage();

        for (int y = (offset / 16) % waterSize - waterSize; y < getHeight(); y += waterSize) {
            for (int x = 0; x < getWidth(); x += waterSize) {
                g.drawImage(waterImage, x, y, waterSize, waterSize, this);
            }
        }
    }
    
    URL roadLeftUrl = getClass().getResource("assets/textures/road-left.png");
    URL roadRightUrl = getClass().getResource("assets/textures/road-right.png");

    public void drawRoad(Graphics g) {
        Image roadLeftImage = new ImageIcon(roadLeftUrl).getImage();
        Image roadRightImage = new ImageIcon(roadRightUrl).getImage();

        for (int y = offset % roadSize - roadSize; y < getHeight(); y += roadSize) {
            g.drawImage(roadLeftImage, getWidth() / 2 - roadSize, y, roadSize, roadSize, this);
            g.drawImage(roadRightImage, getWidth() / 2, y, roadSize, roadSize, this);
        }
    }

    public void drawTruck(Graphics g) {
        truck.y = getHeight() - 128;

        URL truckUrl = getClass().getResource("assets/textures/truck.png");
        Image truckImage = new ImageIcon(truckUrl).getImage();

        g.drawImage(truckImage, truck.x, truck.y, truck.getWidth(), truck.getHeight(), this);

        if (gameOver) {
            URL explosionUrl = getClass().getResource("assets/textures/explosion.png");
            Image explosionImage = new ImageIcon(explosionUrl).getImage();

            g.drawImage(explosionImage, truck.getX() + truck.getOffset(), getHeight() - 160, truck.getHeight(), truck.getHeight(), this);
        }
    }

    public void drawObstacles(Graphics g) {
        for (int obstacleIndex = 0; obstacleIndex < obstacles.size(); ++obstacleIndex) {
            Obstacle obstacle = obstacles.get(obstacleIndex);

            if (obstacle instanceof Barrier) {
                URL barrierUrl = getClass().getResource("assets/textures/barrier.png");
                Image barrierImage = new ImageIcon(barrierUrl).getImage();

                if (obstacle.y > getHeight()) {
                    obstacles.remove(obstacleIndex);
                    --obstacleIndex;
                }

                g.drawImage(barrierImage, obstacle.getX(), obstacle.y, obstacle.getWidth(), obstacle.getHeight(), this);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        drawWater(g);
        drawRoad(g);
        drawObstacles(g);
        
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        FontMetrics metrics = g.getFontMetrics();
        
        if (!gameStarted || gamePaused) {
            g.setColor(new Color(255, 255, 0));
            g.fillRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            g.setColor(new Color(118, 111, 0));
            g.drawRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            String startString = "START";
            g.drawString(startString, getWidth() / 2 - metrics.stringWidth(startString) / 2, getHeight() / 2 - 20 + metrics.getHeight());

            if (gamePaused) {
                String pauseString = "PAUSED";
                g.drawString(pauseString, getWidth() / 2 - metrics.stringWidth(pauseString) / 2, getHeight() / 2 - 32 - metrics.getHeight() / 2);
            }
            else
                truck.x = getWidth() / 2 - 32;
        }
        else if (gameOver) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", 10, 10 + g.getFontMetrics().getHeight());
        }

        drawTruck(g);

        String timeString = String.valueOf(truck.time);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        metrics = g.getFontMetrics();

        g.setColor(Color.BLACK);
        g.drawString(timeString, getWidth() / 2 - metrics.stringWidth(timeString) / 2, 8 + metrics.getHeight());
    }

    class MovingTruck implements MouseMotionListener {
        public void moveTruck(MouseEvent e) {
            if (!gameStarted || gamePaused || gameOver) return;

            truck.x = e.getX() + truck.getOffset();

            if (
                gameStarted &&
                !gamePaused &&
                (
                    truck.x < getWidth() / 2 - roadSize + truck.getOffset() ||
                    truck.x > getWidth() / 2 + roadSize + truck.getOffset()
                )
            ) {
                gameOver = true;
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            moveTruck(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            moveTruck(e);
        }
    }

    class StartAndPauseGame implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (
                !gameOver &&
                e.getButton() == MouseEvent.BUTTON1 &&
                x >= getWidth() / 2 - 112 &&
                x <= getWidth() / 2 + 128 &&
                y >= getHeight() / 2 - 32 &&
                y <= getHeight() / 2 + 32
            ) {
                gameStarted = true;
                gamePaused = false;

                if (!paint.isAlive())
                    paint.start();

                if (!spawnObstacles.isAlive())
                    spawnObstacles.start();

                if (!entityModifier.isAlive())
                    entityModifier.start();

                if (!countdown.isAlive())
                    countdown.start();
            }
            else if (
                gameStarted &&
                e.getButton() == MouseEvent.BUTTON3
            ) {
                gamePaused = true;
            }
        }
    }
    
    class Paint extends Thread {
        @Override
        public void run() {
            while (!gameOver) {
                repaint();
                
                try {
                    Thread.sleep(0, 10);
                } catch (InterruptedException e) {}
            }
        }
    }

    class SpawnObstacles extends Thread {
        public int randomTime() {
            return (int) Math.round(Math.random() * (2000 - 750) + 750);
        }

        @Override
        public void run() {
            try {
                sleep(3250);
            } catch (InterruptedException e) {}

            while (gameStarted) {
                if (!gamePaused) {
                    obstacles.add(new Barrier((int) Math.round(Math.random()), getWidth(), roadSize, false));
                }
                
                try {
                    sleep(randomTime());
                } catch (InterruptedException e) {}
            }
        }
    }
    
    class EntityModifier extends Thread {
        @Override
        public void run() {
            while (!gameOver) {
                try {
                    sleep(0, 10);
                } catch (InterruptedException e) {}

                if (gamePaused) continue;

                offset += truck.speed;
    
                for (Obstacle obstacle : obstacles) {
                    obstacle.y += truck.speed;
                    
                    if (obstacle.isCollidedWith(truck) && obstacle.onCollided() == 0) {
                        gameOver = true;
                        repaint();
                    }
                }
            }
        }
    }

    class Countdown extends Thread {
        @Override
        public void run() {
            while (!gameOver && truck.time > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {}
                
                --truck.time;
            }

            gameOver = true;
            repaint();
        }
    }
}

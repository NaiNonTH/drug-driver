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
    Thread spawnObstacles = new SpawnObstacles();
    Thread moveObjects = new MoveObjects();
    Thread checkCollision = new CheckCollision();

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

    Truck truck = new Truck();

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

    ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawWater(g);
        drawRoad(g);
        drawObstacles(g);
        
        if (!gameStarted || gamePaused) {
            g.setColor(new Color(255, 255, 0));
            g.fillRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            g.setColor(new Color(118, 111, 0));
            g.drawRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);
            
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            FontMetrics metrics = g.getFontMetrics();

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
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            g.drawString("GAME OVER", 10, 10 + g.getFontMetrics().getHeight());
        }

        drawTruck(g);
    }

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

    class MovingTruck implements MouseMotionListener {
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

                if (!moveObjects.isAlive())
                    moveObjects.start();

                if (!checkCollision.isAlive())
                    checkCollision.start();
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
    
    class MoveObjects extends Thread {
        @Override
        public void run() {
            while (!gameOver) {
                if (!gamePaused) {
                    ++offset;
                    
                    for (Obstacle obstacle : obstacles) {
                        ++obstacle.y;
                    }
                }
                    
                try {
                    sleep(0, 10);
                } catch (InterruptedException e) {}
            }
        }
    }

    class CheckCollision extends Thread {
        @Override
        public void run() {
            while (gameOver == false) {
                for (Obstacle obstacle : obstacles) {
                    try {
                        if (obstacle.isCollidedWith(truck)) {
                            gameOver = true;
                            repaint();
                        }
                    } catch (NullPointerException e) {}
                }

                try { // Add this to allow the thread to keep running, despite it has no task to do.
                    sleep(0);
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
                    obstacles.add(new Barrier(0, (int) Math.round(Math.random()), getWidth(), roadSize, false));
                }
                
                try {
                    sleep(randomTime());
                } catch (InterruptedException e) {}
            }
        }
    }
}

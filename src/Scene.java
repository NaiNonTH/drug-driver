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

    int pausePosX;
    int pausePosY;
    
    Thread paint = new Paint();
    Thread entityModifier = new EntityModifier();
    Thread spawnEntities = new SpawnEntities();
    Thread countdown = new Countdown();
    
    Truck truck = new Truck();
    
    ArrayList<Entity> entities = new ArrayList<Entity>();

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
        for (int obstacleIndex = 0; obstacleIndex < entities.size(); ++obstacleIndex) {
            Entity obstacle = entities.get(obstacleIndex);

            URL textureUrl = getClass().getResource("assets/textures/" + obstacle.getName() + ".png");
            Image textureImage;

            try {
                textureImage = new ImageIcon(textureUrl).getImage();
            }
            catch (NullPointerException e) {
                textureImage = new ImageIcon("assets/textures/fallback.png").getImage();
            }

            g.drawImage(textureImage, obstacle.getX(), obstacle.y, obstacle.getWidth(), obstacle.getHeight(), this);
            
            if (obstacle.y > getHeight()) {
                entities.remove(obstacleIndex);
                --obstacleIndex;
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
        
        if (!gameStarted) {
            g.setColor(new Color(255, 255, 0));
            g.fillRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            g.setColor(new Color(118, 111, 0));
            g.drawRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            String startString = "START";
            g.drawString(startString, getWidth() / 2 - metrics.stringWidth(startString) / 2, getHeight() / 2 - 20 + metrics.getHeight());
            truck.x = getWidth() / 2 - 32;
        }
        else if (gamePaused) {
            String pauseString = "PAUSED";

            g.setColor(new Color(118, 111, 0));
            g.drawString(pauseString, getWidth() / 2 - metrics.stringWidth(pauseString) / 2, getHeight() / 2 - 32 - metrics.getHeight() / 2);

            g.setColor(Color.YELLOW);
            g.fillOval(pausePosX - 10, pausePosY - 10, 20, 20);
        }
        else if (gameOver) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", 10, 10 + g.getFontMetrics().getHeight());
        }

        drawTruck(g);

        String timeString = String.valueOf((int) Math.ceil(truck.time));

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
        public void mousePressed(MouseEvent e) {
            if (
                gameStarted &&
                !gamePaused &&
                e.getButton() == MouseEvent.BUTTON1
            ) {
                truck.setFloatingTo(true);
                truck.x = e.getX() + truck.getOffset();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (
                !gameOver &&
                e.getButton() == MouseEvent.BUTTON1
            ) {
                if (
                    !gameStarted &&
                    x >= getWidth() / 2 - 112 &&
                    x <= getWidth() / 2 + 128 &&
                    y >= getHeight() / 2 - 32 &&
                    y <= getHeight() / 2 + 32
                ) {
                    gameStarted = true;
                    
                    paint.start();
                    spawnEntities.start();
                    entityModifier.start();
                    countdown.start();
                }
                else if (
                    gamePaused &&
                    x >= pausePosX - 10 &&
                    x <= pausePosX + 10 &&
                    y >= pausePosY - 10 &&
                    y <= pausePosY + 10
                ) {
                    gamePaused = false;
                }
                else if (
                    gameStarted &&
                    !gamePaused
                ) {
                    truck.setFloatingTo(false);
                    truck.x = e.getX() + truck.getOffset();
                }
            }
            else if (
                gameStarted &&
                !gamePaused &&
                e.getButton() == MouseEvent.BUTTON3
            ) {
                pausePosX = x;
                pausePosY = y;

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
                    sleep(0, 10);
                } catch (InterruptedException e) {}
            }
        }
    }

    class SpawnEntities extends Thread {
        public boolean span() {
            return Math.round(Math.random()) == 1;
        }
        public int randomTime() {
            return (int) Math.round(Math.random() * (2000 - 750) + 750);
        }
        public int randomSlot() {
            return (int) Math.round(Math.random() * 2);
        }
        public int randomEntityType() {
            return (int) Math.round(Math.random() * 2);
        }
        public void spawnEntity(int type, int slot) {
            switch (type) {
                case 0:
                    entities.add(new Barrier(slot, getWidth(), roadSize));
                    break;
                case 1:
                    entities.add(new Rice(slot, getWidth(), roadSize));
                    break;
            }
        }

        @Override
        public void run() {
            try {
                sleep(3250);
            } catch (InterruptedException e) {}

            while (gameStarted) {
                if (!gamePaused) {
                    int type = randomEntityType();
                    int slot = randomSlot();

                    spawnEntity(type, slot);
                    
                    if (span() && offset > 5000) {
                        spawnEntity(type, (slot + 1) % 2);
                    }
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
    
                for (Entity entity : entities) {
                    entity.y += truck.speed;

                    if (entity.isCollidedWith(truck)) {
                        if (entity instanceof Obstacle) {
                            Obstacle obstacle = (Obstacle) entity;
                            
                            if (
                                (!truck.isFloating() || obstacle.isTall()) &&
                                obstacle.onCollided() == 0
                            ) {
                                gameOver = true;
                                repaint();
                            }
                        }
                        else {
                            entity.onCollided();
                        }
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
                    sleep(100);
                } catch (InterruptedException e) {}
                
                if (gamePaused) continue;
                
                truck.time -= 0.1;
            }

            gameOver = true;
            repaint();
        }
    }
}

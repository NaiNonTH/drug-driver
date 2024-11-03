import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

public class Scene extends JPanel {
    double offset = 0;

    boolean gameStarted = false;
    boolean gamePaused = false;
    boolean gameOver = false;

    int pausePosX;
    int pausePosY;

    int mousePosX;
    int mousePosY;
    
    Thread modifyAndPaint = new ModifyAndPaint();
    Thread spawnEntities = new SpawnEntities();
    Thread countdown = new Countdown();
    Thread depleteStamina = new DepleteStamina();
    
    Truck truck = new Truck();
    
    ArrayList<Entity> entities = new ArrayList<Entity>();

    public Scene() {
        setFocusable(true);

        addMouseMotionListener(new MovingTruck());
        addMouseListener(new ClickListener());
    }

    int waterSize = 96;
    int roadSize = 128;
    
    URL waterUrl = getClass().getResource("assets/textures/water.png");
    
    public void drawWater(Graphics g) {
        Image waterImage = new ImageIcon(waterUrl).getImage();
        
        for (int y = (int)(offset / 16) % waterSize - waterSize; y < getHeight(); y += waterSize) {
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

        for (int y = (int) offset % roadSize - roadSize; y < getHeight(); y += roadSize) {
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

            g.drawImage(explosionImage, truck.x + truck.getOffset(), getHeight() - 160, truck.getHeight(), truck.getHeight(), this);
        }
    }

    public void drawEntities(Graphics g) {
        for (int entityIndex = 0; entityIndex < entities.size(); ++entityIndex) {
            Entity entity = entities.get(entityIndex);
            
            if (
                entity instanceof Oil &&
                ((Oil) entity).isCollected
            ) continue;

            URL textureUrl = getClass().getResource("assets/textures/" + entity.getName() + ".png");
            Image textureImage;

            try {
                textureImage = new ImageIcon(textureUrl).getImage();
            }
            catch (NullPointerException e) {
                textureImage = new ImageIcon("assets/textures/fallback.png").getImage();
            }

            g.drawImage(textureImage, entity.getX(), (int) entity.y, entity.getWidth(), entity.getHeight(), this);
            
            if (entity.y > getHeight()) {
                entities.remove(entityIndex);
                --entityIndex;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        drawWater(g);
        drawRoad(g);
        drawEntities(g);
        
        // Render UI
        
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
            String gameOverString = "GAME OVER";
            String scoreString = "Score: " + (int) offset;

            g.setColor(new Color(255, 255, 255, 196));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            g.drawString(gameOverString, getWidth() / 2 - metrics.stringWidth(gameOverString) / 2, getHeight() / 2 + metrics.getHeight() / 2 - 20);
            g.drawString(scoreString, getWidth() / 2 - metrics.stringWidth(scoreString) / 2, getHeight() / 2 + metrics.getHeight() / 2 + 20);
        }

        //////////

        drawTruck(g);

        // Draw time

        String timeString = String.valueOf((int) Math.ceil(truck.time));

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        metrics = g.getFontMetrics();

        g.setColor(Color.BLACK);
        g.drawString(timeString, getWidth() / 2 - metrics.stringWidth(timeString) / 2, 8 + metrics.getHeight());

        //////////

        // Draw Stamina bar

        if (truck.stamina < 10 && !gameOver && !gamePaused) {
            g.setColor(Color.GREEN);
            g.fillRect(mousePosX, mousePosY - 10, (int) Math.ceil(truck.stamina * 10), 4);
        }

        //////////
    }

    class MovingTruck implements MouseMotionListener {
        public void moveTruck(MouseEvent e) {
            if (!gameStarted || gamePaused || gameOver) return;

            truck.x = e.getX() + truck.getOffset();

            mousePosX = e.getX();
            mousePosY = e.getY();

            if (
                !truck.isFloating() &&
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

    class ClickListener implements MouseListener {
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
                    
                    spawnEntities.start();
                    modifyAndPaint.start();
                    countdown.start();
                    depleteStamina.start();
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

                repaint();
            }
        }
    }

    class SpawnEntities extends Thread {
        public boolean random() {
            return Math.round(Math.random()) == 1;
        }
        public int random(int range) {
            return (int) Math.round(Math.random() * range);
        }
        public int random(int start, int end) {
            return (int) Math.round(Math.random() * (end - start) + start);
        }
        public void spawnEntity(int type, int slot) {
            switch (type) {
                case 0:
                    entities.add(new Barrier(slot, getWidth(), roadSize));
                    break;
                case 1:
                    entities.add(new Cone(slot, getWidth(), roadSize));
                    break;
                case 2:
                    entities.add(new Rice(slot, getWidth(), roadSize));
                    break;
                case 3:
                    entities.add(new Hole(slot, getWidth(), roadSize));
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
                    int type = random((int) Math.min(3, (int) offset / 7500));
                    int slot = random(2);
                    boolean spanned = random();
                    boolean spawnOil = random();
                    
                    spawnEntity(type, slot);
                    
                    if (spanned && offset > 7500)
                        spawnEntity(type, (slot + 1) % 2);
                    else if (spawnOil && truck.time < 48)
                        entities.add(new Oil((slot + 1) % 2, getWidth(), roadSize));
                }

                int time = random((int)(2000 / truck.speed), (int)(750 / (1.5 * truck.speed)));
                
                try {
                    sleep(time);
                } catch (InterruptedException e) {}
            }
        }
    }
    
    class ModifyAndPaint extends Thread {
        @Override
        public void run() {
            while (!gameOver) {
                repaint();

                if (!gamePaused) {
                    offset += truck.speed;
        
                    for (int entityIndex = 0; entityIndex < entities.size(); ++entityIndex) {
                        Entity entity = entities.get(entityIndex);
    
                        entity.y += truck.speed;
    
                        if (
                            entity.isCollidedWith(truck) &&
                            !truck.isFloating() &&
                            entity.onCollided(truck) == 0
                        ) {
                            gameOver = true;
                            repaint();
                        }
                    }

                    try {
                        sleep(0, 10);
                    } catch (InterruptedException e) {}
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

    class DepleteStamina extends Thread {
        @Override
        public void run() {
            while (!gameOver) {
                try {
                    if (truck.isFloating()) {
                        truck.stamina -= 0.1;
    
                        if (truck.stamina <= 0)
                            truck.setFloatingTo(false);    
                        
                        sleep(50);
                    }
                    else if (truck.stamina < 10) {
                        truck.stamina += 0.1;
                        
                        sleep(250);
                    }
                    else
                        sleep(0);
                } catch (InterruptedException e) {}
            }
        }
    }
}

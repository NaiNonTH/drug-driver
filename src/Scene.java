import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.*;

public class Scene extends JPanel {
    double offset = 0;

    int gameStatus = 0;

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
    
    public void drawWater(Graphics g) {
        URL waterUrl;
        
        for (int y = (int)(offset / 16) % waterSize - waterSize; y < getHeight(); y += waterSize) {
            waterUrl =
                (offset / 16) - y >= (50000 / 16)
                  ? getClass().getResource("assets/textures/field-water.png")
                  : getClass().getResource("assets/textures/water.png");
                  
            Image waterImage = new ImageIcon(waterUrl).getImage();
            
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

        if (gameStatus == 2) {
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
        
        if (gameStatus == 0) {
            g.setColor(new Color(255, 255, 0));
            g.fillRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            g.setColor(new Color(118, 111, 0));
            g.drawRect(getWidth() / 2 - 120, getHeight() / 2 - 32, 240, 64);

            String startString = "START";
            g.drawString(startString, getWidth() / 2 - metrics.stringWidth(startString) / 2, getHeight() / 2 - 20 + metrics.getHeight());
            truck.x = getWidth() / 2 - 32;
        }
        else if (gameStatus == 2) {
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

        String timeString = String.valueOf(truck.time);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        metrics = g.getFontMetrics();

        g.setColor(Color.BLACK);
        g.drawString(timeString, getWidth() / 2 - metrics.stringWidth(timeString) / 2, 8 + metrics.getHeight());

        //////////

        // Draw Stamina bar

        if (truck.stamina < 10 && gameStatus != 2) {
            g.setColor(Color.GREEN);
            g.fillRect(mousePosX, mousePosY - 10, (int) Math.ceil(truck.stamina * 10), 4);
        }

        //////////
    }

    class MovingTruck implements MouseMotionListener {
        public void moveTruck(MouseEvent e) {
            if (gameStatus != 1) return;

            truck.x = e.getX() + truck.getOffset();

            mousePosX = e.getX();
            mousePosY = e.getY();

            if (
                !truck.isFloating() &&
                gameStatus == 1 &&
                (
                    truck.x < getWidth() / 2 - roadSize + truck.getOffset() ||
                    truck.x > getWidth() / 2 + roadSize + truck.getOffset()
                )
            ) {
                gameStatus = 2;
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
                gameStatus == 1 &&
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
                gameStatus != 2 &&
                e.getButton() == MouseEvent.BUTTON1
            ) {
                if (
                    gameStatus == 0 &&
                    x >= getWidth() / 2 - 112 &&
                    x <= getWidth() / 2 + 128 &&
                    y >= getHeight() / 2 - 32 &&
                    y <= getHeight() / 2 + 32
                ) {
                    gameStatus = 1;
                    
                    spawnEntities.start();
                    modifyAndPaint.start();
                    countdown.start();
                    depleteStamina.start();
                }
                else if (gameStatus == 1) {
                    truck.setFloatingTo(false);
                    truck.x = e.getX() + truck.getOffset();
                }
            }
        }
    }

    class SpawnEntities extends Thread {
        public boolean random() {
            return Math.round(Math.random()) == 1;
        }
        public int random(int range) {
            return (int) Math.floor(Math.random() * range);
        }
        public final void spawnEntity(int type, boolean useSlotRight) {
            switch (type) {
                case 0:
                    entities.add(new Barrier(useSlotRight, getWidth(), roadSize));
                    break;
                case 1:
                    entities.add(new Cone(useSlotRight, getWidth(), roadSize));
                    break;
                case 2:
                    entities.add(new Rice(useSlotRight, getWidth(), roadSize));
                    break;
                case 3:
                    entities.add(new Hole(useSlotRight, getWidth(), roadSize));
                    break;
            }
        }

        @Override
        public void run() {
            try {
                sleep(3250);
            } catch (InterruptedException e) {}

            while (gameStatus != 2) {
                int type = random(offset >= 50000 ? 4 : 2);
                int spanned = random(2);
                boolean useSlotRight = random();
                boolean spawnOil = random();
                
                spawnEntity(type, useSlotRight);
                
                if (spanned > 0 && offset > 15000) {
                    switch (spanned) {
                        case 1:
                            spawnEntity(type, !useSlotRight);
                            break;
                        case 2:
                            type = random(offset >= 50000 ? 4 : 2);
                            spawnEntity(type, !useSlotRight);
                            break;
                    }
                }
                else if (spawnOil && truck.time < 48)
                    entities.add(new Oil(!useSlotRight, getWidth(), roadSize));

                int randTimeBegin = (int)(750 / Math.pow(truck.speed, 2));
                int randTimeEnd = (int)(1500 / Math.pow(truck.speed, 2));
                
                try {
                    sleep(ThreadLocalRandom.current().nextInt(randTimeBegin, randTimeEnd));
                } catch (InterruptedException e) {}
            }
        }
    }
    
    class ModifyAndPaint extends Thread {
        @Override
        public void run() {
            while (gameStatus != 2) {
                offset += truck.speed;

                for (int entityIndex = 0; entityIndex < entities.size(); ++entityIndex) {
                    Entity entity = entities.get(entityIndex);
    
                    entity.y += truck.speed;
    
                    if (
                        entity.isCollidedWith(truck) &&
                        !truck.isFloating() &&
                        entity.onCollided(truck) == 0
                    ) {
                        gameStatus = 2;
                        repaint();
                        break;
                    }
                }

                try {
                    sleep(0, 10);
                } catch (InterruptedException e) {}
                
                repaint();
            }
        }
    }

    class Countdown extends Thread {
        @Override
        public void run() {
            while (gameStatus != 2 && truck.time > 0) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {}
                
                if (gameStatus == 1)
                    truck.time -= 1;
            }

            gameStatus = 2;
            repaint();
        }
    }

    class DepleteStamina extends Thread {
        @Override
        public void run() {
            while (gameStatus != 2) {
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

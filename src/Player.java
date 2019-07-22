import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javax.imageio.ImageIO.read;

public class Player extends Collisonable {
    private double x;
    private double y;
    private double angle;
    private final double r = 40;
    private final double ROTATIONSPEED = 6;
    private int health;
    private boolean dead;
    private int playerIndex;

    private double vx;
    private double vy;
    private double speed=6;
    private BufferedImage img;

    private long firingTimer;
    private long firingDelay;

    private boolean UpPressed;
    private boolean DownPressed;
    private boolean RightPressed;
    private boolean LeftPressed;
    private boolean ShootPressed;

    private int lives=3;

    public Player(double x, double y, double vx, double vy, double angle, BufferedImage img){
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.img = img;
        this.angle = angle;
        playerIndex = x==200? 0:1;

        dead = false;
        firingTimer = System.nanoTime();
        firingDelay = 500;
    }

    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}
    public double getAngle() {return angle;}
    public boolean isDead() {return dead;}
    public int getPlayerIndex() {return playerIndex;}

    public void setX (double x){ this.x = x;}
    public void setY (double x){ this.y = y;}
    public void setAngle (double angle){ this.angle = angle;}

    void toggleUpPressed() {
        this.UpPressed = true;
    }

    void toggleDownPressed() {
        this.DownPressed = true;
    }

    void toggleRightPressed() {
        this.RightPressed = true;
    }

    void toggleLeftPressed() {
        this.LeftPressed = true;
    }

    void toggleShootPressed(){
        this.ShootPressed = true;
    }

    void unToggleUpPressed() {
        this.UpPressed = false;
    }

    void unToggleDownPressed() {
        this.DownPressed = false;
    }

    void unToggleRightPressed() {
        this.RightPressed = false;
    }

    void unToggleLeftPressed() {
        this.LeftPressed = false;
    }

    void untoggleShootPressed(){
        this.ShootPressed = false;
    }

public void update(){
    if (this.UpPressed) {
        this.moveForwards();
    }
    if (this.DownPressed) {
        this.moveBackwards();
    }

    if (this.LeftPressed) {
        this.rotateLeft();
    }
    if (this.RightPressed) {
        this.rotateRight();
    }
    if(this.ShootPressed) {
        this.shooting();
    }
}

    private void rotateLeft() {
        this.angle -= this.ROTATIONSPEED;
    }

    private void rotateRight() {
        this.angle += this.ROTATIONSPEED;
    }

    private void moveBackwards() {

        vx = Math.round(speed * Math.cos(Math.toRadians(angle)));
        vy = Math.round(speed * Math.sin(Math.toRadians(angle)));
        x -= vx;
        y -= vy;
        checkBorder();
    }

    private void moveForwards() {

        vx = Math.round(speed * Math.cos(Math.toRadians(angle)));
        vy = Math.round(speed * Math.sin(Math.toRadians(angle)));
        x += vx;
        y += vy;
        checkBorder();

    }

    private void shooting(){
        long timeElapsed = (System.nanoTime() - firingTimer) / 1000000;
        if (timeElapsed >= firingDelay) {
            BufferedImage bulletImg = Helper.loadImg("Bullet.gif");
            GamePanel.bullets.add(new Bullet(angle, x, y,playerIndex, bulletImg));
            firingTimer = System.nanoTime();
        }
    }

    private void checkBorder() {
        if (x < 0) {
            x = 0;
        }
        if (x >= GamePanel.SCREENWIDTH - 88) {
            x = GamePanel.SCREENWIDTH - 88;
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= GamePanel.SCREENHEIGHT - 80) {
            y = GamePanel.SCREENHEIGHT - 80;
        }

    }

    public void checkWall(double wallX, double wallY) {
        if (x > wallX-37 && x<wallX+40 && y > wallY-37 && y<wallY+40) {
            if(UpPressed) {
                x -= vx;
                y -= vy;
            }else{
                x += vx;
                y += vy;
            }
        }
    }

    @Override
    public String toString() {
        return "x=" + x + ", y=" + y + ", angle=" + angle;
    }

    public void hit(){
        health --;
        if(health<=0){
            dead = true;
        }
    }

public void draw(Graphics2D g){
    AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
    rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(this.img, rotation, null);
}

}

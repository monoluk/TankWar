package tankwar.game.collisonable;


import tankwar.GameConstants;
import tankwar.game.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;


public class Player extends Collisonable {
    private double x;
    private double y;
    private double angle;
    private final double r = 40;
    private final double ROTATIONSPEED = 6;
    private int health;
    private boolean dead;
    private boolean gameOver;
    private int playerIndex;

    private double vx;
    private double vy;
    private double speed;
    private BufferedImage img;
    BufferedImage bulletImg = Helper.loadImg("resources/Bullet.gif");
    GamePanel gp;

    private long firingTimer;
    private long firingDelay;
    private long hitTimer;

    private boolean UpPressed;
    private boolean DownPressed;
    private boolean RightPressed;
    private boolean LeftPressed;
    private boolean ShootPressed;
    private boolean hit;

    private int lives;

    public Player(double x, double y, double vx, double vy, double angle, BufferedImage img, GamePanel gp){
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.img = img;
        this.angle = angle;
        this.health = GameConstants.START_HEALTH;
        this.lives = GameConstants.START_LIFE;
        this.gameOver = false;
        this.speed = 6;
        this.playerIndex = x==200? 0:1;
        this.hit = false;

        dead = false;
        firingTimer = System.nanoTime();
        firingDelay = 500;

        this.gp = gp;
    }

    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}
    public double getAngle() {return angle;}
    public boolean isDead() {return dead;}
    public boolean isGameOver() {return gameOver;}
    public int getLives() {return lives;}
    public int getHealth() {return health;}
    public int getPlayerIndex() {return playerIndex;}

    public void setX (double x){ this.x = x;}
    public void setY (double x){ this.y = y;}
    public void resetAngle (){
        if(playerIndex == 1){
            angle = 180;}
        else{
        this.angle = 0;}}
    public void setDead (boolean isDead){this.dead = isDead;}

    public void resetPlayers() {
        this.y = 200;
        resetAngle();

        this.health = GameConstants.START_HEALTH;
        this.lives =GameConstants.START_LIFE;
        this.gameOver = false;

        this.UpPressed = false;
        this.DownPressed = false;
        this.RightPressed = false;
        this.LeftPressed = false;
        this.ShootPressed = false;

    }



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
            gp.addBullets(angle, x, y,playerIndex, bulletImg);
            firingTimer = System.nanoTime();
        }
    }

    private void checkBorder() {
        if (x < 0) {
            x = 0;
        }
        if (x >= GameConstants.GAME_SCREEN_WIDTH - 88) {
            x = GameConstants.GAME_SCREEN_WIDTH - 88;
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= GameConstants.GAME_SCREEN_HEIGHT - 80) {
            y = GameConstants.GAME_SCREEN_HEIGHT - 80;
        }

    }

    public void checkBlockage(double blockX, double blockY) {
        if (x > blockX-37 && x<blockX+40 && y > blockY-37 && y<blockY+40) {
            if(UpPressed) {
                x -= vx;
                y -= vy;
            }else if (DownPressed){
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

        hit = true;
        if((System.nanoTime()-hitTimer)/1000000 >300) {
            health--;
            if (health <= 0) {
                dead = true;
                lives--;
                health = GameConstants.START_HEALTH;
                if (lives <= 0) {
                    gameOver = true;
                }
            }
            hitTimer = System.nanoTime();
            hit = false;
        }

    }

public void draw(Graphics2D g){
        if((System.nanoTime()-hitTimer)/1000000 >50){
    AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
    rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(this.img, rotation, null);
        }
}

}

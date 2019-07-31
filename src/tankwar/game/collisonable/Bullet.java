package tankwar.game.collisonable;

import tankwar.game.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Bullet extends Collisonable {

    private double x;
    private double y;
    private int r;

    private double dx;
    private double dy;
    private double radian;
    private double speed;
    private double angle;
    private BufferedImage img;
    private int shotBy;

    //shotBy -- "2" represents bullet shot by enemy, "0", "1" represents player1 & player2
    public Bullet(double angle, double x , double y, int shotBy,BufferedImage img){

        this.x = x+25;
        this.y = y+25;
        this.angle = angle;
        this.shotBy = shotBy;
        r=5;
        this.img = img;
        speed = 15;

        radian = Math.toRadians(angle);
        dx = Math.cos(radian)*speed;
        dy = Math.sin(radian)*speed;


    }

    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}
    public int getShotBy () {return shotBy;}

    public boolean update(){
        x += dx;
        y += dy;
    if(Helper.outOfBorder(x,y,r)) return true;
        return false;
    }

    public void draw(Graphics2D g){
//        g.setColor(Color.YELLOW);
//        g.fillOval((int) (x-r), (int) (y-r), 2*r, 2*r);

        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);
    }


}

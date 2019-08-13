package tankwar.game.collisonable.wall;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.security.PublicKey;

import tankwar.game.collisonable.*;

public class Wall extends Collisonable {

    private double x;
    private double y;
    private final double r =32; //57;
    private boolean breakable;
    private BufferedImage img;

    public Wall (double x, double y, BufferedImage img){

        this.x = x ;
        this.y = y ;
        this.breakable = breakable;
        this.img = img;
    }

    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}

    public void update(){

    }

    public void draw(Graphics2D g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img,(int)x+16, (int)y+16,null);
    }


}

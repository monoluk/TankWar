import com.sun.tools.javac.Main;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Bullet {

    private double x;
    private double y;
    private int r;

    private double dx;
    private double dy;
    private double radian;
    private double speed;
    private double angle;
    private BufferedImage img;
    String shotby;


    public Bullet(double angle, double x , double y, String shotby,BufferedImage img){

        this.x = x+25;
        this.y = y+25;
        this.angle = angle;
        this.shotby = shotby;
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
    public String getShotBy () {return shotby;}

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

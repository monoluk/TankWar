import java.awt.*;
import java.awt.image.BufferedImage;


public class PowerUp extends Collisonable{
    private double x;
    private double y;
    private double r;
    private BufferedImage img;
    private String consumedBy;
    private long timer;
    private long delay = 15000;
    private double type;

    //type 1 : rocket

    public PowerUp(int type, double x, double y, BufferedImage img){
        this.x = x+10;
        this.y = y+10;
        this.type = type;
        this.img = img;
        timer = System.nanoTime();
    }

    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}
    public double getType(){return type;}
    public String getConsumedBy(){return consumedBy;}

    public void setConsumedBy(String by){
        this.consumedBy = by;
    }

public  boolean update(){
    long elapsed = (System.nanoTime() - timer)/1000000;

    if(elapsed > delay){
        return true;
    }

    return false;

}

public void draw(Graphics2D g){
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawImage(this.img,(int)x, (int)y,null);

}


}

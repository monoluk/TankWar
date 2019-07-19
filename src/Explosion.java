import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Explosion {
    private double x;
    private double y;
    private double r;
    private double angle;
    private BufferedImage img;
    private int imgwidth=32, imghight=32;

    private long explosionTimer;
    private long delay=250;

    public Explosion (double angle, double x, double y, BufferedImage img){
        this.angle = angle;
        this.x = x+10;
        this.y = y+10;
        this.img = img;
        explosionTimer = System.nanoTime();
    }


    public boolean update(){
        this.img = Helper.resize(img, imgwidth,imghight);
        imgwidth += 10;
        imghight += 10;

        long elaspsed = (System.nanoTime() - explosionTimer)/1000000;
        if(elaspsed < delay){return false;}
        explosionTimer=0;
        return true;
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

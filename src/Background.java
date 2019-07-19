import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javax.imageio.ImageIO.read;

public class Background {
    private BufferedImage img;

    public Background(BufferedImage img){
    this.img = img;

    }



    public void draw(Graphics2D g){
//        g.setColor(color1);
//        g.fillOval((int) (x-r), (int)(y-r), 2*r, 2*r);
//
//        g.setStroke(new BasicStroke(3));
//        g.setColor(color1.darker());
//        g.drawOval((int) (x-r), (int)(y-r), 2*r, 2*r);;
//        g.setStroke(new BasicStroke(1));

        int tileWidth = img.getWidth();
        int tileHight = img.getHeight();
        int numberX = (int) (GamePanel.SCREENWIDTH/tileWidth);
        int numberY = (int) (GamePanel.SCREENHEIGHT/tileHight);

        Graphics2D g2d = (Graphics2D) g;
        for(int i=0;i<numberX;i++) {
            for(int j=0; j<numberY;j++)
            g2d.drawImage(this.img, i*tileWidth, j*tileHight, null);
        }

    }
}

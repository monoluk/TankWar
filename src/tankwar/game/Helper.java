package tankwar.game;

import tankwar.GameConstants;
import tankwar.game.collisonable.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static javax.imageio.ImageIO.read;

public class Helper {

    public static boolean outOfBorder(double x, double y, int r) {
        if(x<-r || x> GameConstants.GAME_SCREEN_WIDTH || y < -r || y>GameConstants.GAME_SCREEN_HEIGHT) return true;
        return false;
    }

    public static boolean isOdd(int num){
        return num%2==0;
    }

    public static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public static BufferedImage loadImg(String fileName){
        BufferedImage img=null;
        try {
           img = read(new File(fileName));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return img;
    }

    public static double getDistance (Collisonable obj1, Collisonable obj2){
        double dx = obj1.getx() - obj2.getx();
        double dy = obj1.gety() - obj2.gety();
        double distance = Math.sqrt(dx*dx + dy*dy);

        return distance;



    }
}

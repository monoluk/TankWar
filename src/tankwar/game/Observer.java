package tankwar.game;

import java.awt.*;
import java.awt.image.BufferedImage;


public class Observer {

    private double x;
    private double y;
    private int health;
    private int lives;
    private BufferedImage playerIcon;


    public Observer(double x, double y, int health, int lives, BufferedImage playerIcon) {
        super();
        this.x = x+7;
        this.y = y+57;
        this.health = health;
        this.lives = lives;
        this.playerIcon = playerIcon;

    }

    public void update (double x, double y, int health,int lives){
        this.x = x+7;
        this.y = y+57;
        this.health = health;
        this.lives = lives;
    }


    public void draw(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.drawRect((int) x, (int) y, 45, 3);
        double healthPercent = (double) health / 3.00;
        int healthLeft = (int) (45 * healthPercent);
        g.fillRect((int) x, (int) y, healthLeft, 3);

        Graphics2D g2d = (Graphics2D) g;
        for(int i=0; i<lives; i++){
            g2d.drawImage(playerIcon, (int)x+14*i,(int)y-69,null);
        }
    }
}

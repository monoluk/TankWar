import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Enemy extends Collisonable {

    private double x;
    private double y;
    private double r;
    private Color color1;

    private double dx;
    private double dy;
    private double radian;
    private double speed;
    private double angle;
    private BufferedImage img;

    private int health;
    private int type;
    private int rank;
    private int randNum;
    private long firingTimer;
    private long firingDelay;

    private boolean ready;
    private boolean dead;
    private boolean pickOnPlayer;
    private long pickOnPlayerTimer=-1;
    private long pickOnPlayerDelay=5000;
    private int consumedBy;

    public Enemy(int type, int rank, BufferedImage img){
        this.type = type;
        this.rank = rank;
        this.img = img;

        if(type==1){
            color1 = Color.blue;
            if(rank ==1){
                speed =10;  //was 2
                r=18;
                health=1;
            }
        }
        x = Math.random()*GamePanel.SCREENWIDTH/2 + GamePanel.SCREENWIDTH/4;
        randNum =(int) (Math.random()*10);
        y = Helper.isOdd(randNum)? GamePanel.SCREENHEIGHT+r:-r;

        angle = Math.random()*140 +20;

//        double targetY = y - GamePanel.players.get(0).gety();
//        double targetX = x - GamePanel.players.get(0).getx();
//        angle = Math.toDegrees(Math.atan(targetY/targetX));
//        System.out.println("degree: "+ angle);

        if(y>0){
            angle = -angle;
        }

        radian = Math.toRadians(angle);
        dx = Math.cos(radian)*speed;
        dy = Math.sin(radian)*speed;

        ready = false;
        dead = false;
        firingTimer = System.nanoTime();
//        firingDelay = (long) (Math.random()*10000);
//        firingDelay = firingDelay <500? 500 : firingDelay;
        firingDelay = (long)((Math.random() * ((2000 - 500) + 1)) + 500);

    }


    public double getx() {return x;}
    public double gety() {return y;}
    public double getr() {return r;}
    public double getAngle() {return angle;}

    public void setX(double newX){this.x = newX;}
    public void setY(double newY){this.y = newY;}


    public void setPickOnPlay(boolean state, int consumedBy){
        pickOnPlayerTimer = System.nanoTime();
        pickOnPlayer = state;

        //records which player pickup the powerUp
        this.consumedBy = consumedBy;
    }

    public void hit(){
            health--;
            if (health <= 0) {
                dead = true;
            }

    }

    private void shooting(){
        long timeElapsed = (System.nanoTime() - firingTimer) / 1000000;
        if (timeElapsed >= firingDelay) {
                BufferedImage bulletImg = Helper.loadImg("Bullet.gif");
                //create new bullet "2" represents bullet shot by enemy, "0", "1" represents player1 & player2
                GamePanel.bullets.add(new Bullet(angle, x, y,2, bulletImg));
            firingTimer = System.nanoTime();
        }
    }

//    public void checkWall(double wallX, double wallY){
//        if(x<wallX+40 && dx<0 &&y<wallY+40 && dy<0 || x>wallX-40 && dx>0 && y>wallY-37 && dy>0) {
//            dx=-dx; angle =(180-angle);
//            dy = -dy; angle = -angle;}
//
////        if(x>wallX-40 && dx>0 && y>wallY-37 && dy>0) { dx=-dx;
////        angle= (180-angle);
////        dy = -dy;
////        angle = -angle;}
//
//    }


    public boolean isDead(){return dead;}


    public void update(){

        x += dx;
        y += dy;

        if(!ready){
            if(x>r && x<GamePanel.SCREENWIDTH-r && y>r && y<GamePanel.SCREENHEIGHT -r){
                ready = true;
            }
        }

        //bounce from border
        if(x<r && dx<0) {dx=-dx; angle =(180-angle);}
        if(y<r && dy<0) {dy = -dy; angle = -angle;}
        if(x>GamePanel.SCREENWIDTH - r && dx>0) {dx=-dx; angle= (180-angle); }
        if(y>GamePanel.SCREENHEIGHT - r && dy>0) {dy = -dy; angle = -angle;}

        //check if powerUp tpye 1 expires
        double elapsed = (System.nanoTime()-pickOnPlayerTimer)/1000000;
        if(elapsed > pickOnPlayerDelay && pickOnPlayerTimer>0) {
            pickOnPlayer = false;
            //angle = Math.random()*140 +20;
            angle = Math.random()*3*100+20;
            radian = Math.toRadians(angle);
            dx = Math.cos(radian)*speed;
            dy = Math.sin(radian)*speed;
            pickOnPlayerTimer = -1;
        }

        //enemy follows player
        if(pickOnPlayer) {
        radian = Math.toRadians(angle);
        dx = Math.cos(radian)*speed;
        dy = Math.sin(radian)*speed;

        int target = consumedBy==0? 1:0;

        double targetY = GamePanel.players.get(target).gety()-y;
        double targetX = GamePanel.players.get(target).getx()-x;
        angle = Math.toDegrees(Math.atan(targetY/targetX));

        if(angle<90 && angle>-90){
            if(x>GamePanel.players.get(target).getx())
            { //System.out.println("add 180");
            angle = angle+180;}
        }
        }

        shooting();
    }


    public void bounce(double wallX, double wallY){
//        if(x<wallX+37 && x>wallX-37 && y<wallY+37 && y>wallY-37)
//        {
//
//            if(this.x -wallX > this.y-wallY ){
//            dx=-dx;
//            angle =(180-angle);}
//            else {
//                dy = -dy;
//                angle = -angle;
//            }

System.out.println("Wx: "+wallX+ " Wy: "+wallY);
System.out.println("x: "+this.x +" y: "+ this.y);


        if(this.x < (wallX+100) && this.x > (wallY-100) ){
            System.out.println("horizontal");
            dx = -dx;
            angle = 180-angle;

        if (this.y < (wallY+100) && this.y > (wallY-100) ) {
            System.out.println("vertical");
            dy = -dy;
            angle = -angle;
        }







        }
//        if(y<wallY+37 && y>wallY-37)
//        {
//        dy = -dy;
//
//        }

        //if(x>GamePanel.SCREENWIDTH - r && dx>0) {dx=-dx; angle= (180-angle); }
        //if(y>GamePanel.SCREENHEIGHT - r && dy>0) {dy = -dy; angle = -angle;}
    }

    public void draw(Graphics2D g){
//        g.setColor(color1);
//        g.fillOval((int) (x-r), (int)(y-r), 2*r, 2*r);
//
//        g.setStroke(new BasicStroke(3));
//        g.setColor(color1.darker());
//        g.drawOval((int) (x-r), (int)(y-r), 2*r, 2*r);;
//        g.setStroke(new BasicStroke(1));
        AffineTransform rotation = AffineTransform.getTranslateInstance(x, y);
        rotation.rotate(Math.toRadians(angle), this.img.getWidth() / 2.0, this.img.getHeight() / 2.0);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.img, rotation, null);


    }
}


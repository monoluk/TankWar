import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static javax.imageio.ImageIO.read;

public class GamePanel extends JPanel implements Runnable{

    public static final int SCREENWIDTH = 1366;//1280;
    public static final int SCREENHEIGHT = 768;//960;

    private Thread thread;

    private Graphics2D g;
    private BufferedImage image;
    private Image leftSide, rightSide, miniMap;
    private int fps =30;
    private double avgFps;
    private boolean isRunning;
    private int enemyNum=0;
    private long waveTimer;
    private long waveDelay=2000;
    private long spawnTimer;

    private double originSpawnPosX = 200;
    private double originSpawnPosy = 200;
    private double originAngle = 0;
    private Player collidedPlayer=null;
    private int collidedPlayerIndex;
    private double oldAngle;

    public static Background bg;
    public static ArrayList<Wall> walls;
    public static ArrayList<Player> players;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<PowerUp> powerUps;


    public GamePanel(){
        super();
        setPreferredSize(new Dimension(SCREENWIDTH, SCREENHEIGHT));
        setFocusable(true);
        requestFocus();
    }

    public void addNotify(){
        super.addNotify();
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run(){
        isRunning = true;

        image = new BufferedImage(SCREENWIDTH, SCREENHEIGHT, BufferedImage.TYPE_INT_RGB);
        //image = Helper.loadImg("Background.bmp");
        //this.image = Helper.resize(this.image,SCREENWIDTH, SCREENHEIGHT);

        g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );



        BufferedImage bgImg = Helper.loadImg("Background.bmp");
        bg = new Background(bgImg);
        players = new ArrayList<Player>();

        //create player 1
        BufferedImage t1img = Helper.loadImg("Tank1.png");
        players.add( new Player(200, 200, 0, 0, 0, t1img));
        PlayerControl tc1 = new PlayerControl(players.get(0), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_M);
        this.addKeyListener(tc1);


        //create player 2
        BufferedImage t2img = Helper.loadImg("Tank2.gif");
        players.add(new Player(500, 200, 0, 0, 0, t2img));
        PlayerControl tc2 = new PlayerControl(players.get(1), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        this.addKeyListener(tc2);


        walls = new ArrayList<Wall>();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        explosions = new ArrayList<Explosion>();
        powerUps = new ArrayList<PowerUp>();

        //create walls
        for(double i =20; i<550; ){
            BufferedImage wallImg = Helper.loadImg("Wall1.gif");
            walls.add(new Wall(i,400,false, wallImg));
            i += 32;
        }

        for(double i =180; i<680; ){
            BufferedImage wallImg = Helper.loadImg("Wall1.gif");
            walls.add(new Wall(800,i,false, wallImg));
            i += 32;
        }


        long startTime;
        long URTimeMill;
        long waitTm;
        long totalTime=0;

        int frameCunt =0;
        int maxFrmCunt =30;
        long targetTime = 1000/fps;



        //game loop
        while (isRunning){
            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            URTimeMill = (System.nanoTime()- startTime) / 1000000;
            waitTm = targetTime - URTimeMill;

            if(waitTm>=0) {
                try {
                    Thread.sleep(waitTm);
                } catch (InterruptedException ignored) {
                }
            }
            totalTime += System.nanoTime() - startTime;
            frameCunt++;
            //System.out.println("frmCount: "+frameCunt);
            if(frameCunt == maxFrmCunt){
                avgFps = 1000.0/((totalTime / frameCunt)/1000000);
                frameCunt = 0;
                totalTime = 0;
            }
        }
    }

    //update components
    private void gameUpdate(){
        //update explosions
        if(explosions.size()>0){
            for(int i=0; i<explosions.size();i++){
              boolean remove =  explosions.get(i).update();
              if(remove) {popExplosion();}
            }

        }


        //update player(s)
        for (int i=0; i<players.size();i++) {
            players.get(i).update();
        }

        //generate enemies
        long elapsed = (System.nanoTime() - waveTimer)/1000000;
        if(enemies.isEmpty() && elapsed>=waveDelay) {
            for (int i = 0; i < enemyNum; i++) {
                //enemies.add(new Enemy(1, 1));
                BufferedImage enemyImg = Helper.loadImg("Tank2.gif");
                GamePanel.enemies.add(new Enemy(1, 1, enemyImg));
            }
            enemyNum++;
        }

        //update bullets
        for(int i=0; i<bullets.size(); i++){
            boolean remove = bullets.get(i).update();
            if(remove){
                bullets.remove(i);
                i--;
            }
            //System.out.println(bullets.size());
        }


        //update enemies
        for(int i=0; i<enemies.size();i++){
            enemies.get(i).update();
        }

        //bullet-enemy & bullet-player collision
        for(int i=0; i<bullets.size(); i++) {
            Bullet b = bullets.get(i);

            //loop through all enemies to check bullet-enemy collision
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);

                double dist = Helper.getDistance(b,e);

                if (dist <= (b.getr() + e.getr()) && b.getShotBy() <2) {
                    e.hit();
                    if (e.isDead()) {
                        BufferedImage explosionImg = Helper.loadImg("Explosion_small.gif");
                        GamePanel.explosions.add(new Explosion(e.getAngle(), e.getx(), e.gety(), explosionImg));
                        enemies.remove(j);
                        j--;
                        //chances for getting powerups
                        double chance = Math.random();
                        if(chance<0.5){
                            BufferedImage powerUpImg = Helper.loadImg("Rocket.gif");
                            powerUps.add(new PowerUp(1, e.getx(),e.gety(),powerUpImg));
                        }

                        if (enemies.isEmpty()) waveTimer = System.nanoTime();
                    }
                    if (bullets.size() > 0) {
                        bullets.remove(i);
                        i--;
                    }
                }
            }

            //loop through all player to check bullet-player collision
            for (int k = 0; k < players.size(); k++) {
                Player p = players.get(k);

                double PBdist = Helper.getDistance(b,p);
                //if (PBdist <= (br + pr) && b.getShotBy().equals("enemy")) {


                if (PBdist <= (b.getr() + p.getr()) && b.getShotBy() != p.getPlayerIndex()) {
                    p.hit();

                    if (p.isDead()) {
                        BufferedImage explosionImg = Helper.loadImg("Explosion_small.gif");
                        GamePanel.explosions.add(new Explosion(p.getAngle(), p.getx(), p.gety(), explosionImg));
                        p.setX(originSpawnPosX);
                        p.setY(originSpawnPosy);
                        p.setAngle(originAngle);
                    }

                }
            }
        }

        //check for bullet-bullet collision
        for(int i=0; i<bullets.size(); i++) {
            Bullet b = bullets.get(i);

            if (i < bullets.size() - 1) {
                for (int l = i + 1; l < bullets.size(); l++) {
                    Bullet b1 = bullets.get(l);

                    double bbDist = Helper.getDistance(b,b1);
                    if (bbDist <= 2*b1.getr()) {
                        try {
                            bullets.remove(l);
                            bullets.remove(i);
                            l--;
                            i--;
                        } catch (IndexOutOfBoundsException ie) {
                            System.out.println(ie.getMessage());
                        }
                    }
                }
            }
        }

        //check for powerUP-player collision
        for (int i =0; i<powerUps.size(); i++) {
            PowerUp pow = powerUps.get(i);
            double pType = powerUps.get(i).getType();

            for(int j=0; j<players.size();j++){
                Player pla = players.get(j);

                double dist = Helper.getDistance(pow,pla);

                if(dist< (pla.getr() + pow.getr())){
                    if(pType==1) {
                        for(Enemy enemy : enemies){
                            enemy.setPickOnPlay(true,j);
                        }
                    }
                    powerUps.remove(i);
                    i--;
                }
            }
        }

        //check for wall-player & wall-enemy collision
        for(int i=0; i<walls.size(); i++) {
            Wall w = walls.get(i);

            players.forEach(player -> player.checkWall(w.getx(),w.gety()));

          for(int k=0; k<enemies.size(); k++){
              Enemy e = enemies.get(k);
              double dist = Helper.getDistance(w,e);
              if(dist < (w.getr()+e.getr())){
                  e.bounce(w.getx(),w.gety());
              }
          }
        }

        //bullet-wall collision
//        for(int i =0; i< bullets.size(); i++){
//            Bullet b = bullets.get(i);
//
//            for(int j=0; j<walls.size(); j++){
//                Wall w = walls.get(j);
//
//                double dist = Helper.getDistance(b,w);
//
//                if(dist<(b.getr()+w.getr())*0.9){
//                    walls.remove(j);
//                    j--;
//                    try {
//                        bullets.remove(i);
//                        i--;
//                    }catch (IndexOutOfBoundsException OB){
//                    }
//                }
//
//            }
//
//
//        }


        //update powerUps
        for(int i=0; i<powerUps.size(); i++){
           boolean remove = powerUps.get(i).update();
           if(remove){
               powerUps.remove(i);
               i--;
           }
        }

    }

    //buffering the game
    private void gameRender(){
        //render background
        g.setColor(Color.black);
        g.fillRect(0,0,SCREENWIDTH,SCREENHEIGHT);
//        g.setColor(Color.BLACK);
//        g.drawString("FPS:" + avgFps, 200, 200);

        bg.draw(g);

        //draw walls
        walls.forEach(wall -> {
            wall.draw(g);
        });


        //draw players
        for(int i=0; i<players.size();i++) {
            players.get(i).draw(g);
        }

        //draw bullets
        for(int i=0; i<bullets.size(); i++){
            bullets.get(i).draw(g);
        }

        //draw explosion
        if(explosions.size()>0){
            for(int i=0; i<explosions.size();i++){
                explosions.get(i).draw(g);
            }
        }

        //draw enemies
        for(int i=0; i<enemies.size();i++){
            enemies.get(i).draw(g);
        }

        //draw PowerUps
        powerUps.forEach(powUP->{
            powUP.draw(g);
        });

    }

    //draw game to screen
    private void gameDraw(){
        Graphics g2 = this.getGraphics();
        g2.drawImage(image,0,0,null);
        g2.dispose();

//        BufferedImage mySubimage1 = image.getSubimage(0, 0, 500, 400);
//        BufferedImage mySubimage2 = image.getSubimage(500,0,500,400);
//        Image miniMap = image.getScaledInstance(400,225, Image.SCALE_FAST);
//        g2.drawImage(mySubimage1,0,0,null);
//        g2.drawImage(mySubimage2,500,0,null);
//        g2.drawImage(miniMap, 200, 410, null);
//        g2.dispose();

//        int xLeft, yLeft, xRight, yRight, width, height;
//        xLeft = (int)players.get(1).getx() - 120;
//        yLeft =(int) players.get(1).gety() - 300;
//        xRight = (int) players.get(0).getx() - 120;
//        yRight =(int) players.get(0).gety() - 300;
//
//        width = 400;
//        height = 600;
//        if (xLeft < 0) {
//            xLeft = 0;
//        }
//
//        if (xRight < 0) {
//            xRight = 0;
//        }
//
//        if (yLeft < 0) {
//            yLeft = 0;
//        }
//
//        if (yRight < 0) {
//            yRight = 0;
//        }
//        if (xLeft + width > 1490) {
//            xLeft = 1095;
//        }
//
//        if (xRight + width > 1490) {
//            xRight = 1095;
//        }
//
//        if (yLeft + height > 900) {
//            yLeft = 300;
//        }
//
//        if (yRight + height > 900) {
//            yRight = 300;
//        }
//
//        leftSide = image.getSubimage(xLeft, yLeft, width, height);
//        rightSide = image.getSubimage(xRight, yRight, width, height);
//
//        leftSide = leftSide.getScaledInstance(800, 900, Image.SCALE_FAST);
//        rightSide = rightSide.getScaledInstance(800, 900, Image.SCALE_FAST);
//        miniMap = image.getScaledInstance(400, 225, Image.SCALE_FAST);
//
//        // render and grab single frame
//        BufferedImage display = new BufferedImage(SCREENWIDTH, SCREENHEIGHT,
//                BufferedImage.TYPE_INT_RGB);
//        Graphics temp = display.getGraphics();
//
//        temp.drawImage(leftSide, 0, 0, null);
//        temp.drawImage(rightSide, 0, 0, null);
//        temp.drawImage(miniMap, 600, 640, null);
//
//        // draw left side, right side and minimap images on screen
//        g2.drawImage(leftSide, 0, 0, this);
//        g2.drawImage(rightSide, 800, 0, this);
//        g2.drawImage(miniMap, 575, 600, this);

    }

    public void popExplosion(){
        explosions.remove(explosions.size()-1);
    }

}

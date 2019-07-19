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

    public static final int SCREENWIDTH = 1280;
    public static final int SCREENHEIGHT = 960;

    private Thread thread;

    private Graphics2D g;
    private BufferedImage image;
    private int fps =30;
    private double avgFps;
    private boolean isRunning;
    private int enemyNum=2;
    private long waveTimer;
    private long waveDelay=2000;
    private long spawnTimer;

    private double originSpawnPosX = 200;
    private double originSpawnPosy = 200;
    private double originAngle = 0;

    public static Background bg;
    public static ArrayList<Player> players;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<Explosion> explosions;

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
        BufferedImage t1img = Helper.loadImg("Tank1.gif");
        players.add( new Player(200, 200, 0, 0, 0, t1img));
        PlayerControl tc1 = new PlayerControl(players.get(0), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_M);
        this.addKeyListener(tc1);


//        //create player 2
//        BufferedImage t2img = Helper.loadImg("Tank2.gif");
//        t2 = new Player(500, 200, 0, 0, 0, t2img);
//        PlayerControl tc2 = new PlayerControl(t2, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
//        this.addKeyListener(tc2);


        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        explosions = new ArrayList<Explosion>();


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
            double bx = b.getx();
            double by = b.gety();
            double br = b.getr();

            //loop through all enemies to check bullet-enemy collision
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                double ex = e.getx();
                double ey = e.gety();
                double er = e.getr();
                double eangle = e.getAngle();

                double dx = bx - ex;
                double dy = by - ey;
                double dist = Math.sqrt(dx * dx - dy * dy);
                if (dist <= (br + er) && b.getShotBy().equals("player")) {
                    e.hit();
                    if (e.isDead()) {
                        BufferedImage explosionImg = Helper.loadImg("Explosion_small.gif");
                        GamePanel.explosions.add(new Explosion(eangle, ex, ey, explosionImg));
                        enemies.remove(j);
                        j--;
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
                double px = p.getx();
                double py = p.gety();
                double pr = p.getr();
                double pAngle = p.getAngle();
                double dx = bx - px;
                double dy = by - py;
                double PBdist = Math.sqrt(dx * dx + dy * dy);
                if (PBdist <= (br + pr) && b.getShotBy().equals("enemy")) {
                    p.hit();

                    if (p.isDead()) {
                        BufferedImage explosionImg = Helper.loadImg("Explosion_small.gif");
                        GamePanel.explosions.add(new Explosion(pAngle, px, py, explosionImg));
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
            double bx = b.getx();
            double by = b.gety();
            double br = b.getr();

            if (i < bullets.size() - 1) {
                for (int l = i + 1; l < bullets.size(); l++) {
                    Bullet b1 = bullets.get(l);
                    double b1x = b1.getx();
                    double b1y = b1.gety();
                    double b1r = b1.getr();

                    double dx = b1x - bx;
                    double dy = b1y - by;

                    double bbDist = Math.sqrt(dx * dx + dy * dy);
                    if (bbDist <= 2*b1r) {
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


    }

    //buffering the game
    private void gameRender(){
        //render background
        g.setColor(Color.black);
        g.fillRect(0,0,SCREENWIDTH,SCREENHEIGHT);
//        g.setColor(Color.BLACK);
//        g.drawString("FPS:" + avgFps, 200, 200);

        bg.draw(g);


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


    }

    //draw game to screen
    private void gameDraw(){
        Graphics g2 = this.getGraphics();
        g2.drawImage(image,0,0,null);
        g2.dispose();
    }

    public void popExplosion(){
        explosions.remove(explosions.size()-1);
    }

}

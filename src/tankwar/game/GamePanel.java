package tankwar.game;


import tankwar.*;
import tankwar.menus.EndGamePanel;
import tankwar.game.collisonable.*;
import tankwar.game.collisonable.wall.*;


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

    private Launcher lf;

    private Graphics2D g;
    private BufferedImage image;
    private Image leftSide, rightSide, miniMap;
    private int fps =30;
    private double avgFps;
    private int enemyNum = GameConstants.START_ENEMY_NO;
    private long waveTimer;
    private long waveDelay=2000;
    private long gameOverTimer;

    private double originSpawnPosX = 1000;
    private double originSpawnPosy = 1000;
    private static String isWon;
    private double originAngle = 0;
    private Player collidedPlayer=null;
    private int collidedPlayerIndex;
    private double oldAngle;
    private int healthBar = 100;

    private final BufferedImage breakablWallImg = Helper.loadImg("resources/Wall1.gif");
    private final BufferedImage unBreakablWallImg = Helper.loadImg("resources/Wall2.gif");
    private final BufferedImage powerUpImg = Helper.loadImg("resources/Rocket.gif");
    private final BufferedImage explosionImg = Helper.loadImg("resources/Explosion_small.gif");
    private final BufferedImage enemyImg = Helper.loadImg("resources/Tank2.gif");
    private final BufferedImage p1Icon = Helper.loadImg("resources/Tank1 small.png");
    private final BufferedImage p2Icon = Helper.loadImg("resources/Tank2 small.png");

    public static Background bg;
    public static ArrayList<Wall> walls;
    public static ArrayList<Player> players;
    public static ArrayList<Bullet> bullets;
    public static ArrayList<Enemy> enemies;
    public static ArrayList<Explosion> explosions;
    public static ArrayList<PowerUp> powerUps;
    public ArrayList<Observer> observers;


    public GamePanel(Launcher lf){

        super();
        setPreferredSize(new Dimension(GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT));
        setFocusable(true);
        requestFocus();
        this.lf = lf;
    }

//    public void addNotify(){
//        super.addNotify();
//        if(thread == null){
//            thread = new Thread(this);
//            thread.start();
//        }
//    }

    @Override
    public void run(){

        resetGame();

//        long startTime;
//        long URTimeMill;
//        long waitTm;
//        long totalTime=0;
//
//        int frameCunt =0;
//        int maxFrmCunt =30;
//        long targetTime = 1000/fps;



        //game loop
        while (true){

            gameUpdate();

            if(players.get(0).isGameOver()||players.get(1).isGameOver()){
                isWon = players.get(0).isGameOver()? "2P Wins" : "1P Wins";
                g.setColor(Color.RED);
                g.setFont(new Font("Courier New", Font.BOLD ,24));
                //g.drawString(isWon , 650, 200);
                if(isWon.equals("1P Wins")) {
                    g.drawString("You win", (int) players.get(0).getx() - 15, (int) players.get(0).gety() + 80);
                }else{
                    g.drawString("You win", (int) players.get(1).getx() - 15, (int) players.get(1).gety() + 80);
                }
                gameDraw();

                if(((System.nanoTime()-gameOverTimer)/1000000) >2000)
                { this.lf.setFrame("end");
                return;}
            }

            gameRender();
            gameDraw();
            try {
                Thread.sleep(1000 / 144);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            URTimeMill = (System.nanoTime()- startTime) / 1000000;
//            waitTm = targetTime - URTimeMill;
//
//            if(waitTm>=0) {
//                try {
//                    Thread.sleep(waitTm);
//                } catch (InterruptedException ignored) {
//                }
//            }
//            totalTime += System.nanoTime() - startTime;
//            frameCunt++;
//            //System.out.println("frmCount: "+frameCunt);
//            if(frameCunt == maxFrmCunt){
//                avgFps = 1000.0/((totalTime / frameCunt)/1000000);
//                frameCunt = 0;
//                totalTime = 0;
//            }


        }
    }


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

        //generate enemies, add one more each wave
        long elapsed = (System.nanoTime() - waveTimer)/1000000;
        if(enemies.isEmpty() && elapsed>=waveDelay) {
            for (int i = 0; i < enemyNum; i++) {
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
        }

        //update enemies
        for(int i=0; i<enemies.size();i++){
            enemies.get(i).update();
        }

        //update powerUps
        for(int i=0; i<powerUps.size(); i++){
            boolean remove = powerUps.get(i).update();
            if(remove){
                powerUps.remove(i);
                i--;
            }
        }

        //update observers
        for (int i=0 ; i<observers.size(); i++){
            observers.get(i).update(players.get(i%2).getx(),players.get(i%2).gety(),players.get(i%2).getHealth(),players.get(i%2).getLives());
        }

        //***collisions***
        // bullet - enemy&player&bullet&wall collision
        for(int i=0; i<bullets.size(); i++) {
            Bullet b = bullets.get(i);

            //4 nested loops
            //1.bullet-enemy collision
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);

                double dist = Helper.getDistance(b,e);

                if (dist <= (b.getr() + e.getr())*1.3 && b.getShotBy() <2) {
                    e.hit();
                    if (e.isDead()) {
                            enemyIsDead(e,j);
                            j--;
                    }
                    if (bullets.size() > 0) {
                        bullets.remove(i);
                        i--;
                    }
                }
            }

            //2.bullet-player collision
            for (int k = 0; k < players.size(); k++) {
                Player p = players.get(k);

                double PBdist = Helper.getDistance(b,p);

                if (PBdist <= (b.getr() + p.getr()) && b.getShotBy() != p.getPlayerIndex()) {
                    p.hit();
                    if (p.isDead()) {
                        playerIsDead(p);
                    }

                    try{
                        bullets.remove(i);
                        i--;
                    }catch (IndexOutOfBoundsException ignore){

                    }

                }
            }

            //3.bullet-bullet collision
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

            //4. bullet-wall collision
            for(int j=0; j<walls.size(); j++){
                Wall w = walls.get(j);

                double dist = Helper.getDistance(b,w);

                if(dist<(b.getr()+w.getr())*1.1){
                    explosions.add(new Explosion(b.getAngle(), b.getx(), b.gety(), explosionImg));
                    if(w instanceof BreakableWall){
                        walls.remove(j);
                        j--;}
                    try {
                        bullets.remove(i);
                        i--;
                    }catch (IndexOutOfBoundsException OB){
                    }
                }
            }
        }


        //powerUP-player collision
        for (int i =0; i<powerUps.size(); i++) {
            PowerUp pow = powerUps.get(i);
            double pType = powerUps.get(i).getType();

            for(int j=0; j<players.size();j++){
                Player pla = players.get(j);

                double dist = Helper.getDistance(pow,pla);

                if(dist< (pla.getr() + pow.getr())){
                    if(pType==1) {
                        for(Enemy enemy : enemies){
                            enemy.setPickOnPlayer(true,j);
                        }
                    }
                    powerUps.remove(i);
                    i--;
                }
            }
        }

        //player-player collision
        Player pla = players.get(0);
        Player plb = players.get(1);
        double distance = Helper.getDistance(pla,plb);
        if(distance < pla.getr()*2){
            pla.checkBlockage(plb.getx(),plb.gety());
            plb.checkBlockage(pla.getx(),pla.gety());
        }





        //check for wall-player & wall-enemy collision
        for(int i=0; i<walls.size(); i++) {
            Wall w = walls.get(i);

            players.forEach(player -> player.checkBlockage(w.getx(),w.gety()));

          for(int k=0; k<enemies.size(); k++){
              Enemy e = enemies.get(k);
              double dist = Helper.getDistance(w,e);
              if(dist < ((w.getr()+e.getr()))*1.3){
                  e.bounce(w.getx(),w.gety());
              }
          }
        }

        //enemy-player collision
        for(int i=0; i<enemies.size(); i++){
            Enemy e = enemies.get(i);

            for(int j=0; j<players.size(); j++){
                Player p = players.get(j);
                double dist = Helper.getDistance(e,p);
                if(dist < e.getr()+p.getr()){
                    p.hit();
                    if(p.isDead()){
                    playerIsDead(p);}

                    e.hit();
                    if(e.isDead()){
                    enemyIsDead(e,i);}
                    i--;
                }
            }
        }

    }


    private void gameRender(){
        //render background
//        g.setColor(Color.black);
//        g.fillRect(0,0,GameConstants.GAME_SCREEN_WIDTH,GameConstants.GAME_SCREEN_HEIGHT);
        bg.draw(g);

        //draw walls
        walls.forEach(wall -> {
            wall.draw(g);
        });


        //draw players
        for(int i=0; i<players.size();i++) {
            if(!players.get(i).isGameOver()){
            players.get(i).draw(g);}
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

        //draw life
//        for(int i=0; i<players.get(0).getLives(); i++){
//           g.drawImage(p1Icon, (int)players.get(0).getx()+14*i,(int)players.get(0).gety()-12,null);
//        }
//        for(int i=0; i<players.get(1).getLives();i++){
//            g.drawImage(p2Icon, (int)players.get(1).getx()+16*i,(int)players.get(1).gety()-14,null);
//        }

        //draw health&Live

            for (int i = 0; i < observers.size(); i++) {
                if(!players.get(i%2).isGameOver())
                observers.get(i).draw(g);
            }


        //player 1
//        if(!players.get(0).isGameOver()) {
//            g.setColor(Color.GREEN);
//            g.drawRect((int) players.get(0).getx() + 7, (int) players.get(0).gety() + 57, 45, 3);
//            double healthPercent = (double) players.get(0).getHealth() / 3.00;
//            int healthLeft = (int) (45 * healthPercent);
//            g.fillRect((int) players.get(0).getx() + 7, (int) players.get(0).gety() + 57, healthLeft, 3);
//        }

        //player 2
//        if(!players.get(1).isGameOver()){
//            g.setColor(Color.GREEN);
//            g.drawRect((int)players.get(1).getx()+7,(int)players.get(1).gety()+57,45,3);
//            double healthPercent1 = (double)players.get(1).getHealth()/3.00;
//            int healthLeft1 = (int)(45 * healthPercent1);
//            g.fillRect((int)players.get(1).getx()+7,(int)players.get(1).gety()+57, healthLeft1, 3);
//        }
    }

    //draw game to screen
    private void gameDraw(){
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

          Graphics g2 = this.getGraphics();


          //one screen
//        g2.drawImage(image,0,0,null);
//        g2.dispose();

        //don't need for either
//        BufferedImage mySubimage1 = image.getSubimage(0, 0, 750, 900);
//        BufferedImage mySubimage2 = image.getSubimage(750,0,750,900);
//        Image miniMap = image.getScaledInstance(267,200, Image.SCALE_FAST);
//
//        g2.drawImage(mySubimage1,0,0,null);
//        g2.drawImage(mySubimage2,750,0,null);
//        g2.drawImage(miniMap, 607, 680, null);
//        g2.dispose();
        //don't need for either


        //split screen

        int xLeft, yLeft, xRight, yRight, width, height;
        width = 400;
        height = 600;
        xLeft = (int) (players.get(0).getx() - 120);
        yLeft = (int) (players.get(0).gety() - 300);
        xRight = (int) (players.get(1).getx() - 120);
        yRight = (int) (players.get(1).gety() - 300);

        if (xLeft < 0) {
            xLeft = 0;
        }

        if (xRight < 0) {
            xRight = 0;
        }

        if (yLeft < 0) {
            yLeft = 0;
        }

        if (yRight < 0) {
            yRight = 0;
        }
        if (xLeft + width > 1490) {
            xLeft = 1095;
        }

        if (xRight + width > 1490) {
            xRight = 1095;
        }

        if (yLeft + height > 900) {
            yLeft = 300;
        }

        if (yRight + height > 900) {
            yRight = 300;
        }

        leftSide = image.getSubimage(xLeft, yLeft, width, height);
        rightSide = image.getSubimage(xRight, yRight, width, height);

        leftSide = leftSide.getScaledInstance(800, 797, Image.SCALE_FAST);
        rightSide = rightSide.getScaledInstance(800, 797, Image.SCALE_FAST);

        //miniMap = image.getScaledInstance(350, 225, Image.SCALE_FAST);
        miniMap = image.getScaledInstance(175, 115, Image.SCALE_FAST);


        // draw left side, right side and minimap images on screen
        g2.drawImage(leftSide, 0, 0, this);
        g2.drawImage(rightSide, 800, 0, this);
        //g2.drawImage(miniMap, 575, 670, this);
        g2.drawImage(miniMap, 700, 795, this);
        g2.dispose();


    }

    public void gameInit() {
        image = new BufferedImage(GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();


        BufferedImage bgImg = Helper.loadImg("resources/Background.bmp");
        bg = new Background(bgImg);
        players = new ArrayList<Player>();

        //create player 1
        BufferedImage t1img = Helper.loadImg("resources/Tank1.png");
        players.add( new Player(200, 200, 0, 0, 0, t1img,this));
        PlayerControl tc1 = new PlayerControl(players.get(0), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_COMMA);
        this.addKeyListener(tc1);

        //create player 2
        BufferedImage t2img = Helper.loadImg("resources/Tank2.png");
        players.add(new Player(1000, 200, 0, 0, 180, t2img, this));
        PlayerControl tc2 = new PlayerControl(players.get(1), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        this.addKeyListener(tc2);

        walls = new ArrayList<Wall>();
        bullets = new ArrayList<Bullet>();
        enemies = new ArrayList<Enemy>();
        explosions = new ArrayList<Explosion>();
        powerUps = new ArrayList<PowerUp>();
        observers = new ArrayList<Observer>();

        observers.add(new Observer(players.get(0).getx(),players.get(0).gety(), players.get(0).getHealth(), players.get(0).getLives(),p1Icon));
        observers.add(new Observer(players.get(1).getx(),players.get(1).gety(), players.get(1).getHealth(),players.get(0).getLives(),p2Icon));

//        observers.add(new Lives(players.get(0).getx(),players.get(0).gety(), players.get(0).getHealth(),players.get(0).getLives(),p1Icon));
//        observers.add(new Lives(players.get(1).getx(),players.get(1).gety(), players.get(1).getHealth(),players.get(1).getLives(),p2Icon));

        //create walls
        setWalls();
    }

    public void resetGame(){


        bullets.clear();
        enemies.clear();
        enemyNum = GameConstants.START_ENEMY_NO;
        explosions.clear();
        powerUps.clear();

        walls.clear();
        setWalls();

        //reset player
        for(int i=0; i<players.size(); i++) {
            players.get(i).setX(i==0? 500:1000);
            players.get(i).resetPlayers();
        }
        gameOverTimer = 0;

    }

    public void popExplosion(){
        explosions.remove(explosions.size()-1);
    }

    private void playerIsDead(Player p){
        GamePanel.explosions.add(new Explosion(p.getAngle(), p.getx(), p.gety(), explosionImg));
        p.setX(p.getPlayerIndex()==0? 500:1000);
        p.setY(200);
        p.resetAngle();
        p.setDead(false);
        gameOverTimer = System.nanoTime();
    }

    private void enemyIsDead(Enemy e, int j){
        GamePanel.explosions.add(new Explosion(e.getAngle(), e.getx(), e.gety(), explosionImg));
        enemies.remove(j);

        //chances for getting powerups
        double chance = Math.random();
        if(chance<0.9){
            powerUps.add(new PowerUp(1, e.getx(),e.gety(),powerUpImg));
        }

        if (enemies.isEmpty()) waveTimer = System.nanoTime();

    }

    public void setWalls (){

        for(double i =20; i<550; ){
            walls.add(new BreakableWall(i,400, breakablWallImg));
            i += 32;
        }

        for(double i =150; i<650; ){
            walls.add(new UnBreakableWall(750,i, unBreakablWallImg));
            i += 32;
        }
    }

    public void addBullets(double angle, double x,double y,int playerIndex, BufferedImage bulletImg){
        bullets.add(new Bullet(angle, x, y, playerIndex,bulletImg));
    }

    public String getWhoWon() {return isWon;}

}

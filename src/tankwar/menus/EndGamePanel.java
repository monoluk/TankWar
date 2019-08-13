package tankwar.menus;

import tankwar.GameConstants;
import tankwar.Launcher;
import tankwar.game.GamePanel;
import tankwar.game.Helper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;



public class EndGamePanel extends JPanel {

    private BufferedImage menuBackground;
    private JButton start;
    private JButton exit;
    private Launcher lf;
    private Graphics2D g;

    private String whoWins;
    private int p1WinCount;
    private int p2WinCount;
    private int inRow=1;

    public EndGamePanel(Launcher lf) {
        this.lf = lf;
//        try {
//            menuBackground = ImageIO.read(this.getClass().getClassLoader().getResource("title.png"));
//        } catch (IOException e) {
//            System.out.println("Error cant read menu background");
//            e.printStackTrace();
//            System.exit(-3);
//        }
        //menuBackground = Helper.loadImg("resources/2p.png");


        this.setBackground(Color.BLACK);
        this.setLayout(null);

        start = new JButton("Restart Game");
        start.setFont(new Font("Courier New", Font.BOLD ,24));
        start.setBounds(136,300,205,50);
        start.addActionListener((actionEvent -> {
            this.lf.setFrame("game");
        }));


        exit = new JButton("Exit");
        exit.setFont(new Font("Courier New", Font.BOLD ,24));
        exit.setBounds(150,400,175,50);
        exit.addActionListener((actionEvent -> {
            this.lf.closeGame();
        }));


        this.add(start);
        this.add(exit);


    }

    public void setWinner(GamePanel Gp){
        if(whoWins != null) {
            if (whoWins.equals(Gp.getWhoWon())) {
                inRow++;
            } else {
                inRow = 1;
            }
        }

        whoWins = Gp.getWhoWon();
        menuBackground = Helper.loadImg(whoWins.equals("1P Wins")? "resources/1p.png":"resources/2p.png");
        if(whoWins.equals("1P Wins")){
            p1WinCount++;
        }else{
            p2WinCount++;
        }
    }

    @Override
    public void paintComponent(Graphics g){

        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.menuBackground,0,0,null);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Courier New", Font.BOLD ,24));
        if(inRow<2){
            g.drawString(whoWins, 190, 50);
        }else{
        g.drawString(whoWins +" "+inRow + " times in a row!", 90, 50);}
        g.drawString("1P Wins: "+p1WinCount, 100, 80);
        g.drawString("2P Wins: "+p2WinCount, 270, 80);

    }


}

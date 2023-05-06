import java.awt.*;

public class Menu {
    public  Rectangle playButton = new Rectangle(300, 250, 200, 50);
    public int HighScore = 0;
    public long HighServivalTime = 0;
    private int size = 800;
    public void render(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(Color.decode("#1E1E1E"));
        g.fillRect(0, 0, size, size);
        Font fnt0 = new Font("arial", Font.BOLD, 50);
        g.setFont(fnt0);
        g.setColor(Color.white);
        g.drawString("Eat My Bullet",250, 100);
        Font fnt1 = new Font("arial", Font.BOLD, 30);
        g.setFont(fnt1);
        g.setColor(Color.white);
        g.drawString("High Score : " + HighScore, 300, 150);
        g.drawString("High Survival Time : " + HighServivalTime, 300, 200);
        g.drawString("Play", playButton.x + 70, playButton.y + 35);
        g2d.draw(playButton);
    }
}

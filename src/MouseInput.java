import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseInput implements MouseListener {

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
//      public  Rectangle playButton = new Rectangle(300, 250, 200, 50);
        int mx = e.getX();
        int my = e.getY();
        if (mx >= 300 && mx <= 500) {
            if (my >= 250 && my <= 350) {
                Window.gameState = Window.GameState.GAME;

            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

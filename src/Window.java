import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Window extends JFrame implements Observer {

    private Renderer renderer;
    private MainMenu mainMenu;
    private World world;
    private int size = 800; // window size
    private int worldSize = 22;
    private long delayed = 40; // game update delay

    public int highScore = 0;
    public long servivalTime = 0;

    private List<Integer> keyCode = new ArrayList<Integer>(); // storing keystroke
    private List<Integer> keyList = List.of(KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D,
            KeyEvent.VK_SPACE);

    public Window() {
        super();
        addKeyListener(new KeyController());
        setLayout(new BorderLayout());
        setSize(size + 10, size + 30);
        setResizable(false);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainMenu = new MainMenu();
        add(mainMenu, BorderLayout.CENTER);
        world = new World(worldSize);
        world.addObserver(this);
        addKeyListener(new KeyController());
    }

    @Override
    public void update(Observable o, Object arg) {
        renderer.repaint();
        moveCommand();

        if (world.isGameOver()) {
            /// update High score
            if (world.getScore() > highScore) {
                highScore = world.getScore();
                servivalTime = world.getElapsedTime();
            }
            JOptionPane.showMessageDialog(Window.this,
                    "Game Over",
                    "",
                    JOptionPane.INFORMATION_MESSAGE);
            Window.this.remove(renderer);
            Window.this.pack();
            mainMenu = new MainMenu();
            Window.this.add(mainMenu, BorderLayout.CENTER);
            Window.this.setSize(size + 10, size + 30);
            revalidate();
            repaint();
            keyCode.clear();
            world = new World(worldSize);
            world.addObserver(this);
            addKeyListener(new KeyController());
        }
        waitFor(delayed);
    }

    private void waitFor(long delayed) {
        try {
            Thread.sleep(delayed);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Renderer extends JPanel {
        // player
        public final Image playerN;
        public final Image playerNE;
        public final Image playerE;
        public final Image playerSE;
        public final Image playerS;
        public final Image playerSW;
        public final Image playerW;
        public final Image playerNW;
        // cTrooper
        public final Image cTrooperN;
        public final Image cTrooperNE;
        public final Image cTrooperE;
        public final Image cTrooperSE;
        public final Image cTrooperS;
        public final Image cTrooperSW;
        public final Image cTrooperW;
        public final Image cTrooperNW;
        // sManiac
        public final Image sManiacN;
        public final Image sManiacNE;
        public final Image sManiacE;
        public final Image sManiacSE;
        // sManiacT2
        public final Image sManiacT2N;
        public final Image sManiacT2NE;

        public Renderer() {
            setDoubleBuffered(true);
            // player
            playerN = new ImageIcon("sprite/player/playerN.png").getImage();
            playerNE = new ImageIcon("sprite/player/playerNE.png").getImage();
            playerE = new ImageIcon("sprite/player/playerE.png").getImage();
            playerSE = new ImageIcon("sprite/player/playerSE.png").getImage();
            playerS = new ImageIcon("sprite/player/playerS.png").getImage();
            playerSW = new ImageIcon("sprite/player/playerSW.png").getImage();
            playerW = new ImageIcon("sprite/player/playerW.png").getImage();
            playerNW = new ImageIcon("sprite/player/playerNW.png").getImage();
            // cTrooper
            cTrooperN = new ImageIcon("sprite/cTrooper/cTrooperN.png").getImage();
            cTrooperNE = new ImageIcon("sprite/cTrooper/cTrooperNE.png").getImage();
            cTrooperE = new ImageIcon("sprite/cTrooper/cTrooperE.png").getImage();
            cTrooperSE = new ImageIcon("sprite/cTrooper/cTrooperSE.png").getImage();
            cTrooperS = new ImageIcon("sprite/cTrooper/cTrooperS.png").getImage();
            cTrooperSW = new ImageIcon("sprite/cTrooper/cTrooperSW.png").getImage();
            cTrooperW = new ImageIcon("sprite/cTrooper/cTrooperW.png").getImage();
            cTrooperNW = new ImageIcon("sprite/cTrooper/cTrooperNW.png").getImage();
            // sManiac
            sManiacN = new ImageIcon("sprite/sManiac/sManiacN.png").getImage();
            sManiacNE = new ImageIcon("sprite/sManiac/sManiacNE.png").getImage();
            sManiacE = new ImageIcon("sprite/sManiac/sManiacE.png").getImage();
            sManiacSE = new ImageIcon("sprite/sManiac/sManiacSE.png").getImage();
            // sManiacT2
            sManiacT2N = new ImageIcon("sprite/sManiacT2/sManiacT2N.png").getImage();
            sManiacT2NE = new ImageIcon("sprite/sManiacT2/sManiacT2NE.png").getImage();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setFont(new Font("arial", Font.PLAIN, 20));
            paintGrids(g);
            // WObject
            paintPlayerBullets(g);
            paintEnemyBullets(g);
            paintItems(g);
            paintCTrooper(g);
            paintSTrooper(g);
            paintSManiac(g);
            paintSManiacT2(g);
            paintPlayer(g);
            paintDeadlyTie(g);
            // GUI
            paintPlayerImmortal(g);
            paintPlayerLife(g);
            paintScore(g);
            paintTimer(g);
        }

        private void paintGrids(Graphics g) {
            g.setColor(Color.decode("#24201C"));
            g.fillRect(0, 0, size, size);
        }

        private void paintPlayer(Graphics g) {
            int perCell = size / world.getSize();
            int x = world.getPlayer().getX();
            int y = world.getPlayer().getY();
            Image image;
            switch (world.getPlayer().getDirection()) {
                case "N":
                    image = playerN;
                    break;
                case "NE":
                    image = playerNE;
                    break;
                case "E":
                    image = playerE;
                    break;
                case "SE":
                    image = playerSE;
                    break;
                case "S":
                    image = playerS;
                    break;
                case "SW":
                    image = playerSW;
                    break;
                case "W":
                    image = playerW;
                    break;
                case "NW":
                    image = playerNW;
                    break;
                default:
                    image = playerN;
            }
            g.drawImage(image, x * perCell, y * perCell, perCell, perCell, null, null);
        }

        private void paintCTrooper(Graphics g) {
            int perCell = size / world.getSize();
            for (Enemy e : world.getCTroopers()) {
                int x = e.getX();
                int y = e.getY();
                Image image;
                switch (e.getDirection()) {
                    case "N":
                        image = cTrooperN;
                        break;
                    case "NE":
                        image = cTrooperNE;
                        break;
                    case "E":
                        image = cTrooperE;
                        break;
                    case "SE":
                        image = cTrooperSE;
                        break;
                    case "S":
                        image = cTrooperS;
                        break;
                    case "SW":
                        image = cTrooperSW;
                        break;
                    case "W":
                        image = cTrooperW;
                        break;
                    case "NW":
                        image = cTrooperNW;
                        break;
                    default:
                        image = cTrooperN;
                }
                g.drawImage(image, x * perCell + 6, y * perCell + 6, perCell - 12, perCell - 12, null, null);
            }
        }

        private void paintSManiac(Graphics g) {
            int perCell = size / world.getSize();
            for (Enemy e : world.getSManiacs()) {
                int x = e.getX();
                int y = e.getY();
                Image image;
                switch (e.getDirection()) {
                    case "N":
                    case "S":
                        image = sManiacN;
                        break;
                    case "NE":
                    case "SW":
                        image = sManiacNE;
                        break;
                    case "E":
                    case "W":
                        image = sManiacE;
                        break;
                    case "SE":
                    case "NW":
                        image = sManiacSE;
                        break;
                    default:
                        image = sManiacN;
                }
                g.drawImage(image, x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }

        private void paintSManiacT2(Graphics g) {
            int perCell = size / world.getSize();
            for (Enemy e : world.getSManiacT2s()) {
                int x = e.getX();
                int y = e.getY();
                Image image;
                switch (e.getDirection()) {
                    case "N":
                    case "E":
                    case "S":
                    case "W":
                        image = sManiacT2N;
                        break;
                    case "NE":
                    case "SE":
                    case "SW":
                    case "NW":
                        image = sManiacT2NE;
                        break;
                    default:
                        image = sManiacT2N;
                }
                g.drawImage(image, x * perCell, y * perCell, perCell, perCell, null, null);
            }
        }

        private void paintSTrooper(Graphics g) {
            int perCell = size / world.getSize();
            g.setColor(Color.decode("#F5F4EE"));
            for (Enemy e : world.getSTroopers()) {
                int x = e.getX();
                int y = e.getY();
                g.fillRect(x * perCell + 6, y * perCell + 6, perCell - 12, perCell - 12);
            }
        }

        private void paintDeadlyTie(Graphics g) {
            int perCell = size / world.getSize();
            g.setColor(Color.decode("#FE2C1F"));
            for (DeadlyTie t : world.getDeadlyTies()) {
                int x = t.getX();
                int y = t.getY();
                g.fillRect(x * perCell, y * perCell, perCell, perCell);
            }
        }

        private void paintEnemyBullets(Graphics g) {
            int perCell = size / world.getSize();
            for (Bullet b : world.getEnemyBullets()) {
                if (b.getLife() < 0) {
                    g.setColor(Color.decode("#CA235D"));
                } else {
                    g.setColor(Color.decode("#E2801D"));
                }
                int x = b.getX();
                int y = b.getY();
                g.fillOval(x * perCell + 8, y * perCell + 8, perCell - 16, perCell - 16);
            }
        }

        private void paintPlayerBullets(Graphics g) {
            int perCell = size / world.getSize();
            g.setColor(Color.white);
            for (Bullet b : world.getPlayerBullets()) {
                int x = b.getX();
                int y = b.getY();
                g.fillOval(x * perCell + 8, y * perCell + 8, perCell - 16, perCell - 16);
            }
        }

        private void paintItems(Graphics g) {
            int perCell = size / world.getSize();
            g.setColor(Color.red);
            for (Item i : world.getItems()) {
                if (i.getLife() == 0) { // life item
                    g.setColor(Color.decode("#0BD92C"));
                } else if (i.getLife() == 1) { // immortal item
                    g.setColor(Color.decode("#004FE9"));
                }
                int x = i.getX();
                int y = i.getY();
                g.fillOval(x * perCell + 12, y * perCell + 12, perCell - 24, perCell - 24);
            }
        }

        private void paintPlayerImmortal(Graphics g) {
            int playerImmuneTime = world.getPlayerImmuneTime();
            g.setColor(Color.white);
            if (!world.isGameOver() && playerImmuneTime > 0) {
                g.drawString("Immortal", (size / 2) - 20, 20);
            }
        }

        private void paintPlayerLife(Graphics g) {
            int playerLife = world.getPlayer().getLife();
            g.setColor(Color.white);
            if (!world.isGameOver()) {
                g.drawString("Lives: " + playerLife, 10, size - 20);
            }
        }

        private void paintScore(Graphics g) {
            int score = world.getScore();
            g.setColor(Color.white);
            if (!world.isGameOver()) {
                g.drawString("Score: " + score, (size / 2) - 30, size - 20);
            }
        }

        private void paintTimer(Graphics g) {
            long time = world.getElapsedTime();
            g.setColor(Color.white);
            if (!world.isGameOver()) {
                g.drawString("Survival Time: " + time / 60 + ":" + time % 60, size - 180, size - 20);
            }
        }
    }

    class MainMenu extends JPanel {

        private JButton startButton;
        public int HighScore = 0;
        public long HighServivalTime = 0;

        public MainMenu() {
            setLayout(null);
            setBackground(Color.decode("#24201C"));
            startButton = new JButton("Start");
            startButton.setBackground(Color.decode("#24201C"));
            startButton.setBorderPainted(false);
            startButton.setForeground(Color.WHITE);
            startButton.setFont(new Font("arial", Font.PLAIN, 40));
            startButton.setBounds(320, 350, 150, 100);
            startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Window.this.requestFocus();
                    Window.this.remove(mainMenu);
                    Window.this.pack();
                    renderer = new Renderer();
                    Window.this.add(renderer, BorderLayout.CENTER);
                    Window.this.setSize(size + 10, size + 30);
                    revalidate();
                    repaint();
                    world.startGame();
                }
            });
            add(startButton);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintMenu(g);
        }

        private void paintMenu(Graphics g) {
            g.setColor(Color.decode("#24201C"));
            g.fillRect(0, 0, size, size);
            Font fnt0 = new Font("arial", Font.BOLD, 50);
            g.setFont(fnt0);
            g.setColor(Color.white);
            g.drawString("Eat My Bullet", 250, 200);
            if (highScore > 0) {
                Font fnt1 = new Font("arial", Font.PLAIN, 25);
                g.setFont(fnt1);
                g.setColor(Color.white);
                g.drawString("Current High Score " + highScore, 280, 250);
                g.drawString("Survival Time " + servivalTime / 60 + ":" + servivalTime % 60, 300, 300);
            }
        }
    }

    class KeyController extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (keyList.contains(e.getKeyCode()) && !keyCode.contains(e.getKeyCode())) {
                keyCode.add(e.getKeyCode());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            List<Integer> toRemoveKey = new ArrayList<Integer>();
            if (keyCode.contains(e.getKeyCode())) {
                toRemoveKey.add(e.getKeyCode());
            }
            keyCode.removeAll(toRemoveKey);
        }
    }

    private void moveCommand() {
        Player player = world.getPlayer();
        Command c;
        if (keyCode.contains(KeyEvent.VK_W) && keyCode.contains(KeyEvent.VK_D)) {
            c = new CommandTurnNorthEast(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_S) && keyCode.contains(KeyEvent.VK_D)) {
            c = new CommandTurnSouthEast(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_S) && keyCode.contains(KeyEvent.VK_A)) {
            c = new CommandTurnSouthWest(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_W) && keyCode.contains(KeyEvent.VK_A)) {
            c = new CommandTurnNorthWest(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_W)) {
            c = new CommandTurnNorth(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_D)) {
            c = new CommandTurnEast(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_S)) {
            c = new CommandTurnSouth(player);
            c.execute();
        } else if (keyCode.contains(KeyEvent.VK_A)) {
            c = new CommandTurnWest(player);
            c.execute();
        }

        if (keyCode.contains(KeyEvent.VK_SPACE)) {
            world.burstPlayerBullets(2);
        }
    }

    public static void main(String[] args) {
        Window window = new Window();
        window.setVisible(true);
    }

}

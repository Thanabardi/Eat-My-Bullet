import java.util.Observable;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World extends Observable {
    private Thread thread;
    private int size;
    private int tick;
    private Random random;
    private boolean isOver;

    private Player player;
    private int playerLife;

    private int cTrooperCount;
    private int cTrooperLife;

    private int sTrooperCount;
    private int sTrooperLife;

    private int sManiacCount;
    private int sManiacLife;

    private int sManiacT2Count;
    private int sManiacT2Life;

    private int deadlyTieCount;
    private int deadlyTieLife;

    private BulletPool bulletPool;
    private List<Bullet> playerBullets;
    private List<Bullet> enemyBullets;

    private List<Enemy> cTroopers = new ArrayList<Enemy>();
    private List<Enemy> sTroopers = new ArrayList<Enemy>();
    private List<Enemy> sManiacs = new ArrayList<Enemy>();
    private List<Enemy> sManiacT2s = new ArrayList<Enemy>();

    private List<Item> items = new ArrayList<Item>();

    private List<DeadlyTie> deadlyTies = new ArrayList<DeadlyTie>();

    private int immuneAfterHit;
    private int itemDropRate;
    private int playerImmuneTime;
    private int itemImmuneTime;
    private int score;
    private long startTime;
    private long elapsedTime;

    public World(int worldSize) {
        playerLife = Config.initPlayerLife;

        cTrooperCount = Config.initCTrooperCount;
        cTrooperLife = Config.initCTrooperLife;

        sTrooperCount = Config.initSTrooperCount;
        sTrooperLife = Config.initSTrooperLife;

        sManiacCount = Config.initSManiacCount;
        sManiacLife = Config.initSManiacLife;

        sManiacT2Count = Config.initSManiacT2Count;
        sManiacT2Life = Config.initSManiacT2Life;

        deadlyTieCount = Config.initDeadlyTieCount;
        deadlyTieLife = Config.initDeadlyTieLife;

        immuneAfterHit = Config.immuneAfterHit;
        itemImmuneTime = Config.itemImmuneTime;
        itemDropRate = Config.itemDropRate;

        size = worldSize;

        bulletPool = new BulletPool();
        playerBullets = new ArrayList<Bullet>();
        enemyBullets = new ArrayList<Bullet>();
        items = new ArrayList<Item>();

        player = new Player(size / 2, (int) Math.round(size / 1.5));
    }

    public void startGame() { // start game
        startTime = System.currentTimeMillis();
        random = new Random();
        score = 0;
        playerImmuneTime = 0;
        tick = 0;
        isOver = false;

        player.setObject(size / 2, (int) Math.round(size / 1.5), 0, 0, playerLife);
        createDeadlyTie(deadlyTieCount);
        createEnemy(cTroopers, cTrooperCount, cTrooperLife);
        createEnemy(sTroopers, sTrooperCount, sTrooperLife);
        createEnemy(sManiacs, sManiacCount, sManiacLife);
        createEnemy(sManiacT2s, sManiacT2Count, sManiacT2Life);
        thread = new Thread() {
            @Override
            public void run() {
                while (!isOver) {
                    tick++;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    moveBullets(playerBullets, 1); // move player bullets
                    moveBullets(enemyBullets, 20); // move enemy bullets

                    BulletHitEnemy(cTroopers);
                    BulletHitEnemy(sTroopers);
                    BulletHitEnemy(sManiacs);
                    BulletHitEnemy(sManiacT2s);
                    BulletHitDeadlyTie(deadlyTies);
                    BulletHit(); // check bullet hit

                    movePlayer(player, 4);
                    touchDeadlyTie();

                    moveEnemyFollow(cTroopers, 20, 2);
                    bustEnemyBullets(cTroopers, 1, 40, 6); // bust cTroopers bullets
                    bustEnemyBullets(sTroopers, 4, 40, 5); // bust cTroopers bullets
                    moveEnemyRandom(sManiacs, 20, 2);
                    bustEnemyBullets(sManiacs, 2, 40, 4); // bust cTroopers bullets
                    moveEnemyRandom(sManiacT2s, 20, 2);
                    bustEnemyBullets(sManiacT2s, 4, 40, 3); // bust cTroopers bullets

                    collectItem();
                    updatePlayerStatus();
                    addEnemy(50);
                    reducePoolSize();
                    setChanged();
                    notifyObservers();
                }
            }
        };
        thread.start();
    }

    private void createDeadlyTie(int countTie) {
        int x;
        int y;
        for (int i = 0; i < countTie; i++) {
            deadlyTies.add(new DeadlyTie(0, 0));
            while (true) {
                x = random.nextInt(1, size - 1);
                y = random.nextInt(1, size - 1);
                if (collideCheck(x, y) && x != player.getX() && y != player.getY()) {
                    break;
                }
            }
            deadlyTies.get(i).setObject(x, y, 0, -1, deadlyTieLife);
        }
    }

    private void createEnemy(List<Enemy> enemyList, int countEnemy, int enemyLife) {
        int x;
        int y;
        for (int i = 0; i < countEnemy; i++) {
            enemyList.add(new Enemy(0, 0));
            while (true) {
                x = random.nextInt(1, size - 1);
                y = random.nextInt(1, size - 1);
                if (collideCheck(x, y) && x != player.getX() && y != player.getY()) {
                    break;
                }
            }
            enemyList.get(i).setObject(x, y, 0, -1, enemyLife);
        }
    }

    private void addEnemy(int interval) {
        if (tick % interval == 0) {
            int randomNum = random.nextInt(10);
            if (randomNum < 1) { // 10%
                createEnemy(sManiacT2s, 1, sManiacT2Life);
            } else if (randomNum < 2) { // 10%
                createEnemy(sTroopers, 1, sTrooperLife);
            } else if (randomNum < 4) { // 20%
                createDeadlyTie(1);
            } else if (randomNum < 6) { // 20%
                createEnemy(cTroopers, 1, cTrooperLife);
            } else if (randomNum < 10) { // 40%
                createEnemy(sManiacs, 1, sManiacLife);
            }
        }
    }

    private void movePlayer(Player p, int delayed) {
        if (tick % delayed == 0) {
            if (collideCheck(p.getX() + p.getdX(), p.getY() + p.getdY())) {
                p.move();
                updateBulletDirection(player);
            }
            p.reset();
        }
    }

    private void moveEnemyFollow(List<Enemy> enemyList, int moveDelayed, int offset) {
        if (tick % moveDelayed == 0) {
            int playerX = player.getX();
            int playerY = player.getY();
            for (Enemy enemy : enemyList) {
                int enemyX = enemy.getX();
                int enemyY = enemy.getY();
                if (enemyX < playerX - offset && enemyY < playerY - offset) {
                    enemy.turnSouthEast();
                } else if (enemyX < playerX - offset && enemyY > playerY + offset) {
                    enemy.turnNorthEast();
                } else if (enemyX > playerX + offset && enemyY < playerY - offset) {
                    enemy.turnSouthWest();
                } else if (enemyX > playerX + offset && enemyY > playerY + offset) {
                    enemy.turnNorthWest();
                } else if (enemyX == playerX && enemyY > playerY + offset) {
                    enemy.turnNorth();
                } else if (enemyX == playerX && enemyY < playerY - offset) {
                    enemy.turnSouth();
                } else if (enemyX > playerX + offset && enemyY == playerY) {
                    enemy.turnWest();
                } else if (enemyX < playerX - offset && enemyY == playerY) {
                    enemy.turnEast();
                }
                // collide deadlyTie and map border
                if (collideCheck(enemyX + enemy.getdX(), enemyY + enemy.getdY())) {
                    enemy.move();
                    updateBulletDirection(enemy); 
                }
                enemy.reset();
            }
        }
    }

    private void moveEnemyRandom(List<Enemy> enemyList, int moveDelayed, int offset) {
        if (tick % moveDelayed == 0) {
            for (Enemy enemy : enemyList) {
                switch (random.nextInt(8)) {
                    case 0:
                        enemy.turnNorth();
                        break;
                    case 1:
                        enemy.turnNorthEast();
                        break;
                    case 2:
                        enemy.turnEast();
                        break;
                    case 3:
                        enemy.turnSouthEast();
                        break;
                    case 4:
                        enemy.turnSouth();
                        break;
                    case 5:
                        enemy.turnSouthWest();
                        break;
                    case 6:
                        enemy.turnWest();
                        break;
                    case 7:
                        enemy.turnNorthWest();
                        break;
                }
                // collide deadlyTie and map border
                if (collideCheck(enemy.getX() + enemy.getdX(), enemy.getY() + enemy.getdY())) {
                    enemy.move();
                    updateBulletDirection(enemy);   
                }
                enemy.reset();
            }
        }
    }

    private void bustEnemyBullets(List<Enemy> enemyList, int Way, int fireRate, int bulletImmuneRate) {
        if (tick % fireRate == 0) {
            int life;
            for (Enemy enemy : enemyList) {
                int x = enemy.getX();
                int y = enemy.getY();
                int bx = enemy.getbX();
                int by = enemy.getbY();
                life = ((random.nextInt(bulletImmuneRate) == 0) ? -1 : 1);
                for (int i = 0; i < Way; i++) {
                    if (i == 0) {
                        enemyBullets.add(bulletPool.requestBullet(x, y, bx, by, life));
                    } else if (i == 1) {
                        enemyBullets.add(bulletPool.requestBullet(x, y, bx * -1, by * -1, life));
                    } else if (i == 2) {
                        enemyBullets.add(bulletPool.requestBullet(x, y, by, bx, life));
                    } else if (i == 3) {
                        enemyBullets.add(bulletPool.requestBullet(x, y, by * -1, bx * -1, life));
                    }

                }
            }
        }
    }

    private boolean collideCheck(int x, int y) {
        // collide map border
        if (x <= -1 || x >= size || y <= -1 || y >= size) {
            return false;
        }
        // collide deadlyTie
        for (DeadlyTie deadlyTie : deadlyTies) {
            if (x == deadlyTie.getX() && y == deadlyTie.getY()) {
                return false;
            }
        }
        return true;
    }

    private void touchDeadlyTie() {
        for (DeadlyTie deadlyTie : deadlyTies) {
            int px = player.getX();
            int py = player.getY();
            int dx = deadlyTie.getX();
            int dy = deadlyTie.getY();
            // player in the area around deadlyTie
            if ((dx - 1 <= px && px <= dx + 1) && (dy - 1 <= py && py <= dy + 1)) {
                if (playerImmuneTime == 0) {
                    player.updateLife(-1);
                    playerImmuneTime = immuneAfterHit;
                }
                break;
            }
        }
    }

    private void updateBulletDirection(WObject object) {
        if (object.getdX() != 0 || object.getdY() != 0) {
            object.setBullet(object.getdX(), object.getdY());
        }
    }

    private void BulletHitEnemy(List<Enemy> enemyList) {
        List<Enemy> toRemoveEnemy = new ArrayList<Enemy>();
        List<Bullet> toRemoveBullet = new ArrayList<Bullet>();
        for (Bullet b : playerBullets) {
            for (Enemy enemy : enemyList) { // hit cTrooper
                if (b.hit(enemy)) {
                    enemy.updateLife(-1);
                    if (enemy.getLife() <= 0) {
                        score += 1;
                        dropItem(enemy.getX(), enemy.getY(), 3);
                        toRemoveEnemy.add(enemy);
                    }
                    toRemoveBullet.add(b);
                    break;
                }
            }
        }
        enemyList.removeAll(toRemoveEnemy);
        playerBullets.removeAll(toRemoveBullet);
    }

    private void BulletHitDeadlyTie(List<DeadlyTie> deadlyTieList) {
        List<DeadlyTie> toRemoveDeadlyTie = new ArrayList<DeadlyTie>();
        List<Bullet> toRemoveBullet = new ArrayList<Bullet>();
        for (Bullet b : playerBullets) {
            for (DeadlyTie deadlyTie : deadlyTieList) {
                if (b.hit(deadlyTie)) {
                    deadlyTie.updateLife(-1);
                    if (deadlyTie.getLife() <= 0) {
                        dropItem(deadlyTie.getX(), deadlyTie.getY(), 0);
                        toRemoveDeadlyTie.add(deadlyTie);
                    }
                    toRemoveBullet.add(b);
                    break;
                }
            }
        }
        deadlyTieList.removeAll(toRemoveDeadlyTie);
        playerBullets.removeAll(toRemoveBullet);
    }

    private void BulletHit() {
        List<Bullet> toRemoveBullet = new ArrayList<Bullet>();
        // player bullets
        for (Bullet bP : playerBullets) {
            for (Bullet bE : enemyBullets) { // hit enemy bullet
                if (bE.getLife() > 0 && bP.hit(bE)) {
                    bE.updateLife(-1);
                    if (bE.getLife() == 0) {
                        toRemoveBullet.add(bE);
                    }
                    toRemoveBullet.add(bP);
                    break;
                }
            }
            if (!collideCheck(bP.getX(), bP.getY())) { // hit deadlyTie and map border
                toRemoveBullet.add(bP);
                break;
            }
        }
        // enemy bullets
        for (Bullet b : enemyBullets) {
            if (b.hit(player)) { // hit player
                if (playerImmuneTime == 0) {
                    player.updateLife(-1);
                    playerImmuneTime = immuneAfterHit;
                }
                toRemoveBullet.add(b);
                break;
            }
            if (!collideCheck(b.getX(), b.getY())) { // hit deadlyTie and map border
                toRemoveBullet.add(b);
                break;
            }
        }
        playerBullets.removeAll(toRemoveBullet);
        enemyBullets.removeAll(toRemoveBullet);
    }

    private void dropItem(int x, int y, int dropWeight) {
        if (random.nextInt(itemDropRate + dropWeight) == 0) {
            items.add(new Item(x, y));
            // random item type 0 1
            items.get(items.size() - 1).setObject(x, y, 0, -1, random.nextInt(2));
        }
    }

    private void collectItem() {
        for (Item i : items) {
            if (player.hit(i)) {
                if (i.getLife() == 0) { // life item
                    player.updateLife(1);
                } else if (i.getLife() == 1) { // immortal item
                    playerImmuneTime = itemImmuneTime;
                }
                items.remove(i);
                break;
            }
        }
    }

    private void updatePlayerStatus() {
        if (playerImmuneTime > 0) {
            playerImmuneTime -= 1;
        }
        if (player.getLife() == 0) {
            isOver = true;
        }
    }

    private void moveBullets(List<Bullet> bullets, int delayed) {
        if (tick % delayed == 0) {
            for (Bullet bullet : bullets) {
                bullet.move();
            }
        }
    }

    private void reducePoolSize() {
        while (bulletPool.bullets.size() > 30) {
            if (System.currentTimeMillis() - bulletPool.getTime() >= 30000) {
                bulletPool.bullets.remove(0);
            } else {
                break;
            }
        }
    }

    public void burstPlayerBullets(int delayed) {
        if (tick % delayed == 0) {
            playerBullets
                    .add(bulletPool.requestBullet(player.getX(), player.getY(), player.getbX(), player.getbY(), 1));
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Bullet> getPlayerBullets() {
        return playerBullets;
    }

    public List<Bullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<Enemy> getCTroopers() {
        return cTroopers;
    }

    public List<Enemy> getSTroopers() {
        return sTroopers;
    }

    public List<Enemy> getSManiacs() {
        return sManiacs;
    }

    public List<Enemy> getSManiacT2s() {
        return sManiacT2s;
    }

    public List<DeadlyTie> getDeadlyTies() {
        return deadlyTies;
    }

    public List<Item> getItems() {
        return items;
    }

    public int getTick() {
        return tick;
    }

    public int getSize() {
        return size;
    }

    public boolean isGameOver() {
        return isOver;
    }

    public int getPlayerImmuneTime() {
        return playerImmuneTime;
    }

    public int getScore() {
        return score;
    }

    public long getElapsedTime() {
        return elapsedTime / 1000;
    }
}

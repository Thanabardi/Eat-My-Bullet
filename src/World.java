import java.util.Observable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World extends Observable {
    private int tick;
    private int size;
    private boolean notOver;
    private Thread thread;

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

    private List<Bullet> playerBullets;
    private List<Bullet> enemyBullets;
    private BulletPool bulletPool;

    private Player player;
    private List<Enemy> cTroopers = new ArrayList<Enemy>();
    private List<Enemy> sTroopers = new ArrayList<Enemy>();
    private List<Enemy> sManiacs = new ArrayList<Enemy>();
    private List<Enemy> sManiacT2s = new ArrayList<Enemy>();

    private List<Item> items = new ArrayList<Item>();

    private List<DeadlyTie> deadlyTies = new ArrayList<DeadlyTie>();

    private int playerImmuneTime;
    private int immuneAfterHit;
    private int itemImmuneTime;
    private int score;
    private long startTime;
    private long elapsedTime;

    private int itemDropRate;

    public World(int size) {
        Config config = new Config();

        playerLife = config.initPlayerLife;

        cTrooperCount = config.initCTrooperCount;
        cTrooperLife = config.initCTrooperLife;

        sTrooperCount = config.initSTrooperCount;
        sTrooperLife = config.initSTrooperLife;

        sManiacCount = config.initSManiacCount;
        sManiacLife = config.initSManiacLife;

        sManiacT2Count = config.initSManiacT2Count;
        sManiacT2Life = config.initSManiacT2Life;

        deadlyTieCount = config.initDeadlyTieCount;
        deadlyTieLife = config.initDeadlyTieLife;

        immuneAfterHit = config.immuneAfterHit;
        itemImmuneTime = config.itemImmuneTime;
        itemDropRate = config.itemDropRate;
        this.size = size;

        player = new Player(size / 2, size / 2);
        bulletPool = new BulletPool();
        playerBullets = new ArrayList<Bullet>();
        enemyBullets = new ArrayList<Bullet>();

        items = new ArrayList<Item>();

        createDeadlyTie(deadlyTieCount);

        createEnemy(cTroopers, cTrooperCount, cTrooperLife);
        createEnemy(sTroopers, sTrooperCount, sTrooperLife);
        createEnemy(sManiacs, sManiacCount, sManiacLife);
        createEnemy(sManiacT2s, sManiacT2Count, sManiacT2Life);
    }

    private void createDeadlyTie(int countTie) {
        Random random = new Random();
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
        Random random = new Random();
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

    public void startGame() { // start game
        startTime = System.currentTimeMillis();
        score = 0;
        player.setObject(size / 2, size / 2, 0, 0, playerLife);
        playerImmuneTime = 0;
        tick = 0;
        notOver = true;
        thread = new Thread() {
            @Override
            public void run() {
                while (notOver) {
                    tick++;
                    elapsedTime = System.currentTimeMillis() - startTime;
                    moveBullets(playerBullets, 1); // move player bullets
                    moveBullets(enemyBullets, 20); // move enemy bullets
                    BulletHit();
                    updateBulletDirection(player);
                    movePlayer(player, 4);
                    touchDeadlyTie();

                    moveEnemyFollow(cTroopers, 20, 2);
                    bustEnemyBullets(cTroopers, 1, 40, 5); // bust cTroopers bullets

                    bustEnemyBullets(sTroopers, 4, 40, 5); // bust cTroopers bullets

                    moveEnemyRandom(sManiacs, 20, 2);
                    bustEnemyBullets(sManiacs, 2, 40, 5); // bust cTroopers bullets

                    moveEnemyRandom(sManiacT2s, 20, 2);
                    bustEnemyBullets(sManiacT2s, 4, 40, 5); // bust cTroopers bullets
                    collectItem();
                    updatePlayerStatus();
                    reducePoolSize();
                    addEnemy(50);
                    setChanged();
                    notifyObservers();
                }
            }
        };
        thread.start();
    }

    private void addEnemy(int interval) {
        if (tick % interval == 0) {
            Random random = new Random();
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
                boolean move = true;
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
                if (collideCheck(enemyX + enemy.getdX(), enemyY + enemy.getdY())) { // collide deadlyTie and map border
                    if (move) {
                        enemy.move();
                        updateBulletDirection(enemy);
                        enemy.reset();
                    }

                }
            }
        }
    }

    private void moveEnemyRandom(List<Enemy> enemyList, int moveDelayed, int offset) {
        if (tick % moveDelayed == 0) {
            for (Enemy enemy : enemyList) {
                Random random = new Random();
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
                if (collideCheck(enemy.getX() + enemy.getdX(), enemy.getY() + enemy.getdY())) { // collide deadlyTie and
                                                                                                // map border
                    enemy.move();
                    updateBulletDirection(enemy);
                    enemy.reset();

                }
            }
        }
    }

    private void bustEnemyBullets(List<Enemy> enemyList, int Way, int fireRate, int immuneRate) {
        if (tick % fireRate == 0) {
            Random random = new Random();
            int life;
            for (Enemy enemy : enemyList) {
                if (random.nextInt(immuneRate) == 0) {
                    life = -1;
                } else {
                    life = 1;
                }
                for (int i = 0; i < Way; i++) {
                    if (i == 0) {
                        enemyBullets.add(bulletPool.requestBullet(enemy.getX(), enemy.getY(), enemy.getbX(),
                                enemy.getbY(), life));
                    } else if (i == 1) {
                        enemyBullets.add(bulletPool.requestBullet(enemy.getX(), enemy.getY(), enemy.getbX() * -1,
                                enemy.getbY() * -1, life));
                    } else if (i == 2) {
                        enemyBullets.add(bulletPool.requestBullet(enemy.getX(), enemy.getY(), enemy.getbY(),
                                enemy.getbX(), life));
                    } else if (i == 3) {
                        enemyBullets.add(bulletPool.requestBullet(enemy.getX(), enemy.getY(), enemy.getbY() * -1,
                                enemy.getbX() * -1, life));
                    }

                }
            }
        }
    }

    private boolean collideCheck(int x, int y) {
        if (x <= -1 || x >= size || y <= -1 || y >= size) {
            return false;
        }
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

    private void BulletHit() {
        List<Enemy> toRemoveEnemy = new ArrayList<Enemy>();
        List<Bullet> toRemoveBullet = new ArrayList<Bullet>();
        List<DeadlyTie> toRemoveDeadlyTie = new ArrayList<DeadlyTie>();
        // player bullets
        for (Bullet bP : playerBullets) {
            for (Enemy cTrooper : cTroopers) { // hit cTrooper
                if (bP.hit(cTrooper)) {
                    cTrooper.updateLife(-1);
                    if (cTrooper.getLife() <= 0) {
                        score += 1;
                        addItem(cTrooper.getX(), cTrooper.getY(), 3);
                        toRemoveEnemy.add(cTrooper);
                    }
                    toRemoveBullet.add(bP);
                    break;
                }
            }
            for (Enemy sTrooper : sTroopers) { // hit sTrooper
                if (bP.hit(sTrooper)) {
                    sTrooper.updateLife(-1);
                    if (sTrooper.getLife() <= 0) {
                        score += 1;
                        addItem(sTrooper.getX(), sTrooper.getY(), 2);
                        toRemoveEnemy.add(sTrooper);
                    }
                    toRemoveBullet.add(bP);
                    break;
                }
            }
            for (Enemy sManiac : sManiacs) { // hit sManiac
                if (bP.hit(sManiac)) {
                    sManiac.updateLife(-1);
                    if (sManiac.getLife() <= 0) {
                        score += 1;
                        addItem(sManiac.getX(), sManiac.getY(), 1);
                        toRemoveEnemy.add(sManiac);
                    }
                    toRemoveBullet.add(bP);
                    break;
                }
            }
            for (Enemy sManiacT2 : sManiacT2s) { // hit sManiacT2
                if (bP.hit(sManiacT2)) {
                    sManiacT2.updateLife(-1);
                    if (sManiacT2.getLife() <= 0) {
                        score += 1;
                        addItem(sManiacT2.getX(), sManiacT2.getY(), 0);
                        toRemoveEnemy.add(sManiacT2);
                    }
                    toRemoveBullet.add(bP);
                    break;
                }
            }
            for (DeadlyTie deadlyTie : deadlyTies) { // hit deadlyTie
                if (bP.hit(deadlyTie)) {
                    deadlyTie.updateLife(-1);
                    if (deadlyTie.getLife() <= 0) {
                        addItem(deadlyTie.getX(), deadlyTie.getY(), 0);
                        toRemoveDeadlyTie.add(deadlyTie);
                    }
                    break;
                }
            }
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
        for (Bullet bE : enemyBullets) {
            if (bE.hit(player)) { // hit player
                if (playerImmuneTime == 0) {
                    player.updateLife(-1);
                    playerImmuneTime = immuneAfterHit;
                }
                toRemoveBullet.add(bE);
                break;
            }
            if (!collideCheck(bE.getX(), bE.getY())) { // hit deadlyTie and map border
                toRemoveBullet.add(bE);
                break;
            }
        }
        cTroopers.removeAll(toRemoveEnemy);
        sTroopers.removeAll(toRemoveEnemy);
        sManiacs.removeAll(toRemoveEnemy);
        sManiacT2s.removeAll(toRemoveEnemy);
        playerBullets.removeAll(toRemoveBullet);
        enemyBullets.removeAll(toRemoveBullet);
        deadlyTies.removeAll(toRemoveDeadlyTie);
    }

    private void addItem(int x, int y, int weight) {
        Random random = new Random();
        if (random.nextInt(itemDropRate + weight) == 0) {
            items.add(new Item(x, y));
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
            notOver = false;
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
        return !notOver;
    }

    public List<DeadlyTie> getDeadlyTies() {
        return deadlyTies;
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

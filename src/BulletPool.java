import java.util.ArrayList;
import java.util.List;

public class BulletPool {
    List<Bullet> bullets = new ArrayList<Bullet>();
    private long time;

    public BulletPool() {
        int size = 40;
        for (int i = 0; i < size; i++) {
            bullets.add(new Bullet(-999, -999));
        }
    }

    public Bullet requestBullet(int x, int y, int dx, int dy, int life) {
        time = System.currentTimeMillis();
        try {
            Bullet bullet = bullets.remove(0);
            bullet.setObject(x, y, dx, dy, life);
            return bullet;
        } catch (Exception e) {
            bullets.add(new Bullet(-999, -999));
            Bullet bullet = bullets.remove(0);
            bullet.setObject(x, y, dx, dy, life);
            return bullet;
        }
    }

    public long getTime() {
        return time;
    }
}

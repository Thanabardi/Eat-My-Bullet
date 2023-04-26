public abstract class WObject {

    private int x;
    private int y;

    private int dx = 0;
    private int dy = 0;

    private int bx = 0;
    private int by = -1;

    private int life; // normal bullet if > 0 , immune bullet if < 0,
                      // life item if == 0, immortal item if == 1
    private String direction;

    public WObject(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = "N";
    }

    public void turnNorth() {
        dy = -1;
        direction = "N";
    }

    public void turnNorthEast() {
        dx = 1;
        dy = -1;
        direction = "NE";
    }

    public void turnEast() {
        dx = 1;
        direction = "E";
    }

    public void turnSouthEast() {
        dx = 1;
        dy = 1;
        direction = "SE";
    }

    public void turnSouth() {
        dy = 1;
        direction = "S";
    }

    public void turnSouthWest() {
        dx = -1;
        dy = 1;
        direction = "SW";
    }

    public void turnWest() {
        dx = -1;
        direction = "W";
    }

    public void turnNorthWest() {
        dx = -1;
        dy = -1;
        direction = "NW";
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public void setObject(int x, int y, int dx, int dy, int life) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.life = life;
    }

    public void setBullet(int bx, int by) {
        this.bx = bx;
        this.by = by;
    }

    public boolean hit(WObject wObj) {
        return x == wObj.x && y == wObj.y;
    }

    public void updateLife(int value) {
        life += value;
    }

    public void reset() {
        dx = dy = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getdX() {
        return dx;
    }

    public int getdY() {
        return dy;
    }

    public int getbX() {
        return bx;
    }

    public int getbY() {
        return by;
    }

    public int getLife() {
        return life;
    }

    public String getDirection() {
        return direction;
    }
}

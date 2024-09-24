package entity;

public class shop_item {
    private int shot_freq;
    private int bullet_speed;

    public shop_item(int shot_freq, int bullet_speed)
    {
        this.shot_freq = shot_freq;
        this.bullet_speed = bullet_speed;
    }

    public int getShot_freq()
    {
        return shot_freq;
    }

    public int getBullet_speed()
    {
        return bullet_speed;
    }
}

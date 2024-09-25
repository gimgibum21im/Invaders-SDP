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

    public void setShot_freq(int shot_freq)
    {
        this.shot_freq = shot_freq;
    }

    public void setBullet_speed(int bullet_speed)
    {
        this.bullet_speed = bullet_speed;
    }
}

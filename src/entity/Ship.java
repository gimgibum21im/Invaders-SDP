package entity;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager.SpriteType;

/**
 * Implements a ship, to be controlled by the player.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class Ship extends Entity {

	/** Time between shots. */
	private static final int SHOOTING_INTERVAL = 750; //이 부분을 수정하면 총알 발사 빈도 조절이 가능하다. => ship객체 생성시에 설정된다는 점에 유의!
	/** Speed of the bullets shot by the ship. */
	private static final int BULLET_SPEED = -6; //이부분을 수정해서 총알 속도를 설정 가능하다. 너무 낮으면 문제생기니 적절한 속도 찾기. => 총알 생성시 값이 넣어짐.


	private int sh_interval;
	private int bull_speed;

	public void setSh_interval(int sh_interval)
	{
		this.sh_interval = sh_interval;
	}

	public void setBull_speed(int bull_speed)
	{
		this.bull_speed = bull_speed;
	}

	/** Movement of the ship for each unit of time. */
	private static final int SPEED = 2;
	
	/** Minimum time between shots. */
	private Cooldown shootingCooldown;
	/** Time spent inactive between hits. */
	private Cooldown destructionCooldown;

	/**
	 * Constructor, establishes the ship's properties.
	 * 
	 * @param positionX
	 *            Initial position of the ship in the X axis.
	 * @param positionY
	 *            Initial position of the ship in the Y axis.
	 */
	public Ship(final int positionX, final int positionY) {
		super(positionX, positionY, 13 * 2, 8 * 2, Color.GREEN);

		this.spriteType = SpriteType.Ship;
		this.shootingCooldown = Core.getCooldown(SHOOTING_INTERVAL);
		this.destructionCooldown = Core.getCooldown(1000);
		//2인용 모드 고려해서, 그냥 기본값으로 해둠 처음에.
		sh_interval = SHOOTING_INTERVAL;
		bull_speed = BULLET_SPEED;
	}

	public void changeShootingCooldown()
	{
		this.shootingCooldown = Core.getCooldown(sh_interval);
	}

	/**
	 * Moves the ship speed uni ts right, or until the right screen border is
	 * reached.
	 */
	public final void moveRight() {
		this.positionX += SPEED;
	}

	/**
	 * Moves the ship speed units left, or until the left screen border is
	 * reached.
	 */
	public final void moveLeft() {
		this.positionX -= SPEED;
	}

	/**
	 * Shoots a bullet upwards.
	 * 
	 * @param bullets
	 *            List of bullets on screen, to add the new bullet.
	 * @return Checks if the bullet was shot correctly.
	 */
	public final boolean shoot(final Set<Bullet> bullets) {
		if (this.shootingCooldown.checkFinished()) { //발사가능하면
			this.shootingCooldown.reset(); //시간초기화
			bullets.add(BulletPool.getBullet(positionX + this.width / 2,
					positionY, bull_speed)); //함선의 가로위치에서가운데 & 세로위치그대로, bullet_speed속도로 발사!
			return true;
		}
		return false;
	}

	/**
	 * Updates status of the ship.
	 */
	//함선의 상태(모습)를 업데이트함
	public final void update() {
		//유저 함선이 피격되었고, 움직이지 못하는 시간이 끝나지않았으면 모습을 ShipDestroyed로.
		if (!this.destructionCooldown.checkFinished())
			this.spriteType = SpriteType.ShipDestroyed;
		//아니면 일반 함선모습으로
		else
			this.spriteType = SpriteType.Ship;
	}

	/**
	 * Switches the ship to its destroyed state.
	 */
	//피격당했으니, 피격시작시간을 현재시각으로 reset
	public final void destroy() {
		this.destructionCooldown.reset();
	}

	/**
	 * Checks if the ship is destroyed.
	 * 
	 * @return True if the ship is currently destroyed.
	 */
	//피격여부
	public final boolean isDestroyed() {
		return !this.destructionCooldown.checkFinished();
	}


	/**
	 * Getter for the ship's speed.
	 * 
	 * @return Speed of the ship.
	 */
	public final int getSpeed() {
		return SPEED;
	}
}

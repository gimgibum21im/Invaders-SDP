package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;

/**
 * Implements the game screen, where the action happens.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class GameScreen extends Screen {

	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 6000; //6초후부터 입력가능
	/** Bonus score for each life remaining at the end of the level. */
	private static final int LIFE_SCORE = 100;//레벨클리어시 보너스점수 : ((남은 생명-1) * LIFE_SCORE)
	/** Minimum time between bonus ship's appearances. */
	private static final int BONUS_SHIP_INTERVAL = 20000;//보너스 함선 빈도 늦어도 20초만에 나옴
	/** Maximum variance in the time between bonus ship's appearances. */
	private static final int BONUS_SHIP_VARIANCE = 10000;//보너스 함선 빈도 빠르면 10초
	/** Time until bonus ship explosion disappears. */
	private static final int BONUS_SHIP_EXPLOSION = 500;//보너스 함선 폭파 후 잔해 사라지기까지 0.5초
	/** Time from finishing the level to screen change. */
	private static final int SCREEN_CHANGE_INTERVAL = 1500; //다음 레벨로 전환되기까지 1.5초
	/** Height of the interface separation line. */
	private static final int SEPARATION_LINE_HEIGHT = 40; //라이프 밑에 라인높이(값이 클수록 아래로)

	/** Current game difficulty settings. */
	private GameSettings gameSettings; //현재 게임의 레벨(난이도)세팅
	/** Current difficulty level number. */
	private int level;//현재 겜이 몇렙인지
	/** Formation of enemy ships. */
	private EnemyShipFormation enemyShipFormation; //적 포메이션
	/** Player's ship. */
	private Ship ship; //내 함선
	/** Bonus enemy ship that appears sometimes. */
	private EnemyShip enemyShipSpecial; //보너스함선
	/** Minimum time between bonus ship appearances. */
	private Cooldown enemyShipSpecialCooldown; //적보너스함선 출현빈도를 위한 cooldown객체
	/** Time until bonus ship explosion disappears. */
	private Cooldown enemyShipSpecialExplosionCooldown; //적보너스함선 폭파 잔해를 위한 cooldown객체
	/** Time from finishing the level to screen change. */
	private Cooldown screenFinishedCooldown; //겜 종료후 담 렙전환을 위한 cooldown객체
	/** Set of all bullets fired by on screen ships. */
	private Set<Bullet> bullets; //발사되어 화면에 표시되는 모든 총알집합
	/** Current score. */
	private int score;
	/** Player lives left. */
	private int lives;
	/** Total bullets shot by the player. */
	private int bulletsShot; //지금까지 쏜 총알수
	/** Total ships destroyed by the player. */
	private int shipsDestroyed; //지금까지 파괴한 함선수
	/** Moment the game starts. */
	private long gameStartTime; //현재 레벨 겜시작시간
	/** Checks if the level is finished. */
	private boolean levelFinished; //지금 레벨 끝났는지(게임오버나 현재레벨 클리어 모두 포함)
	/** Checks if a bonus life is received. */
	private boolean bonusLife; //보너스라이프 여부

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param gameState
	 *            Current game state.
	 * @param gameSettings
	 *            Current game settings.
	 * @param bonusLife
	 *            Checks if a bonus life is awarded this level.
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public GameScreen(final GameState gameState,
			final GameSettings gameSettings, final boolean bonusLife,
			final int width, final int height, final int fps) {
		super(width, height, fps);

		this.gameSettings = gameSettings;
		this.bonusLife = bonusLife;
		this.level = gameState.getLevel();
		this.score = gameState.getScore();
		this.lives = gameState.getLivesRemaining();
		if (this.bonusLife)
			this.lives++;
		this.bulletsShot = gameState.getBulletsShot();
		this.shipsDestroyed = gameState.getShipsDestroyed();
	}

	/**
	 * Initializes basic screen properties, and adds necessary elements.
	 */
	public final void initialize() {
		super.initialize();

		//gameSettings(레벨정보)으로 적 포메이션 생성
		enemyShipFormation = new EnemyShipFormation(this.gameSettings);
		enemyShipFormation.attach(this); //적포메이션이 스크린의 정보를 알수있도록 넘겨줌(적이 사이드에 닿아서 반대로 가야하는지 여부 등 판별시 필요)
		this.ship = new Ship(this.width / 2, this.height - 30); //유저함선을 스크린에 맞는 위치(가로 중간, 바닥에서 30만큼 위)에 생성

		// Appears each 10-30 seconds. 보너스 함선 생성을 위한 cooldown객체 설정
		this.enemyShipSpecialCooldown = Core.getVariableCooldown(
				BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
		this.enemyShipSpecialCooldown.reset();
		this.enemyShipSpecialExplosionCooldown = Core
				.getCooldown(BONUS_SHIP_EXPLOSION);
		this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL); //다음레벨 전환을 위한 객체 설정(1.5초)
		this.bullets = new HashSet<Bullet>(); //총알 저장할 집합 생성

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis(); // 이번 게임(레벨) 시작시간 찍고
		this.inputDelay = Core.getCooldown(INPUT_DELAY); // 게임시작 6초 후부터 입력가능하도록 하는 cooldown 객체 설정
		this.inputDelay.reset(); //inputDelay 시작시간 현재시간으로 설정
	}

	/**
	 * Starts the action.
	 * 
	 * @return Next screen code.
	 */
	public final int run() {
		super.run(); //super.run()에서 바로 아래 this.update() 실행할겨.
		//여기로 빠져나왔으면 현재 레벨 겜이 끝났다는 것.(클리어든 게임오버든)

		this.score += LIFE_SCORE * (this.lives - 1); //보너스 점수는 (현재생명-1) * 100
		this.logger.info("Screen cleared with a score of " + this.score);

		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();

		if (this.inputDelay.checkFinished() && !this.levelFinished) { //겜 시작후 6초가 지났고, 레벨이 끝나지 않았다면!

			//유저 함선이 피격당하지 않았다면
			if (!this.ship.isDestroyed()) {
				//오른쪽이동 입력여부
				boolean moveRight = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
						|| inputManager.isKeyDown(KeyEvent.VK_D);
				//왼쪽이동 입력여부
				boolean moveLeft = inputManager.isKeyDown(KeyEvent.VK_LEFT)
						|| inputManager.isKeyDown(KeyEvent.VK_A);

				//오른쪽 이동시 오른쪽 경계에 닿았는지(닿을건지) 여부
				boolean isRightBorder = this.ship.getPositionX()
						+ this.ship.getWidth() + this.ship.getSpeed() > this.width - 1;

				//왼쪽 이동시 왼쪽 경계에 닿았는지(닿을건지) 여부
				boolean isLeftBorder = this.ship.getPositionX()
						- this.ship.getSpeed() < 1;

				//오른쪽이동입력이 있고, 오른쪽 경계에 닿지 않는다면 유저함선을 오른쪽으로 이동
				if (moveRight && !isRightBorder) {
					this.ship.moveRight();
				}
				//경계에 닿는다면 이동하지 않겠지

				//왼쪽이동입력이 있고, 왼쪽 경계에 닿지 않는다면 유저함선을 오른쪽으로 이동
				if (moveLeft && !isLeftBorder) {
					this.ship.moveLeft();
				}
				//경계에 닿는다면 이동하지 않겠지

				//스페이스입력이면
				if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
					if (this.ship.shoot(this.bullets)) //참이라면, ship.shoot()에서 bullets리스트에 총알 추가해줌.
						this.bulletsShot++; //발사한 총알 수 1 증가
					//발사대기시간이면 위 조건문 false.
			}

			//지금 보너스 함선이 존재한다면
			if (this.enemyShipSpecial != null) {
				//보너스함선이 피격당하지 않은상태라면
				if (!this.enemyShipSpecial.isDestroyed())
					this.enemyShipSpecial.move(2, 0);//이동 ㅇㅇ
				//피격당한상태고, 잔해보여주는 쿨타임이 끝났다면 null로 만든다.
				else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
					this.enemyShipSpecial = null;
			}

			//지금 보너스함선이 없고 && 생성 쿨타임이 돌았으면
			if (this.enemyShipSpecial == null
					&& this.enemyShipSpecialCooldown.checkFinished()) {
				this.enemyShipSpecial = new EnemyShip(); //생성
				this.enemyShipSpecialCooldown.reset(); // 쿨타임 초기화(보너스함선은 생성시간기준으로 주기가 도는듯)
				this.logger.info("A special ship appears");
			}
			//지금 보너스함선이 있고, 화면을 벗어났다면
			if (this.enemyShipSpecial != null
					&& this.enemyShipSpecial.getPositionX() > this.width) {
				this.enemyShipSpecial = null; //없앰
				this.logger.info("The special ship has escaped");
			}

			this.ship.update(); //함선 모습 설정(피격모습/일반모습)
			this.enemyShipFormation.update(); //보기 귀찮다
			this.enemyShipFormation.shoot(this.bullets); // 적발사쿨탐이 지났고 등등 만족하면 발사(가장 아래의 적들중 랜덤 1개선택해 발사시키고 총알을 bullets에 추가해줌
		}

		//@@@@@@@@@@@@@@중요한부분@@@@@@@@@@//
		manageCollisions(); //총알이 유저나 적에 맞았는지 확인하기위해 모든 총알을 확인. 맞은 총알 삭제하고, score, 라이프--, 피격상태 등의 확인 및 처리가 모두 여기서 이루어짐.
		cleanBullets();//총알이 화면경계를 벗어났다면 삭제해줌
		draw();//화면 그리기

		//적이 없거나, 라이프0이고 이번레벨이 끝나지 않았다면 끝내줌
		if ((this.enemyShipFormation.isEmpty() || this.lives == 0)
				&& !this.levelFinished) {
			this.levelFinished = true;
			this.screenFinishedCooldown.reset();//화면전환을 위한 카운트(1.5초) 시작시간 설정
		}
		//겜이 끝났고, 화면전환쿨탐이 끝났다면 running=false로. => 이제 Screen.run()의 while문을 빠져나감.. 그리고 GameScreen의 run()으로 다시 이동하겟지
		if (this.levelFinished && this.screenFinishedCooldown.checkFinished())
			this.isRunning = false;

	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		drawManager.initDrawing(this);

		drawManager.drawEntity(this.ship, this.ship.getPositionX(),
				this.ship.getPositionY());
		if (this.enemyShipSpecial != null)
			drawManager.drawEntity(this.enemyShipSpecial,
					this.enemyShipSpecial.getPositionX(),
					this.enemyShipSpecial.getPositionY());

		enemyShipFormation.draw();

		for (Bullet bullet : this.bullets)
			drawManager.drawEntity(bullet, bullet.getPositionX(),
					bullet.getPositionY());

		// Interface.
		drawManager.drawScore(this, this.score);
		drawManager.drawLives(this, this.lives);
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);

		// Countdown to game start.
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY
					- (System.currentTimeMillis()
							- this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.level, countdown,
					this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height
					/ 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height
					/ 12);
		}

		drawManager.completeDrawing(this);
	}

	/**
	 * Cleans bullets that go off screen.
	 */
	private void cleanBullets() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets) {
			bullet.update();
			if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
					|| bullet.getPositionY() > this.height)
				recyclable.add(bullet);
		}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Manages collisions between bullets and ships.
	 */
	private void manageCollisions() {
		Set<Bullet> recyclable = new HashSet<Bullet>();
		for (Bullet bullet : this.bullets)
			if (bullet.getSpeed() > 0) {
				if (checkCollision(bullet, this.ship) && !this.levelFinished) {
					recyclable.add(bullet);
					if (!this.ship.isDestroyed()) {
						this.ship.destroy();
						this.lives--;
						this.logger.info("Hit on player ship, " + this.lives
								+ " lives remaining.");
					}
				}
			} else {
				for (EnemyShip enemyShip : this.enemyShipFormation)
					if (!enemyShip.isDestroyed()
							&& checkCollision(bullet, enemyShip)) {
						this.score += enemyShip.getPointValue();
						this.shipsDestroyed++;
						this.enemyShipFormation.destroy(enemyShip);
						recyclable.add(bullet);
					}
				if (this.enemyShipSpecial != null
						&& !this.enemyShipSpecial.isDestroyed()
						&& checkCollision(bullet, this.enemyShipSpecial)) {
					this.score += this.enemyShipSpecial.getPointValue();
					this.shipsDestroyed++;
					this.enemyShipSpecial.destroy();
					this.enemyShipSpecialExplosionCooldown.reset();
					recyclable.add(bullet);
				}
			}
		this.bullets.removeAll(recyclable);
		BulletPool.recycle(recyclable);
	}

	/**
	 * Checks if two entities are colliding.
	 * 
	 * @param a
	 *            First entity, the bullet.
	 * @param b
	 *            Second entity, the ship.
	 * @return Result of the collision test.
	 */
	private boolean checkCollision(final Entity a, final Entity b) {
		// Calculate center point of the entities in both axis.
		int centerAX = a.getPositionX() + a.getWidth() / 2;
		int centerAY = a.getPositionY() + a.getHeight() / 2;
		int centerBX = b.getPositionX() + b.getWidth() / 2;
		int centerBY = b.getPositionY() + b.getHeight() / 2;
		// Calculate maximum distance without collision.
		int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
		int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
		// Calculates distance.
		int distanceX = Math.abs(centerAX - centerBX);
		int distanceY = Math.abs(centerAY - centerBY);

		return distanceX < maxDistanceX && distanceY < maxDistanceY;
	}

	/**
	 * Returns a GameState object representing the status of the game.
	 * 
	 * @return Current game state.
	 */
	public final GameState getGameState() {
		return new GameState(this.level, this.score, this.lives,
				this.bulletsShot, this.shipsDestroyed);
	}
}
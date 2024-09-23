package screen;

import java.awt.Insets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import engine.Cooldown;
import engine.Core;
import engine.DrawManager;
import engine.InputManager;

/**
 * Implements a generic screen.
 * 
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 * 
 */
public class Screen {
	
	/** Milliseconds until the screen accepts user input. */
	private static final int INPUT_DELAY = 1000;//1초후부터 입력가능

	/** Draw Manager instance. */
	protected DrawManager drawManager;
	/** Input Manager instance. */
	protected InputManager inputManager;
	/** Application logger. */
	protected Logger logger;

	/** Screen width. */
	protected int width;
	/** Screen height. */
	protected int height;
	/** Frames per second shown on the screen. */
	protected int fps;
	/** Screen insets. */
	protected Insets insets;
	/** Time until the screen accepts user input. */
	protected Cooldown inputDelay;

	/** If the screen is running. */
	protected boolean isRunning;
	/** What kind of screen goes next. */
	protected int returnCode;

	/**
	 * Constructor, establishes the properties of the screen.
	 * 
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public Screen(final int width, final int height, final int fps) {
		this.width = width;
		this.height = height;
		this.fps = fps;

		this.drawManager = Core.getDrawManager();
		this.inputManager = Core.getInputManager();
		this.logger = Core.getLogger();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
		this.returnCode = 0;
	}

	/**
	 * Initializes basic screen properties.
	 */
	public void initialize() {

	}

	/**
	 * Activates the screen.
	 * 
	 * @return Next screen code.
	 */
	public int run() {
		this.isRunning = true;

		while (this.isRunning) {
			//프레임을 위한 시작시간 찍기
			long time = System.currentTimeMillis();

			//동적바인딩으로 자식의 메서드 실행할겨
			update();
			//titlescreen이면, 스페이스가 눌러져서 isRunning=false 될 때 까지 반복함.
			//scorescreen이면, esc 또는 스페이스 눌러져서 isRunning=false 될 때 까지 반복함.
			//HighScoreScreen이면, 스페이스 눌러져서 isRunning=flase 될 때 까지 반복


			// 1초=1000밀리초. fps를 맞추기위해 1000/fps 밀리초마다 프레임을 찍어야됨.
			time = (1000 / this.fps) - (System.currentTimeMillis() - time);
			//만약 시작시간~지금까지 1000/fps밀리초가 지나지 않았다면, 기다리기.
			if (time > 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(time);
				} catch (InterruptedException e) {
					return 0;
				}
			}
		}

		return 0; //이 함수가 끝나면, 각각의(title,score,highscore) run에서 super.run()끝난 시점으로 으로 되돌아감.
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected void update() {
	}

	/**
	 * Getter for screen width.
	 * 
	 * @return Screen width.
	 */
	public final int getWidth() {
		return this.width;
	}

	/**
	 * Getter for screen height.
	 * 
	 * @return Screen height.
	 */
	public final int getHeight() {
		return this.height;
	}
}
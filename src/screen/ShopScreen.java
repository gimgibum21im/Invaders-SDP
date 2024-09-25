package screen;

import java.awt.event.KeyEvent;

import engine.Cooldown;
import engine.Core;
import entity.shop_item;

/**
 * Implements the title screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class ShopScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;

    /** Time between changes in user selection. */
    private Cooldown selectionCooldown;

    private shop_item s_i;

    private int selected_item;

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
    public ShopScreen(final int width, final int height, final int fps, final shop_item s_i) {
        super(width, height, fps);

        // Defaults to play.
        this.returnCode = 2;
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME); //200밀리초를 갖는 cooldown객체 생성해서 반환
        this.selectionCooldown.reset(); // cooldown의 시작시간을 현재시간으로 설정
        this.s_i = s_i;
        selected_item = 1;
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();
        //스페이스 입력으로, Screen.run()에서 this.update()실행이 끝나고, Screen.run()이 종료되면 여기로 나옴

        //위에서 바뀐 returnCode로 다음 스크린번호 반환
        return 1;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (this.selectionCooldown.checkFinished() //마지막 동작으로부터 0.2초가 지났고 &&
                && this.inputDelay.checkFinished()) { //객체생성으로부터 1초 지났다면. (매 입력이 아닌 그냥 스크린 전환후 1초가 지났는지)

            //윗키 또는 W 누르면 위 선택지로 returnCode변경
            if (inputManager.isKeyDown(KeyEvent.VK_UP)
                    || inputManager.isKeyDown(KeyEvent.VK_W)) {
                previousMenuItem();
                this.selectionCooldown.reset();//동작 뒤 현재시간 갱신
            }
            //아랫키 또는 S 누르면 아래 선택지로 returnCode변경
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
                    || inputManager.isKeyDown(KeyEvent.VK_S)) {
                nextMenuItem();
                this.selectionCooldown.reset();//동작 뒤 현재시간 갱신
            }

            if (inputManager.isKeyDown(KeyEvent.VK_SPACE))
                //화폐 처리 여기서 구현하기.
                //..

                if(selected_item == 1)
                {
                    s_i.setBullet_speed(s_i.getBullet_speed() + 1);
                }
                else if(selected_item == 2)
                {
                    s_i.setShot_freq(s_i.getShot_freq() + 1);
                }
                else //추가 라이프
                {

                }
            this.selectionCooldown.reset();//동작 뒤 현재시간 갱신


            //esc누르면 running false
            if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE))
                this.isRunning = false;
        }
    }

    /**
     * Shifts the focus to the next menu item.
     */
    //맨 아래서 또 아래 누르면 처음으로 이동하는 등의 로직 + 2->3->0->2->3->0반복!
    private void nextMenuItem() {
        if (this.selected_item == 3)
            this.selected_item = 1;
        else
            this.selected_item++;
    }

    /**
     * Shifts the focus to the previous menu item.
     */
    //맨 위에서 또 위 누르면 마지막으로 이동하는 등의 로직
    private void previousMenuItem() {
        if (this.selected_item == 1)
            this.selected_item = 3;
        else
            this.selected_item--;
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);


        drawManager.drawShop(this,selected_item,s_i);

        drawManager.completeDrawing(this);
    }
}

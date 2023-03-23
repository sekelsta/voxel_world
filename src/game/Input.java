package sekelsta.game;

import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import sekelsta.engine.InputManager;
import sekelsta.engine.Gamepad;
import sekelsta.engine.Log;
import sekelsta.engine.entity.IController;
import sekelsta.engine.render.Camera;
import sekelsta.engine.render.Window;
import sekelsta.game.entity.Pawn;
import sekelsta.game.render.gui.Overlay;

public class Input extends InputManager implements IController {
    Overlay overlay;
    Camera camera;
    Pawn pawn;
    private Game game;

    private ArrayList<Gamepad> gamepads = new ArrayList<>();

    public Input(Game game) {
        this.game = game;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setPawn(Pawn pawn) {
        this.pawn = pawn;
    }

    public void updateConnectedGamepads() {
        for (int joystickID = GLFW.GLFW_JOYSTICK_1; joystickID < GLFW.GLFW_JOYSTICK_LAST; ++joystickID) {
            boolean present = GLFW.glfwJoystickPresent(joystickID);
            boolean known = false;
            for (Gamepad gamepad : gamepads) {
                if (gamepad.joystickID == joystickID) {
                    known = true;
                }
            }
            if (present && !known) {
                joystickConnectionChanged(joystickID, GLFW.GLFW_CONNECTED);
            }
        }
    }

    @Override
    public void processKey(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_F11) {
                window.toggleFullscreen();
            }
            else if (key == GLFW.GLFW_KEY_F5 && camera != null) {
                camera.nextMode();
            }
            else if (key == GLFW.GLFW_KEY_ESCAPE) {
                game.escape();
            }
            else if (key == GLFW.GLFW_KEY_ENTER) {
                overlay.trigger();
            }
            else if (key == GLFW.GLFW_KEY_UP) {
                overlay.up();
            }
            else if (key == GLFW.GLFW_KEY_DOWN) {
                overlay.down();
            }
            else if (key == GLFW.GLFW_KEY_LEFT) {
                overlay.left();
            }
            else if (key == GLFW.GLFW_KEY_RIGHT) {
                overlay.right();
            }
            else if (key == GLFW.GLFW_KEY_HOME) {
                overlay.top();
            }
            else if (key == GLFW.GLFW_KEY_END) {
                overlay.bottom();
            }
            else if (key == GLFW.GLFW_KEY_BACKSPACE) {
                overlay.backspace();
            }
            else if (key == GLFW.GLFW_KEY_TAB) {
                overlay.tab();
            }
        }
    }

    @Override
    public void inputCharacter(char character) {
        overlay.inputCharacter(character);
    }

    @Override
    public void moveCursor(double xPos, double yPos) {
        overlay.positionPointer(xPos, yPos);
        if (window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            || window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            ) {
            if (camera != null) {
                double diffX = xPos - prevCursorX;
                double diffY = yPos - prevCursorY;
                camera.addYaw((float)-diffX / 100f);
                camera.addPitch((float)diffY / 100f);
            }
        }
        super.moveCursor(xPos, yPos);        
    }

    @Override
    public void processMouseClick(int button, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                boolean consumed = overlay.click();
                if (consumed) {
                    return;
                }
            }
        }
    }

    // With a standard mouse scroll wheel, yOffset will be 1 or -1
    // (up is positive, down is negative)
    @Override
    public void processScroll(double xOffset, double yOffset) {
        if (camera != null) {
            camera.scroll(-1 * yOffset);
        }
    }

    @Override
    public void joystickConnectionChanged(int joystickID, int event) {
        if (event == GLFW.GLFW_CONNECTED) {
            if (GLFW.glfwJoystickIsGamepad(joystickID)) {
                gamepads.add(new Gamepad(joystickID, this));
            }
            else {
                String GUID = GLFW.glfwGetJoystickGUID(joystickID);
                String name = GLFW.glfwGetJoystickName(joystickID);
                Log.warn(name + " is not a compatible joystick, gamepad, or controller.\n" + 
                    "    Reason: No gamepad mapping for GUID " + GUID);
            }
        }
        else if (event == GLFW.GLFW_DISCONNECTED) {
            gamepads.removeIf(g -> g.joystickID == joystickID);
        }
    }

    @Override
    public void processGamepadButton(int button, int action) {
        if (action == GLFW.GLFW_PRESS) {
            if (button == GLFW.GLFW_GAMEPAD_BUTTON_B) {
                game.escape();
            }
            else if (button == GLFW.GLFW_GAMEPAD_BUTTON_A) {
                overlay.trigger();
            }
            else if (button == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN) {
                overlay.down();
            }
            else if (button == GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP) {
                overlay.up();
            }
        }
    }

    public void update() {
        for (Gamepad gamepad : gamepads) {
            gamepad.update();

            // Special case to handle axis
            float y = gamepad.axis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
            float prevY = gamepad.prevAxis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
            final float PRESS_THRESHOLD = 0.4f;
            if (y > PRESS_THRESHOLD && prevY <= PRESS_THRESHOLD) {
                overlay.down();
            }
            else if (y < -PRESS_THRESHOLD && prevY >= -PRESS_THRESHOLD) {
                overlay.up();
            }
        }

        if (window.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            overlay.holdLeftMouseButton();
        }
    }

    @Override
    public void preUpdate() {
        // TODO
    }
}

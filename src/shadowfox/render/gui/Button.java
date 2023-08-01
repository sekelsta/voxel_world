package shadowfox.render.gui;

import java.awt.Color;

import shadowfox.render.SpriteBatch;
import shadowfox.render.text.BitmapFont;

public class Button extends GuiElement {
    private GuiElement display;
    private Runnable onTrigger;

    public Button(GuiElement display, Runnable onTrigger) {
        this.display = display;
        this.onTrigger = onTrigger;
    }

    @Override
    public void position(int x, int y) {
        super.position(x, y);
        display.position(x, y);
    }


    @Override
    public int getWidth() {
        return display.getWidth();
    }

    @Override
    public int getHeight() {
        return display.getHeight();
    }

    @Override
    public boolean trigger() {
        onTrigger.run();
        return true;
    }

    @Override
    public void blit(SpriteBatch spritebatch, boolean focused) {
        display.blit(spritebatch, focused);
    }
}

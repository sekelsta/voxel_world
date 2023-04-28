package sekelsta.game.render.gui;

import java.util.ArrayList;

import sekelsta.engine.Pair;
import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class OptionsScreen extends Screen {
    private SpriteBatch spritebatch = new SpriteBatch();
    private Texture texture = new Texture("ui.png");
    private Slider slider;
    private TextChoice uiScale;

    public OptionsScreen(Overlay overlay) {
        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();

        addItem(new TextElement(font, "Audio volume:"));
        slider = new Slider(
            game.getSettings().getVolume(),
            () -> game.getSettings().setVolume(slider.getValue())
        );
        addSelectableItem(slider);

        addItem(new TextElement(font, "UI Scale:"));
        ArrayList<String> scales = new ArrayList<>();
        scales.add("Small");
        scales.add("Medium");
        scales.add("Large");
        int startscale = 1;
        if (game.getSettings().uiScale < 0.9f) {
            startscale = 2;
        }
        else if (game.getSettings().uiScale > 1.1f) {
            startscale = 0;
        }
        uiScale = new TextChoice(Fonts.getTextFont(), scales, startscale,
            (v) -> game.getSettings().uiScale = 1 - 0.25f * (v - 1));
        addSelectableItem(uiScale);

        addSelectableItem(new Button(new TextElement(font, "Toggle fullscreen"), () -> game.toggleFullscreen()));

        addSelectableItem(new Button(new TextElement(font, "Credits"), 
            () -> overlay.pushScreen(new CreditsScreen(game))));
        addSelectableItem(new Button(new TextElement(font, "Done"), () -> game.escape()));
    }

    @Override
    public boolean pausesGame() {
        return true;
    }

    @Override
    public void blit(double screenWidth, double screenHeight) {
        spritebatch.setTexture(texture);

        int height = 0;
        for (int i = 0; i < items.size(); ++i) {
            Pair<GuiElement, Float> pair = items.get(i);
            int h = pair.getKey().getHeight();
            height += (i + 1 == items.size()) ? h : h * pair.getValue();
        }
        int yPos = ((int)screenHeight - height) / 2;
        GuiElement selected = selectable.getSelected();
        for (Pair<GuiElement, Float> pair : items) {
            GuiElement item = pair.getKey();
            float spacing = pair.getValue();
            item.position(((int)screenWidth - item.getWidth()) / 2, yPos);
            yPos += (int)(spacing * item.getHeight());
            item.blit(spritebatch, item == selected);
        }

        spritebatch.render();
    }
}

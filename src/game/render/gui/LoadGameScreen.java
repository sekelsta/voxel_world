package sekelsta.game.render.gui;

import java.util.function.Consumer;

import sekelsta.engine.file.SaveName;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class LoadGameScreen extends Screen {
    protected TextElement title;
    protected TextButton start;
    protected TextButton cancel;

    public LoadGameScreen(Overlay overlay, Consumer<SaveName> onChosen) {
        Game game = overlay.getGame();
        this.title = new TextElement(Fonts.getTitleFont(), "Load Game");
        this.start = new TextButton(Fonts.getButtonFont(), "Start", () -> System.out.println("Not yet implemented"));
        this.cancel = new TextButton(Fonts.getButtonFont(), "Cancel", () -> game.escape());
        addItem(title);
        addSelectableItem(start);
        addSelectableItem(cancel);

        SaveGame[] saves = SaveGame.loadMetadata();
        for (SaveGame save : saves) {
            System.out.println(save);
        }
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        return start.trigger();
    }
}

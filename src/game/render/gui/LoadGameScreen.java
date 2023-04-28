package sekelsta.game.render.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Consumer;

import sekelsta.engine.file.SaveName;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class LoadGameScreen extends Screen {
    // Package-private
    static final int MAX_SAVES = 5;

    private Overlay overlay;
    private Consumer<SaveName> onChosen;

    public LoadGameScreen(Overlay overlay, Consumer<SaveName> onChosen) {
        this.overlay = overlay;
        this.onChosen = onChosen;
        refresh();
    }

    @Override
    public void refresh() {
        // Clear previous values
        selectable = new SelectableElementList();
        items = new ArrayList<>();

        Game game = overlay.getGame();
        addItem(new TextElement(Fonts.getTitleFont(), "Load Game"));

        SaveGame[] saves = SaveGame.loadMetadata();
        for (int i = 0; i < MAX_SAVES && i < saves.length; ++i) {
            SaveGame save = saves[i];
            addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), save.getName()),
                () -> onChosen.accept(save)), 1);
            addItem(new TextElement(Fonts.getTextFont(), save.getFileName(), Color.LIGHT_GRAY));
        }

        if (saves.length < MAX_SAVES) {
            addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "New Game"), 
                () -> overlay.pushScreen(new NewGameScreen(overlay, onChosen))
            ));
        }

        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Delete Game"), 
            () -> overlay.pushScreen(new DeleteGameScreen(overlay, saves))
        ));

        addSelectableItem(new Button(new TextElement(Fonts.getButtonFont(), "Cancel"), () -> game.escape()));
    }
}

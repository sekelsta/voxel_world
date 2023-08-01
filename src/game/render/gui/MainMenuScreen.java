package sekelsta.game.render.gui;

import java.util.ArrayList;

import shadowfox.render.gui.*;
import shadowfox.render.text.BitmapFont;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class MainMenuScreen extends Screen {
    private Overlay overlay;

    public MainMenuScreen(Overlay overlay) {
        this.overlay = overlay;
        refresh(); 
    }

    @Override
    public void refresh() {
        // Clear previous values
        selectable = new SelectableElementList();
        items = new ArrayList<>();

        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();
        SaveGame prev = SaveGame.getDefault(game.getSettings().lastJoinedWorld);
        if (prev != null) {
            addSelectableItem(new Button(new TextElement(font, "Continue " + prev.getName()), () -> game.defaultStart()));
        }
        addSelectableItem(new Button(new TextElement(font, "Single player"),
            () -> game.pushLoadOrNewScreen((x) -> game.startPlaying(x))));
        addSelectableItem(new Button(new TextElement(font, "Host and play"), 
            () -> game.pushLoadOrNewScreen((x) -> overlay.pushScreen(new HostScreen(overlay, x))))
        );
        addSelectableItem(new Button(new TextElement(font, "Join server"), 
            () -> overlay.pushScreen(new JoinScreen(overlay))
        ));
        addSelectableItem(new Button(new TextElement(font, "Options"), () -> overlay.pushScreen(new OptionsScreen(overlay))));
        addSelectableItem(new Button(new TextElement(font, "Exit"), () -> game.stop()));
    }
}

package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class MainMenuScreen extends Screen {
    public MainMenuScreen(Overlay overlay) {
        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();
        addSelectableItem(new TextButton(font, "Single player",
            () -> overlay.pushScreen(new NewGameScreen(overlay))));
        addSelectableItem(new TextButton(font, "Host and play", 
            () -> overlay.pushScreen(new HostScreen(overlay)))
        );
        addSelectableItem(new TextButton(font, "Join server", 
            () -> overlay.pushScreen(new JoinScreen(overlay)))
        );
        addSelectableItem(new TextButton(font, "Options", () -> overlay.pushScreen(new OptionsScreen(overlay))));
        addSelectableItem(new TextButton(font, "Exit", () -> game.stop()));     
    }
}

package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class MainMenuScreen extends Screen {
    public MainMenuScreen(Overlay overlay) {
        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();
        if (game.hasPreviousSave()) {
            addSelectableItem(new TextButton(font, game.getContinueText(), () -> game.continuePrevious()));
        }
        addSelectableItem(new TextButton(font, "Single player",
            () -> game.pushLoadOrNewScreen((x) -> game.startPlaying(x))));
        addSelectableItem(new TextButton(font, "Host and play", 
            () -> game.pushLoadOrNewScreen((x) -> overlay.pushScreen(new HostScreen(overlay, x))))
        );
        addSelectableItem(new TextButton(font, "Join server", 
            () -> overlay.pushScreen(new JoinScreen(overlay)))
        );
        addSelectableItem(new TextButton(font, "Options", () -> overlay.pushScreen(new OptionsScreen(overlay))));
        addSelectableItem(new TextButton(font, "Exit", () -> game.stop()));     
    }
}

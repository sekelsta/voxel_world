package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class MainMenuScreen extends Screen {
    public MainMenuScreen(Overlay overlay) {
        Game game = overlay.getGame();
        BitmapFont font = Fonts.getButtonFont();
        if (game.hasPreviousSave()) {
            addSelectableItem(new Button(new TextElement(font, game.getContinueText()), () -> game.continuePrevious()));
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

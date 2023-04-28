package sekelsta.game.render.gui;

import sekelsta.engine.file.Log;
import sekelsta.engine.file.SaveName;
import sekelsta.engine.network.RuntimeBindException;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class HostScreen extends PortEntryScreen {
    public HostScreen(Overlay overlay, SaveName saveName) {
        super(overlay);
        this.title = new TextElement(Fonts.getTitleFont(), "Host Multiplayer");
        this.cancel = new Button(new TextElement(Fonts.getButtonFont(), "Cancel"), () -> overlay.getGame().escape());
        this.done = new Button(new TextElement(Fonts.getButtonFont(), "Done"), () -> tryHostMultiplayer(saveName));
        addItem(title);
        addItem(portLabel);
        addSelectableItem(portInput, 2);
        addItem(error);
        addSelectableItem(done);
        addSelectableItem(cancel);
    }

    private void tryHostMultiplayer(SaveName saveName) {
        String strPort = portInput.getEnteredText();
        int port = tryParsePort(strPort);
        if (port == -1) {
            return;
        }

        Game game = overlay.getGame();
        try {
            game.allowConnections(port);
        }
        catch (IllegalArgumentException e) {
            error.setText("Invalid port");
            return;
        }
        catch (RuntimeBindException e) {
            Log.error("Error: " + e.getMessage());
            error.setText("Error binding port");
            return;
        }
        if (!game.isInGame()) {
            game.startPlaying(saveName);
        }
        overlay.popScreenIfEquals(this);
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        return done.trigger();
    }
}

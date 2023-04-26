package sekelsta.game.render.gui;

import sekelsta.engine.Log;
import sekelsta.engine.network.RuntimeBindException;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class HostScreen extends PortEntryScreen {
    public HostScreen(Overlay overlay) {
        super(overlay);
        this.title = new TextElement(Fonts.getTitleFont(), "Host Multiplayer");
        BitmapFont font = Fonts.getButtonFont();
        this.done = new TextButton(font, "Done", () -> tryHostMultiplayer());
        addItem(title);
        addItem(portLabel);
        addSelectableItem(portInput, 2);
        addItem(error);
        addSelectableItem(done);
        addSelectableItem(cancel);
    }

    private void tryHostMultiplayer() {
        String strPort = portInput.getEnteredText();
        int port = tryParsePort(strPort);
        if (port == -1) {
            return;
        }

        Game game = overlay.getGame();
        try {
            game.allowConnections(port);
        }
        catch (RuntimeBindException e) {
            Log.error("Error: " + e.getMessage());
            error.setText("Error binding port");
            return;
        }
        if (!game.isInGame()) {
            game.enterWorld();
        }
        overlay.popScreenIfEquals(this);
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        tryHostMultiplayer();
        return true;
    }
}

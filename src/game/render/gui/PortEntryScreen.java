package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class PortEntryScreen extends Screen {
    protected Overlay overlay;

    protected TextElement title;
    protected TextElement portLabel;
    protected TextInput portInput;
    protected OptionalText error;
    protected TextButton done;
    protected TextButton cancel;

    protected PortEntryScreen(Overlay overlay) {
        this.overlay = overlay;
        Game game = overlay.getGame();

        this.portLabel = new TextElement(Fonts.getButtonFont(), "Enter port number:");
        this.portInput = new TextInput(Fonts.getButtonFont(), String.valueOf(Game.DEFAULT_PORT), "Port");
        this.error = new OptionalText(Fonts.getTextFont(), Fonts.ERROR_COLOR);
        this.cancel = new TextButton(Fonts.getButtonFont(), "Cancel", () -> game.escape());
    }

    protected int tryParsePort(String strPort) {
        if (strPort.equals("")) {
            error.setText("Enter a port number");
            return -1;
        }

        int port = 0;
        try {
            port = Integer.valueOf(strPort);
        }
        catch (NumberFormatException e) {
            error.setText("Could not parse port number");
            return -1;
        }

        if (port == 0) {
            error.setText("Enter a non-zero port number");
            return -1;
        }

        return port;
    }
}

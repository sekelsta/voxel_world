package sekelsta.game.render.gui;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import sekelsta.engine.Log;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class JoinScreen extends PortEntryScreen {
    private TextElement addressLabel;
    private TextInput addressInput;

    public JoinScreen(Overlay overlay) {
        super(overlay);
        Game game = overlay.getGame();
        this.title = new TextElement(Fonts.getTitleFont(), "Join Server");
        BitmapFont font = Fonts.getButtonFont();
        this.addressLabel = new TextElement(font, "Enter server IP address:");
        this.addressInput = new TextInput(font, game.getSettings().lastJoinedIP, "IP address");
        this.done = new TextButton(font, "Done", () -> tryJoinServer());
        addItem(title);
        addItem(addressLabel);
        addSelectableItem(addressInput);
        addItem(portLabel);
        addSelectableItem(portInput, 2);
        addItem(error);
        addSelectableItem(done);
        addSelectableItem(cancel);
    }

    private void tryJoinServer() {
        String strAddress = addressInput.getEnteredText();
        String strPort = portInput.getEnteredText();

        if (strAddress.equals("")) {
            error.setText("Enter an IP address");
            selectable.setTextFocus(addressInput);
            return;
        }

        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName(strAddress);
        }
        catch (UnknownHostException e) {
            error.setText("Error parsing IP address");
            selectable.setTextFocus(addressInput);
            return;
        }

        int port = tryParsePort(strPort);
        if (port == -1) {
            selectable.setTextFocus(portInput);
            return;
        }

        Game game = overlay.getGame();
        try {
            game.joinServer(new InetSocketAddress(netAddress, port));
        }
        catch (IllegalArgumentException e) {
            error.setText("Invalid port");
            return;
        }
        overlay.pushScreen(new ConnectingScreen(overlay, strAddress + ":" + strPort));

        game.getSettings().lastJoinedIP = strAddress;
    }

    public void positionPointer(double xPos, double yPos) {
        selectable.clearSelection();
        selectable.selectByPointer(xPos, yPos);
    }

    @Override
    public boolean trigger() {
        if (selectable.isLastTextInputFocused()) {
            tryJoinServer();
            return true;
        }

        selectable.tab();
        return true;
    }

    @Override
    public boolean click(double xPos, double yPos) {
        boolean used = selectable.focusTextByPointer(xPos, yPos);
        if (used) {
            return true;
        }
        return super.click(xPos, yPos);
    }
}

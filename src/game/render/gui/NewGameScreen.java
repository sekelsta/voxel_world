package sekelsta.game.render.gui;

import java.util.function.Consumer;

import sekelsta.engine.file.SaveName;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class NewGameScreen extends Screen {
    protected TextElement title;
    protected TextElement nameLabel;
    protected TextInput nameInput;
    protected OptionalText error;
    protected TextButton start;
    protected TextButton cancel;

    public NewGameScreen(Overlay overlay, Consumer<SaveName> onComplete) {
        Game game = overlay.getGame();
        this.title = new TextElement(Fonts.getTitleFont(), "New Game");
        this.nameLabel = new TextElement(Fonts.getButtonFont(), "Enter name:");
        this.nameInput = new TextInput(Fonts.getButtonFont(), "", "Name");
        this.error = new OptionalText(Fonts.getTextFont());
        this.start = new TextButton(Fonts.getButtonFont(), "Done", () -> tryStart(game, onComplete));
        this.cancel = new TextButton(Fonts.getButtonFont(), "Cancel", () -> game.escape());
        addItem(title);
        addItem(nameLabel);
        addSelectableItem(nameInput, 2);
        addItem(error);
        addSelectableItem(start);
        addSelectableItem(cancel);
    }

    private void tryStart(Game game, Consumer<SaveName> onComplete) {
        String name = nameInput.getEnteredText();
        if ("".equals(name)) {
            error.setText("Enter a name", Fonts.ERROR_COLOR);
            return;
        }

        onComplete.accept(new SaveName(name));
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        return start.trigger();
    }
}

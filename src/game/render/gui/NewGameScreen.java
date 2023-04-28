package sekelsta.game.render.gui;

import java.util.function.Consumer;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;
import sekelsta.game.SaveGame;

public class NewGameScreen extends Screen {
    protected TextElement title;
    protected TextElement nameLabel;
    protected TextInput nameInput;
    protected OptionalText error;
    protected Button start;
    protected Button cancel;

    public NewGameScreen(Overlay overlay, Consumer<SaveGame> onComplete) {
        Game game = overlay.getGame();
        this.title = new TextElement(Fonts.getTitleFont(), "New Game");
        this.nameLabel = new TextElement(Fonts.getButtonFont(), "Enter name:");
        this.nameInput = new TextInput(Fonts.getButtonFont(), "", "Name");
        this.error = new OptionalText(Fonts.getTextFont());
        this.start = new Button(new TextElement(Fonts.getButtonFont(), "Done"), () -> tryStart(game, onComplete));
        this.cancel = new Button(new TextElement(Fonts.getButtonFont(), "Cancel"), () -> game.escape());
        addItem(title);
        addItem(nameLabel);
        addSelectableItem(nameInput, 2);
        addItem(error);
        addSelectableItem(start);
        addSelectableItem(cancel);
    }

    private void tryStart(Game game, Consumer<SaveGame> onComplete) {
        String name = nameInput.getEnteredText();
        if ("".equals(name)) {
            error.setText("Enter a name", Fonts.ERROR_COLOR);
            return;
        }
        name = name.strip();
        if ("".equals(name)) {
            error.setText("Include visible characters", Fonts.ERROR_COLOR);
            return;
        }

        onComplete.accept(SaveGame.createNew(name));
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        return start.trigger();
    }
}

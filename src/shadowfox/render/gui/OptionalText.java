package shadowfox.render.gui;

import java.awt.Color;

import shadowfox.render.text.BitmapFont;

public class OptionalText extends TextElement {
    public OptionalText(BitmapFont font) {
        super(font, "");
    }

    public OptionalText(BitmapFont font, Color color) {
        super(font, "", color);
    }

    public OptionalText(BitmapFont font, String text, Color color) {
        super(font, text, color);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setText(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public int getHeight() {
        return "".equals(text) ? 0 : font.getHeight();
    }
}

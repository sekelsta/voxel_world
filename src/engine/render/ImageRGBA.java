package sekelsta.engine.render;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ImageRGBA {
    public final int width;
    public final int height;
    public final ByteBuffer pixels;

    public ImageRGBA(int width, int height, ByteBuffer pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public ImageRGBA(BufferedImage image) {
        this(image, false);
    }

    private ImageRGBA(BufferedImage image, boolean flip) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());
        int[] intPixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), intPixels, 0, image.getWidth());

        if (flip) {
            for (int y = image.getHeight() - 1; y >= 0; y -= 1) {
                loadLine(image, pixels, intPixels, y);
            }
        }
        else {
            for (int y = 0; y < image.getHeight(); ++y) {
                loadLine(image, pixels, intPixels, y);
            }
        }

        pixels.flip();
    }

    private void loadLine(BufferedImage image, ByteBuffer pixels, int[] intPixels, int y) {
        for (int x = 0; x < image.getWidth(); ++x) {
            int pixel = intPixels[y * image.getWidth() + x];
            // R, G, B, A
            pixels.put((byte)((pixel >> 16) & 0xff));
            pixels.put((byte)((pixel >> 8) & 0xff));
            pixels.put((byte)((pixel >> 0) & 0xff));
            pixels.put((byte)((pixel >> 24) & 0xff));
        }
    }

    public static ImageRGBA flip(BufferedImage image) {
        return new ImageRGBA(image, true);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}

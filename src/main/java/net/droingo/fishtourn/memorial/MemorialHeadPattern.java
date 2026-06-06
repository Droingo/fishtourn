package net.droingo.fishtourn.memorial;

import net.minecraft.util.math.Vec3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class MemorialHeadPattern {
    private static final int FACE_SIZE = 8;

    private MemorialHeadPattern() {
    }

    public static List<HeadPixel> loadHeadPixels(String resourcePath) {
        try (InputStream stream = MemorialHeadPattern.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Could not find memorial skin resource: " + resourcePath);
            }

            BufferedImage image = ImageIO.read(stream);
            List<HeadPixel> pixels = new ArrayList<>();

            // Base head layer.
            loadFace(image, pixels, HeadFace.TOP, 8, 0, false);
            loadFace(image, pixels, HeadFace.BOTTOM, 16, 0, false);
            loadFace(image, pixels, HeadFace.RIGHT, 0, 8, false);
            loadFace(image, pixels, HeadFace.FRONT, 8, 8, false);
            loadFace(image, pixels, HeadFace.LEFT, 16, 8, false);
            loadFace(image, pixels, HeadFace.BACK, 24, 8, false);

            // Second/hat layer.
            loadFace(image, pixels, HeadFace.TOP, 40, 0, true);
            loadFace(image, pixels, HeadFace.BOTTOM, 48, 0, true);
            loadFace(image, pixels, HeadFace.RIGHT, 32, 8, true);
            loadFace(image, pixels, HeadFace.FRONT, 40, 8, true);
            loadFace(image, pixels, HeadFace.LEFT, 48, 8, true);
            loadFace(image, pixels, HeadFace.BACK, 56, 8, true);

            return pixels;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load memorial skin head", e);
        }
    }

    private static void loadFace(
            BufferedImage image,
            List<HeadPixel> pixels,
            HeadFace face,
            int startX,
            int startY,
            boolean overlay
    ) {
        for (int y = 0; y < FACE_SIZE; y++) {
            for (int x = 0; x < FACE_SIZE; x++) {
                int argb = image.getRGB(startX + x, startY + y);

                int alpha = (argb >> 24) & 0xFF;
                if (alpha < 10) {
                    continue;
                }

                float red = ((argb >> 16) & 0xFF) / 255.0F;
                float green = ((argb >> 8) & 0xFF) / 255.0F;
                float blue = (argb & 0xFF) / 255.0F;

                pixels.add(new HeadPixel(face, x, y, red, green, blue, overlay));
            }
        }
    }

    public static Vec3d getCubeOffset(HeadPixel pixel, double pixelSpacing) {
        double half = FACE_SIZE * pixelSpacing / 2.0D;
        double inset = pixelSpacing / 2.0D;

        double localX = -half + inset + pixel.x() * pixelSpacing;
        double localY = half - inset - pixel.y() * pixelSpacing;
        double localZ = -half + inset + pixel.x() * pixelSpacing;

        double faceDepth = half;
        if (pixel.overlay()) {
            faceDepth += pixelSpacing * 0.18D;
        }

        return switch (pixel.face()) {
            case FRONT -> new Vec3d(localX, localY, -faceDepth);
            case BACK -> new Vec3d(-localX, localY, faceDepth);
            case LEFT -> new Vec3d(-faceDepth, localY, -localZ);
            case RIGHT -> new Vec3d(faceDepth, localY, localZ);
            case TOP -> new Vec3d(localX, faceDepth, localZ);
            case BOTTOM -> new Vec3d(localX, -faceDepth, -localZ);
        };
    }

    public enum HeadFace {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

    public record HeadPixel(
            HeadFace face,
            int x,
            int y,
            float red,
            float green,
            float blue,
            boolean overlay
    ) {
    }
}
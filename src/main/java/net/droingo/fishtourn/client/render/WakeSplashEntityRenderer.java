package net.droingo.fishtourn.client.render;

import net.droingo.fishtourn.entity.WakeSplashEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class WakeSplashEntityRenderer extends EntityRenderer<WakeSplashEntity> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/misc/unknown_pack.png");

    public WakeSplashEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(WakeSplashEntity entity) {
        return TEXTURE;
    }
}
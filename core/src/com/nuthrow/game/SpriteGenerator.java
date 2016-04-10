package com.nuthrow.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by mehul on 4/10/16.
 */
public class SpriteGenerator {
    public static Sprite generateSpriteForBody(AssetManager assetManager, Body body){
        if("horizontal".equals(body.getUserData()))
            return createSprite(assetManager, "obstacleHorizontal.png");
        if("vertical".equals(body.getUserData()))
            return createSprite(assetManager, "obstacleVertical.png");
        if("enemy".equals(body.getUserData()))
            return createSprite(assetManager, "bird.png");
        return null;
    }
    private static Sprite createSprite(AssetManager assetManager,
                                       String textureName) {
        Sprite sprite = new Sprite(assetManager.get(textureName,
                Texture.class));
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() /
                2);
        return sprite;
    }
}

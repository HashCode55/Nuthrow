package com.nuthrow.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.nuthrow.game.screens.LoadingScreen;

public class Nuthrow extends Game {
	SpriteBatch batch;
	Texture img;

	private final AssetManager assetManager = new AssetManager();
	@Override
	public void create () {
        //initialise Box2D
        Box2D.init();
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(
                new InternalFileHandleResolver()
        ));
		setScreen(new LoadingScreen(this));
	}

    public AssetManager getAssetManager(){
        return assetManager;
    }
}

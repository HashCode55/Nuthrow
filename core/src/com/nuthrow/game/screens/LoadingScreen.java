package com.nuthrow.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nuthrow.game.Nuthrow;

/**
 * Created by mehul on 3/30/16.
 */
public class LoadingScreen extends ScreenAdapter {
    private static final float WORLD_WIDTH  = 640;
    private static final float WORLD_HEIGHT  = 480;
    private static final float PROGRESS_BAR_WIDTH = 100;
    private static final float PROGRESS_BAR_HEIGHT  = 25;

    private Nuthrow nuthrow;
    private Viewport viewport;
    private Camera camera;
    private float progress = 0;
    private ShapeRenderer shapeRenderer;

    public LoadingScreen(Nuthrow nuthrow){
        this.nuthrow = nuthrow;
    }

    @Override
    public void show() {
        super.show();
        camera = new OrthographicCamera();
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapeRenderer = new ShapeRenderer();
        nuthrow.getAssetManager().load("nuthrow_tileMap.tmx", TiledMap.class);
        //nuthrow.getAssetManager().load("nuttybirds.tmx", TiledMap.class);
        nuthrow.getAssetManager().load("obstacleVertical.png", Texture.class);
        nuthrow.getAssetManager().load("obstacleHorizontal.png", Texture.class);
        nuthrow.getAssetManager().load("bird.png", Texture.class);
        nuthrow.getAssetManager().load("slingshot.png",
                Texture.class);
        nuthrow.getAssetManager().load("squirrel.png", Texture.class);
        nuthrow.getAssetManager().load("acorn.png", Texture.class);
        nuthrow.getAssetManager().finishLoading();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update();
        clearScreen();
        draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }

    private void update(){
        if(nuthrow.getAssetManager().update()){
            nuthrow.setScreen(new GameScreen(nuthrow));
        }
        else {
            progress = nuthrow.getAssetManager().getProgress() * PROGRESS_BAR_WIDTH;
        }
    }

    private void draw(){
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect((WORLD_WIDTH - PROGRESS_BAR_WIDTH) / 2, (WORLD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        shapeRenderer.end();
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}

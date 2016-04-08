package com.nuthrow.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nuthrow.game.Nuthrow;
import com.nuthrow.game.helper.TiledObjectBodyBuilder;

/**
 * Created by mehul on 3/30/16.
 */
public class GameScreen extends ScreenAdapter {

    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float UNITS_PER_METER = 32F;
    private static float UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static float UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Body body;
    private OrthographicCamera camera;
    private OrthographicCamera box2dCamera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private Nuthrow nuthrow;

    private SpriteBatch spriteBatch;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;

    public GameScreen(Nuthrow nuthrow){
        this.nuthrow = nuthrow;
    }

    @Override
    public void show() {
        super.show();
        box2dCamera = new OrthographicCamera(UNIT_WIDTH, UNIT_HEIGHT);
        world = new World(new Vector2(0, -10F), true);
        debugRenderer = new Box2DDebugRenderer();
        body = createBody();
        body.setTransform(100, 120, 0);
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);
        shapeRenderer = new ShapeRenderer();
        tiledMap = nuthrow.getAssetManager().get("nuthrow_tileMap.tmx", TiledMap.class);
        //tiledMap = nuthrow.getAssetManager().get("nuttybirds.tmx", TiledMap.class);
        spriteBatch = new SpriteBatch();
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, spriteBatch);
        orthogonalTiledMapRenderer.setView(camera);
        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world);
        TiledObjectBodyBuilder.bulidFloorBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildBirBodies(tiledMap, world);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer,
                                     int button) {
                createBullet();
                return true;
            }
        });
    }

    private Body createBody(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body box = world.createBody(bodyDef);
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(60 / UNITS_PER_METER, 60 / UNITS_PER_METER);
        box.createFixture(polygonShape, 1);
        polygonShape.dispose();
        return box;
    }

    private void createBullet(){
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(0.5f);
        circleShape.setPosition(new Vector2(3, 6));
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        Body bullet = world.createBody(bd);
        bullet.createFixture(circleShape, 0);
        circleShape.dispose();
        bullet.setLinearVelocity(10, 6);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void drawDebug(){
        debugRenderer.render(world, box2dCamera.combined);
    }

    private void draw(){
        spriteBatch.setProjectionMatrix(camera.projection);
        spriteBatch.setTransformMatrix(camera.view);
        orthogonalTiledMapRenderer.render();
    }

    private void update(float delta){
        box2dCamera.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);
        box2dCamera.update();
        world.step(delta, 6, 2);
        body.setAwake(true);
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(Color.TEAL.r, Color.TEAL.g, Color.TEAL.b, Color.TEAL.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

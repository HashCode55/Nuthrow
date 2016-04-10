package com.nuthrow.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.nuthrow.game.Nuthrow;
import com.nuthrow.game.SpriteGenerator;
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

    private static final float MAX_STRENGTH = 15;
    private static final float MAX_DISTANCE = 100;
    private static final float UPPER_ANGLE = 3 * MathUtils.PI / 2f;
    private static final float LOWER_ANGLE = MathUtils.PI / 2f;

    private final Vector2 anchor = new
            Vector2(convertMetresToUnits(6.125f), convertMetresToUnits(5.75f));
    private final Vector2 firingPosition = anchor.cpy();
    private float distance;
    private float angle;

    private ObjectMap<Body, Sprite> sprites = new ObjectMap<Body, Sprite>();

    private Sprite slingShot;
    private Sprite squirrel;
    private Sprite staticAcorn;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Body body;
    private OrthographicCamera camera;
    private OrthographicCamera box2dCamera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private Nuthrow nuthrow;
    private Array<Body> toRemove = new Array<Body>();

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
        spriteBatch = new SpriteBatch();

        tiledMap = nuthrow.getAssetManager().get("nuthrow_tileMap.tmx", TiledMap.class);
        //tiledMap = nuthrow.getAssetManager().get("nuttybirds.tmx", TiledMap.class);
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, spriteBatch);
        orthogonalTiledMapRenderer.setView(camera);
        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world);
        TiledObjectBodyBuilder.bulidFloorBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildBirBodies(tiledMap, world);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDragged(int screenX, int screenY, int
                    pointer) {
                calculateAngleAndDistanceForBullet(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer,
                                   int button) {
                createBullet();
                firingPosition.set(anchor.cpy());
                return true;
            }
        });

        world.setContactListener(new NuttyContactListener());

        Array<Body> bodies = new Array<Body>();
        world.getBodies(bodies);
        for(Body body : bodies){
            Sprite sprite = SpriteGenerator.generateSpriteForBody(nuthrow.getAssetManager(),
                    body);
            if(sprite != null) sprites.put(body, sprite);
        }

        slingShot = new Sprite(nuthrow.getAssetManager().get("slingshot.png", Texture.class));
        slingShot.setPosition(170, 64);
        squirrel = new Sprite(nuthrow.getAssetManager().get("squirrel.png", Texture.class));
        squirrel.setPosition(32, 64);
        staticAcorn = new Sprite(nuthrow.getAssetManager().get("acorn.png", Texture.class));


    }

    @Override
    public void render(float delta) {
        super.render(delta);
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }

    private void update(float delta){
        clearDeadBodies();
        world.step(delta, 6, 2);
        body.setAwake(true);
        box2dCamera.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);
        box2dCamera.update();
        updateSpritePositions();
    }

    private void updateSpritePositions(){
        for(Body body : sprites.keys()){
            Sprite sprite = sprites.get(body);
            sprite.setPosition(convertMetresToUnits(body.getPosition().x) - sprite.getWidth() / 2f,
                               convertMetresToUnits(body.getPosition().y) - sprite.getHeight() / 2f);
            sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
        }
        staticAcorn.setPosition(firingPosition.x - staticAcorn.getWidth() / 2f, firingPosition.y -
                staticAcorn.getHeight() / 2f);
    }

    private void createBullet(){
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(0.5f);
        //circleShape.setPosition(new Vector2(convertUnitsToMetres(firingPosition.x), convertUnitsToMetres(firingPosition.y)));
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        Body bullet = world.createBody(bd);
        bullet.setUserData("acorn");
        bullet.createFixture(circleShape, 1);
        bullet.setTransform(new Vector2(convertUnitsToMetres(firingPosition.x), convertUnitsToMetres(firingPosition.y)), 0);

        Sprite sprite = new Sprite(nuthrow.getAssetManager().get
                ("acorn.png", Texture.class));
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
        sprites.put(bullet, sprite);

        circleShape.dispose();

        float velX = Math.abs( (MAX_STRENGTH * -MathUtils.cos(angle) *
                (distance / 100f)));
        float velY = Math.abs( (MAX_STRENGTH * -MathUtils.sin(angle) *
                (distance / 100f)));
        bullet.setLinearVelocity(velX, velY);
    }

    private void drawDebug(){
        debugRenderer.render(world, box2dCamera.combined);
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(anchor.x - 5, anchor.y - 5, 10, 10);
        shapeRenderer.rect(firingPosition.x - 5, firingPosition.y - 5, 10, 10);
        shapeRenderer.line(anchor.x, anchor.y, firingPosition.x, firingPosition.y);
        shapeRenderer.end();
    }

    private void draw(){
        spriteBatch.setProjectionMatrix(camera.projection);
        spriteBatch.setTransformMatrix(camera.view);
        orthogonalTiledMapRenderer.render();
        spriteBatch.begin();
        for(Sprite sprite : sprites.values()){
            sprite.draw(spriteBatch);
        }
        squirrel.draw(spriteBatch);
        staticAcorn.draw(spriteBatch);
        slingShot.draw(spriteBatch);
        spriteBatch.end();
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

    private void clearDeadBodies(){
        for(Body body : toRemove){
            sprites.remove(body);
            world.destroyBody(body);
        }
        toRemove.clear();
    }

    //helper functions
    private float convertUnitsToMetres(float pixels) {
        return pixels / UNITS_PER_METER;
    }

    private float convertMetresToUnits(float metres) {
        return metres * UNITS_PER_METER;
    }
    private float angleBetweenTwoPoints(){
        float angle = MathUtils.atan2((anchor.y - firingPosition.y), (anchor.x - firingPosition.x));
        angle %= 2 * MathUtils.PI;
        if(angle < 0) angle += 2 * MathUtils.PI2;
        return angle;
    }
    private float distanceBetweenTwoPoints(){
        return (float) Math.sqrt(((anchor.x - firingPosition.x) *
                (anchor.x - firingPosition.x)) + ((anchor.y - firingPosition.y)
                * (anchor.y - firingPosition.y)));
    }
    private void calculateAngleAndDistanceForBullet(int screenX, int screenY){
        firingPosition.set(screenX, screenY);
        //this step is important
        viewport.unproject(firingPosition);
        distance = distanceBetweenTwoPoints();
        angle = angleBetweenTwoPoints();
        if(distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }
        if(angle > LOWER_ANGLE){
            if(angle > UPPER_ANGLE){
                angle = 0;
            }
            else {
                angle = LOWER_ANGLE;
            }
        }
        firingPosition.set(anchor.x + (distance * -
                MathUtils.cos(angle)), anchor.y + (distance * -
                MathUtils.sin(angle)));
    }

    class NuttyContactListener implements ContactListener {


        public void beginContact(Contact contact){
            //their AABBs are touching but the bodies might not be touching.
            //Axis Aligned Bounding Boxes
            if(contact.isTouching()){
                Fixture attacker = contact.getFixtureA();
                Fixture defender = contact.getFixtureB();
                WorldManifold worldManifold = contact.getWorldManifold();
                if("enemy".equals(defender.getUserData())){
                    Vector2 vel1 = attacker.getBody().
                            getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 vel2 = defender.getBody().
                            getLinearVelocityFromWorldPoint(worldManifold.getPoints()[0]);
                    Vector2 impactVelocity = vel1.sub(vel2);
                    if(Math.abs(impactVelocity.x) > 1 || Math.abs(impactVelocity.y) > 1){
                        toRemove.add(defender.getBody());
                    }
                }
            }
        }
        @Override
        public void endContact(Contact contact) {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }

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

}

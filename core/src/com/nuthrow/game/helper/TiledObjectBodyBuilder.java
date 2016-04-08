package com.nuthrow.game.helper;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by mehul on 3/31/16.
 */
//this class is used to extract the objects from the tile map.
public class TiledObjectBodyBuilder {
    private static final float PIXELS_PER_TILE = 32F;
    private static final float HALF = 0.5F;

    public static void buildBuildingBodies(TiledMap tiledMap, World world){
        MapObjects objects = tiledMap.getLayers().get("Physics_Buildings").getObjects();
        for(MapObject mapObject : objects){
            PolygonShape rectangle = getRectangle((RectangleMapObject)mapObject);
            BodyDef bd = new BodyDef();
            //define a dynamic body
            bd.type = BodyDef.BodyType.DynamicBody;
            //create the body
            Body body = world.createBody(bd);
            //add a fixture
            body.createFixture(rectangle, 1);
            //dispose the shape.
            rectangle.dispose();
        }
    }

    public static void bulidFloorBodies(TiledMap tiledMap, World world){
        MapObjects objects = tiledMap.getLayers().get("Physics_Floor").getObjects();
        for(MapObject mapObject : objects){
            PolygonShape rectangle = getRectangle((RectangleMapObject)mapObject);
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bd);
            body.createFixture(rectangle, 1);
            rectangle.dispose();
        }
    }

    public static void buildBirBodies(TiledMap tiledMap, World world){
        MapObjects objects = tiledMap.getLayers().get("Physics_Birds").getObjects();
        for(MapObject mapObject : objects){
            CircleShape circleShape = getCircle((EllipseMapObject)mapObject);
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.DynamicBody;
            Body body = world.createBody(bd);
            body.createFixture(circleShape, 1);
            circleShape.dispose();
        }
    }

    private static PolygonShape getRectangle(RectangleMapObject rectangleMapObject){
        Rectangle rectangle = rectangleMapObject.getRectangle();
        PolygonShape polygonShape = new PolygonShape();
        Vector2 size = new Vector2(
                (rectangle.x + rectangle.width * HALF) / PIXELS_PER_TILE,
                (rectangle.y + rectangle.height * HALF) / PIXELS_PER_TILE
        );
        //set as box takes the first two parameters as locations and then the
        //vector2 center, and then the rotation.
        polygonShape.setAsBox(
                rectangle.width * HALF / PIXELS_PER_TILE,
                rectangle.height * HALF / PIXELS_PER_TILE,
                size,
                0.0f
        );
        return polygonShape;
    }

    private static CircleShape getCircle(EllipseMapObject ellipseMapObject){
        Ellipse ellipse = ellipseMapObject.getEllipse();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(ellipse.width * HALF / PIXELS_PER_TILE);
        circleShape.setPosition(new Vector2((ellipse.x + ellipse.width * HALF) / PIXELS_PER_TILE,
                (ellipse.y + ellipse.height * HALF) / PIXELS_PER_TILE));
        return circleShape;
    }
}

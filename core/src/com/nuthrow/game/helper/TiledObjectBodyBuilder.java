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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.lang.reflect.Field;

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
            RectangleMapObject rectangleMapObject = (RectangleMapObject)mapObject;
            PolygonShape rectangle = getRectangle(rectangleMapObject);
            BodyDef bd = new BodyDef();
            //define a dynamic body
            bd.type = BodyDef.BodyType.DynamicBody;
            //create the body
            Body body = world.createBody(bd);

            if(rectangleMapObject.getRectangle().width >
                    rectangleMapObject.getRectangle().height){
                body.setUserData("horizontal");
            }
            else {
                body.setUserData("vertical");
            }
            //add a fixture
            body.createFixture(rectangle, 1);
            body.setTransform(getTransformForRectangle(rectangleMapObject.getRectangle()), 0);
            //dispose the shape.
            rectangle.dispose();
        }
    }

    public static void bulidFloorBodies(TiledMap tiledMap, World world){
        MapObjects objects = tiledMap.getLayers().get("Physics_Floor").getObjects();
        for(MapObject mapObject : objects){
            RectangleMapObject rectangleMapObject = (RectangleMapObject)mapObject;
            PolygonShape rectangle = getRectangle(rectangleMapObject);
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.StaticBody;
            Body body = world.createBody(bd);
            body.setUserData("floor");
            body.createFixture(rectangle, 1);
            body.setTransform(getTransformForRectangle(rectangleMapObject.getRectangle()), 0);
            rectangle.dispose();
        }
    }

    public static void buildBirBodies(TiledMap tiledMap, World world){
        MapObjects objects = tiledMap.getLayers().get("Physics_Birds").getObjects();
        for(MapObject mapObject : objects){
            EllipseMapObject ellipseMapObject = (EllipseMapObject) mapObject;
            CircleShape circleShape = getCircle(ellipseMapObject);
            BodyDef bd = new BodyDef();
            bd.type = BodyDef.BodyType.DynamicBody;
            Body body = world.createBody(bd);
            Fixture fixture = body.createFixture(circleShape, 1);
            fixture.setUserData("enemy");
            body.setUserData("enemy");

            Ellipse ellipse = ellipseMapObject.getEllipse();
            body.setTransform(new Vector2((ellipse.x + ellipse.width *
                    HALF) / PIXELS_PER_TILE, (ellipse.y + ellipse.height * HALF) /
                    PIXELS_PER_TILE), 0);
            circleShape.dispose();
        }
    }

    private static PolygonShape getRectangle(RectangleMapObject rectangleMapObject){
        Rectangle rectangle = rectangleMapObject.getRectangle();
        PolygonShape polygonShape = new PolygonShape();
        //rectangle.x is the x coordinate of the bottom left corner.
        //Vector2 size = new Vector2(
        //        (rectangle.x + rectangle.width * HALF) / PIXELS_PER_TILE,
        //        (rectangle.y + rectangle.height * HALF) / PIXELS_PER_TILE
        //);
        //set as box takes the first two parameters as locations and then the
        //vector2 center, and then the rotation.
        polygonShape.setAsBox(
                rectangle.width * HALF / PIXELS_PER_TILE,
                rectangle.height * HALF / PIXELS_PER_TILE
        );
        return polygonShape;
    }

    private static CircleShape getCircle(EllipseMapObject ellipseMapObject){
        Ellipse ellipse = ellipseMapObject.getEllipse();
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(ellipse.width * HALF / PIXELS_PER_TILE);
        //circleShape.setPosition(new Vector2((ellipse.x + ellipse.width * HALF) / PIXELS_PER_TILE,
          //      (ellipse.y + ellipse.height * HALF) / PIXELS_PER_TILE));
        return circleShape;
    }

    private static Vector2 getTransformForRectangle(Rectangle
                                                            rectangle) {
        return new Vector2((rectangle.x + (rectangle.width * HALF)) /
                PIXELS_PER_TILE, (rectangle.y  + (rectangle.height * HALF)) /
                PIXELS_PER_TILE);
    }
}

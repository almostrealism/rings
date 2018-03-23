package com.almostrealism.projection;

import org.almostrealism.algebra.Vector;
import org.almostrealism.space.BoundingSolid;
import org.almostrealism.space.Scene;

/**
 * Auto positions the camera along the -z axis to view the whole scene.
 *
 * @author Dan Chivers
 */
public class CameraPositioner {
    private final PinholeCamera camera;
    private final Scene scene;

    private Vector location;
    private Vector direction;

    public CameraPositioner(PinholeCamera camera, Scene scene) {
        this.camera = camera;
        this.scene = scene;
        calculatePosition();
    }

    private void calculatePosition() {
        // Bounding sphere of the scene
        BoundingSolid sceneBounds = scene.calculateBoundingSolid();
        Vector sceneMidpoint = (Vector) sceneBounds.center;
        double sceneBoundingRadius = Math.sqrt(Math.pow(sceneBounds.dx/2, 2) +
                Math.pow(sceneBounds.dy/2, 2) +
                Math.pow(sceneBounds.dz/2, 2));

        // FOVs
        double[] fovs = camera.getFOV();
        double horizontalFov = fovs[0];
        double verticalFov = fovs[1];

        // Camera distance
        double distance = sceneBoundingRadius / Math.sin(Math.min(horizontalFov, verticalFov));

        // I think this should be .add(), but camera axis moves seem to be opposite.
        location = new Vector(0, 0, distance).subtract(sceneMidpoint);

        // Direction doesn't appear to have an effect at the moment.
        direction = sceneMidpoint.add(location);    // Add instead of subtract due to the inverted axis.
    }

    public Vector getLocation() {
        return location;
    }

    public Vector getViewingDirection() {
        return direction;
    }
}
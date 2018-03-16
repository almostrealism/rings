package com.almostrealism.projection;

import org.almostrealism.algebra.Vector;
import org.almostrealism.space.BoundingSolid;
import org.almostrealism.space.Scene;

/**
 * Auto position the camera along the z axis to view the whole scene.
 *
 * @author Dan Chivers
 */
public class CameraPositioner {
    private final PinholeCamera camera;
    private final Scene scene;

    private Vector location;

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

        // Buffer
        sceneBoundingRadius *= 1.5;

        // FOVs
        double[] fovs = camera.getFOV();
        double horizontalFov = fovs[0];
        double verticalFov = fovs[1];

        // Camera distance
        double distance = sceneBoundingRadius / Math.sin(Math.min(horizontalFov, verticalFov));

        location = new Vector(0, 0, distance).subtract(sceneMidpoint);
    }

    public Vector getLocation() {
        return location;
    }

    public Vector getViewingDirection() {
        return new Vector(0,0,1);
    }
}

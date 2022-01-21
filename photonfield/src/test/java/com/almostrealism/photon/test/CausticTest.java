/*
 * Copyright 2022 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.photon.test;

import com.almostrealism.lighting.PointLight;
import com.almostrealism.primitives.Sphere;
import com.almostrealism.projection.ThinLensCamera;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.RefractionShader;
import com.almostrealism.raytrace.FogParameters;
import com.almostrealism.raytrace.RayIntersectionEngine;
import com.almostrealism.raytrace.RenderParameters;
import com.almostrealism.raytracer.RayTracedScene;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBFeatures;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.FileDecoder;
import org.almostrealism.space.Plane;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.texture.StripeTexture;
import org.almostrealism.CodeFeatures;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CausticTest implements Runnable, RGBFeatures, CodeFeatures {
    public static boolean useStripedFloor = false;
    public static boolean useCornellBox = false;
    public static boolean displaySpheres = true;
    public static boolean displayDragon = false;

    @Test
    public void run() {
        Scene<ShadableSurface> scene = new Scene<>();

        try {
            if (useCornellBox) {
                scene = FileDecoder.decodeScene(new FileInputStream(new File("../rings/CornellBox.xml")),
                        FileDecoder.XMLEncoding,
                        false, (e) -> {
                            e.printStackTrace();
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }

        if (useStripedFloor) {
            Plane p = new Plane(Plane.XZ);
            p.setLocation(new Vector(0.0, -10, 0.0));
            p.addTexture(new StripeTexture());
            scene.add(p);
        }

        if (displaySpheres) {
            Sphere s1 = new Sphere(new Vector(-1.0, -2.25, -2), 0.8, new RGB(0.3, 0.3, 0.3));
            s1.getShaderSet().clear();
            s1.addShader(new RefractionShader());

            Sphere s2 = new Sphere(new Vector(1.0, -2.25, -2), 0.8, new RGB(0.3, 0.3, 0.3));
            s2.addShader(new ReflectionShader(0.8, rgb(0.8)));
            s2.getShaderSet().clear();

            scene.add(s1);
            scene.add(s2);
        }

        if (displayDragon) {
            try {
                scene.add(((Scene<ShadableSurface>) FileDecoder.decodeScene(new FileInputStream(new File("dragon.ply")),
                                                    FileDecoder.PLYEncoding, false, null)).get(0));
            } catch (Exception e) {
                e.printStackTrace();
                assert false;
            }
        }

        Plane p = new Plane(Plane.XY);
        p.setLocation(new Vector(0, 0, 1));
        p.setShadeBack(true);
        p.setShadeFront(true);
//		scene.add(p);

        for (ShadableSurface s : scene) {
            if (s instanceof AbstractSurface) {
                ((AbstractSurface) s).setShadeFront(true);
                ((AbstractSurface) s).setShadeBack(true);
            }

            if (s instanceof Plane) {
                ((Plane) s).setColor(new RGB(1.0, 1.0, 1.0));
                if (((Plane) s).getType() == Plane.XZ) {
                    if (((Plane) s).getLocation().getX() <= 0) {
                        System.out.println(((Plane) s).getLocation());
                    }
                }
            }
        }

        scene.addLight(new PointLight(new Vector(00.0, 10.0, -1.0), 1.0, new RGB(0.8, 0.9, 0.7)));

        ThinLensCamera c = scene.getCamera() == null ? new ThinLensCamera() : (ThinLensCamera) scene.getCamera();
        c.setViewDirection(new Vector(0.0, -0.05, 1.0));
        c.setProjectionDimensions(50, 45);
        c.setFocalLength(400);
        Vector l = c.getLocation();
        l.setZ(-60);
        c.setLocation(l);
        scene.setCamera(c);

        RenderParameters params = new RenderParameters();
        params.width = (int) (c.getProjectionWidth() * 10);
        params.height = (int) (c.getProjectionHeight() * 10);
        params.dx = (int) (c.getProjectionWidth() * 10);
        params.dy = (int) (c.getProjectionHeight() * 10);

        RayTracedScene r = new RayTracedScene(new RayIntersectionEngine(scene, new FogParameters()), c, params);

        try {
            ImageCanvas.encodeImageFile(r.realize(params).get(), new File("test.jpeg"),
                    ImageCanvas.JPEGEncoding);
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: Output file not found");
            assert false;
        } catch (IOException ioe) {
            System.out.println("IO ERROR");
            assert false;
        }
    }

    public static void main(String args[]) {
        new CausticTest().run();
        System.exit(0);
    }
}

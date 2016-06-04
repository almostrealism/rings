/*
 * Copyright (C) 2004  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.graphics;

import java.awt.Graphics;

import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.engine.ParticleGroup;
import com.almostrealism.util.TransformMatrix;
import com.almostrealism.util.Vector;


/**
 * @author Mike Murray
 */
public class ParticleGroupRenderer {
    public static void draw(ParticleGroup p, PinholeCamera c, Graphics g, double ox, double oy, double scale, double minSize, double maxSize, double far) {
        double v[][] = p.getParticleVertices();
        
        TransformMatrix m = c.getRotationMatrix();
        
        i: for (int i = 0; i < v.length; i++) {
            Vector l = new Vector(v[i][0], v[i][1], v[i][2]);
            m.transform(l, TransformMatrix.TRANSFORM_AS_LOCATION);
            
            if (l.getZ() < 0.0) continue i;
            
            double r = minSize + (maxSize - minSize) * (l.getZ() / far);
            
            double x = (l.getX() - r) * scale;
            double y = (l.getY() - r) * scale;
            
            g.fillOval((int)(ox + x), (int)(oy - y), (int)(2 * r * scale), (int)(2 * r * scale));
        }
    }
}

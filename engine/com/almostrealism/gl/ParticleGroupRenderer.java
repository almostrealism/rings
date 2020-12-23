/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.gl;

import java.awt.Graphics;

import org.almostrealism.algebra.ParticleGroup;
import org.almostrealism.geometry.TransformMatrix;

import com.almostrealism.projection.PinholeCamera;
import org.almostrealism.algebra.Vector;

import static org.almostrealism.util.Ops.*;

/**
 * @author Michael Murray
 */
public class ParticleGroupRenderer {
    public static void draw(ParticleGroup p, PinholeCamera c, Graphics g, double ox, double oy, double scale, double minSize, double maxSize, double far) {
        double v[][] = p.getParticleVertices();
        
        TransformMatrix m = c.getRotationMatrix();
        
        i: for (int i = 0; i < v.length; i++) {
            Vector l = m.transform(ops().vector(v[i][0], v[i][1], v[i][2]), TransformMatrix.TRANSFORM_AS_LOCATION).get().evaluate();
            
            if (l.getZ() < 0.0) continue i;
            
            double r = minSize + (maxSize - minSize) * (l.getZ() / far);
            
            double x = (l.getX() - r) * scale;
            double y = (l.getY() - r) * scale;
            
            g.fillOval((int)(ox + x), (int)(oy - y), (int)(2 * r * scale), (int)(2 * r * scale));
        }
    }
}

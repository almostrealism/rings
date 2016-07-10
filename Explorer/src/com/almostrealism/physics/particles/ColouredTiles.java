/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.physics.particles;


// ColouredTiles.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

// ColouredTiles creates a coloured quad array of tiles.
// No lighting since no normals or Material used

import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.ArrayList;


public class ColouredTiles extends Shape3D 
{
  private QuadArray plane;


  public ColouredTiles(ArrayList coords, Color3f col) 
  {
    plane = new QuadArray(coords.size(), 
				GeometryArray.COORDINATES | GeometryArray.COLOR_3 );
    createGeometry(coords, col);
    createAppearance();
  }    


  private void createGeometry(ArrayList coords, Color3f col)
  { 
    int numPoints = coords.size();

    Point3f[] points = new Point3f[numPoints];
    coords.toArray( points );
    plane.setCoordinates(0, points);

    Color3f cols[] = new Color3f[numPoints];
    for(int i=0; i < numPoints; i++)
      cols[i] = col;
    plane.setColors(0, cols);

    setGeometry(plane);
  }  // end of createGeometry()


  private void createAppearance()
  {
    Appearance app = new Appearance();

    PolygonAttributes pa = new PolygonAttributes();
    pa.setCullFace(PolygonAttributes.CULL_NONE);   
      // so can see the ColouredTiles from both sides
    app.setPolygonAttributes(pa);

    setAppearance(app);
  }  // end of createAppearance()


} // end of ColouredTiles class

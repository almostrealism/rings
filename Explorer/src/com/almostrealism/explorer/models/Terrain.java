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

package com.almostrealism.explorer.models;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;

import org.almostrealism.texture.ImageLayers;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.renderable.RenderableMesh;

public class Terrain extends RenderableMesh {
	private ImageLayers textures;
	
	public Terrain(double width, double height) {
		super(createMesh(width, height));
		
		textures = new ImageLayers();
	}
	
	/**
	 * TODO  This should add a texture layer with the shape file data?
	 */
	public void addShapeFile(String shapeURL) {
		try {
			Map connect = new HashMap();
			connect.put("url", shapeURL);
			
			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];
			
			System.out.println("Reading content " + typeName);
			
			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection collection = featureSource.getFeatures();

			try (FeatureIterator iterator = collection.features()) {
				while (iterator.hasNext()) {
					Feature feature = iterator.next();
					System.out.println(feature);
//					Geometry sourceGeometry = feature.getDefaultGeometryProperty().getDescriptor().;
				}
			}
		} catch (Throwable e) { }
	}
	
	public ImageLayers getTexture() { return textures; }
	
	public void init(GL2 gl) {
		list.setTexture(getTexture());
		super.init(gl);
	}
	
	private static Mesh createMesh(double width, double height) {
		return new Mesh(new TerrainVertexData(10, width, height));
	}
}

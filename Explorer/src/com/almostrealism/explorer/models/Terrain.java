package com.almostrealism.explorer.models;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL2;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.visualize.geometry.RenderableGeometry;

public class Terrain extends RenderableGeometry {

	public Terrain(String shapeURL) {
		super(new BasicGeometry());

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
//					Geometry sourceGeometry = feature.getDefaultGeometryProperty().getDescriptor().;
				}
			}
		} catch (Throwable e) { }
	}

	@Override
	public void init(GL2 gl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(GL2 gl) {
		// TODO Auto-generated method stub

	}

}

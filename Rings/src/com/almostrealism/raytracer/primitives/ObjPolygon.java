package com.almostrealism.raytracer.primitives;

public class ObjPolygon {
	private float vertices[][];
	private float texCoords[][];
	
	public float[][] getVertices() { return vertices; }
	public void setVertices(float vertices[][]) { this.vertices = vertices; }
	public float[][] getTexCoords() { return texCoords; }
	public void setTexCoords(float texCoords[][]) { this.texCoords = texCoords; }
}

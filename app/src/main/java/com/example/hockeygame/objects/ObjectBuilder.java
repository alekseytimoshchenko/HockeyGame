package com.example.hockeygame.objects;

import com.example.hockeygame.util.Geometry;

public class ObjectBuilder
{
	private static final int FLOATS_PER_VERTEX = 3;
	private final float[] vertexData;
	private int offset = 0;
	
	private ObjectBuilder(int sizeInVertices)
	{
		vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
	}
	
	private static int sizeOfCircleInVertices(int numPoints)
	{
		return 1 + (numPoints + 1);
	}
	
	private static int sizeOfOpenCylinderInVertices(int numPoints)
	{
		return (numPoints + 1) * 2;
	}
	
	!!! TOPIC !!!
	Building a Circle with a Triangle Fan
	
	static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints)
	{
		int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);
		ObjectBuilder builder = new ObjectBuilder(size);
		Geometry.Circle puckTop = new Geometry.Circle(puck.center.translateY(puck.height / 2f), puck.radius);
		builder.appendCircle(puckTop, numPoints);
		builder.appendOpenCylinder(puck, numPoints);
		
		return builder.build();
	}
}

package com.example.hockeygame.util;

import java.util.Vector;

public class Geometry
{
	public static class Point
	{
		public final float x, y, z;
		
		public Point(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Point translateY(float distance)
		{
			return new Point(x, y + distance, z);
		}
	}
	
	public static class Circle
	{
		public final Point center;
		public final float radius;
		
		public Circle(Point center, float radius)
		{
			this.center = center;
			this.radius = radius;
		}
		
		public Circle scale(float scale)
		{
			return new Circle(center, radius * scale);
		}
	}
	
	public static class Ray
	{
		public final Point point;
		public final Vector vector;
		
		public Ray(Point point, Vector vector)
		{
			this.point = point;
			this.vector = vector;
		}
	}
	
	public static class Vector
	{
		public final float x, y, z;
		
		public Vector(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static Vector vectorBetween(Point from, Point to)
	{
		return new Vector(to.x - from.x, to.y - from.y, to.z - from.z);
	}
	
	public static class Cylinder
	{
		public final Point center;
		public final float radius;
		public final float height;
		
		public Cylinder(Point center, float radius, float height)
		{
			this.center = center;
			this.radius = radius;
			this.height = height;
		}
	}
}

package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.hockeygame.objects.Mallet;
import com.example.hockeygame.objects.Puck;
import com.example.hockeygame.objects.Table;
import com.example.hockeygame.programs.ColorShaderProgram;
import com.example.hockeygame.programs.TextureShaderProgram;
import com.example.hockeygame.util.Geometry;
import com.example.hockeygame.util.MatrixHelper;
import com.example.hockeygame.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glViewport;

public class AirHockeyRenderer implements GLSurfaceView.Renderer
{
	private final Context mContext;
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mModelMatrix = new float[16];
	
	private Table mTable;
	private Mallet mMallet;
	private Puck mPuck;
	
	private TextureShaderProgram mTextureProgram;
	private ColorShaderProgram mColorProgram;
	
	private final float[] mViewMatrix = new float[16];
	private final float[] mViewProjectionMatrix = new float[16];
	private final float[] mModelViewProjectionMatrix = new float[16];
	
	private final float[] invertedViewProjectionMatrix = new float[16];
	
	private boolean blueMalletPressed = false;
	private boolean redMalletPressed = false;
	private Geometry.Point blueMalletPosition;
	private Geometry.Point redMalletPosition;
	
	private final float leftBound = -0.5f;
	private final float rightBound = 0.5f;
	private final float farBound = -0.8f;
	private final float nearBound = 0.8f;
	
	private Geometry.Point puckPosition;
	private Geometry.Vector puckVector;
	
	private Geometry.Point previousBlueMalletPosition;
	private Geometry.Point previousRedMalletPosition;
	
	private int mTexture;
	
	AirHockeyRenderer(Context context)
	{
		this.mContext = context;
	}
	
	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
	{
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		mTable = new Table();
		mMallet = new Mallet(0.08f, 0.15f, 32);
		mPuck = new Puck(0.06f, 0.02f, 32);
		mTextureProgram = new TextureShaderProgram(mContext);
		mColorProgram = new ColorShaderProgram(mContext);
		mTexture = TextureHelper.loadTexture(mContext, R.drawable.air_hockey_surface);
		blueMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, 0.4f);
		redMalletPosition = new Geometry.Point(0f, mMallet.height / 2f, -0.4f);
		puckPosition = new Geometry.Point(0f, mPuck.height / 2f, 0f);
		puckVector = new Geometry.Vector(0f, 0f, 0f);
	}
	
	@Override
	public void onSurfaceChanged(final GL10 iGL10, final int width, final int height)
	{
		// Set the OpenGL viewport to fill the entire surface.
		glViewport(0, 0, width, height);
		
		MatrixHelper.perspectiveM(mProjectionMatrix, 45, (float) width / (float) height, 1f, 10f);
		Matrix.setLookAtM(mViewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
	}
	
	private void positionTableInScene()
	{
		// The table is defined in terms of X & Y coordinates, so we rotate it
		// 90 degrees to lie flat on the XZ plane.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.rotateM(mModelMatrix, 0, -90f, 1f, 0f, 0f);
		Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
	}
	
	private void positionObjectInScene(float x, float y, float z)
	{
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);
		Matrix.multiplyMM(mModelViewProjectionMatrix, 0, mViewProjectionMatrix, 0, mModelMatrix, 0);
	}
	
	void handleTouchPress(float normalizedX, float normalizedY)
	{
		Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
		
		// Now test if this ray intersects with the mallet by creating a
		// bounding sphere that wraps the mallet.
		Geometry.Sphere blueMalletBoundingSphere = new Geometry.Sphere(new Geometry.Point(//
				blueMalletPosition.x, //
				blueMalletPosition.y, //
				blueMalletPosition.z), //
				mMallet.height / 2f);
		
		Geometry.Sphere redMalletBoundingSphere = new Geometry.Sphere(new Geometry.Point(//
				redMalletPosition.x, //
				redMalletPosition.y, //
				redMalletPosition.z), //
				mMallet.height / 2f);
		
		// If the ray intersects (if the user touched a part of the screen that
		// intersects the mallet's bounding sphere), then set malletPressed = true.
		blueMalletPressed = Geometry.intersects(blueMalletBoundingSphere, ray);
		redMalletPressed = Geometry.intersects(redMalletBoundingSphere, ray);
	}
	
	void handleTouchDrag(float normalizedX, float normalizedY)
	{
		if (blueMalletPressed)
		{
			Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
			
			// Define a plane representing our air hockey table.
			Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 1, 0));
			
			// Find out where the touched point intersects the plane
			// representing our table. We'll move the mallet along this plane
			Geometry.Point touchedPoint = Geometry.intersectionPoint(ray, plane);
			
			previousBlueMalletPosition = blueMalletPosition;
			
			blueMalletPosition = new Geometry.Point(//
					clamp(touchedPoint.x, leftBound + mMallet.radius, rightBound - mMallet.radius), //
					mMallet.height / 2f, //
					clamp(touchedPoint.z, 0f + mMallet.radius, nearBound - mMallet.radius)//
			);
			
			float distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
			
			if (distance < (mPuck.radius + mMallet.radius))
			{
				// The mallet has struck the puck. Now send the puck flying
				// based on the mallet velocity.
				puckVector = Geometry.vectorBetween(previousBlueMalletPosition, blueMalletPosition);
			}
		}
		
		if (redMalletPressed)
		{
			Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
			
			// Define a plane representing our air hockey table.
			Geometry.Plane plane = new Geometry.Plane(new Geometry.Point(0, 0, 0), new Geometry.Vector(0, 1, 0));
			
			// Find out where the touched point intersects the plane
			// representing our table. We'll move the mallet along this plane
			Geometry.Point touchedPoint = Geometry.intersectionPoint(ray, plane);
			
			previousRedMalletPosition = redMalletPosition;
			
			redMalletPosition = new Geometry.Point(//
					clamp(touchedPoint.x, leftBound + mMallet.radius, rightBound - mMallet.radius), //
					mMallet.height / 2f, //
					clamp(touchedPoint.z, farBound + mMallet.radius, 0f - mMallet.radius )//
			);
			
			float distance = Geometry.vectorBetween(redMalletPosition, puckPosition).length();
			
			if (distance < (mPuck.radius + mMallet.radius))
			{
				// The mallet has struck the puck. Now send the puck flying
				// based on the mallet velocity.
				puckVector = Geometry.vectorBetween(previousRedMalletPosition, redMalletPosition);
			}
		}
	}
	
	private float clamp(float value, float min, float max)
	{
		return Math.min(max, Math.max(value, min));
	}
	
	private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY)
	{
		// We'll convert these normalized device coordinates into world-space
		// coordinates. We'll pick a point on the near and far planes, and draw a
		// line between them. To do this transform, we need to first multiply by
		// the inverse matrix, and then we need to undo the perspective divide.
		final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
		final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};
		
		final float[] nearPointWorld = new float[4];
		final float[] farPointWorld = new float[4];
		
		Matrix.multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
		Matrix.multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);
		
		divideByW(nearPointWorld);
		divideByW(farPointWorld);
		
		Geometry.Point nearPointRay = new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
		Geometry.Point farPointRay = new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
		
		return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
	}
	
	private void divideByW(float[] vector)
	{
		vector[0] /= vector[3];
		vector[1] /= vector[3];
		vector[2] /= vector[3];
	}
	
	@Override
	public void onDrawFrame(GL10 glUnused)
	{
		// Clear the rendering surface.
		GLES20.glClear(GL_COLOR_BUFFER_BIT);
		
		puckPosition = puckPosition.translate(puckVector);
		
		if (puckPosition.x < leftBound + mPuck.radius || puckPosition.x > rightBound - mPuck.radius)
		{
			puckVector = new Geometry.Vector(-puckVector.x, puckVector.y, puckVector.z);
			puckVector = puckVector.scale(0.99f);
		}
		
		if (puckPosition.z < farBound + mPuck.radius || puckPosition.z > nearBound - mPuck.radius)
		{
			puckVector = new Geometry.Vector(puckVector.x, puckVector.y, -puckVector.z);
			puckVector = puckVector.scale(0.99f);
		}
		
		puckVector = puckVector.scale(0.99f);
		
		// Clamp the puck position.
		puckPosition = new Geometry.Point(//
				clamp(puckPosition.x, leftBound + mPuck.radius, rightBound - mPuck.radius), //
				puckPosition.y, //
				clamp(puckPosition.z, farBound + mPuck.radius, nearBound - mPuck.radius)//
		);
		
		Matrix.multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		Matrix.invertM(invertedViewProjectionMatrix, 0, mViewProjectionMatrix, 0);
		
		positionTableInScene();
		mTextureProgram.useProgram();
		mTextureProgram.setUniforms(mModelViewProjectionMatrix, mTexture);
		mTable.bindData(mTextureProgram);
		mTable.draw();
		
		// Draw the Puck.
		positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
		mColorProgram.useProgram();
		mColorProgram.setUniforms(mModelViewProjectionMatrix, 1f, 1f, 0f);
		mPuck.bindData(mColorProgram);
		mPuck.draw();
		
		// Draw the mallets.
		positionObjectInScene(redMalletPosition.x, redMalletPosition.y, redMalletPosition.z);
		mColorProgram.useProgram();
		mColorProgram.setUniforms(mModelViewProjectionMatrix, 1f, 0f, 0f);
		mMallet.bindData(mColorProgram);
		mMallet.draw();
		
		positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z);
		
		mColorProgram.setUniforms(mModelViewProjectionMatrix, 0f, 0f, 1f);
		
		// Note that we don't have to define the object data twice -- we just
		// draw the same mallet again but in a different position and with a
		// different color.
		mMallet.draw();
	}
}

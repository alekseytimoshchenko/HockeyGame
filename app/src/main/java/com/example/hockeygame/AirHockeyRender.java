package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.hockeygame.objects.Mallet;
import com.example.hockeygame.objects.Puck;
import com.example.hockeygame.objects.Table;
import com.example.hockeygame.programs.ColorShaderProgram;
import com.example.hockeygame.programs.TextureShaderProgram;
import com.example.hockeygame.util.MatrixHelper;
import com.example.hockeygame.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glViewport;

class AirHockeyRender implements GLSurfaceView.Renderer
{
	private final Context mContext;
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mModelMatrix = new float[16];
	
	private Table mTable;
	private Mallet mMallet;
	private Puck mPuck;
	
	private TextureShaderProgram mTextureProgram;
	private ColorShaderProgram mColorProgram;
	
	private final float[] viewMatrix = new float[16];
	private final float[] viewProjectionMatrix = new float[16];
	private final float[] modelViewProjectionMatrix = new float[16];
	
	private int mTexture;
	
	public AirHockeyRender(Context context)
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
	}
	
	@Override
	public void onSurfaceChanged(final GL10 iGL10, final int width, final int height)
	{
		// Set the OpenGL viewport to fill the entire surface.
		glViewport(0, 0, width, height);
		
		MatrixHelper.perspectiveM(mProjectionMatrix, 45, (float) width / (float) height, 1f, 10f);
		Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
	}
	
	private void positionTableInScene()
	{
		// The table is defined in terms of X & Y coordinates, so we rotate it
		// 90 degrees to lie flat on the XZ plane.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.rotateM(mModelMatrix, 0, -90f, 1f, 0f, 0f);
		Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
	}
	
	private void positionObjectInScene(float x, float y, float z)
	{
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, x, y, z);
		Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
	}
	
	@Override
	public void onDrawFrame(GL10 glUnused)
	{
		// Clear the rendering surface.
		GLES20.glClear(GL_COLOR_BUFFER_BIT);
		
		Matrix.multiplyMM(viewProjectionMatrix, 0, mProjectionMatrix, 0, viewMatrix, 0);
		positionTableInScene();
		mTextureProgram.useProgram();
		mTextureProgram.setUniforms(modelViewProjectionMatrix, mTexture);
		mTable.bindData(mTextureProgram);
		mTable.draw();
		
		// Draw the mallets.
		positionObjectInScene(0f, mMallet.height / 2f, -0.4f);
		mColorProgram.useProgram();
		mColorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
		mMallet.bindData(mColorProgram);
		mMallet.draw();
		
		positionObjectInScene(0f, mMallet.height / 2f, 0.4f);
		mColorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
		
		// Note that we don't have to define the object data twice -- we just
		// draw the same mallet again but in a different position and with a
		// different color.
		mMallet.draw();
	}
}

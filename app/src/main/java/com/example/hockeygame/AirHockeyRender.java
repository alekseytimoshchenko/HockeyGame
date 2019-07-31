package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.hockeygame.objects.Mallet;
import com.example.hockeygame.objects.Table;
import com.example.hockeygame.programs.ColorShaderProgram;
import com.example.hockeygame.programs.TextureShaderProgram;
import com.example.hockeygame.util.MatrixHelper;
import com.example.hockeygame.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

class AirHockeyRender implements GLSurfaceView.Renderer
{
	private final Context mContext;
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mModelMatrix = new float[16];
	
	private Table mTable;
	private Mallet mMallet;
	private TextureShaderProgram mTextureProgram;
	private ColorShaderProgram mColorProgram;
	
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
		mMallet = new Mallet();
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
		
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0f, 0f, -3f);
		Matrix.rotateM(mModelMatrix, 0, -60f, 1f, 0f, 0f);
		
		final float[] temp = new float[16];
		Matrix.multiplyMM(temp, 0, mProjectionMatrix, 0, mModelMatrix, 0);
		System.arraycopy(temp, 0, mProjectionMatrix, 0, temp.length);
	}
	
	@Override
	public void onDrawFrame(GL10 glUnused)
	{
		// Clear the rendering surface.
		GLES20.glClear(GL_COLOR_BUFFER_BIT);
		
		// Draw the table.
		mTextureProgram.useProgram();
		mTextureProgram.setUniforms(mProjectionMatrix, mTexture);
		mTable.bindData(mTextureProgram);
		mTable.draw();
		
		// Draw the mallets.
		mColorProgram.useProgram();
		mColorProgram.setUniforms(mProjectionMatrix);
		mMallet.bindData(mColorProgram);
		mMallet.draw();
	}
}

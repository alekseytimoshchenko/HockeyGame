package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.hockeygame.util.LoggerConfig;
import com.example.hockeygame.util.MatrixHelper;
import com.example.hockeygame.util.ShaderHelper;
import com.example.hockeygame.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

class AirHockeyRender implements GLSurfaceView.Renderer
{
	private static final int POSITION_COMPONENT_COUNT = 4;
	private static final int BYTES_PER_FLOAT = 4;
	private final FloatBuffer mVertexData;
	private Context mContext;
	private int mProgram;
	
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	
	private static final String A_COLOR = "a_Color";
	private int mAColorLocation;
	
	private static final String A_POSITION = "a_Position";
	private int mAPositionLocation;
	
	private static final String U_MATRIX = "u_Matrix";
	private int mUMatrixLocation;
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mModelMatrix = new float[16];
	
	private float[] mTableVerticesWithTriangles = { //
			// Order of coordinates: X, Y, Z, W, R, G, B
			// Triangle Fan
			0f, 0f, 0f, 1.5f, 1f, 1f, 1f, //
			-0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f, //
			0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f, //
			0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f,//
			-0.5f, 0.8f, 0f, 2f, 0.7f, 0.7f, 0.7f, //
			-0.5f, -0.8f, 0f, 1f, 0.7f, 0.7f, 0.7f,
			
			// Line 1
			-0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f, //
			0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f,
			
			// Mallets
			0f, -0.4f, 0f, 1.25f, 0f, 0f, 1f, //
			0f, 0.4f, 0f, 1.75f, 1f, 0f, 0f};
	
	AirHockeyRender(@NonNull final Context iContext)
	{
		mContext = iContext;
		
		mVertexData = ByteBuffer.allocateDirect(mTableVerticesWithTriangles.length * BYTES_PER_FLOAT)//
		                        .order(ByteOrder.nativeOrder())//
		                        .asFloatBuffer();
		
		mVertexData.put(mTableVerticesWithTriangles);
	}
	
	@Override
	public void onSurfaceCreated(final GL10 iGL10, final EGLConfig iEGLConfig)
	{
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		final String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
		final String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);
		
		final int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		final int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
		
		mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);
		
		if (LoggerConfig.ON)
		{
			ShaderHelper.validateProgram(mProgram);
		}
		
		glUseProgram(mProgram);
		bindData();
		
		bindMatrix();
	}
	
	private void bindMatrix()
	{
		mUMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
	}
	
	private void bindData()
	{
		mAPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
		mVertexData.position(0);
		glVertexAttribPointer(mAPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, mVertexData);
		glEnableVertexAttribArray(mAPositionLocation);
		
		mAColorLocation = glGetAttribLocation(mProgram, A_COLOR);
		mVertexData.position(POSITION_COMPONENT_COUNT);
		glVertexAttribPointer(mAColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, mVertexData);
		glEnableVertexAttribArray(mAColorLocation);
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
	public void onDrawFrame(final GL10 iGL10)
	{
		//Clear the rendering surface
		glClear(GL_COLOR_BUFFER_BIT);
		
		//Matrix
		glUniformMatrix4fv(mUMatrixLocation, 1, false, mProjectionMatrix, 0);
		
		//Table Top
		glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
		
		//Middle line
		glDrawArrays(GL_LINES, 6, 2);
		
		// Draw the first mallet blue.
		glDrawArrays(GL_POINTS, 8, 1);
		
		// Draw the second mallet red.
		glDrawArrays(GL_POINTS, 9, 1);
	}
}

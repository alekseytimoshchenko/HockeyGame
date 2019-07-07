package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.hockeygame.util.LoggerConfig;
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
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

class AirHockeyRender implements GLSurfaceView.Renderer
{
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int BYTES_PER_FLOAT = 4;
	private final FloatBuffer mVertexData;
	private Context mContext;
	private int mProgram;
	
	private static final String U_COLOR = "u_Color";
	private int mUColorLocation;
	
	private static final String A_POSITION = "a_Position";
	private int mAPositionLocation;
	
	private float[] mTableVerticesWithTriangles = { //
//			 Top
			-0.5f, 0.5f, //
			-0.5f, 0f, //
			0.5f, 0.5f,//
			0.5f, 0f,//
			
//			  Bottom
			-0.5f, 0f, //
			-0.5f, -0.5f, //
			0.5f, 0f,//
			0.5f, -0.5f,//

			// Central Line 1
			-0.5f, 0f, //
			0.5f, 0f,//

			// Mallets
			0f, -0.25f, //
			0f, 0.25f//
	};
	
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
		getLocations();
		bindData();
	}
	
	private void bindData()
	{
		mVertexData.position(0);
		glVertexAttribPointer(mAPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, mVertexData);
		glEnableVertexAttribArray(mAPositionLocation);
		
	}
	
	private void getLocations()
	{
		mUColorLocation = glGetUniformLocation(mProgram, U_COLOR);
		mAPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
	}
	
	@Override
	public void onSurfaceChanged(final GL10 iGL10, final int width, final int height)
	{
		// Set the OpenGL viewport to fill the entire surface.
		glViewport(0, 0, width, height);
	}
	
	@Override
	public void onDrawFrame(final GL10 iGL10)
	{
		//Clear the rendering surface
		glClear(GL_COLOR_BUFFER_BIT);
		
		//Table Top
		glUniform4f(mUColorLocation, .5f, .5f, 1.0f, 1.0f);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
		//Table Bottom
		glUniform4f(mUColorLocation, .5f, 1.0f, .5f, .5f);
		glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);

		//Middle line
		glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
		glDrawArrays(GL_LINES, 8, 2);

		// Draw the first mallet blue.
		glUniform4f(mUColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
		glDrawArrays(GL_POINTS, 10, 1);

		// Draw the second mallet red.
		glUniform4f(mUColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
		glDrawArrays(GL_POINTS, 11, 1);
	}
}

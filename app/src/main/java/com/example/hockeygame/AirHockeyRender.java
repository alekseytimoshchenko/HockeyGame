package com.example.hockeygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.hockeygame.util.ShaderHelper;
import com.example.hockeygame.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidx.annotation.NonNull;

class AirHockeyRender implements GLSurfaceView.Renderer
{
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int BYTES_PER_FLOAT = 4;
	private final FloatBuffer mVertexData;
	private Context mContext;
	
	float[] mTableVerticesWithTriangles = { //
			// Triangle 1
			0f, 0f, //
			9f, 14f, //
			0f, 14f,//
			// Triangle 2
			0f, 0f, //
			9f, 0f, //
			9f, 14f,//
			
			// Line 1
			0f, 7f, //
			9f, 7f,//
			
			// Mallets
			4.5f, 2f,//
			4.5f, 12f//
	};
	
	AirHockeyRender(@NonNull final Context iContext)
	{
		mContext = iContext;
		
		mVertexData = ByteBuffer.allocate(mTableVerticesWithTriangles.length * BYTES_PER_FLOAT)//
		                        .order(ByteOrder.nativeOrder())//
		                        .asFloatBuffer();
		
		mVertexData.put(mTableVerticesWithTriangles);
	}
	
	@Override
	public void onSurfaceCreated(final GL10 iGL10, final EGLConfig iEGLConfig)
	{
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
		
		final String vertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_vertex_shader);
		final String fragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.simple_fragment_shader);
		
		final int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		final int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
		
		
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
	}
}

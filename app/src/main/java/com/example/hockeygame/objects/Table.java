package com.example.hockeygame.objects;

import android.opengl.GLES20;

import com.example.hockeygame.Constants;
import com.example.hockeygame.data.VertexArray;
import com.example.hockeygame.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;

public class Table
{
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
	private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * Constants.BYTES_PER_FLOAT;
	
	private final VertexArray mVertexArray;
	
	private static final float[] VERTEX_DATA = { //
			// Order of coordinates: X, Y, S, T
			// Triangle Fan
			0f, 0f, 0.5f, 0.5f, //
			-0.5f, -0.8f, 0f, 0.9f, //
			0.5f, -0.8f, 1f, 0.9f,//
			0.5f, 0.8f, 1f, 0.1f,//
			-0.5f, 0.8f, 0f, 0.1f,//
			-0.5f, -0.8f, 0f, 0.9f //
	};
	
	public Table()
	{
		mVertexArray = new VertexArray(VERTEX_DATA);
	}
	
	public void bindData(TextureShaderProgram iTextureProgram)
	{
		mVertexArray.setVertexAttribPointer(//
				0, //
				iTextureProgram.getPositionAttributeLocation(), //
				POSITION_COMPONENT_COUNT, //
				STRIDE//
		);
		
		mVertexArray.setVertexAttribPointer(//
				POSITION_COMPONENT_COUNT, //
				iTextureProgram.getTextureCoordinatesAttributeLocation(), //
				TEXTURE_COORDINATES_COMPONENT_COUNT,//
				STRIDE//
		);
	}
	
	public void draw()
	{
		GLES20.glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
	}
}

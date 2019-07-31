package com.example.hockeygame.data;

import android.opengl.GLES20;

import com.example.hockeygame.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;

public class VertexArray
{
	private final FloatBuffer mFloatBuffer;
	
	public VertexArray(float[] vertexData)
	{
		mFloatBuffer = ByteBuffer.allocateDirect(vertexData.length * Constants.BYTES_PER_FLOAT)//
		                         .order(ByteOrder.nativeOrder())//
		                         .asFloatBuffer()//
		                         .put(vertexData);
	}
	
	public void setVertexAttribPointer(int iDataOffset, int iAttributeLocation, int iComponentCount, int iStride)
	{
		mFloatBuffer.position(iDataOffset);
		GLES20.glVertexAttribPointer(iAttributeLocation, iComponentCount, GL_FLOAT, false, iStride, mFloatBuffer);
		GLES20.glEnableVertexAttribArray(iAttributeLocation);
		mFloatBuffer.position(0);
	}
}

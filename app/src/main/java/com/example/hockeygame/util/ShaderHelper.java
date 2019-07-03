package com.example.hockeygame.util;

import android.util.Log;

import androidx.annotation.NonNull;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glShaderSource;

public class ShaderHelper
{
	private static final String TAG = ShaderHelper.class.getSimpleName();
	
	public static int compileVertexShader(String shaderCode)
	{
		return compileShader(GL_VERTEX_SHADER, shaderCode);
	}
	
	public static int compileFragmentShader(String shaderCode)
	{
		return compileShader(GL_FRAGMENT_SHADER, shaderCode);
	}
	
	private static int compileShader(final int type, @NonNull final String shaderCode)
	{
		final int shaderObjectId = glCreateShader(type);
		
		if (shaderObjectId == 0)
		{
			if (LoggerConfig.ON)
			{
				Log.w(TAG, "Could not create new shader.");
			}
			
			return 0;
		}
		
		glShaderSource(shaderObjectId, shaderCode);
		glCompileShader(shaderObjectId);
		
		final int[] compileStatus = new int[1];
		glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
		
		if (LoggerConfig.ON)
		{
			// Print the shader info log to the Android log output.
			Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));
		}
		
		if (compileStatus[0] == 0)
		{
			// If it failed, delete the shader object.
			glDeleteShader(shaderObjectId);
			
			if (LoggerConfig.ON)
			{
				Log.w(TAG, "Compilation of shader failed.");
			}
			
			return 0;
		}
		
		return shaderObjectId;
	}
}

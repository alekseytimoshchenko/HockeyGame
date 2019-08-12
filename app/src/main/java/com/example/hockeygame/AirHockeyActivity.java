package com.example.hockeygame;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class AirHockeyActivity extends AppCompatActivity
{
	private GLSurfaceView mGLSurfaceView;
	private boolean mRendererSet = false;
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mGLSurfaceView = new GLSurfaceView(this);
		
		boolean supportsEs2 = checkOpenGL20Compatibility();
		
		final AirHockeyRenderer airHockeyRenderer = new AirHockeyRenderer(this);
		
		if (supportsEs2)
		{
			//Request an OpenGL ES 2.0 compatible context.
			mGLSurfaceView.setEGLContextClientVersion(2);
			
			//Assign our render
			mGLSurfaceView.setRenderer(airHockeyRenderer);
			mRendererSet = true;
		}
		else
		{
			Toast.makeText(this, "This device does not support OpneGL ES 2.0", Toast.LENGTH_SHORT).show();
			return;
		}
		
		mGLSurfaceView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event != null)
				{
					// Convert touch coordinates into normalized device
					// coordinates, keeping in mind that Android's Y
					// coordinates are inverted.
					final float normalizedX = (event.getX() / (float) v.getWidth()) * 2 - 1;
					final float normalizedY = -((event.getY() / (float) v.getHeight()) * 2 - 1);
					
					if (event.getAction() == MotionEvent.ACTION_DOWN)
					{
						mGLSurfaceView.queueEvent(new Runnable()
						{
							@Override
							public void run()
							{
								airHockeyRenderer.handleTouchPress(normalizedX, normalizedY);
							}
						});
					}
					else if (event.getAction() == MotionEvent.ACTION_MOVE)
					{
						mGLSurfaceView.queueEvent(new Runnable()
						{
							@Override
							public void run()
							{
								airHockeyRenderer.handleTouchDrag(normalizedX, normalizedY);
							}
						});
					}
					
					return true;
				}
				else
				{
					return false;
				}
			}
		});
		
		setContentView(mGLSurfaceView);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if (mRendererSet)
		{
			mGLSurfaceView.onPause();
		}
	}
	
	private boolean checkOpenGL20Compatibility()
	{
		final ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo info = manager.getDeviceConfigurationInfo();
		
		return info.reqGlEsVersion >= 0x20000 //
				|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 //
				&& (Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("unknown") //
				|| Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86"))//
		);
	}
}

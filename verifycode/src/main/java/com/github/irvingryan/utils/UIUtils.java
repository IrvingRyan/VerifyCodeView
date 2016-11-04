package com.github.irvingryan.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class UIUtils {
	private static int sreenWidth = 0;
	private static int sreenHeight = 0;
	public static int getWidth(Context context) {
		if (sreenWidth<=0) {
			readScreenInfo(context);
		}
		return sreenWidth;
	}
	
	
	public static int getHeight(Context context) {
		if (sreenHeight<= 0) {
			readScreenInfo(context);
		}
		return sreenHeight;
	}


	public static void readScreenInfo(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		 wm.getDefaultDisplay().getMetrics(outMetrics );
		 sreenHeight =outMetrics.heightPixels;// 屏幕高度		
		sreenWidth = outMetrics.widthPixels;// 屏幕宽度
	}
}

package com.industry.printer.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

	private static final String TAG = FileUtil.class.getSimpleName();
	/**
	 * copy file
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bread=0;
			File oldFile = new File(oldPath);
			if (oldFile.exists()) {
				InputStream inStream = new FileInputStream(oldFile);
				FileOutputStream outStream = new FileOutputStream(newPath);
				byte[] buffer = new byte[1024];
				while ((bread = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bread);
				}
				inStream.close();
				outStream.flush();
				outStream.close();
			}
			
		} catch (Exception e) {
			Debug.e(TAG, "--->err:" + e.getCause());
		}
	}
}

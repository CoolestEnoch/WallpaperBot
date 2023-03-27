package com.coolest.wallpaper.bot.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class ImageUtils {

	static View gotView;

	/**
	 * 删除文件夹及其文件
	 *
	 * @param sPath
	 * @return
	 */
	public static boolean deleteDirAndFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) {  // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) {  // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else {  // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除单个文件
	 *
	 * @param sPath 被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 *
	 * @param sPath 被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		//如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		//如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		Boolean flag = true;
		//删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			//删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag) break;
			} //删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag) break;
			}
		}
		if (!flag) return false;
		//删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param filePath
	 * @return
	 */
	public static boolean detectFileExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * 创建文件如果不存在
	 *
	 * @param filePath
	 */
	public static void createFileIfNotExist(String filePath) throws Exception {
		File file = new File(filePath);
		String fileFolder = filePath.substring(0, filePath.lastIndexOf('/'));
		if (!file.exists()) {
			createFolderIfNotExist(fileFolder);
			file.createNewFile();
		}
	}

	/**
	 * 创建文件夹如果不存在
	 *
	 * @param folderPath
	 */
	public static void createFolderIfNotExist(String folderPath) throws Exception {
		File file = new File(folderPath);
		File parent = new File(folderPath.substring(0, folderPath.lastIndexOf('/')));
		if(!parent.exists()){
			createFolderIfNotExist(parent.toString());
		}
		file.mkdir();
		/*if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}*/
	}

	/**
	 * 根据URL从网上下载图片
	 *
	 * @param imgUrl
	 * @param imgFilePath
	 * @throws Exception
	 */
	public static void downloadPicture(String imgUrl, String imgFilePath) throws Exception {
		new Thread(new DownloadPictureThread(imgUrl, imgFilePath)).start();
	}

	static class DownloadPictureThread implements Runnable {
		String imgUrl;
		String imgFilePath;

		public DownloadPictureThread(String imgUrl, String imgFilePath) {
			this.imgUrl = imgUrl;
			this.imgFilePath = imgFilePath;
		}

		@Override
		public void run() {
			try {
				String file_downloading = imgFilePath + "_downloading";
				createFileIfNotExist(file_downloading);
				URL url = new URL(imgUrl);
				DataInputStream dataInputStream = new DataInputStream(url.openStream());

				FileOutputStream fileOutputStream = new FileOutputStream(new File(file_downloading));
				ByteArrayOutputStream output = new ByteArrayOutputStream();

				byte[] buffer = new byte[1024];
				int length;

				while ((length = dataInputStream.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
				fileOutputStream.write(output.toByteArray());
				dataInputStream.close();
				fileOutputStream.close();
				renameFile(file_downloading, imgFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 重命名文件
	 *
	 * @param oldFile
	 * @param newFile
	 * @return
	 */
	public static boolean renameFile(String oldFile, String newFile) {
		// 旧的文件或目录
		File oldName = new File(oldFile);
		// 新的文件或目录
		File newName = new File(newFile);
		if (newName.exists()) {  //  确保新的文件名不存在
			deleteFile(newFile);
		}
		if (oldName.renameTo(newName)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断包含关键字的文件是否存在
	 *
	 * @param keyWord
	 */
	public static boolean dirContainFile(String rootDir, String keyWord) {
		File file = new File(rootDir);
		File[] files = file.listFiles();
		for (File file1 : files) {
			if (file1.isFile() && file1.getName().contains(keyWord)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将Bitmap对象保存到本地图片
	 */
	public static void saveImage(String path, Bitmap bitmap) throws Exception {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		bos.flush();
		bos.close();
	}

	/**
	 * 从磁盘读取图片存储到Bitmap对象中
	 */
	public static Bitmap readImageFromDisk(String path) {
		Bitmap bitmap = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
			bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("File Not Found  ", path + "  " + detectFileExist(path));
		}
		return bitmap;
	}

	/**
	 * 根据View进行截屏，然后保存到本地图片文件
	 */
	public static void screenshotByViewAndSave(View v, String path) throws Exception {
		saveImage(path, screenshotByView(v));
	}

	/**
	 * 根据View进行截屏
	 */
	public static Bitmap screenshotByView(View v) {
		gotView = v;
		v.setDrawingCacheBackgroundColor(0xffffffff);
		v.setDrawingCacheEnabled(true);
		Bitmap bitmap = v.getDrawingCache();
		mHandler.postDelayed(mResetCache, 200);
		return bitmap;
	}

	/**
	 * 截屏辅助进程
	 */
	private static Handler mHandler = new Handler();
	private static Runnable mResetCache = new Runnable() {
		@Override
		public void run() {
			gotView.setDrawingCacheEnabled(false);
			gotView.setDrawingCacheEnabled(true);
		}
	};

}

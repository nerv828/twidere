package org.mariotaku.twidere.util;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Lazy image loader for {@link ListView} and {@link GridView} etc.</br> </br>
 * Inspired by <a href="https://github.com/thest1/LazyList">LazyList</a>, this
 * class has extra features like image loading/caching image to
 * /mnt/sdcard/Android/data/[package name]/cache features.</br> </br> Requires
 * Android 2.2, you can modify {@link Context#getExternalCacheDir()} to other to
 * support Android 2.1 and below.
 * 
 * @author mariotaku
 * 
 */
public class ProfileImageLoader {

	private final MemoryCache mMemoryCache = new MemoryCache();
	private final FileCache mFileCache;
	private final Map<ImageView, URL> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, URL>());
	private final ExecutorService mExecutorService;
	private final Drawable mFallbackDrawable;
	private final int mRequiredSize;

	public ProfileImageLoader(Context context, int fallback, int required_size) {
		mFileCache = new FileCache(context);
		mExecutorService = Executors.newFixedThreadPool(5);
		mFallbackDrawable = context.getResources().getDrawable(fallback);
		mRequiredSize = required_size % 2 == 0 ? required_size : required_size + 1;
	}

	public void clearFileCache() {
		mFileCache.clear();
	}

	public void clearMemoryCache() {
		mMemoryCache.clear();
	}

	public void displayImage(URL url, ImageView imageview) {
		if (imageview == null) return;
		if (url == null) {
			imageview.setImageDrawable(mFallbackDrawable);
			return;
		}
		mImageViews.put(imageview, url);
		final Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
			imageview.setImageDrawable(mFallbackDrawable);
		}
	}

	private void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			final byte[] bytes = new byte[buffer_size];
			int count = is.read(bytes, 0, buffer_size);
			while (count != -1) {
				os.write(bytes, 0, count);
				count = is.read(bytes, 0, buffer_size);
			}
		} catch (final IOException e) {
			// e.printStackTrace();
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, options);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = options.outWidth, height_tmp = options.outHeight;
			int scale = 1;
			while (width_tmp / 2 >= mRequiredSize || height_tmp / 2 >= mRequiredSize) {
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			final BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale / 2;
			final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			if (bitmap == null) {
				// The file is corrupted, so we remove it from cache.
				if (f.isFile()) {
					f.delete();
				}
			}
			return bitmap;
		} catch (final FileNotFoundException e) {
			// e.printStackTrace();
		}
		return null;
	}

	private void queuePhoto(URL url, ImageView imageview) {
		final ImageToLoad p = new ImageToLoad(url, imageview);
		mExecutorService.submit(new ImageLoader(p));
	}

	boolean imageViewReused(ImageToLoad imagetoload) {
		final Object tag = mImageViews.get(imagetoload.imageview);
		if (tag == null || !tag.equals(imagetoload.source)) return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {

		Bitmap bitmap;
		ImageToLoad imagetoload;

		public BitmapDisplayer(Bitmap b, ImageToLoad p) {
			bitmap = b;
			imagetoload = p;
		}

		@Override
		public final void run() {
			if (imageViewReused(imagetoload)) return;
			if (bitmap != null) {
				imagetoload.imageview.setImageBitmap(bitmap);
			} else {
				imagetoload.imageview.setImageDrawable(mFallbackDrawable);
			}
		}
	}

	private static class FileCache {

		private static final String CACHE_DIR_NAME = "profile_images";

		private File mCacheDir;
		private Context mContext;

		public FileCache(Context context) {
			mContext = context;
			init();
		}

		public void clear() {
			if (mCacheDir == null) return;
			final File[] files = mCacheDir.listFiles();
			if (files == null) return;
			for (final File f : files) {
				f.delete();
			}
		}

		public File getFile(URL tag) {
			if (mCacheDir == null) return null;
			final String filename = getURLFilename(tag);
			if (filename == null) return null;
			final File file = new File(mCacheDir, filename);
			return file;
		}

		public void init() {
			/* Find the dir to save cached images. */
			if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mCacheDir = new File(
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor.getExternalCacheDir(mContext)
								: new File(getExternalStorageDirectory().getPath() + "/Android/data/"
										+ mContext.getPackageName() + "/cache/"), CACHE_DIR_NAME);
			} else {
				mCacheDir = new File(mContext.getCacheDir(), CACHE_DIR_NAME);
			}
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

		private String getURLFilename(URL url) {
			if (url == null) return null;
			return url.toString().replaceAll("[^a-zA-Z0-9]", "_");
		}

	}

	private class ImageLoader implements Runnable {
		private ImageToLoad imagetoload;

		public ImageLoader(ImageToLoad imagetoload) {
			this.imagetoload = imagetoload;
		}

		public Bitmap getBitmap(URL url, ImageView imageview) {
			if (url == null) return null;
			final File f = mFileCache.getFile(url);

			// from SD cache
			final Bitmap b = decodeFile(f);
			if (b != null) return b;

			// from web
			try {
				Bitmap bitmap = null;
				final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				conn.setInstanceFollowRedirects(true);
				final InputStream is = conn.getInputStream();
				final OutputStream os = new FileOutputStream(f);
				copyStream(is, os);
				os.close();
				bitmap = decodeFile(f);
				return bitmap;
			} catch (final FileNotFoundException e) {
				// Storage state may changed, so call FileCache.init() again.
				// e.printStackTrace();
				mFileCache.init();
			} catch (final IOException e) {
				// e.printStackTrace();
			}
			return null;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload) || imagetoload.source == null) return;
			final Bitmap bmp = getBitmap(imagetoload.source, imagetoload.imageview);
			mMemoryCache.put(imagetoload.source, bmp);
			if (imageViewReused(imagetoload)) return;
			final BitmapDisplayer bd = new BitmapDisplayer(bmp, imagetoload);
			final Activity a = (Activity) imagetoload.imageview.getContext();
			a.runOnUiThread(bd);
		}
	}

	private static class ImageToLoad {
		public final URL source;
		public final ImageView imageview;

		public ImageToLoad(final URL source, final ImageView imageview) {
			this.source = source;
			this.imageview = imageview;
		}
	}

}

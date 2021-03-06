package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.ServiceUtils.bindToService;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.ITwidereService;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public class ServiceInterface implements Constants, ITwidereService {

	private ITwidereService mService;

	private final ServiceConnection mConntecion = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName service, IBinder obj) {
			mService = ITwidereService.Stub.asInterface(obj);
		}

		@Override
		public void onServiceDisconnected(ComponentName service) {
			mService = null;
		}
	};

	private static ServiceInterface sInstance;

	private ServiceInterface(Context context) {
		bindToService(context, mConntecion);

	}

	@Override
	public IBinder asBinder() {
		// Useless here
		return mService.asBinder();
	}

	@Override
	public int cancelRetweet(long account_id, long status_id) {
		if (mService == null) return -1;
		try {
			return mService.cancelRetweet(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createBlock(long account_id, long user_id) {
		if (mService == null) return -1;
		try {
			return mService.createBlock(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createFavorite(long account_id, long status_id) {
		if (mService == null) return -1;
		try {
			return mService.createFavorite(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int createFriendship(long account_id, long user_id) {
		if (mService == null) return -1;
		try {
			return mService.createFriendship(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyBlock(long account_id, long user_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyBlock(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyFavorite(long account_id, long status_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyFavorite(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyFriendship(long account_id, long user_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyFriendship(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int destroyStatus(long account_id, long status_id) {
		if (mService == null) return -1;
		try {
			return mService.destroyStatus(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getHomeTimeline(long[] account_ids, long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getHomeTimeline(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getMentions(long[] account_ids, long[] max_ids) {
		if (mService == null) return -1;
		try {
			return mService.getMentions(account_ids, max_ids);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int getMessages(long account_id, long max_id) {
		if (mService == null) return -1;
		try {
			return mService.getMessages(account_id, max_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean hasActivatedTask() {
		if (mService == null) return false;
		try {
			return mService.hasActivatedTask();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isHomeTimelineRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isHomeTimelineRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isMentionsRefreshing() {
		if (mService == null) return false;
		try {
			return mService.isMentionsRefreshing();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int reportSpam(long account_id, long user_id) throws RemoteException {
		if (mService == null) return -1;
		try {
			return mService.reportSpam(account_id, user_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int retweetStatus(long account_id, long status_id) {
		if (mService == null) return -1;
		try {
			return mService.retweetStatus(account_id, status_id);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public boolean test() {
		if (mService == null) return false;
		try {
			return mService.test();
		} catch (final RemoteException e) {
			// Maybe service died, so we return false value to let
			// ServiceInterface restart the service.
		}
		return false;
	}

	@Override
	public int updateProfile(long account_id, String name, String url, String location, String description) {
		if (mService == null) return -1;
		try {
			return mService.updateProfile(account_id, name, url, location, description);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int updateProfileImage(long account_id, Uri image_uri, boolean delete_image) {
		if (mService == null) return -1;
		try {
			return mService.updateProfileImage(account_id, image_uri, delete_image);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int updateStatus(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to,
			boolean delete_image) {
		if (mService == null) return -1;
		try {
			return mService.updateStatus(account_ids, content, location, image_uri, in_reply_to, delete_image);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static ServiceInterface getInstance(Context context) {
		if (sInstance == null || !sInstance.test()) {
			sInstance = new ServiceInterface(context);
		}
		return sInstance;
	}
}

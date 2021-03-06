package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class SettingsActivity extends BasePreferenceActivity implements OnPreferenceChangeListener,
		OnPreferenceClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		addPreferencesFromResource(R.xml.settings);
		findPreference(PREFERENCE_KEY_DARK_THEME).setOnPreferenceChangeListener(this);
		findPreference(PREFERENCE_KEY_CLEAR_DATABASES).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_KEY_CLEAR_CACHE).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (PREFERENCE_KEY_DARK_THEME.equals(preference.getKey())) {
			boolean show_anim = false;
			try {
				final float transition_animation = Settings.System.getFloat(getContentResolver(),
						Settings.System.TRANSITION_ANIMATION_SCALE);
				show_anim = transition_animation > 0.0;
			} catch (final SettingNotFoundException e) {
				e.printStackTrace();
			}
			restartActivity(this, show_anim);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (PREFERENCE_KEY_CLEAR_DATABASES.equals(preference.getKey())) {
			final ContentResolver resolver = getContentResolver();
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			resolver.delete(Statuses.CONTENT_URI, null, null);
			resolver.delete(Mentions.CONTENT_URI, null, null);
			resolver.delete(CachedUsers.CONTENT_URI, null, null);
			suggestions.clearHistory();
		} else if (PREFERENCE_KEY_CLEAR_CACHE.equals(preference.getKey())) {
			getTwidereApplication().clearCache();
		}
		return true;
	}
}

/*
 * This file is part of Turn of War which is a fork of Dragon Wars
 * as of 20/11/2013.
 *
 * Copyright (C) 2013 Ed Woodhouse <edwoodhou@gmail.com>
 *
 * Turn of War is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turn of War is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Turn of War. If not, see <http://www.gnu.org/licenses/>.
 */
/* This file is part of Dragon Wars.
 *
 * Dragon Wars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dragon Wars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dragon Wars. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.fuuzetsu.turnofwar;

import java.util.Map;

import org.json.JSONException;

import uk.co.fuuzetsu.turnofwar.engine.GameMap;
import uk.co.fuuzetsu.turnofwar.engine.GameState;
import uk.co.fuuzetsu.turnofwar.engine.Logic;
import uk.co.fuuzetsu.turnofwar.engine.MapReader;
import uk.co.fuuzetsu.turnofwar.engine.Statistics;
import uk.co.fuuzetsu.turnofwar.engine.Database.Database;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

//import android.view.KeyEvent;

public class GameActivity extends Activity {
	private static final String TAG = "GameActivity";
	private GameState state = null; // was private
	public static int ranFlag = 0;

	@SuppressLint("NewApi")
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

		// This work only for android 4.4+
		if (currentApiVersion >= 19) {

			getWindow().getDecorView().setSystemUiVisibility(flags);
			// Code below is for case when you press Volume up or Volume down.
			// Without this after pressing valume buttons navigation bar will
			// show up and don't hide
			final View decorView = getWindow().getDecorView();
			decorView
					.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

						@Override
						public void onSystemUiVisibilityChange(int visibility) {
							if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
								decorView.setSystemUiVisibility(flags);
							}
						}
					});
		}
		else {
			// remove the status bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			Log.d(TAG, "in onCreate");
			// setContentView(R.layout.loading_screen);
			Log.d(TAG, "on inCreate");
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= 19) {
		final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
		final View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(flags);
		}
		if (ranFlag == 0) {
			Bundle b = getIntent().getExtras();
			String mapFileName = b.getString("mapFileName");
			boolean[] isAi = b.getBooleanArray("isAi");
			GameMap map = null;

			try {
				map = MapReader.readMapFromFile(mapFileName, this, isAi);
			} catch (JSONException e) {
				Log.d(TAG, "Failed to load the map: " + e.getMessage());
			}

			if (map == null) {
				Log.d(TAG, "map is null");
				System.exit(1);
			}
			// getWindow().setFormat(PixelFormat.RGBA_8888); //fix banding which
			// ruined all my nice images
			setContentView(R.layout.activity_game);
			GameView gameView = (GameView) this.findViewById(R.id.gameView);
			state = new GameState(map, new Logic(), map.getPlayers(), gameView);
			Button menuButton = (Button) this.findViewById(R.id.menuButton);
			menuButton.setOnClickListener(gameView);
			gameView.setState(state, this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		ranFlag = 1;
		SharedPreferences preferencesReader = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferencesReader.edit();
		editor.putInt("ranFlag", ranFlag);
		editor.commit();
	}

	public final void endGame() {
		setContentView(R.layout.loading_screen);
		Intent intent = new Intent(this, Results.class);
		Bundle b = new Bundle();
		b.putString("winnerName", state.getWinner().getName());
		b.putInt("turns", state.getTurns());
		Statistics stats = state.getStatistics();
		Double damageDealt = stats.getStatistic("Damage dealt");
		Double damageReceived = stats.getStatistic("Damage received");
		Double distanceTravelled = stats.getStatistic("Distance travelled");
		Integer goldCollected = stats.getStatistic("Gold received").intValue();
		Integer unitsKilled = stats.getStatistic("Units killed").intValue();
		Integer unitsMade = stats.getStatistic("Units produced").intValue();

		Database db = new Database(getApplicationContext());
		db.AddEntry(damageDealt, damageReceived, distanceTravelled,
				goldCollected, unitsKilled, unitsMade);
		db.Close();

		for (Map.Entry<String, Double> ent : stats.getEntrySet()) {
			b.putDouble(ent.getKey(), ent.getValue().doubleValue());
		}

		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

}

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

package ed.turnofwar;

import java.util.Map;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

//import android.view.KeyEvent;

import ed.turnofwar.engine.GameMap;
import ed.turnofwar.engine.GameState;
import ed.turnofwar.engine.Logic;
import ed.turnofwar.engine.MapReader;
import ed.turnofwar.engine.Statistics;
import ed.turnofwar.engine.Database.Database;

public class GameActivity extends Activity {
	private static final String TAG = "GameActivity";

	private GameState state = null;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove the title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// remove the status bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Log.d(TAG, "in onCreate");
		// setContentView(R.layout.loading_screen);
		Log.d(TAG, "on inCreate");
	}

	@Override
	protected final void onStart() {
		super.onStart();
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

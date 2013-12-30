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

package ed.turnofwar.engine.GoalArbitration;

import android.util.Log;

import ed.turnofwar.engine.GameState;
import ed.turnofwar.engine.Position;
import ed.turnofwar.engine.Unit;

public class BuildUnit extends AtomicAction {
	Position destination;

	public BuildUnit(final GameState gameState, final Unit unit,
			final Position destin, final float value) {
		super(gameState, unit, value);
		destination = destin;
	}

	@Override
	public void Perform() {
		Log.d("BU", "Unit being built");
		super.gameState.produceUnit(gameState.getMap().getField(destination),
				actionUnit);
	}
}

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

import ed.turnofwar.engine.GameState;
import ed.turnofwar.engine.Unit;

//abstract class from which "AttackUnit, DefendTile, MoveTo, CaptureTile" etc are derived from
public abstract class AtomicAction {
	private float actionValue = 0;
	protected GameState gameState;
	protected Unit actionUnit;

	public AtomicAction(final GameState gamestate, final Unit unit,
			final float value) {
		actionValue = value;
		gameState = gamestate;
		actionUnit = unit;
	}

	public abstract void Perform();

	public Unit getUnit() {
		return actionUnit;
	}

	public float getActionValue() {
		return actionValue;
	}
}

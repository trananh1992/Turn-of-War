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

package uk.co.fuuzetsu.turnofwar.engine.GoalArbitration;

import ed.turnofwar.engine.GameState;
import ed.turnofwar.engine.Position;
import ed.turnofwar.engine.Unit;

public class AttackAt extends AtomicAction {
	private Unit targetUnit;
	private Position moveTo;

	public AttackAt(final GameState gameState, final Unit unit,
			final Unit target, final float value, final Position moveTo) {
		super(gameState, unit, value);
		targetUnit = target;
		this.moveTo = moveTo;
	}

	@Override
	public void Perform() {
		Boolean moved = true;

		if (moveTo != null) {
			super.gameState.move(getUnit(), moveTo);
		}

		/* Only attack if we managed to move (or pretended to) */
		if (moved) {
			super.gameState.attack(getUnit(), targetUnit);
		}
	}
}

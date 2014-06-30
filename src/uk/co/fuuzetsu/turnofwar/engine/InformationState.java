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

package uk.co.fuuzetsu.turnofwar.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used by GameState to provide data to GameView in a transparent fashion. All
 * the logic of whether to recalculate something or not will be moved here.
 */
public final class InformationState {

	private final GameState containingState;
	private GameField currentlySelected;
	private GameField lastField;
	private Set<Position> lastAttackables;
	private Set<Position> lastAttackables2;
	private List<Position> lastDestinations;
	private Unit lastUnit;
	private List<Position> path;
	private Long startingTime;
	private Long timeElapsed = 0L;
	private Long framesSinceLastSecond = 0L;
	private Double fps = 0.0;
	private final Logic logic = new Logic();

	public InformationState(final GameState state) {
		containingState = state;
	}

	public List<Position> getPath() {
		if (path == null || lastDestinations == null
				|| lastDestinations.size() == 0) {
			return new ArrayList<Position>(0);
		}

		return path;
	}

	public void setPath(final List<Position> path) {
		this.path = path;
	}

	public Set<Position> getAttackables() {
		if (lastAttackables == null) {
			return new HashSet<Position>(0);
		}

		return lastAttackables;
	}

	public Set<Position> getTargets() {
		if (lastAttackables2 == null) {
			return new HashSet<Position>(0);
		}

		return lastAttackables2;
	}

	public List<Position> getUnitDestinations() {
		return getUnitDestinations(currentlySelected);
	}

	public List<Position> getUnitDestinations(final GameField field) {
		List<Position> unitDests = new ArrayList<Position>(0);

		if (field == null) {
			return unitDests;
		}

		if (!field.hostsUnit()) {
			lastUnit = null;
			lastField = null;
			lastAttackables = null;
			lastAttackables2 = null;
			path = null;
			return unitDests;
		}
		Unit u = field.getUnit();
		if (field.hostsUnit()
				&& (u.getOwner() != containingState.getCurrentPlayer())) {
			u.resetMovement(); // redraw move distance when its not ur turn}
			u.resetTurnStatistics2();
		}

		// cant show range of enemy stealth or subs
		if ((u.hasFinishedTurn() && (u.getOwner() == containingState
				.getCurrentPlayer()))
				|| (u.getName().equals("Stealth") && (u.getOwner() != containingState
						.getCurrentPlayer()))
				|| (u.getName().equals("MissileSub") && (u.getOwner() != containingState
						.getCurrentPlayer()))
				|| (u.getName().equals("Submarine") && (u.getOwner() != containingState
						.getCurrentPlayer()))) {
			lastAttackables = null;
			lastAttackables2 = null;

			if((u.getOwner() == containingState
					.getCurrentPlayer())){
			lastAttackables2 = logic.getAttackableUnitPositions(0,
					containingState.getMap(), u);
			}
			if (u.hasMoved()) { // ranged units move path no longer show
				path = null;
			}
		} else {
			if ((lastDestinations == null || lastUnit == null
					|| lastField == null || lastAttackables == null || lastAttackables2 == null)
					&& !u.hasMoved()) {
				lastUnit = u;
				lastField = field;

				int myGo = 1;
				if (u.getOwner() != containingState.getCurrentPlayer()
						&& u.isRanged) {
					// unitDests = logic.destinations(containingState.getMap(),
					// u);
					lastDestinations = null;
					lastAttackables = logic.getAttackableUnitPositions(myGo,
							containingState.getMap(), u);
					myGo = 0;
					lastAttackables2 = logic.getAttackableUnitPositions(myGo,
							containingState.getMap(), u);
				} else {
					unitDests = logic.destinations(containingState.getMap(), u);
					lastDestinations = unitDests;
					myGo = 1; // run this for the specific squares
					lastAttackables = logic.getAttackableUnitPositions(myGo,
							containingState.getMap(), u);
					myGo = 0; // display every square
					lastAttackables2 = logic.getAttackableUnitPositions(myGo,
							containingState.getMap(), u);
				}

				path = null;
				return unitDests;
			}
			if (u.equals(lastUnit) && field.equals(lastField)) {
				return lastDestinations;
			}
			if (field.hostsUnit()) { // /
				lastUnit = null;
				lastField = null;
				lastAttackables = null;
				lastAttackables2 = null;
				path = null;
			}
		}
		if (u.hasMoved() && u.getOwner() == containingState.getCurrentPlayer()
				&& !u.hasFinishedTurn()) {
			path = null;
			int myGo = 1;
			lastAttackables = logic.getAttackableUnitPositions(myGo,
					containingState.getMap(), u);
			myGo = 0; // display every square
			lastAttackables2 = logic.getAttackableUnitPositions(myGo,
					containingState.getMap(), u);
		}
		// u.resetMovement();
		// u.reduceMovement(u.getRemainingMovement());

		return unitDests;
	}

	public void startFrame() {
		startingTime = System.currentTimeMillis();
	}

	public void endFrame() {
		framesSinceLastSecond++;

		timeElapsed += System.currentTimeMillis() - startingTime;

		if (timeElapsed >= 1000) {
			fps = framesSinceLastSecond / (timeElapsed * 0.001);
			framesSinceLastSecond = 0L;
			timeElapsed = 0L;
		}
	}

	public Double getFps() {
		return fps;
	}

}

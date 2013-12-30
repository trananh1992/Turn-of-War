/*
 * This file is part of TurnOfWar which is a fork of Dragon Wars
 * as of 20/11/2013.
 *
 * Copyright (C) 2013 Ed Woodhouse <edwoodhou@gmail.com>
 *
 * TurnOfWar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TurnOfWar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TurnOfWar. If not, see <http://www.gnu.org/licenses/>.
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

package com.ed.turnofwar.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ed.turnofwar.GameView;

public final class GameState {

	private GameMap map;
	private Logic logic;
	private List<Player> players = new ArrayList<Player>();
	private Player winner = null;
	private Integer playerIndex = 0;
	private Integer turns = 1;
	private Boolean gameFinished = false;
	private Statistics stats = new Statistics();
	private InformationState info;
	private GameView gvCallback;
	private Player whoCappedThatLastHQ;

	public GameState(final GameMap map, final Logic logic,
			final List<Player> players, final GameView gv) {
		this.map = map;
		this.logic = logic;
		this.players = players;
		this.info = new InformationState(this);
		this.gvCallback = gv;
		income(); // run through income calculations once, so starting
					// money isn't 0
		for (Player p : players) {
			p.setGameState(this);
		}
	}

	public List<Position> getUnitDestinations(final GameField field) {
		return info.getUnitDestinations(field);
	}

	public void startFrame() {
		info.startFrame();
	}

	public void endFrame() {
		info.endFrame();
	}

	public Double getFps() {
		return info.getFps();
	}

	public List<Position> getCurrentPath() {
		return info.getPath();
	}

	public void setPath(final List<Position> path) {
		info.setPath(path);
	}

	public Set<Position> getAttackables() {
		return info.getAttackables();
	}

	public Set<Position> getTargets() {
		return info.getTargets();
	}

	public void attack(final Unit attacker, final Unit defender) {
		Set<Position> attackable = logic.getAttackableUnitPositions(0, map,
				attacker);
		// if (!attackable.contains(defender.getPosition()))
		// return;
		boolean contains = false;
		for (Position pos : attackable) {
			if (pos.equals(defender.getPosition())) {
				contains = true;
			}
			// map.getField(pos).getUnit()
		}

		if (!contains) {
			return;
		}

		// String A = "yolo";
		// Log.v(A, "lol" + attacker + " " + defender);
		// if (attacker != null && defender != null) {
		Pair<Double, Double> damage = logic.calculateDamage(map, attacker,
				defender);

		defender.reduceHealth(damage.getLeft());
		gvCallback.addDamagedUnit(defender);
		Boolean died = removeUnitIfDead(defender);
		stats.increaseStatistic("Damage dealt", damage.getLeft());

		if (died) {
			return;
		}

		/* Possibly counter */
		attacker.reduceHealth(damage.getRight());
		gvCallback.addDamagedUnit(attacker);
		removeUnitIfDead(attacker);

		stats.increaseStatistic("Damage received", damage.getRight());
		// }
	}

	public Boolean move(final Unit unit, final Position destination) {
		/*
		 * We are assuming that the destination was already checked to be within
		 * this unit's reach
		 */

		List<Position> path = logic.findPath(map, unit, destination);

		Integer movementCost = logic.calculateMovementCost(map, unit, path);

		if (!map.isValidField(destination) || path.size() == 0) {
			return false;
		}

		GameField destField = map.getField(destination);

		if (destField.hostsUnit()) {// could be useful for tranports
			return false;
		}

		/* Double check */
		if (unit.getRemainingMovement() < movementCost) {
			return false;
		}

		GameField currentField = map.getField(unit.getPosition());
		destField.setUnit(unit);
		unit.reduceMovement(movementCost);

		currentField.setUnit(null);
		unit.setPosition(destination);
		unit.setMoved(true);

		stats.increaseStatistic("Distance travelled", 1.0 * movementCost);

		String CurrPlayer = String.valueOf(getCurrentPlayer()); // reset hp if
																// moved unit
																// off building
		String CurrString = CurrPlayer;
		movedOffBuildingCaptureCounters(CurrString);
		if (unit.isRanged()) {
			unit.setFinishedTurn(true);

			map.getField(unit.getPosition()).setUnit(unit);
		}
		return true;

	}

	private Boolean removeUnitIfDead(final Unit unit) {
		if (unit.isDead()) {
			map.getField(unit.getPosition()).setUnit(null);
			unit.getOwner().removeUnit(unit);
			stats.increaseStatistic("Units killed");

			String CurrPlayer = String.valueOf(getCurrentPlayer()); // reset hp
																	// if unit
																	// is dead
			String CurrString = CurrPlayer;
			movedOffBuildingCaptureCounters(CurrString);
			return true;
		}

		return false;
	}

	private void updateBuildingRepairUnits(String currString) { // healing
																// mechanic
		for (GameField gf : map) {

			/* No building. */
			if (!gf.hostsBuilding()) {
				continue;
			}
			/* Unit on the building. */
			if (gf.hostsUnit()) {
				Unit unit = gf.getUnit();
				String[] cArr = currString.split(" ");
				String currArr = cArr[1];
				int currInt = Integer.parseInt(currArr, 10);

				String Owner = String.valueOf(unit.getOwner());
				String[] Arr = Owner.split(" ");
				String OwnerArr = Arr[1];
				int OwnerInt = Integer.parseInt(OwnerArr, 10);
				// String tagasdafs = "playersSize";
				// Log.v(tagasdafs, "lol " + unit.getName());

				int playerCash = unit.getOwner().getGoldAmount();
				int healCost = 0;
				int healAmount = 0; // hp to restore
				healCost = unit.getProductionCost() / 10;

				if (unit.getHealth() < unit.getMaxHealth()
						&& OwnerInt == currInt
						&& unit.getOwner() == gf.getBuilding().getOwner()) {
					if ((gf.getBuilding().getName().equals("seafac") && unit
							.isBoat())
							|| (gf.getBuilding().getName().equals("airfac") && unit
									.isFlying())
							|| ((gf.getBuilding().getName().equals("landfac")
									|| gf.getBuilding().getName()
											.equals("city") || gf.getBuilding()
									.getName().equals("hq"))
									&& !unit.isFlying() && !unit.isBoat())) { // units
																				// only
																				// repair
																				// on
																				// their
																				// respective
																				// factories
																				// +
																				// land
																				// repair
																				// on
																				// cities
						// also need to check for unit type and building type in
						// conditions
						if (healCost * 2 <= playerCash) {
							healAmount = 2;
						} else if (healCost <= playerCash
								&& healCost * 2 >= playerCash) {
							healAmount = 1;
						} else {
							healAmount = 0;
						}

						// Log.v(tagasdafs, "lol" + unit.getHealth() + " " +
						// healAmount);
						unit.restoreHealth((double) healAmount);
						unit.getOwner().setGoldAmount(
								(playerCash - (healCost * healAmount)));
					}
				}
			}
		}
	}

	private void updateBuildingCaptureCounters(String currString) { // this is
																	// for
																	// making
																	// capture
																	// counters
																	// adjust
																	// after
																	// each turn
																	// in
																	// realtime
		for (GameField gf : map) {
			/* No building. */
			if (!gf.hostsBuilding()) {
				continue;
			}
			Building b = gf.getBuilding();
			/* Unit on the building. */
			if (gf.hostsUnit() && (gf.getUnit().getName().equals("Tank"))) {
				Unit unit = gf.getUnit();
				String[] cArr = currString.split(" ");
				String currArr = cArr[1];
				int currInt = Integer.parseInt(currArr, 10);

				String Owner = String.valueOf(unit.getOwner());
				String[] Arr = Owner.split(" ");
				String OwnerArr = Arr[1];
				int OwnerInt = Integer.parseInt(OwnerArr, 10);
				// String A = "yolo";
				// Log.v(A, "lol" + unit.getOwner() + " £ " + currInt + " " +
				// OwnerInt + " " + gf.hostsUnit());

				if (OwnerInt == currInt) {
					Integer turnReduce = (int) Math.floor(unit.getHealth());
					// Integer turnReduce = unit.getHealth().intValue();

					/* Unit already owns the building or is capturing. */
					if (unit.getOwner().equals(b.getLastCapturer())
							&& unit.getOwner() != gf.getBuilding().getOwner()
							&& unit.hasFinishedTurn() == false) { // just add
																	// &&unit.getType()
																	// == ""
						whoCappedThatLastHQ = b.reduceCaptureTime(turnReduce);
						continue;
					} else {
						// b.resetCaptureTime();
						b.setLastCapturer(unit.getOwner());
						if (unit.getOwner() == b.getLastCapturer()
								&& unit.getOwner() != gf.getBuilding()
										.getOwner()
								&& unit.hasFinishedTurn() == false) { // just
																		// add
																		// &&unit.getType()
																		// == ""
							whoCappedThatLastHQ = b
									.reduceCaptureTime(turnReduce);
						}
					}
				}
			}
			if (gf.hostsUnit() != true) {
				b.resetCaptureTime();
			}
		}
	}

	private void movedOffBuildingCaptureCounters(String currString) { // reset
																		// HP
																		// when
																		// moved
																		// off
																		// building
																		// or
																		// killed
		for (GameField gf : map) {
			/* No building. */
			if (!gf.hostsBuilding()) {
				continue;
			}
			Building b = gf.getBuilding();
			if (gf.hostsUnit() != true) {
				b.resetCaptureTime();
			}
		}
	}

	public Statistics getStatistics() {
		return stats;
	}

	public void nextPlayer() throws GameFinishedException {
		Iterator<Player> iter = players.iterator();

		String CurrPlayer = String.valueOf(getCurrentPlayer());
		String CurrString = CurrPlayer;

		updateBuildingCaptureCounters(CurrString); // deal with those pesky
													// counters

		playerIndex++;
		if (playerIndex >= players.size()) {
			playerIndex = 0;
			advanceTurn();
			income();
		}

		String nextPlayerComingUp = (players.get(playerIndex).toString()); // these
																			// two
																			// lines
																			// make
																			// repairs
																			// happen
																			// at
																			// start
																			// of
																			// round
																			// instead
																			// of
																			// end
		updateBuildingRepairUnits(nextPlayerComingUp);

		while (iter.hasNext()) {
			Player p = iter.next();
			if (p.hasLost()) { // what to do when player dies

				for (Building b : p.getOwnedBuildings()) {
					for (GameField gf : map) { // this for loop is to reset the
												// counters of formerly
												// capturing units that are now
												// dead
						if (gf.hostsBuilding() && gf.hostsUnit()) {
							Building c = gf.getBuilding();
							if (c.getLastCapturer() == p)
								c.remainingCaptureTime = b
										.getCaptureDifficulty();
						}
					}

					b.setOwner(whoCappedThatLastHQ);
					whoCappedThatLastHQ.addBuilding(b);
					b.remainingCaptureTime = b.getCaptureDifficulty();
				}

				for (Unit u : p.getOwnedUnits()) {
					map.getField(u.getPosition()).setUnit(null);
				}
				iter.remove(); // removes all units
				// DISPLAY TOAST AND SOUND WHEN PLAYER IS DEFEATED
				// Toast.makeText(context,
				// String.format("%s has been defeated!", p.getName()),
				// Toast.LENGTH_SHORT).show();

				// MediaPlayer mp = MediaPlayer.create(context, R.raw.cannhvy1);
				// //death sound
				// mp.start();
			}
		}

		if (players.size() <= 1) {
			this.winner = players.get(0);
			this.gameFinished = true;
			throw new GameFinishedException(players.get(0));
		}

		if (getCurrentPlayer().isAi()) {
			getCurrentPlayer().takeTurn();
			/* Uh oh, dirty hack for concurrent mod 9h before presentation. */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				/* Just let it slide and pray for the best */
			}
			nextPlayer();
		}
	}

	private void income() {
		for (Player p : players) {
			Integer goldWorth = 0;

			for (Building b : p.getOwnedBuildings()) {
				goldWorth += b.getCaptureWorth();
				// times capturewoth by a multiplier for varying levels of
				// funds, dont remove capture worth - it allows more valuable
				// buildings
			}

			p.setGoldAmount(goldWorth + p.getGoldAmount());

			stats.increaseStatistic("Gold received", 1.0 * goldWorth);

			for (Unit u : p.getOwnedUnits()) {
				u.resetTurnStatistics();
			}
		}
	}

	private void advanceTurn() {

		++this.turns;
		stats.increaseStatistic("Turns taken");
	}

	public Integer getTurns() {
		return this.turns;
	}

	public GameMap getMap() {
		return this.map;
	}

	public Logic getLogic() {
		return this.logic;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public Player getCurrentPlayer() {
		if (players.size() > playerIndex) {
			return players.get(playerIndex);
		} else {
			return players.get(players.size() - 1);
		}
	}

	public Player getWinner() {
		return this.winner;
	}

	public boolean isGameFinished() {
		return gameFinished;
	}

	public void setGameFinished(final boolean gameFinished) {
		this.gameFinished = gameFinished;
	}

	public Boolean produceUnit(final GameField field, final Unit unit) {
		// produces a unit "at" a building
		if (!field.hostsBuilding() || field.hostsUnit()) {
			return false;
		}

		Building building = field.getBuilding();

		for (Unit u : building.getProducibleUnits()) {
			if (u.getName().equals(unit.getName())) {
				Player player = building.getOwner();

				if (player.getGoldAmount() < u.getProductionCost()) {
					return false;
				}

				Unit newUnit = new Unit(u);
				newUnit.setPosition(building.getPosition());
				newUnit.setOwner(player);

				player.setGoldAmount(player.getGoldAmount()
						- unit.getProductionCost());
				player.addUnit(newUnit);
				newUnit.setFinishedTurn(true);
				field.setUnit(newUnit);
				stats.increaseStatistic("Units produced");
				return true;
			}
		}

		return false;
	}
}
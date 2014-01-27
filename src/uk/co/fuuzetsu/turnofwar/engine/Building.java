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
import java.util.List;

public final class Building extends DrawableMapObject {

	private final Integer captureWorth;
	private final Integer captureDifficulty;
	Integer remainingCaptureTime;
	private final Double attackBonus, defenseBonus;
	private Player owner = null;
	private Player lastCapturer;
	private final List<Unit> producibleUnits = new ArrayList<Unit>();
	private Position position;

	public Building(final String name, final Integer captureDifficulty,
			final Double attackBonus, final Double defenseBonus,
			final Integer captureWorth, final String spriteLocation,
			final String spriteDir, final String spritePack) {
		super(name, spriteLocation, spriteDir, spritePack);

		this.captureDifficulty = captureDifficulty;
		this.remainingCaptureTime = this.captureDifficulty;

		this.attackBonus = attackBonus;
		this.defenseBonus = defenseBonus;
		this.captureWorth = captureWorth;

		generateInfo();

	}

	public Building(final Building building) {
		super(building.getName(), building.getSpriteLocation(), building
				.getSpriteDir(), building.getSpritePack());

		this.captureDifficulty = building.getCaptureDifficulty();
		this.remainingCaptureTime = this.captureDifficulty;

		this.attackBonus = building.getAttackBonus();
		this.defenseBonus = building.getDefenseBonus();

		this.captureWorth = building.getCaptureWorth();

		this.info = building.info;

	}

	public Boolean canProduceUnits() {
		return !this.producibleUnits.isEmpty();
	}

	public void addProducibleUnit(final Unit unit) {
		this.producibleUnits.add(unit);
		this.generateInfo();
	}

	public List<Unit> getProducibleUnits() {
		return this.producibleUnits;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Player getLastCapturer() {
		return this.lastCapturer;
	}

	public void setLastCapturer(final Player player) {
		this.lastCapturer = player;
	}

	public Integer getCaptureDifficulty() {
		return this.captureDifficulty;
	}

	public Integer getRemainingCaptureTime() {
		return this.remainingCaptureTime;
	}

	public Double getAttackBonus() {
		return this.attackBonus;
	}

	public Double getDefenseBonus() {
		return this.defenseBonus;
	}

	public Boolean hasOwner() {
		return this.owner != null;
	}

	public Player getOwner() {
		return this.owner;
	}

	public void setOwner(final Player player) {
		this.owner = player;
	}

	public Position getPosition() {
		return this.position;
	}

	public void setPosition(final Position pos) {
		this.position = pos;
	}

	public Integer getCaptureWorth() {
		return this.captureWorth;
	}

	@Override
	public String getInfo() {
		String r = getName();

		if (hasOwner()) {
			r += " ~ " + getOwner().getName();
		}

		r += "\n";

		return r + this.info;
	}

	@Override
	public void generateInfo() {
		String r = "";
		this.info = r;

	}
}

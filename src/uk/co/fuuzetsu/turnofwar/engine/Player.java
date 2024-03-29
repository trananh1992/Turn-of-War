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
import java.util.Map;

import android.graphics.Bitmap;

public class Player {
    private final String name;
    private final Boolean lost;
    private Integer gold = 0;
    private final Integer colour;
    private Bitmap flag;
    private Map<String, Bitmap> unitSprites;

    private final List<Unit> ownedUnits = new ArrayList<Unit>();
    private final List<Building> ownedBuildings = new ArrayList<Building>();
    private boolean isAi = false;

    public Player(String name, final Integer colour) {
        this.name = name;
        this.colour = colour;
        this.lost = false;
    }

    public final String getName() {
        return this.name;
    }

    public final Boolean hasLost() {
        Boolean hasHQ = true;
        for (Unit u : getOwnedUnits()) {
            if (u.getName().equals("CommandVehicle")) {
                hasHQ = false;
            }
        }
        return (hasHQ);
    }

    // return this.lost || (ownedUnits.isEmpty() && ownedBuildings.isEmpty());
    // to win when they have no buildings and units left

    public final Boolean hasMoveableUnits() {
        for (Unit u : ownedUnits)
            if (!u.hasFinishedTurn()) {
                return true;
            }

        return false;
    }

    public final void removeUnit(final Unit unit) {
        ownedUnits.remove(unit);
    }

    public final void removeBuilding(final Building building) {
        ownedBuildings.remove(building);
    }

    public final List<Unit> getOwnedUnits() {
        return this.ownedUnits;
    }

    public final void addUnit(final Unit unit) {
        this.ownedUnits.add(unit);
    }

    public final void addBuilding(final Building building) {
        this.ownedBuildings.add(building);
    }

    @Override
	public final String toString() {
        return name;
    }

    public final Integer getGoldAmount() {
        return this.gold;
    }

    public final void setGoldAmount(final Integer amount) {
        this.gold = amount;
    }

    public final List<Building> getOwnedBuildings() {
        return this.ownedBuildings;
    }

    public final Integer getColour() {
        return this.colour;
    }

    public final Bitmap getFlag() {
        return flag;
    }

    public final Boolean hasFlag() {
        return flag != null;
    }

    public final void setFlag(final Bitmap flag) {
        this.flag = flag;
    }

    public final Bitmap getUnitSprite(final String unitName) {
        return unitSprites.get(unitName);
    }

    public final void setUnitSprites(final Map<String, Bitmap> sprites) {
        unitSprites = sprites;
    }

    public boolean isAi() {
        return this.isAi;
    }

    public final void setIsAi(final boolean isAi) {
        this.isAi = isAi;
    }

    public void setGameState(final GameState gameState) {
        return;
    }

    public void takeTurn() {
        return; /* Regular players do nothing here */
    }

    /* For AI to override */
    public void setState(final GameState gs) {
        return;
    }
}

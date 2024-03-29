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

import java.text.DecimalFormat;

public class Unit extends DrawableMapObject {
    private final String mobility;
    private final Integer maxMovement;
	private Integer movement;
    private final Integer maxHealth;
	private int health;
	private Double lastDamage;
    private final Integer attack;
    private Position position;
    private Boolean hasFinishedTurn = false;
    private Boolean hasMoved = false;
    private Player owner;
    final Boolean isRanged;
    private final Boolean isFlying;
    private final Boolean isBoat;
    private final Integer productionCost;

    private static DecimalFormat decformat = new DecimalFormat("#.##");

    // return new Unit(name, maxHealth, maxMovement,
    // attack, meleeDefense, rangeDefense, flying,
    // productionCost, file, file2, dir, pack);

    public Unit(final String name, final Integer maxHealth, String mobility,
                final Integer maxMovement, final Integer attack,
                final Boolean isRanged, final Boolean isFlying,
                final Boolean isBoat, final Integer productionCost,
                final String spriteLocation, final String spriteDir,
                final String spritePack) {
        super(name, spriteLocation, spriteDir, spritePack);

        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.lastDamage = 0.0;

        this.mobility = mobility;

        this.maxMovement = maxMovement;
        this.movement = this.maxMovement;

        this.attack = attack;
        this.isRanged = isRanged;
        this.isFlying = isFlying;
        this.isBoat = isBoat;
        this.productionCost = productionCost;

        generateInfo();
    }

    /* Used for copying the unit template */
    public Unit(final Unit unit) {

        super(unit.getName(), unit.getSpriteLocation(), unit.getSpriteDir(),
              unit.getSpritePack());

        this.maxHealth = unit.getMaxHealth();
        this.health = this.maxHealth;
        this.lastDamage = 0.0;

        this.mobility = unit.getMobility();

        this.maxMovement = unit.getMaxMovement();
        this.movement = this.maxMovement;

        this.attack = unit.getAttack();

        this.owner = unit.getOwner();
        this.isRanged = unit.isRanged();
        this.isFlying = unit.isFlying();
        this.isBoat = unit.isBoat();
        this.productionCost = unit.getProductionCost();
        this.info = unit.info;
    }

    public String getMobility() {
        return this.mobility;
    }

    public Boolean isDead() {
        return health <= 0;
    }

    public int getHealth() {
        return this.health;
    }

    public Integer getMaxHealth() {
        return this.maxHealth;
    }

    public Double getLastDamage() {
        return this.lastDamage;
    }

    public Integer getAttack() {
        return this.attack;
    }

    public Position getPosition() {
        return this.position;
    }

    public Integer getRemainingMovement() {
        return this.movement;
    }

    public Integer getMaxMovement() {
        return this.maxMovement;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(final Player player) {
        this.owner = player;
    }

    public void reduceHealth(final Double damage) {
        this.lastDamage += -damage;
        this.health -= damage;

        if (this.health < 0) {
            this.health = 0;
        }
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    public void restoreHealth(final int heal) {
        int newHealth = (this.health) + heal;
        this.health = (newHealth <= maxHealth) ? newHealth : maxHealth;
    }

    public Boolean hasFinishedTurn() {
        return this.hasFinishedTurn;
    }

    public void setFinishedTurn(final Boolean b) {
        this.hasFinishedTurn = b;
        // if(this.getName().equals("Stealth")){
        // this.getSpriteLocation();
        // String A2 = "yolo2";
        // Log.v(A2, this.getSpriteLocation() + " " + this.getSpriteDir() + " "
        // + this.getSpritePack() + " " + this.getInfo());
    }

    @Override
	public String toString() {
        return getName();
    }

    public Boolean isRanged() {
        return this.isRanged;
    }

    public Boolean isFlying() {
        return this.isFlying;
    }

    public Boolean isBoat() {
        return this.isBoat;
    }

    public void resetLastDamage() {
        this.lastDamage = 0.0;
    }

    public Boolean reduceMovement(final Integer movementCost) {
        if (this.movement - movementCost < 0) {
            return false;
        }

        this.movement = movementCost;
        return true;
    }

    public void setMoved(Boolean b) {
        hasMoved = true;
    }

    public Boolean hasMoved() {
        return hasMoved;
    }

    public void resetTurnStatistics() {
        movement = maxMovement;
        hasMoved = false;
        setFinishedTurn(false);
    }

    public void resetTurnStatistics2() {
        // movement = maxMovement;
        hasMoved = false;
        // setFinishedTurn(false);
    }

    public void zeroMovement() {
        this.movement = 0;
        // hasMoved = false;
        // setFinishedTurn(false);
    }

    public void resetMovement() {
        movement = maxMovement;
        // hasMoved = false;
        // setFinishedTurn(false);
    }

    public Integer getProductionCost() {
        return this.productionCost;
    }

    @Override
    public final String getInfo() {
        String r = getName() + " ~ " + "Move Points " + getMaxMovement() + "\n"; //times by 4
        // r += "Health: " + getMaxHealth();
        return r + this.info;
    }

    @Override
    public void generateInfo() {
        // String r = "/" + decformat.format(getMaxHealth()) + "\n";
        String r = "";
        // r += "Defense: " + decformat.format(getMeleeDefense()) + " (Melee) "
        // + decformat.format(getRangeDefense()) + " (Ranged)\n";

        this.info = r;
    }
}

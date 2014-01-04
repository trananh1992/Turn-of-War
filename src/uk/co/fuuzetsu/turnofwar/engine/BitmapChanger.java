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
import java.util.Locale;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public final class BitmapChanger {

	private BitmapChanger() {
	}

	public static Bitmap changeColour(final Integer itsaflag,
			final Bitmap templateBitmap, final Integer originalColour,
			final Integer replacementColour) {
		if (replacementColour == 0) {
			return templateBitmap;
		}
		Bitmap replacement = templateBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Integer allPxs = replacement.getHeight() * replacement.getWidth();
		int[] allpixels = new int[allPxs];
		replacement.getPixels(allpixels, 0, replacement.getWidth(), 0, 0,
				replacement.getWidth(), replacement.getHeight());

		for (Integer i = 0; i < allPxs; i++) {
			if (allpixels[i] == originalColour) {
				allpixels[i] = replacementColour;
			}
		}
		replacement = new ColourSwap().Swap(allPxs, allpixels,
				replacementColour, replacement, itsaflag);

		return replacement;
	}

	public static Bitmap combineMap(final GameMap map, final Integer tilesize,
			final Map<String, Map<String, Bitmap>> graphics) {
		Bitmap result = null;
		Integer width = map.getWidth() * tilesize;
		Integer height = map.getHeight() * tilesize;

		result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas combined = new Canvas(result);

		for (int i = 0; i < map.getWidth(); ++i) {
			for (int j = 0; j < map.getHeight(); j++) {
				Position pos = new Position(i, j);
				GameField gf = map.getField(i, j);
				String gfn = gf.getName();
				RectF dest = new RectF(tilesize * i, tilesize * j, tilesize * i
						+ tilesize, tilesize * j + tilesize);
				combined.drawBitmap(graphics.get("Fields").get(gfn), null,
						dest, null);

				BitmapChanger.drawBorder(map, combined, pos, dest, graphics);

				if (gf.hostsBuilding()) {
					Building b = gf.getBuilding();
					String n = b.getName();

					// String A = "yolo";
					// Log.v(A, "n " + n + " getbuilding" +
					// (graphics.get("Buildings").get(n)) + " " + "dev" +
					// n.toLowerCase(Locale.ENGLISH));

					if (((graphics.get("Buildings").get(n))) != null) { // swap
																		// texture
																		// to
																		// dev
																		// texture
																		// if
																		// missing
						combined.drawBitmap(graphics.get("Buildings").get(n),
								null, dest, null);
					} else {
						String name = "dev" + n.toLowerCase(Locale.ENGLISH); // terrible
																				// bugs
																				// otherwise
						Bitmap bitmap = graphics.get("Misc").get(name);
						combined.drawBitmap(bitmap, null, dest, null);
					}
				}
			}
		}
		return result;
	}

	private static void drawBorder(final GameMap map, final Canvas canvas,
			final Position currentField, final RectF dest,
			final Map<String, Map<String, Bitmap>> graphics) {
		/* TODO not hard-code this */
		Integer i = currentField.getX();
		Integer j = currentField.getY();

		final String NF = "NoTaReAlTiL3";

		String gfn = map.getField(currentField).getSpriteLocation();

		Position nep, np, nwp, ep, cp, wp, sep, sp, swp;
		String ne, n, nw, e, c, w, se, s, sw;

		nep = new Position(i + 1, j - 1);
		np = new Position(i, j - 1);
		nwp = new Position(i - 1, j - 1);

		ep = new Position(i + 1, j);
		cp = new Position(i, j);
		wp = new Position(i - 1, j);

		sep = new Position(i + 1, j + 1);
		sp = new Position(i, j + 1);
		swp = new Position(i - 1, j + 1);

		ne = map.isValidField(nep) ? map.getField(nep).getSpriteLocation() : NF;
		n = map.isValidField(np) ? map.getField(np).getSpriteLocation() : NF;
		nw = map.isValidField(nwp) ? map.getField(nwp).getSpriteLocation() : NF;

		e = map.isValidField(ep) ? map.getField(ep).getSpriteLocation() : NF;
		c = map.isValidField(cp) ? map.getField(cp).getSpriteLocation() : NF;
		w = map.isValidField(wp) ? map.getField(wp).getSpriteLocation() : NF;

		se = map.isValidField(sep) ? map.getField(sep).getSpriteLocation() : NF;
		s = map.isValidField(sp) ? map.getField(sp).getSpriteLocation() : NF;
		sw = map.isValidField(swp) ? map.getField(swp).getSpriteLocation() : NF;

		Func<String, Void> drawer = new Func<String, Void>() {
			public Void apply(String sprite) {
				canvas.drawBitmap(graphics.get("Fields").get(sprite), null,
						dest, null);
				return null; /* Java strikes again */
			}
		};
		List<String> land = new ArrayList<String>(); // add terrain types here
		land.add("grass");
		land.add("sand");
		land.add("mountain");
		land.add("forest");

		List<String> liquid = new ArrayList<String>();
		liquid.add("water");
		liquid.add("lava");
		liquid.add("highway");
		liquid.add("river");
		liquid.add("bridge_horizontal");
		liquid.add("bridge_vertical");

		if (liquid.contains(gfn)) {

			if (land.contains(s)) {
				drawer.apply(String.format("border %s %d", s, 1));
			}

			if (land.contains(e)) {
				drawer.apply(String.format("border %s %d", e, 2));
			}

			if (land.contains(n)) {
				drawer.apply(String.format("border %s %d", n, 3));
			}

			if (land.contains(w)) {
				drawer.apply(String.format("border %s %d", w, 4));
			}

			if (liquid.contains(s) && liquid.contains(e) && land.contains(se)) {
				drawer.apply(String.format("corner %s %d", se, 1));
			}

			if (liquid.contains(n) && liquid.contains(e) && land.contains(ne)) {
				drawer.apply(String.format("corner %s %d", ne, 2));
			}

			if (liquid.contains(n) && liquid.contains(w) && land.contains(nw)) {
				drawer.apply(String.format("corner %s %d", nw, 3));
			}

			if (liquid.contains(s) && liquid.contains(w) && land.contains(sw)) {
				drawer.apply(String.format("corner %s %d", sw, 4));
			}

			if (land.contains(s) && land.contains(e) && land.contains(se)) {
				if (s.equals(e) && e.equals(se)) {
					drawer.apply(String.format("fullcorner %s %d", s, 1));
				}
			}

			if (land.contains(n) && land.contains(e) && land.contains(ne)) {
				if (n.equals(e) && e.equals(ne)) {
					drawer.apply(String.format("fullcorner %s %d", n, 2));
				}
			}

			if (land.contains(n) && land.contains(w) && land.contains(nw)) {
				if (n.equals(w) && w.equals(nw)) {
					drawer.apply(String.format("fullcorner %s %d", n, 3));
				}
			}

			if (land.contains(s) && land.contains(w) && land.contains(sw)) {
				if (s.equals(w) && w.equals(sw)) {
					drawer.apply(String.format("fullcorner %s %d", s, 4));
				}
			}

		}
	}

}

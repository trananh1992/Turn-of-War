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

package ed.turnofwar.engine;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ColourSwap {
	public Bitmap Swap(Integer allPxs, int[] allpixels,
			Integer replacementColour, Bitmap replacement, Integer itsaflag) {

		// redteam do nothing
		if (replacementColour == -13000705) { // blueteam sprites

			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -13000705;
				}
			}
		}
		if (replacementColour == -9256376) { // leaf green team sprites

			for (Integer i = 0; i < allPxs; i++) {

				if (allpixels[i] == -1030072) {
					allpixels[i] = -9256376;
				}
			}
		}

		if (replacementColour == -7623) { // yellowteam sprites

			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -7623;
				}
			}
		}

		if (replacementColour == -1804345) {
			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -1804345;
				}
			}
		}

		if (replacementColour == -8495908) { // purple team sprites
			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -8495908;
				}
			}
		}

		if (replacementColour == -10586212) { // stealth grey team sprites

			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -10586212;
				}
			}
		}

		if (replacementColour == -3932416) { // vibrant greenteam sprites

			for (Integer i = 0; i < allPxs; i++) {
				if (allpixels[i] == -1030072) {
					allpixels[i] = -7944448;
				}
			}
		}
		replacement.setPixels(allpixels, 0, replacement.getWidth(), 0, 0,
				replacement.getWidth(), replacement.getHeight());
		if ((replacementColour == -571062 || replacementColour == -9256376
				|| replacementColour == -1804345 || replacementColour == -10586212)
				&& itsaflag != 1) { // spawn odd teams facing right
			replacement = flipIt(replacement);
		}
		return replacement;
	}

	private static Bitmap flipIt(Bitmap replacement) {
		Matrix matrix = new Matrix(); // matrix to flip image
		matrix.preScale(-1, 1);
		// Create a Bitmap with the flip matix applied to it.
		Bitmap reflectionImage = Bitmap.createBitmap(replacement, 0, 0,
				replacement.getWidth(), replacement.getWidth(), matrix, false);
		return reflectionImage;
	}
}

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
/*Copyright 2013 Joseph Czubiak

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.*/

package uk.co.fuuzetsu.turnofwar;

import uk.co.fuuzetsu.turnofwar.R.anim;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.insideoutlier.glass.lib.Glass;

public class MainGyroSplash extends Activity {
    private Glass g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);




		if (Build.VERSION.SDK_INT >= 19) {
			hideSystemUi();
		} else {
			// remove the status bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);


		}




        setContentView(R.layout.activity_gyromain);
        FrameLayout fl = (FrameLayout) findViewById(R.id.glass);

        int numImages = 3; // how many background pictures total
        int imageArr[] = new int[numImages];
        imageArr[0] = R.id.glass_imageview1;
        imageArr[1] = R.id.glass_imageview2;
        imageArr[2] = R.id.glass_imageview3;

        int n = (int)(Math.random() * numImages);

        ImageView iv = (ImageView) findViewById(imageArr[n]);

        // iv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // iv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        iv.setVisibility(1);

        g = new Glass(this, fl, iv);
        g.scale(0.9);
        g.setResponsiveness(15, 15); // 5 is default
        // g.dimLights();
        g.start();

        Button button = (Button) findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainGyroSplash.this,
                                             MenuActivity.class);
                startActivity(myIntent);
                overridePendingTransition(anim.push_from_below_in,
                                          anim.push_from_below_out);
                // overridePendingTransition(R.anim.fade, R.anim.hold);
            }
        });
    }
	@SuppressLint("NewApi")
	private void hideSystemUi() {
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
	}
    @Override
    protected void onPause() {
        super.onPause();
        g.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        g.start();
    }
}

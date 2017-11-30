package com.xingren.imaging.core.clip;

import android.graphics.RectF;

/**
 * Created by felix on 2017/11/28 下午6:15.
 */

public interface IMGClip {

    float CLIP_MARGIN = 60f;

    float CLIP_FRAME_MIN = 100f;

    enum Anchor {
        LEFT(1),
        RIGHT(2),
        TOP(4),
        BOTTOM(8),
        LEFT_TOP(5),
        RIGHT_TOP(6),
        LEFT_BOTTOM(9),
        RIGHT_BOTTOM(10);

        int v;

        /**
         * left: 0
         * top: 2
         * right: 1
         * bottom 3
         */

        final static int P = 0, N = 1;

        final static int H = 0, V = 2;

        final static int[] PN = {1, -1};

        Anchor(int v) {
            this.v = v;
        }

        public void d(RectF win, RectF frame, float dx, float dy) {
            float[] ow = cohesion(win, CLIP_MARGIN);
            float[] iw = cohesion(frame, CLIP_FRAME_MIN);
            float[] fw = cohesion(frame, 0);

            float[] dxy = {dx, 0, dy};

            for (int i = 0; i < 4; i++) {
                if (((1 << i) & v) != 0) {

                    int pn = PN[i & 1];

                    // TODO
                    fw[i] = pn * revise(pn * (fw[i] + dxy[i & 2]), pn * ow[i], pn * iw[i + PN[i & 1]]);


                }
            }

            frame.left = fw[0];
            frame.top = fw[2];
            frame.right = fw[1];
            frame.bottom = fw[3];

        }

        private static float revise(float v, float min, float max) {
            return Math.min(Math.max(v, min), max);
        }

        private static float[] cohesion(RectF win, float v) {
            return new float[]{
                    win.left + v, win.right - v,
                    win.top + v, win.bottom - v
            };
        }
    }
}
/*
 * This file is a part of Telegram X
 * Copyright © 2014-2022 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 14/02/2016 at 22:17
 */
package org.thunderdog.challegram.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;

import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.Views;

import me.vkryl.android.AnimatorUtils;
import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.widget.FrameLayoutFix;
import me.vkryl.core.ColorUtils;

public class CheckBoxView extends View {
  private final BoolAnimator isChecked = new BoolAnimator(this, AnimatorUtils.DECELERATE_INTERPOLATOR, 165l);
  private final BoolAnimator isHidden = new BoolAnimator(this, AnimatorUtils.DECELERATE_INTERPOLATOR, 165l);
  private final BoolAnimator isDisabled = new BoolAnimator(this, AnimatorUtils.DECELERATE_INTERPOLATOR, 165l);
  private final BoolAnimator isIndeterminate = new BoolAnimator(this, AnimatorUtils.DECELERATE_INTERPOLATOR, 165l);

  private final RectF rect;
  private final Paint outerPaint;

  public CheckBoxView (Context context) {
    super(context);

    outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    outerPaint.setColor(Theme.radioOutlineColor());
    outerPaint.setStyle(Paint.Style.STROKE);

    rect = new RectF();
  }

  public void setChecked (boolean checked, final boolean animated) {
    isChecked.setValue(checked, animated);
  }

  public void setIndeterminate(boolean indeterminate, boolean animated){
    isIndeterminate.setValue(indeterminate, animated);
  }

  public void setHidden (boolean hidden, final boolean animated) {
    isHidden.setValue(hidden, animated);
  }

  public boolean toggle () {
    return isChecked.toggleValue(true);
  }

  public void setDisabled (boolean disabled, boolean animated) {
    isDisabled.setValue(disabled, animated);
  }

  // Internal

  private static final float FACTOR_DIFF = .65f;
  private static final float SCALE_DIFF = .15f;
  private Path path=new Path();
  private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);

  @Override
  protected void onDraw (Canvas c) {
    final float showFactor = 1f - isHidden.getFloatValue();

    if (showFactor == 0f) {
      return;
    }

    final float factor = isChecked.getFloatValue();
    final int alpha = (int) (255f * showFactor);

//    final int x1 = Screen.dp(4f);
//    final int y1 = Screen.dp(11f);
    final int lineSize = Screen.dp(1.5f);

    float rectFactor = Math.min(factor / FACTOR_DIFF, 1f);
    float checkFactor = factor <= FACTOR_DIFF ? 0f : (factor - FACTOR_DIFF) / (1f - FACTOR_DIFF);
    float scaleFactor = 1f - (rectFactor == 1f ? 1f - checkFactor : rectFactor) * SCALE_DIFF;

    final int radius = Screen.dp(2f);
    final int offset = (int) ((float) radius * .5f);

    outerPaint.setStrokeWidth(radius);

    int size = Math.min(getMeasuredWidth(), getMeasuredHeight());

    rect.left = offset;
    rect.top = offset;
    rect.right = size - offset * 2;
    rect.bottom = size - offset * 2;

    float cx = (rect.left + rect.right) * .5f;
    float cy = (rect.top + rect.bottom) * .5f;

    final int restoreToCount = Views.save(c);
    c.scale(scaleFactor, scaleFactor, cx, cy);

    int color = ColorUtils.fromToArgb(Theme.radioOutlineColor(), Theme.radioFillingColor(), rectFactor * (1f - isDisabled.getFloatValue()));
    outerPaint.setColor(color);
    outerPaint.setAlpha(alpha);
    c.drawRoundRect(rect, radius, radius, outerPaint);

    if (rectFactor != 0f) {
      int w = (int) ((rect.right - rect.left - offset * 2) * .5f * rectFactor);
      int h = (int) ((rect.bottom - rect.top - offset * 2) * .5f * rectFactor);

      int left = (int) (rect.left + offset + w);
      int right = (int) (rect.right - offset - w);

      final int alphaColor = ColorUtils.alphaColor(showFactor, color);

      c.drawRect(rect.left + offset, rect.top + offset, left, rect.bottom - offset, Paints.fillingPaint(alphaColor));
      c.drawRect(right, rect.top + offset, rect.right - offset, rect.bottom - offset, Paints.fillingPaint(alphaColor));
      c.drawRect(left, rect.top + offset, right, rect.top + offset + h, Paints.fillingPaint(alphaColor));
      c.drawRect(left, rect.bottom - offset - h, right, rect.bottom - offset, Paints.fillingPaint(alphaColor));

      if (checkFactor != 0f) {
        final int checkColor = ColorUtils.alphaColor(showFactor, Theme.radioCheckColor());
        float indFactor=isIndeterminate.getFloatValue();
        float x1=Screen.dp(3);
        float y1=interpolate(Screen.dp(8.5f), Screen.dp(8.5f), indFactor);
        float x2=interpolate(Screen.dp(6.5f), Screen.dp(8.5f), indFactor);
        float y2=interpolate(Screen.dp(12), Screen.dp(8.5f), indFactor);
        float x3=Screen.dp(14.25f);
        float y3=interpolate(Screen.dp(4.5f), Screen.dp(8.5f), indFactor);

        path.rewind();
        path.moveTo(interpolate(x2, x1, checkFactor), interpolate(y2, y1, checkFactor));
        path.lineTo(x2, y2);
        path.lineTo(interpolate(x2, x3, checkFactor), interpolate(y2, y3, checkFactor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(checkColor);
        paint.setStrokeWidth(lineSize);
        c.drawPath(path, paint);
      }
    }

    Views.restore(c, restoreToCount);
  }

  private static float interpolate(float x1, float x2, float k){
    return x1*(1f-k)+x2*k;
  }

  public static CheckBoxView simpleCheckBox (Context context) {
    return simpleCheckBox(context, Lang.rtl());
  }

  public static CheckBoxView simpleCheckBox (Context context, boolean rtl) {
    FrameLayoutFix.LayoutParams params;

    params = FrameLayoutFix.newParams(Screen.dp(18f), Screen.dp(18f));
    params.gravity = Gravity.CENTER_VERTICAL | (rtl ? Gravity.LEFT : Gravity.RIGHT);
    params.leftMargin = params.rightMargin = Screen.dp(19f);

    CheckBoxView checkBox;
    checkBox = new CheckBoxView(context);
    checkBox.setLayoutParams(params);

    return checkBox;
  }
}

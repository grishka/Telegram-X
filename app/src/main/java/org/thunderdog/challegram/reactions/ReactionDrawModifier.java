package org.thunderdog.challegram.reactions;

import android.graphics.Canvas;
import android.view.View;

import org.drinkless.td.libcore.telegram.TdApi;
import org.thunderdog.challegram.component.sticker.TGStickerObj;
import org.thunderdog.challegram.loader.ImageReceiver;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.util.DrawModifier;

public class ReactionDrawModifier implements DrawModifier{
	private ImageReceiver receiver;

	public ReactionDrawModifier(String reaction, Tdlib tdlib, View parent){
		receiver=new ImageReceiver(parent, 0);
		TdApi.Sticker _sticker=tdlib.getReaction(reaction).staticIcon;
		TGStickerObj sticker=new TGStickerObj(tdlib, _sticker, null, _sticker.type);
		receiver.requestFile(sticker.getFullImage());
	}

	@Override
	public void afterDraw(View view, Canvas c){
		int size=Screen.dp(18);
		int cx = view.getMeasuredWidth() - Screen.dp(18f) - size/2;
		int cy = view.getMeasuredHeight() / 2;

		int x=cx-size/2;
		int y=cy-size/2;
		receiver.setBounds(x, y, x+size, y+size);
		receiver.draw(c);
	}
}
package com.plusls.ommc.mixin.feature.dontClearChatHistory;

import com.plusls.ommc.config.Configs;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// From https://www.curseforge.com/minecraft/mc-mods/dont-clear-chat-history
@Mixin(ChatHud.class)
public class MixinChatHud {
    @Inject(method = "clear", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 2), cancellable = true)
    private void dontClearChatHistory(boolean clearHistory, CallbackInfo ci) {
        if (Configs.Generic.DONT_CLEAR_CHAT_HISTORY.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Redirect(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    private int modifySize(List<ChatHudLine<OrderedText>> list) {
        if (Configs.Generic.DONT_CLEAR_CHAT_HISTORY.getBooleanValue()) {
            return 1;
        }
        return list.size();
    }
}

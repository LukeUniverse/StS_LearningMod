package macdonaldmod.Patches;

import com.esotericsoftware.spine.AnimationState;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import macdonaldmod.relics.CrossClassRelicInterface;

import java.lang.reflect.Method;

import com.megacrit.cardcrawl.characters.AbstractPlayer;




import java.lang.reflect.Method;

import static macdonaldmod.LearningMacMod.PantsPath;

public class CrossClassRelicPatches {

    //Flashes our cross class relics everytime card rewards are offered, since our relic modifies the card pool.
    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "getRewardCards"
    )
    public static class FlashCrossClassRelics {
        public static void Postfix() {
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                if (r instanceof CrossClassRelicInterface) {
                    r.flash();
                }
            }
        }
    }


    //This modifies the card pool if the player has a cross class relic
    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "initializeCardPools"
    )
    public static class CardpoolInitFix {
        public static void Postfix(AbstractDungeon __instance) {
            if (AbstractDungeon.player != null) {
                for (AbstractRelic r : AbstractDungeon.player.relics) {
                    if (r instanceof CrossClassRelicInterface) {
                        ((CrossClassRelicInterface) r).modifyCardPool();
                    }
                }
            }
        }
    }
}
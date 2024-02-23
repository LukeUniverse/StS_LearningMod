package macdonaldmod.Patches;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.stances.AbstractStance;
import com.megacrit.cardcrawl.stances.WrathStance;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import macdonaldmod.Orbs.CalmOrb;
import macdonaldmod.Orbs.WrathOrb;
import macdonaldmod.relics.CrossClassRelicInterface;
import macdonaldmod.relics.StanceChip;

import java.util.Iterator;

import static macdonaldmod.LearningMacMod.ActuallyChangeStance;
import static macdonaldmod.LearningMacMod.logger;


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

    //TODO move this somewhere else, OR rename this parent class.
    @SpirePatch(
            clz = ChangeStanceAction.class,
            method = "update"
    )
    public static class WatcherOnStanceChangePatch {
        public static SpireReturn<Void> Prefix(ChangeStanceAction __instance, String ___id) {
            //Only pull this nonsense if we're trying to enter calm or wrath, (and we have stance chip) otherwise do normal stuff.
            if ((___id.equals("Wrath") || ___id.equals("Calm")) && AbstractDungeon.player.hasRelic(StanceChip.ID)) {

                if (ActuallyChangeStance) {
                    ActuallyChangeStance = false;
                    return SpireReturn.Continue();

                } else {
                    if (___id.equals("Wrath")) {
                        AbstractDungeon.actionManager.addToBottom(new ChannelAction(new WrathOrb()));
                        __instance.isDone = true;
                    } else /*if (___id.equals("Calm")) should I do something for Divinity?*/ {
                        AbstractDungeon.actionManager.addToBottom(new ChannelAction(new CalmOrb()));
                        __instance.isDone = true;
                    }
                    return SpireReturn.Return();
                }
            } else {
                return SpireReturn.Continue();
            }
        }
    }

    @SpirePatch(
            clz = ScryAction.class,
            method = "update"
    )
    public static class ScryIsDiscardPatch {
        @SpireInsertPatch(
                loc = 61
        )
        public static SpireReturn<Void> Insert(ScryAction __instance) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {

                //Do something here if it is the first time you've discard
                //X, Y, and Z.

                for (AbstractRelic r : AbstractDungeon.player.relics)
                    r.onManualDiscard();

                AbstractDungeon.player.drawPile.moveToDiscardPile(c);
                c.triggerOnManualDiscard();
                GameActionManager.incrementDiscard(false);
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();

            return SpireReturn.Return();
        }
    }

}
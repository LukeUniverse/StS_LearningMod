package macdonaldmod.Patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.blue.BallLightning;
import com.megacrit.cardcrawl.cards.blue.Recursion;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.Eruption;
import com.megacrit.cardcrawl.cards.purple.Vigilance;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.cards.red.Defend_Red;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.neow.NeowRoom;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import macdonaldmod.LearningMacMod;
import macdonaldmod.Orbs.CalmOrb;
import macdonaldmod.Orbs.HellfireOrb;
import macdonaldmod.Orbs.WrathOrb;
import macdonaldmod.relics.*;
import macdonaldmod.util.CrossCharacterRelicUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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

                if (CrossCharacterRelicUtility.ActuallyChangeStance) {
                    CrossCharacterRelicUtility.ActuallyChangeStance = false;
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

                AbstractDungeon.player.drawPile.moveToDiscardPile(c);
                GameActionManager.incrementDiscard(false);

                for (AbstractRelic r : AbstractDungeon.player.relics)
                    r.onManualDiscard();
                c.triggerOnManualDiscard();
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();

            return SpireReturn.Return();
        }
    }

    @SpirePatch(
            clz = com.megacrit.cardcrawl.neow.NeowRoom.class,
            method = SpirePatch.CONSTRUCTOR,
            paramtypez = boolean.class)
    public static class ReflectTwist {
        @SpirePostfixPatch
        public static void Postfix(NeowRoom room, boolean b) {
            SpireConfig config = null;
            try {
                config = new SpireConfig("MacdonaldMod", "config");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
           {
               String var = config.getString(LearningMacMod._macdonaldModOverrideIronclad);
               switch (var) {
                   case "Green": {
                       AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
                       break;
                   }
                   case "Blue": {
                       AbstractRelic hellFireRelic = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(hellFireRelic);
                       break;
                   }
                   case "Purple": {
                       AbstractRelic bloodRedLotus = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(bloodRedLotus);
                       break;
                   }
               }
           } else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
           {
               switch (config.getString(LearningMacMod._macdonaldModOverrideSilent)) {
                   case "Red": {
                       AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
                       break;
                   }
                   case "Blue": {
                       AbstractRelic noxiousRelic = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(noxiousRelic);
                       break;
                   }
                   case "Purple": {
                       AbstractRelic locket = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
                       CrossCharacterRelicUtility.ResolveClassMerge(locket);
                       break;
                   }
               }
           } else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
            {
                switch (config.getString(LearningMacMod._macdonaldModOverrideDefect)) {
                    case "Red": {
                        AbstractRelic infectionRelic = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
                        break;
                    }
                    case "Green": {
                        AbstractRelic noxiousRelic = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(noxiousRelic);
                        break;
                    }
                    case "Purple": {
                        AbstractRelic chip = RelicLibrary.getRelic(StanceChip.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(chip);
                        break;
                    }
                }
            }else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
            {
                switch (config.getString(LearningMacMod._macdonaldModOverrideWatcher)) {
                    case "Red": {
                        AbstractRelic infectionRelic = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
                        break;
                    }
                    case "Green": {
                        AbstractRelic noxiousRelic = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(noxiousRelic);
                        break;
                    }
                    case "Blue": {
                        AbstractRelic chip = RelicLibrary.getRelic(StanceChip.ID).makeCopy();
                        CrossCharacterRelicUtility.ResolveClassMerge(chip);
                        break;
                    }
                }
            }

        }

    }

}
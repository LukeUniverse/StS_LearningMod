package macdonaldmod.Patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.cards.blue.BallLightning;
import com.megacrit.cardcrawl.cards.blue.Recursion;
import com.megacrit.cardcrawl.cards.green.BouncingFlask;
import com.megacrit.cardcrawl.cards.green.PoisonedStab;
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

            String var = config.getString(LearningMacMod.MacdonaldModOverrideIronclad);
            //this DEFINITELY feels weird to be here, and as mentioned on the event, it bares repeating here... this logic should definitely be on the relic itself
           if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
           {
               switch (var) {
                   case "Green": {
                       //New Relic
                       AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
                       //Cards to remove
                       List<String> cardIDsToRemove = new ArrayList<>();
                       cardIDsToRemove.add(Bash.ID);
                       cardIDsToRemove.add(Strike_Red.ID);
                       //Cards to add
                       List<AbstractCard> cardsToAdd = new ArrayList<>();
                       cardsToAdd.add(new BouncingFlask());
                       cardsToAdd.add(new PoisonedStab()); //maybe do a  custom poisoned strike instead?

                       //Julie do the thing!
                       CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic, cardIDsToRemove, cardsToAdd);
                       break;
                   }
                   case "Blue": {
                       //New Relic
                       AbstractRelic hellFireRelic = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
                       //Cards to remove
                       List<String> cardIDsToRemove = new ArrayList<>();
                       cardIDsToRemove.add(Bash.ID);
                       cardIDsToRemove.add(Strike_Red.ID);
                       //Cards to add
                       List<AbstractCard> cardsToAdd = new ArrayList<>();
                       cardsToAdd.add(new Recursion());
                       cardsToAdd.add(new BallLightning());
                       //Julie do the thing!
                       CrossCharacterRelicUtility.ResolveClassMerge(hellFireRelic, cardIDsToRemove, cardsToAdd);
                       break;
                   }
                   case "Purple": {
                       //New Relic
                       AbstractRelic bloodRedLotus = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
                       //Cards to remove
                       List<String> cardIDsToRemove = new ArrayList<>();
                       cardIDsToRemove.add(Bash.ID);
                       cardIDsToRemove.add(Defend_Red.ID);
                       //Cards to add
                       List<AbstractCard> cardsToAdd = new ArrayList<>();
                       cardsToAdd.add(new Eruption());
                       cardsToAdd.add(new Vigilance());
                       //Julie do the thing!
                       CrossCharacterRelicUtility.ResolveClassMerge(bloodRedLotus, cardIDsToRemove, cardsToAdd);
                       break;
                   }
               }
           }
        }

    }

}
package macdonaldmod.util;


import com.esotericsoftware.spine.AnimationState;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import macdonaldmod.relics.*;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//TODO: Should this logger be moved to a util somewhere?
import static macdonaldmod.LearningMacMod.logger;

/*
Let's start refactoring the code some, and move some of the methods and what not that I want to be calling from multiple various places at various times
to here. Because I'm definitely repeating some code across various places, and I would vastly prefer to have that code only in a single place, please and thank you.
 */
public class CrossCharacterRelicUtility {
    //Variables
    public static boolean ActuallyChangeStance = false;

    //this actually would probably be a good place to also include the card reward changes...
    public static void ResolveClassMerge(AbstractRelic relicToAdd, List<String> cardsToRemove, List<AbstractCard> cardsToAdd)
    {
        relicToAdd.spawn((float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F);
        relicToAdd.obtain();
        relicToAdd.isObtained = true;
        relicToAdd.isAnimating = false;
        relicToAdd.isDone = false;
        relicToAdd.flash();

        for (String  r : cardsToRemove)
            AbstractDungeon.player.masterDeck.removeCard(r);
        for (AbstractCard a : cardsToAdd)
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(a, (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));
    }

    //Methods
    public static void GlobalChangeLook() {
        /*
        user "Melt" from discord said:
        oh right should probably mention this
        if you're just calling loadAnimation again
        you'll want to dispose the player's atlas first
        So this is probably a TODO
         */

        try {
            //Try this to maybe dispose of current atlas? I don't know man.
            Field atlas = AbstractCreature.class.getDeclaredField("atlas");
            Field skeleton = AbstractCreature.class.getDeclaredField("skeleton");
            atlas.setAccessible(true);
            atlas.set(AbstractDungeon.player, null);
            skeleton.setAccessible(true);
            skeleton.set(AbstractDungeon.player, null);

            Method loadAnimationMethod = AbstractCreature.class.getDeclaredMethod("loadAnimation", String.class, String.class, Float.TYPE);
            loadAnimationMethod.setAccessible(true);
            loadAnimationMethod.invoke(AbstractDungeon.player, AlternateLookPath("skeleton.atlas"), AlternateLookPath("skeleton.json"), 1.0F);
            AnimationState.TrackEntry e = AbstractDungeon.player.state.setAnimation(0, "Idle", true);
            e.setTimeScale(0.6F);

            //loadEyeAnimation for the watcher here???
        } catch (Exception ex) {
            logger.error(ex.getMessage());

        }
    }

    public static String AlternateLookPath(String fileName) {
        String rv = "images/characters/ironclad"; //Default location for default Ironclad look.
        if (AbstractDungeon.player != null) {
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                if (r instanceof CrossClassRelicInterface) {
                    if (r.relicId.equals(InfectionMutagen.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/greenpants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Red/" + fileName;
                        break;

                    } else if (r.relicId.equals(HellfireBattery.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/bluepants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Red/" + fileName;
                        break;
                    } else if (r.relicId.equals(BloodLotus.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/purplepants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Red/" + fileName;
                        break;
                    } else if (r.relicId.equals(NoxiousBattery.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Blue/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Green/" + fileName;
                        break;

                    } else if (r.relicId.equals(LocketOfTheSnake.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Purple/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Green/" + fileName;
                        break;
                    } else if (r.relicId.equals(StanceChip.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Purple/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Blue/" + fileName;
                        break;
                    }
                }
            }
        }

        return rv;
    }

    //Should I modify Relic pool too? Probs.
    public static void ModifyCardPool(ArrayList<AbstractCard> cardList) {
        for (AbstractCard c : cardList) {
            if(c.rarity != AbstractCard.CardRarity.BASIC) {
                switch (c.rarity) {
                    case COMMON: {
                        AbstractDungeon.commonCardPool.removeCard(c);
                        AbstractDungeon.srcCommonCardPool.removeCard(c);
                        AbstractDungeon.commonCardPool.addToTop(c);
                        AbstractDungeon.srcCommonCardPool.addToBottom(c);
                        continue;
                    }
                    case UNCOMMON: {
                        AbstractDungeon.uncommonCardPool.removeCard(c);
                        AbstractDungeon.srcUncommonCardPool.removeCard(c);
                        AbstractDungeon.uncommonCardPool.addToTop(c);
                        AbstractDungeon.srcUncommonCardPool.addToBottom(c);
                        continue;
                    }
                    case RARE: {
                        AbstractDungeon.rareCardPool.removeCard(c);
                        AbstractDungeon.srcRareCardPool.removeCard(c);
                        AbstractDungeon.rareCardPool.addToTop(c);
                        AbstractDungeon.srcRareCardPool.addToBottom(c);
                        continue;
                    }
                    case CURSE: {
                        AbstractDungeon.curseCardPool.removeCard(c);
                        AbstractDungeon.srcCurseCardPool.removeCard(c);
                        AbstractDungeon.curseCardPool.addToTop(c);
                        AbstractDungeon.srcCurseCardPool.addToBottom(c);
                    }
                }
            }
        }
    }

}

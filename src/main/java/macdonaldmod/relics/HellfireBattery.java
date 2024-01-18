package macdonaldmod.relics;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.BurningBlood;
import macdonaldmod.Orbs.HellfireOrb;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.PantsPath;
import static macdonaldmod.LearningMacMod.makeID;

public class HellfireBattery extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "HellfireBattery"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public HellfireBattery() {
        super(ID, NAME, AbstractCard.CardColor.RED, RARITY, SOUND);
    }


    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {

        //this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        getUpdatedDescription();
        this.addToBot(new ChannelAction(new HellfireOrb()));
    }

    // maybe??
    //public void onVictory() {
        //this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        //AbstractPlayer p = AbstractDungeon.player;
        /*
        Add a heal here based off blue shit.
        if (p.currentHealth > 0) {
            p.heal(AbstractPlayer.poisonKillCount);
            this.counter = 0;
        }
        */

    //}

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }


    public void update() {
        super.update();
        //this.counter = //some trackable thing here
        getUpdatedDescription();
        refreshTips();
    }

    //this is needed to actually update the tool tips.
    public void refreshTips() {
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.DESCRIPTIONS[0]));
        this.initializeTips();
    }

    @Override
    public void obtain()
    {
        if (AbstractDungeon.player.hasRelic(BurningBlood.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(BurningBlood.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        } else {
            super.obtain();
        }
    }

    // Skill book Region
    @Override
    public void onEquip() {
        modifyCardPool();
        ChangeLook();
        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD) && AbstractDungeon.player.masterMaxOrbs == 0) {
            AbstractDungeon.player.masterMaxOrbs = 2;
        }
    }

    public void ChangeLook() //PANTS! (and eyes)
    {
        try {
            //I was having many issues with the damned reflection, hence all the loggers, but it's finally working for now! Woo.
            if (AbstractDungeon.player != null) {

                logger.info("AbstractDungeon.player is not null.");
                Method loadAnimationMethod = AbstractCreature.class.getDeclaredMethod("loadAnimation", String.class, String.class, Float.TYPE);
                loadAnimationMethod.setAccessible(true);

                logger.info("loadAnimationMethod set up, about to run.");
                loadAnimationMethod.invoke(AbstractDungeon.player, PantsPath("skeleton.atlas"), PantsPath("skeleton.json"), 1.0F);

                logger.info("loadAnimation Method invoked^, beginning AnimationState.TrackEntry.");
                AnimationState.TrackEntry e = AbstractDungeon.player.state.setAnimation(0, "Idle", true);

                logger.info("setting time scale now.");
                e.setTimeScale(0.6F);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public void modifyCardPool() {
        logger.info(ID +" acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        //I only want a SUBSET of the Blue cards, not all of them, so here we go.
        //Common
        classCards.add(CardLibrary.getCard(BallLightning.ID));
        classCards.add(CardLibrary.getCard(Recursion.ID));

        //was just testing this
        //classCards.add((CardLibrary.getCard("macdonaldmod:ChannelHellfire")));

        //Uncommon
        classCards.add(CardLibrary.getCard(LockOn.ID));
        classCards.add(CardLibrary.getCard(Capacitor.ID));
        classCards.add(CardLibrary.getCard(Consume.ID));
        classCards.add(CardLibrary.getCard(Defragment.ID));
        classCards.add(CardLibrary.getCard(StaticDischarge.ID));
        classCards.add(CardLibrary.getCard(Storm.ID));
        classCards.add(CardLibrary.getCard(Tempest.ID));

        //Rare
        classCards.add(CardLibrary.getCard(Electrodynamics.ID));
        classCards.add(CardLibrary.getCard(MultiCast.ID));
        classCards.add(CardLibrary.getCard(ThunderStrike.ID));

        mixCardpools(classCards);

    }

    protected void mixCardpools(ArrayList<AbstractCard> cardList) {
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

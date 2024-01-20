package macdonaldmod.relics;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.red.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.BurningBlood;
import com.megacrit.cardcrawl.relics.RingOfTheSerpent;
import com.megacrit.cardcrawl.relics.SnakeRing;
import com.megacrit.cardcrawl.screens.CharSelectInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.PantsPath;
import static macdonaldmod.LearningMacMod.makeID;

public class InfectionMutagen extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "InfectiousMutagen"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public InfectionMutagen() {
        super(ID, NAME, AbstractCard.CardColor.RED, RARITY, SOUND);
    }

    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {

        this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, 1), 1));
        this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        //getUpdatedDescription();
    }

    public void onAttack(DamageInfo info, int damageAmount, AbstractCreature target) {
        if (damageAmount > 0 && info.type == DamageInfo.DamageType.NORMAL) {
            {
                if (AbstractDungeon.player.getPower("Strength") != null)
                {
                    this.addToTop(new ApplyPowerAction(target, AbstractDungeon.player, new PoisonPower(target, AbstractDungeon.player, AbstractDungeon.player.getPower("Strength").amount), AbstractDungeon.player.getPower("Strength").amount, true));
                }
            }
        }
    }

    //At end of combat heal for each monster killed by poison (This is currently also tracking minion kills)
    //which might be something I want to change...
    public void onVictory() {
        this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        AbstractPlayer p = AbstractDungeon.player;
        if (p.currentHealth > 0) {
            p.heal(AbstractPlayer.poisonKillCount);
            this.counter = 0;
        }
    }

    @Override
    public void obtain()
    {
        //This logic should probably actually be in the event, not the relics but for now.... Oh well.
        if (AbstractDungeon.player.hasRelic(BurningBlood.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(BurningBlood.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        }
        else if(AbstractDungeon.player.hasRelic(SnakeRing.ID))
        {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(RingOfTheSerpent.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        }
         else {
            super.obtain();
        }
    }

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0] +" NL " + DESCRIPTIONS[1] + this.counter;
    }


    public void update() {
        super.update();
        this.counter = AbstractPlayer.poisonKillCount;
        getUpdatedDescription();
        refreshTips();
    }

    //this is needed to actually update the tool tips.
    public void refreshTips() {
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.DESCRIPTIONS[0] +" NL " + DESCRIPTIONS[1] + this.counter));
        this.initializeTips();
    }

    // Skill book Region
    @Override
    public void onEquip() {

        modifyCardPool();
        ChangeLook(); //This seems like ultimately the most logical place for this. Check if I need to change any reloading stuff though?
    }

    public void ChangeLook()
    {
        try {
            //I was having many issues with the damned reflection, hence all the loggers, but it's finally working for now! Woo.
            if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD)) {
                Method loadAnimationMethod = AbstractCreature.class.getDeclaredMethod("loadAnimation", String.class, String.class, Float.TYPE);
                loadAnimationMethod.setAccessible(true);
                loadAnimationMethod.invoke(AbstractDungeon.player, PantsPath("skeleton.atlas"), PantsPath("skeleton.json"), 1.0F);
                AnimationState.TrackEntry e = AbstractDungeon.player.state.setAnimation(0, "Idle", true);
                e.setTimeScale(0.6F);
            }
            else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT)){
                //Change Silent Look here eventually
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void modifyCardPool() {
        logger.info("Infection Mutagen acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD)) {

            //Common
            classCards.add(CardLibrary.getCard(Bane.ID));
            classCards.add(CardLibrary.getCard(DeadlyPoison.ID));
            classCards.add(CardLibrary.getCard(PoisonedStab.ID));

            //I had only added this here as a test
            //classCards.add((CardLibrary.getCard("macdonaldmod:Poisoned Strike")));

            //Uncommon
            classCards.add(CardLibrary.getCard(BouncingFlask.ID));
            classCards.add(CardLibrary.getCard(Catalyst.ID));
            classCards.add(CardLibrary.getCard(CripplingPoison.ID));
            classCards.add(CardLibrary.getCard(NoxiousFumes.ID));

            //Rare
            classCards.add(CardLibrary.getCard(CorpseExplosion.ID));
            classCards.add(CardLibrary.getCard(Envenom.ID));
            classCards.add(CardLibrary.getCard(Burst.ID));
        }
        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
        {
            //Common
            classCards.add(CardLibrary.getCard(Flex.ID));
            classCards.add(CardLibrary.getCard(HeavyBlade.ID));
            classCards.add(CardLibrary.getCard(TwinStrike.ID));

            //Uncommon
            classCards.add(CardLibrary.getCard(Inflame.ID));
            classCards.add(CardLibrary.getCard(Pummel.ID));
            classCards.add(CardLibrary.getCard(SpotWeakness.ID));

            //Rare
            classCards.add(CardLibrary.getCard(DemonForm.ID));
            classCards.add(CardLibrary.getCard(DoubleTap.ID));
            classCards.add(CardLibrary.getCard(LimitBreak.ID));
        }

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

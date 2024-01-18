package macdonaldmod.relics;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.powers.PoisonPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.BurningBlood;
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

        if (AbstractDungeon.player.hasRelic(BurningBlood.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(BurningBlood.ID)) {
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
        ChangeLook(); //This seems like ultimately the most logical place for this. Check if I need to change any reloading stuff though.
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
        logger.info("Infection Mutagen acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        //I only want a SUBSET of the Green cards, not all of them, so here we go.
        //Common
        classCards.add(CardLibrary.getCard("Bane"));
        classCards.add(CardLibrary.getCard("Deadly Poison"));
        classCards.add(CardLibrary.getCard("Poisoned Stab"));

        //I had only added this here as a test
        //classCards.add((CardLibrary.getCard("macdonaldmod:Poisoned Strike")));

        //Uncommon
        classCards.add(CardLibrary.getCard("Bouncing Flask"));
        classCards.add(CardLibrary.getCard("Catalyst"));
        classCards.add(CardLibrary.getCard("Crippling Poison"));
        classCards.add(CardLibrary.getCard("Noxious Fumes"));

        //Rare
        classCards.add(CardLibrary.getCard("Corpse Explosion"));
        classCards.add(CardLibrary.getCard("Envenom"));
        classCards.add(CardLibrary.getCard("Burst"));

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

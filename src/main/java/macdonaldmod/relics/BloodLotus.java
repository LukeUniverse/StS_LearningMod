package macdonaldmod.relics;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.BurningBlood;
import com.megacrit.cardcrawl.stances.AbstractStance;

import java.util.ArrayList;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.makeID;

public class BloodLotus extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "BloodLotus"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public BloodLotus() {
        super(ID, NAME, AbstractCard.CardColor.RED, RARITY, SOUND);
    }

    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void onChangeStance(AbstractStance prevStance, AbstractStance newStance) {
        if (!prevStance.ID.equals(newStance.ID) && newStance.ID.equals("Calm")) {
            this.flash();
            this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new DexterityPower(AbstractDungeon.player, 1), 1));
            this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new WeakPower(AbstractDungeon.player, 1, false), 1));
        }else if (!prevStance.ID.equals(newStance.ID) && newStance.ID.equals("Wrath")) {
            this.flash();
            this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, 1), 1));
            this.addToTop(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new FrailPower(AbstractDungeon.player, 1, false), 1));
        }
        //should I do something for Divinity? lol

    }

    //likely don't need this...
    public void onVictory() {

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

    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0] +" NL " + DESCRIPTIONS[1];
    }


    public void update() {
        super.update();
        //I might actually want to impliment this if I do healing on stance change
        //this.counter = 0;
        getUpdatedDescription();
        //refreshTips();
    }

    //this is needed to actually update the tool tips if I do something with the counter
    public void refreshTips() {
        //this.tips.clear();
        //this.tips.add(new PowerTip(this.name, this.DESCRIPTIONS[0] +" NL " + DESCRIPTIONS[1] + this.counter));
        //this.initializeTips();
    }

    // Skill book Region
    @Override
    public void onEquip() {
        modifyCardPool();
        ChangeLook();
    }


    public void ChangeLook()
    {
        macdonaldmod.LearningMacMod.GlobalChangeLook();
    }

    //Hmmm I feel like I had to include A LOT of common and uncommon, but there are barely any relevant rares...
    public void modifyCardPool() {
        logger.info("BloodRedLotus acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD)) {


            //I only want a SUBSET of the PURPLE cards, not all of them, so here we go.
            //Common
            classCards.add(CardLibrary.getCard(Crescendo.ID));
            classCards.add(CardLibrary.getCard(EmptyBody.ID));
            classCards.add(CardLibrary.getCard(EmptyFist.ID));
            classCards.add(CardLibrary.getCard(FlurryOfBlows.ID));
            classCards.add(CardLibrary.getCard(Tranquility.ID));
            //Halt?

            //Uncommon
            classCards.add(CardLibrary.getCard(EmptyMind.ID));
            classCards.add(CardLibrary.getCard(FearNoEvil.ID));
            classCards.add(CardLibrary.getCard(Indignation.ID));
            classCards.add(CardLibrary.getCard(InnerPeace.ID));
            classCards.add(CardLibrary.getCard(LikeWater.ID));
            classCards.add(CardLibrary.getCard(SimmeringFury.ID));
            classCards.add(CardLibrary.getCard(Tantrum.ID));

            //classCards.add(CardLibrary.getCard(MentalFortress.ID));
            //classCards.add(CardLibrary.getCard(Rushdown.ID));
            //fasting?
            //foreign influence?

            //Rare
            classCards.add(CardLibrary.getCard(Blasphemy.ID));
        }
        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER)) {
            //add some cards here
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

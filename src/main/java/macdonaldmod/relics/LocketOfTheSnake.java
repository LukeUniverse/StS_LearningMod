package macdonaldmod.relics;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.cards.tempCards.Shiv;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.PureWater;
import com.megacrit.cardcrawl.relics.SnakeRing;
import com.megacrit.cardcrawl.stances.AbstractStance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.AlternateLookPath;
import static macdonaldmod.LearningMacMod.makeID;

public class LocketOfTheSnake extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "LocketOfTheSnake"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public LocketOfTheSnake() {
        super(ID, NAME, AbstractCard.CardColor.GREEN, RARITY, SOUND);
    }


    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {

        //this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        getUpdatedDescription();

        //TODO, look at bloodredlotus for examples of stuffies.
    }

    public void onChangeStance(AbstractStance prevStance, AbstractStance newStance) {
        if (!prevStance.ID.equals(newStance.ID) && newStance.ID.equals("Calm")) {
            Iterator<AbstractCard> hand = AbstractDungeon.player.hand.group.iterator();
            boolean generateCard = true;
            while(hand.hasNext()) {
                AbstractCard c = (AbstractCard)hand.next();
                if (c.cardID.equals(Miracle.ID)) {
                    generateCard = false;
                }
            }
            if(generateCard)
            {
                this.flash();
                this.addToBot(new MakeTempCardInHandAction(new Miracle(), 1, false));
            }
        }else if (!prevStance.ID.equals(newStance.ID) && newStance.ID.equals("Wrath")) {
            this.flash();
            this.addToBot(new MakeTempCardInHandAction(new Shiv(), 1, false));
        }
        //should I do something for Divinity? lol

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
        return this.DESCRIPTIONS[0] +" NL " + DESCRIPTIONS[1];
    }


    public void update() {
        super.update();
        //this.counter = //some trackable thing here
        getUpdatedDescription();
        //refreshTips();
    }

    //this is needed to actually update the tool tips.
//    public void refreshTips() {
//        this.tips.clear();
//        this.tips.add(new PowerTip(this.name, this.DESCRIPTIONS[0]));
//        this.initializeTips();
//    }

    @Override
    public void obtain()
    {
        if (AbstractDungeon.player.hasRelic(SnakeRing.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(SnakeRing.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        } else if (AbstractDungeon.player.hasRelic(PureWater.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(PureWater.ID)) {
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
    }

    public void ChangeLook()
    {
        macdonaldmod.LearningMacMod.GlobalChangeLook();
    }

    public void modifyCardPool() {
        logger.info(ID +" acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();
        //kept it mostly the same as the BloodLotus relic list
        //but added a few cards focused around card creation.

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT)) {

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

            classCards.add(CardLibrary.getCard(BattleHymn.ID));
            classCards.add(CardLibrary.getCard(Study.ID));
            //fasting?
            //foreign influence?

            //Rare
            classCards.add(CardLibrary.getCard(Blasphemy.ID));
            classCards.add(CardLibrary.getCard(MasterReality.ID));

            classCards.add(CardLibrary.getCard(SpiritShield.ID));
            classCards.add(CardLibrary.getCard(ConjureBlade.ID));
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

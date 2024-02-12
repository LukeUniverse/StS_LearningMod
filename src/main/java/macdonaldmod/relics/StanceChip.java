package macdonaldmod.relics;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.CrackedCore;
import com.megacrit.cardcrawl.relics.PureWater;
import com.megacrit.cardcrawl.stances.AbstractStance;

import java.util.ArrayList;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.makeID;

public class StanceChip extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "StanceChip"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public StanceChip() {
        super(ID, NAME, AbstractCard.CardColor.PURPLE, RARITY, SOUND);
    }


    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {

        //this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        getUpdatedDescription();
        //this.addToBot(new ChannelAction(new HellfireOrb()));
    }

    public void onChangeStance(AbstractStance prevStance, AbstractStance newStance) {
        //Do anything here, or let a patch handle everything?
    }

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
        //This logic should probably actually be in the event, not the relics but for now.... Oh well.
        if (AbstractDungeon.player.hasRelic(PureWater.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(PureWater.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        }
        else if(AbstractDungeon.player.hasRelic(CrackedCore.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(CrackedCore.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        }
        else {
            super.obtain();
        }
    }

    // Skill book Region
    @Override
    public void onEquip() {
        modifyCardPool();
        ChangeLook();
        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER) && AbstractDungeon.player.masterMaxOrbs == 0) {
            AbstractDungeon.player.masterMaxOrbs = 2;
        }
    }

    public void ChangeLook()
    {
        macdonaldmod.LearningMacMod.GlobalChangeLook();
    }

    public void modifyCardPool() {
        logger.info(ID +" acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER)) {

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
        }
        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT)) {
            //For now I just copied the card list for the Blood Lotus, modify it more later to be more unique.

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

package macdonaldmod.relics;

import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.CrackedCore;
import com.megacrit.cardcrawl.relics.SnakeRing;
import macdonaldmod.Orbs.NoxiousOrb;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static basemod.BaseMod.logger;
import static macdonaldmod.LearningMacMod.makeID;

public class NoxiousBattery extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "NoxiousBattery"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    public NoxiousBattery() {
        super(ID, NAME, AbstractCard.CardColor.GREEN, RARITY, SOUND);
    }


    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {

        //this.addToTop(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        getUpdatedDescription();
        this.addToBot(new ChannelAction(new NoxiousOrb()));
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
        //This logic should probably actually be in the event, not the relics but for now.... Oh well.
        if (AbstractDungeon.player.hasRelic(SnakeRing.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(SnakeRing.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        }else if (AbstractDungeon.player.hasRelic(CrackedCore.ID)) {
            for (int i=0; i<AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(CrackedCore.ID)) {
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
        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT) && AbstractDungeon.player.masterMaxOrbs == 0) {
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

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT)) {

            //TODO, I've for now made this the same exact subset that gets unlockeed for the Ironclad + the addition of Claw.
            //TODO, I might want to change that down the line, but heck, this is just in testing right now.

            //Common
            classCards.add(CardLibrary.getCard(BallLightning.ID));
            classCards.add(CardLibrary.getCard(Recursion.ID));
            classCards.add(CardLibrary.getCard((Claw.ID)));

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
            //TODO, I've for now made this the same exact subset that gets unlockeed the infection mutagen
            //change this later I assume.

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

package macdonaldmod.relics;

import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.green.BouncingFlask;
import com.megacrit.cardcrawl.cards.green.Defend_Green;
import com.megacrit.cardcrawl.cards.green.PoisonedStab;
import com.megacrit.cardcrawl.cards.green.Survivor;
import com.megacrit.cardcrawl.cards.red.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.BurningBlood;
import com.megacrit.cardcrawl.relics.CrackedCore;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import macdonaldmod.Orbs.HellfireOrb;
import macdonaldmod.util.CrossCharacterRelicUtility;

import java.util.ArrayList;
import java.util.List;

import static basemod.BaseMod.logger;
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
    public void obtain() {
        if (AbstractDungeon.player.hasRelic(BurningBlood.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(BurningBlood.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        } else if (AbstractDungeon.player.hasRelic(CrackedCore.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
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
        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD) && AbstractDungeon.player.masterMaxOrbs == 0) {
            AbstractDungeon.player.masterMaxOrbs = 2;
        }
    }

    public void ChangeLook() {
        CrossCharacterRelicUtility.GlobalChangeLook();
    }

    public void modifyCardPool() {
        logger.info(ID + " acquired, modifying card pool.");

        ArrayList<AbstractCard> classCards = new ArrayList<>();

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD)) {

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
        } else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT)) {
            //For now, I just copied the card list for the Infection Mutagen, modify it more later to be more unique.

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
        CrossCharacterRelicUtility.ModifyCardPool(classCards);

    }

    public void ModifyDeck() {
        //Moving this here, allow me to only have to modify it directly right here
        List<String> cardIDsToRemove = new ArrayList<>();
        List<AbstractCard> cardsToAdd = new ArrayList<>();

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD)) {
            //Cards to remove
            cardIDsToRemove.add(Bash.ID);
            cardIDsToRemove.add(Strike_Red.ID);
            //Cards to add
            cardsToAdd.add(new Recursion());
            cardsToAdd.add(new BallLightning());

        } else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT)) {
            //Cards to remove
            cardIDsToRemove.add(Zap.ID);
            //Cards to add
            cardsToAdd.add(new Bash());
            //Julie do the thing!
        }

        for (String  r : cardIDsToRemove)
            AbstractDungeon.player.masterDeck.removeCard(r);
        for (AbstractCard a : cardsToAdd)
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(a, (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));

    }


}

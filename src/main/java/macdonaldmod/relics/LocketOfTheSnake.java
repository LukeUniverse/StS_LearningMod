package macdonaldmod.relics;

import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.*;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.cards.tempCards.Miracle;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.PureWater;
import com.megacrit.cardcrawl.relics.SnakeRing;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import macdonaldmod.util.CrossCharacterRelicUtility;

import java.util.ArrayList;
import java.util.List;

import static basemod.BaseMod.logger;
import static macdonaldmod.util.CrossCharacterRelicUtility.GlobalChangeLook;
import static macdonaldmod.LearningMacMod.makeID;

public class LocketOfTheSnake extends BaseRelic implements CrossClassRelicInterface {
    private static final String NAME = "LocketOfTheSnake"; //The name will be used for determining the image file as well as the ID.
    public static final String ID = makeID(NAME); //This adds the mod's prefix to the relic ID, resulting in modID:MyRelic
    private static final RelicTier RARITY = RelicTier.SPECIAL; //The relic's rarity.
    private static final LandingSound SOUND = LandingSound.CLINK; //The sound played when the relic is clicked.

    private boolean powerDiscardedThisTurn = false;
    private boolean skillDiscardedThisTurn = false;
    private boolean attackDiscardedThisTurn = false;

    public LocketOfTheSnake() {
        super(ID, NAME, AbstractCard.CardColor.GREEN, RARITY, SOUND);
    }


    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        super.onUseCard(targetCard, useCardAction);
    }

    public void atBattleStart() {
        powerDiscardedThisTurn = false;
        skillDiscardedThisTurn = false;
        attackDiscardedThisTurn = false;

        getUpdatedDescription();

    }

    public void onManualDiscard() {
        AbstractCard card = AbstractDungeon.player.discardPile.getTopCard();

        if ((card.type == AbstractCard.CardType.POWER) && (!powerDiscardedThisTurn)) {
            this.flash();
            AbstractDungeon.player.hand.addToBottom(new Miracle());
            powerDiscardedThisTurn = true;
        } else if ((card.type == AbstractCard.CardType.SKILL) && (!skillDiscardedThisTurn)) {
            this.flash();
            this.addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, 5));
            skillDiscardedThisTurn = true;
        } else if ((card.type == AbstractCard.CardType.ATTACK) && (!attackDiscardedThisTurn)) {
            this.flash();
            AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(true);
            if (target != null) {
                this.addToTop(new DamageAction(target, new DamageInfo(AbstractDungeon.player, 6, DamageInfo.DamageType.NORMAL)));
            }
            attackDiscardedThisTurn = true;
        }
    }

    public String getUpdatedDescription() {
        return
                this.DESCRIPTIONS[0]
                        + " NL " + DESCRIPTIONS[1]
                        + " NL " + DESCRIPTIONS[2]
                        + " NL " + DESCRIPTIONS[3];
    }


    public void update() {
        super.update();
        getUpdatedDescription();
    }

    @Override
    public void obtain() {
        if (AbstractDungeon.player.hasRelic(SnakeRing.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
                if (AbstractDungeon.player.relics.get(i).relicId.equals(SnakeRing.ID)) {
                    instantObtain(AbstractDungeon.player, i, true);
                    break;
                }
            }
        } else if (AbstractDungeon.player.hasRelic(PureWater.ID)) {
            for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
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

    public void ChangeLook() {
        GlobalChangeLook();
    }

    public void modifyCardPool() {
        logger.info(ID + " acquired, modifying card pool.");

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
        } else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER)) {
            //add some cards here
        }
        CrossCharacterRelicUtility.ModifyCardPool(classCards);
    }

    public void ModifyDeck() {
        //Moving this here, allow me to only have to modify it directly right here
        List<String> cardIDsToRemove = new ArrayList<>();
        List<AbstractCard> cardsToAdd = new ArrayList<>();
        //TODO this def needs changing here, this relic isn't even about stances anymore.
        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT)) {
            //Cards to remove
            cardIDsToRemove.add(Strike_Green.ID);
            cardIDsToRemove.add(Defend_Green.ID);
            //Cards to add
            cardsToAdd.add(new Eruption());
            cardsToAdd.add(new Vigilance());


        } else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER)) {
            //Cards to Remove
            cardIDsToRemove.add(Defend_Watcher.ID);
            //Cards to add
            cardsToAdd.add(new Survivor());
        }

        for (String  r : cardIDsToRemove)
            AbstractDungeon.player.masterDeck.removeCard(r);
        for (AbstractCard a : cardsToAdd)
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(a, (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));

    }

}

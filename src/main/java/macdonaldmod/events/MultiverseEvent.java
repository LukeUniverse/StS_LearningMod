package macdonaldmod.events;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.*;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.Eruption;
import com.megacrit.cardcrawl.cards.purple.FlurryOfBlows;
import com.megacrit.cardcrawl.cards.purple.Vigilance;
import com.megacrit.cardcrawl.cards.red.Bash;
import com.megacrit.cardcrawl.cards.red.Defend_Red;
import com.megacrit.cardcrawl.cards.red.Strike_Red;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CrackedCore;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import macdonaldmod.relics.*;
import macdonaldmod.util.CrossCharacterRelicUtility;

import java.util.ArrayList;
import java.util.List;

import static macdonaldmod.LearningMacMod.makeID;


public class MultiverseEvent extends AbstractImageEvent {


    public static final String NAME = "MultiverseEvent";
    public static final String ID = makeID("MultiverseEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;

    private State state;

    private enum State {
        HERE,
        LEAVING
    }

    private enum TwistColor {
        RED,
        GREEN,
        BLUE,
        PURPLE
    }

    TwistColor twistState;

    public MultiverseEvent() {
        super("Multiverse(WIP)", DESCRIPTIONS[0] + " NL " + DESCRIPTIONS[1] + " NL " + DESCRIPTIONS[2], "macdonaldmod/images/1024/events/MultiverseEvent.png");


        int current_int = GetClassInt();

        int rand_int = current_int;
        //Proceed to loop through this until we have an int that isn't representative of the char we are currently playing.
        while (rand_int == current_int)
            rand_int = AbstractDungeon.miscRng.random(0, 3);

        //Then set our twist state on offer to the appropriate color.
        if (rand_int == 0)
            twistState = TwistColor.RED;
        else if (rand_int == 1)
            twistState = TwistColor.GREEN;
        else if (rand_int == 2)
            twistState = TwistColor.BLUE;
        else if (rand_int == 3)
            twistState = TwistColor.PURPLE;

        SetUp();
    }


    private void SetUp() {

        if (twistState == TwistColor.GREEN) {
            this.body = (DESCRIPTIONS[0] + " NL " + DESCRIPTIONS[1] + " NL " + DESCRIPTIONS[2]);
        } else if (twistState == TwistColor.BLUE) {
            this.body = (DESCRIPTIONS[0] + " NL " + DESCRIPTIONS[3] + " NL " + DESCRIPTIONS[2]);
        } else if (twistState == TwistColor.PURPLE) {
            this.body = (DESCRIPTIONS[0] + " NL " + DESCRIPTIONS[4] + " NL " + DESCRIPTIONS[2]);
        } else if (twistState == TwistColor.RED) { //This one got added late, may want to re-arrange strings. hah
            this.body = (DESCRIPTIONS[0] + " NL " + DESCRIPTIONS[5] + " NL " + DESCRIPTIONS[2]);
        }


        this.state = State.HERE;
        //This is where you would create your dialog options
        this.imageEventText.setDialogOption(OPTIONS[0]); //This adds the option to a list of options
        this.imageEventText.setDialogOption(OPTIONS[1]); //This adds the option to a list of options
    }


    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (state) {
            case HERE: {
                switch (buttonPressed) {
                    case 0:
                        //I want to eventually make this option only selectable if ...
                        //well, only selectable if X, Y, and Z are present depending on your class.
                        imageEventText.updateBodyText(OPTIONS[2]);
                        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;

                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            IroncladMerge();
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            SilentMerge();
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            DefectMerge();
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            WatcherMerge();
                        else
                            break;
                        break;
                    case 1:
                        imageEventText.updateBodyText(OPTIONS[3]);
                        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                        this.imageEventText.clearAllDialogs();
                        break;

                    default:
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[5]);
                        AbstractDungeon.getCurrRoom().phase = RoomPhase.COMPLETE;
                        break;
                }

                this.imageEventText.clearAllDialogs();
                this.imageEventText.setDialogOption("Leave now");
            }
            case LEAVING: {
                this.state = State.LEAVING;
                openMap();
                break;
            }
        }
    }


    private int GetClassInt() {
        int classInt = -1;

        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
            classInt = 0;
        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
            classInt = 1;
        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
            classInt = 2;
        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
            classInt = 3;
        return classInt;
    }


//REGION: Character Merges
    private void IroncladMerge() {
        if (twistState == TwistColor.GREEN) {
            AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
        } else if (twistState == TwistColor.BLUE) {
            AbstractRelic hellFireRelic = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(hellFireRelic);
        } else if (twistState == TwistColor.PURPLE) {
            AbstractRelic bloodRedLotus = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(bloodRedLotus);
        }
    }

    private void SilentMerge() {
        if (twistState == TwistColor.RED) {
            AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(infectionRelic);
        } else if (twistState == TwistColor.BLUE) {
            AbstractRelic noxiousRelic = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(noxiousRelic);
        } else if (twistState == TwistColor.PURPLE) {
            AbstractRelic locket = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(locket);
        }
    }

    //Should I be swapping Duelcast for Recursion here? Or turning Zap into a "Channel 1 [custom] Orb" ?
    private void DefectMerge() {
        if (twistState == TwistColor.RED) {
            AbstractRelic hellfire = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(hellfire);
        } else if (twistState == TwistColor.GREEN) {
            AbstractRelic noxiousBattery = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(noxiousBattery);
        } else if (twistState == TwistColor.PURPLE) {
            AbstractRelic chip = RelicLibrary.getRelic(StanceChip.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(chip);
        }
    }

    private void WatcherMerge() {
        if (twistState == TwistColor.RED) {
            AbstractRelic blood = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(blood);
        } else if (twistState == TwistColor.GREEN) {
            AbstractRelic locket = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(locket);
        } else if (twistState == TwistColor.BLUE) {
            AbstractRelic chip = RelicLibrary.getRelic(StanceChip.ID).makeCopy();
            CrossCharacterRelicUtility.ResolveClassMerge(chip);
        }
    }

//ENDREGION Character Merges
}

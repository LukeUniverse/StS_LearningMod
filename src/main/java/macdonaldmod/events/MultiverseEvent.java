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
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.CrackedCore;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import macdonaldmod.LearningMacMod;
import macdonaldmod.relics.*;

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

    private enum TwistColor{
        RED,
        GREEN,
        BLUE,
        PURPLE
    }
    TwistColor twistState;

    public MultiverseEvent() {
        super("Multiverse(WIP)", DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[1] + " NL "+DESCRIPTIONS[2], "macdonaldmod/images/1024/events/MultiverseEvent.png");


        int current_int =-1;

        if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
            current_int = 0;
        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
            current_int = 1;
        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
            current_int =2;
        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
            current_int =3;

        int rand_int = current_int; //will this work in Java? Guess we'll find out.

        while(rand_int == current_int)
            rand_int = AbstractDungeon.miscRng.random(0,3);

        if(rand_int == 0)
            twistState = TwistColor.RED;
        else if(rand_int == 1)
            twistState = TwistColor.GREEN;
        else if (rand_int == 2)
            twistState = TwistColor.BLUE;
        else if (rand_int == 3)
            twistState = TwistColor.PURPLE;

        SetUp();
    }


    private void SetUp()
    {

        if(twistState == TwistColor.GREEN) {
            this.body = (DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[1] + " NL "+DESCRIPTIONS[2]);
        } else if(twistState == TwistColor.BLUE) {
            this.body = (DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[3] + " NL "+DESCRIPTIONS[2]);
        } else if(twistState == TwistColor.PURPLE) {
            this.body = (DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[4] + " NL "+DESCRIPTIONS[2]);
        } else if (twistState == TwistColor.RED) { //This one got added late, may want to re-arrange strings. hah
            this.body = (DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[5] + " NL "+DESCRIPTIONS[2]);
        }


        this.state = State.HERE;
        //This is where you would create your dialog options
        this.imageEventText.setDialogOption(OPTIONS[0]); //This adds the option to a list of options
        this.imageEventText.setDialogOption(OPTIONS[1]); //This adds the option to a list of options
    }

    public void ResolveClassMerge(AbstractRelic relicToAdd, List<String> cardsToRemove, List<AbstractCard> cardsToAdd)
    {
        //Add New Relic
        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F, relicToAdd);
        //Remove cards
        for (String  r : cardsToRemove) {
            AbstractDungeon.player.masterDeck.removeCard(r);
        }
        //Add cards
        for (AbstractCard a : cardsToAdd) {
            AbstractDungeon.effectsQueue.add(new ShowCardAndObtainEffect(a, (float) Settings.WIDTH / 2.0F, (float) Settings.HEIGHT / 2.0F));

        }
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

                        if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            IroncladMerge();
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            SilentMerge();
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            DefectMerge();
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
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

    private void IroncladMerge(){
        if(twistState == TwistColor.GREEN) {
            //New Relic
            AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Bash.ID);
            cardIDsToRemove.add(Strike_Red.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new BouncingFlask());
            cardsToAdd.add(new PoisonedStab()); //maybe do a  custom poisoned strike instead?
            //Julie do the thing!
            ResolveClassMerge(infectionRelic,cardIDsToRemove,cardsToAdd);
        }
        else if(twistState == TwistColor.BLUE) {
            //New Relic
            AbstractRelic hellFireRelic = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Bash.ID);
            cardIDsToRemove.add(Strike_Red.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new Recursion());
            cardsToAdd.add(new BallLightning());
            //Julie do the thing!
            ResolveClassMerge(hellFireRelic,cardIDsToRemove,cardsToAdd);
        }
        else if(twistState == TwistColor.PURPLE) {
            //New Relic
            AbstractRelic bloodRedLotus = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Bash.ID);
            cardIDsToRemove.add(Defend_Red.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new Eruption());
            cardsToAdd.add(new Vigilance());
            //Julie do the thing!
            ResolveClassMerge(bloodRedLotus,cardIDsToRemove,cardsToAdd);
        }
    }

    private void SilentMerge() {
        if (twistState == TwistColor.RED) {
            //New Relic
            AbstractRelic infectionRelic = RelicLibrary.getRelic(InfectionMutagen.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Defend_Green.ID);
            cardIDsToRemove.add(Survivor.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new Bash());
            //cardsToAdd.add(new PoisonedStab()); //maybe do a  custom poisoned strike instead?
            //Julie do the thing!
            ResolveClassMerge(infectionRelic, cardIDsToRemove, cardsToAdd);
        } else if (twistState == TwistColor.BLUE) {
        //New Relic
        AbstractRelic noxiousRelic = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
        //Cards to remove
        List<String> cardIDsToRemove = new ArrayList<>();
        cardIDsToRemove.add(Defend_Green.ID);
        cardIDsToRemove.add(Strike_Green.ID); //TODO, decide which cards to remove here
        //Cards to add
        List<AbstractCard> cardsToAdd = new ArrayList<>();
        cardsToAdd.add(new Recursion());
        cardsToAdd.add(new BallLightning());
//        //Julie do the thing!
        ResolveClassMerge(noxiousRelic,cardIDsToRemove,cardsToAdd);

        } else if (twistState == TwistColor.PURPLE) {
        //New Relic
        AbstractRelic locket = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
        //Cards to remove
        List<String> cardIDsToRemove = new ArrayList<>();
        cardIDsToRemove.add(Strike_Green.ID);
        cardIDsToRemove.add(Defend_Green.ID);
        //Cards to add
        List<AbstractCard> cardsToAdd = new ArrayList<>();
        cardsToAdd.add(new Eruption());
        cardsToAdd.add(new Vigilance());
        //Julie do the thing!
        ResolveClassMerge(locket,cardIDsToRemove,cardsToAdd);
        }
    }

    private void DefectMerge() //Should I be swapping Duelcast for Recursion here? Or turning Zap into a "Channel 1 [custom] Orb" ?
    {
        if (twistState == TwistColor.RED) {
            //New Relic
            AbstractRelic hellfire = RelicLibrary.getRelic(HellfireBattery.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Zap.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new Bash());
            //cardsToAdd.add(new PoisonedStab()); //maybe do a  custom poisoned strike instead?
            //Julie do the thing!
            ResolveClassMerge(hellfire, cardIDsToRemove, cardsToAdd);
        } else if (twistState == TwistColor.GREEN) {
            //New Relic
            AbstractRelic noxiousBattery = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Zap.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new DeadlyPoison());
//        //Julie do the thing!
            ResolveClassMerge(noxiousBattery,cardIDsToRemove,cardsToAdd);

        } else if (twistState == TwistColor.PURPLE) {
            //TODO does not exist yet, hah

              AbstractRelic locket = RelicLibrary.getRelic(CrackedCore.ID).makeCopy();
            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();
            cardIDsToRemove.add(Strike_Blue.ID);
            cardIDsToRemove.add(Defend_Blue.ID);
            cardIDsToRemove.add(Zap.ID);
            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();
            cardsToAdd.add(new Eruption());
            cardsToAdd.add(new Vigilance());
            cardsToAdd.add(new FlurryOfBlows());
            //Julie do the thing!
            ResolveClassMerge(locket,cardIDsToRemove,cardsToAdd);
        }
    }

    private void WatcherMerge()
    {
        if (twistState == TwistColor.RED) { //TODO does this even need deck changes?
            //New Relic
            AbstractRelic blood = RelicLibrary.getRelic(BloodLotus.ID).makeCopy();

            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();

            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();

            //Julie do the thing!
            ResolveClassMerge(blood, cardIDsToRemove, cardsToAdd);
        } else if (twistState == TwistColor.GREEN) { //TODO does this even need deck changes?
            //New Relic
            AbstractRelic noxiousBattery = RelicLibrary.getRelic(NoxiousBattery.ID).makeCopy();

            //Cards to remove
            List<String> cardIDsToRemove = new ArrayList<>();

            //Cards to add
            List<AbstractCard> cardsToAdd = new ArrayList<>();

            ResolveClassMerge(noxiousBattery,cardIDsToRemove,cardsToAdd);
        } else if (twistState == TwistColor.BLUE) {
            //TODO does not exist yet, hah
            //AbstractRelic locket = RelicLibrary.getRelic(LocketOfTheSnake.ID).makeCopy();
//            //Cards to remove
//            List<String> cardIDsToRemove = new ArrayList<>();
//            cardIDsToRemove.add(Strike_Green.ID);
//            cardIDsToRemove.add(Defend_Green.ID);
//            //Cards to add
//            List<AbstractCard> cardsToAdd = new ArrayList<>();
//            cardsToAdd.add(new Eruption());
//            cardsToAdd.add(new Vigilance());
//            //Julie do the thing!
//            ResolveClassMerge(locket,cardIDsToRemove,cardsToAdd);
        }
    }

}

package macdonaldmod.events;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.BallLightning;
import com.megacrit.cardcrawl.cards.blue.Dualcast;
import com.megacrit.cardcrawl.cards.blue.Recursion;
import com.megacrit.cardcrawl.cards.green.BouncingFlask;
import com.megacrit.cardcrawl.cards.green.PoisonedStab;
import com.megacrit.cardcrawl.cards.purple.Eruption;
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
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import macdonaldmod.LearningMacMod;
import macdonaldmod.cards.PoisonedStrike;
import macdonaldmod.relics.BloodRedLotus;
import macdonaldmod.relics.InfectionMutagen;
import macdonaldmod.relics.HellfireBattery;

import java.util.ArrayList;
import java.util.List;

import static macdonaldmod.LearningMacMod.makeID;


public class MultiverseEvent extends AbstractImageEvent {

    //TODO: The leave button isn't working x.x (did I ever fix this?)

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
        GREEN,
        BLUE,
        PURPLE
    }
    TwistColor twistState;

    public MultiverseEvent() {
        super("Multiverse(WIP)", DESCRIPTIONS[0] +" NL "+DESCRIPTIONS[1] + " NL "+DESCRIPTIONS[2], "macdonaldmod/images/1024/events/MultiverseEvent.png");

        int rand_int = AbstractDungeon.miscRng.random(0,2);

        if(rand_int == 0)
            twistState = TwistColor.GREEN;
        else if (rand_int == 1)
            twistState = TwistColor.BLUE;
        else if (rand_int == 2)
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
        }


        this.state = State.HERE;
        LearningMacMod.logger.warn("Multiverse event spawn");

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
                        //I want to eventually make this option only selectable if you have Bash, a strike left, and burning blood.
                        //well, only selectable if X, Y, and Z are present depending on your class.
                        imageEventText.updateBodyText(OPTIONS[2]);
                        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;

                        if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            IroncladMerge();
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            SilentMerge(); // Event spawning for the IronClad..., so this should never be hit for now.
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            DefectMerge(); //see above
                        else if(AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            WatcherMerge(); //see above
                        else  //see above
                            break;
                        break;
                    case 1:
                        imageEventText.updateBodyText(OPTIONS[3]);
                        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
                        //Double down here...
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
            AbstractRelic bloodRedLotus = RelicLibrary.getRelic(BloodRedLotus.ID).makeCopy();
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

    private void SilentMerge()
    {
        //TODO
    }

    private void DefectMerge()
    {
        //TODO
    }

    private void WatcherMerge()
    {
        //TODO
    }

}
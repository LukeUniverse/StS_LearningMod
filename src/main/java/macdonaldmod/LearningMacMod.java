package macdonaldmod;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.eventUtil.AddEventParams;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.spine.AnimationState;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import macdonaldmod.cards.BaseCard;
import macdonaldmod.events.MultiverseEvent;
import macdonaldmod.relics.*;
import macdonaldmod.util.GeneralUtils;
import macdonaldmod.util.KeywordInfo;
import macdonaldmod.util.TextureLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static macdonaldmod.Patches.AbstractCardEnum.MACDONALDYELLOW;

@SpireInitializer
public class LearningMacMod implements
        EditCardsSubscriber,
        EditRelicsSubscriber,
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber {


    public static ModInfo info;
    public static String modID; //Edit your pom.xml to change this
    static { loadModInfo(); }
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.
    private static final String resourcesFolder = "macdonaldmod";

    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new LearningMacMod();

        }

    public LearningMacMod() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");

        //Okay, I've grabbed the actual pngs, so let's try this now...

        BaseMod.addColor(MACDONALDYELLOW,
                Color.YELLOW,
                cards512Path("bg_attack_red.png"), cards512Path("bg_skill_red.png"),
                cards512Path("bg_power_red.png"), cards512Path("card_yellow_orb.png"),
                cards1024Path("bg_attack_red.png"), cards1024Path("bg_skill_red.png"),
                cards1024Path("bg_power_red.png"), cards512Path("card_yellow_orb.png"),//TODO make a large version of this art?
                cards512Path("card_yellow_orb.png")); //What is this final orb path for???  Hmm, maybe the tiny art one?


    }

    @Override
    public void receivePostInitialize() {
        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);

        //Seems funky, but I think the bast way to handle this is just creating an instance of my event for each class...
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.IRONCLAD).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.THE_SILENT).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.DEFECT).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.WATCHER).create());
    }

    @Override
    public void receiveEditCards() {
        new AutoAdd(modID) //Loads files from this mod
                .packageFilter(BaseCard.class) //In the same package as this class
                .setDefaultSeen(true) //And marks them as seen in the compendium
                .cards(); //Adds the cards
    }

    @Override
    public void receiveEditRelics(){
        new AutoAdd(modID) //Loads files from this mod
                .packageFilter(BaseRelic.class) //In the same package as this class
                .any(BaseRelic.class, (info, relic) -> { //Run this code for any classes that extend this class
                    if (relic.pool != null)
                        BaseMod.addRelicToCustomPool(relic, relic.pool); //Register a custom character specific relic
                    else
                        BaseMod.addRelic(relic, relic.relicType); //Register a shared or base game character specific relic

                    //If the class is annotated with @AutoAdd.Seen, it will be marked as seen, making it visible in the relic library.
                    //If you want all your relics to be visible by default, just remove this if statement.
                    if (info.seen)
                        UnlockTracker.markRelicAsSeen(relic.relicId);
                });
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }
    private static final String defaultLanguage = "eng";

    public static final Map<String, KeywordInfo> keywords = new HashMap<>();

    //this probably shouldn't be here, hah.
    public static void GlobalChangeLook()
    {
        try {
            //I don't think this needs to be any different for any of the classes...
            Method loadAnimationMethod = AbstractCreature.class.getDeclaredMethod("loadAnimation", String.class, String.class, Float.TYPE);
            loadAnimationMethod.setAccessible(true);
            loadAnimationMethod.invoke(AbstractDungeon.player, AlternateLookPath("skeleton.atlas"), AlternateLookPath("skeleton.json"), 1.0F);
            AnimationState.TrackEntry e = AbstractDungeon.player.state.setAnimation(0, "Idle", true);
            e.setTimeScale(0.6F);

            // loadEyeAnimation for the watcher???;
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public static boolean ActuallyChangeStance = false;

    public static String AlternateLookPath(String fileName) {
        String rv = "images/characters/ironclad"; //Default location for default Ironclad look.
        //TODO^^Make that the default string for what ever character you are current playing, lol
        /*
        user "Melt" from discord said:
        oh right should probably mention this
        if you're just calling loadAnimation again
        you'll want to dispose the player's atlas first
        So this is probably a TODO
         */
        if(AbstractDungeon.player != null) {
            for (AbstractRelic r : AbstractDungeon.player.relics) {
                if (r instanceof CrossClassRelicInterface) {
                    if (r.relicId.equals(InfectionMutagen.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/greenpants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Red/" + fileName;
                        break;

                    } else if (r.relicId.equals(HellfireBattery.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/bluepants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Red/" + fileName;
                        break;
                    } else if (r.relicId.equals(BloodLotus.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.IRONCLAD))
                            rv = "macdonaldmod/images/characters/purplepants/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Red/" + fileName;
                        break;
                    }
                    else if (r.relicId.equals(NoxiousBattery.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Blue/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Green/" + fileName;
                        break;

                    }
                    else if (r.relicId.equals(LocketOfTheSnake.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.THE_SILENT))
                            rv = "macdonaldmod/images/characters/Silent/Purple/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Green/" + fileName;
                        break;
                    }
                    else if (r.relicId.equals(StanceChip.ID)) {
                        if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.DEFECT))
                            rv = "macdonaldmod/images/characters/Defect/Purple/" + fileName;
                        else if (AbstractDungeon.player.chosenClass.equals(AbstractPlayer.PlayerClass.WATCHER))
                            rv = "macdonaldmod/images/characters/Watcher/Blue/" + fileName;
                        break;
                    }
                }
            }
        }

        return rv;
    }

    @Override
    public void receiveEditStrings() {
        /*
            First, load the default localization.
            Then, if the current language is different, attempt to load localization for that language.
            This results in the default localization being used for anything that might be missing.
            The same process is used to load keywords slightly below.
        */
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    @Override
    public void receiveEditKeywords()
    {
        Gson gson = new Gson();
        String json = Gdx.files.internal(localizationPath(defaultLanguage, "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);
        for (KeywordInfo keyword : keywords) {
            keyword.prep();
            registerKeyword(keyword);
        }

        if (!defaultLanguage.equals(getLangString())) {
            try
            {
                json = Gdx.files.internal(localizationPath(getLangString(), "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
                keywords = gson.fromJson(json, KeywordInfo[].class);
                for (KeywordInfo keyword : keywords) {
                    keyword.prep();
                    registerKeyword(keyword);
                }
            }
            catch (Exception e)
            {
                logger.warn(modID + " does not support " + getLangString() + " keywords.");
            }
        }
    }

    private void registerKeyword(KeywordInfo info) {
        BaseMod.addKeyword(modID.toLowerCase(), info.PROPER_NAME, info.NAMES, info.DESCRIPTION);
        if (!info.ID.isEmpty())
        {
            keywords.put(info.ID, info);
        }
    }

    //These methods are used to generate the correct filepaths to various parts of the resources folder.
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }
    public static String characterPath(String file) {
        return resourcesFolder + "/images/character/" + file;
    }
    public static String powerPath(String file) {
        return resourcesFolder + "/images/powers/" + file;
    }
    public static String relicPath(String file) {
        return resourcesFolder + "/images/relics/" + file;
    }

    public static String orbsPath(String file) {
        return resourcesFolder + "/images/orbs/" + file;
    }
    public static String cards512Path(String file) {
        return resourcesFolder + "/images/512/cards/" + file;
    }
    public static String cards1024Path(String file) {
        return resourcesFolder + "/images/1024/cards/" + file;
    }


    //This determines the mod's ID based on information stored by ModTheSpire.
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(LearningMacMod.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}

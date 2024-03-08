package macdonaldmod;

import basemod.*;
import basemod.eventUtil.AddEventParams;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.helpers.FontHelper;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

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
    public static final String _multiverseEventEnabled = "MacdonaldModEventEnabled";
    public static final String _macdonaldModOverrideIronclad = "MacdonaldModOverrideIronclad";
    public static final String _macdonaldModOverrideSilent = "MacdonaldModOverrideSilent";
    public static final String _macdonaldModOverrideDefect = "MacdonaldModOverrideDefect";
    public static final String _macdonaldModOverrideWatcher = "MacdonaldModOverrideWatcher";

    private ModPanel settingsPanel;

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

        ReadyModPanel();


        //The information used is taken from your pom.xml file.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, settingsPanel);

        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.IRONCLAD).spawnCondition(() -> config.getBool(_multiverseEventEnabled)).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.THE_SILENT).spawnCondition(() -> config.getBool(_multiverseEventEnabled)).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.DEFECT).spawnCondition(() -> config.getBool(_multiverseEventEnabled)).create());
        BaseMod.addEvent(new AddEventParams.Builder(MultiverseEvent.ID, MultiverseEvent.class).dungeonID(Exordium.ID).playerClass(AbstractPlayer.PlayerClass.WATCHER).spawnCondition(() -> config.getBool(_multiverseEventEnabled)).create());
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

    //Can't these just be declared in the method, why are they here?
    /*
    private final ArrayList<ModColorDisplay> ironcladButtons = new ArrayList<ModColorDisplay>();
    private final ArrayList<ModColorDisplay> silentButtons = new ArrayList<ModColorDisplay>();
    private final ArrayList<ModColorDisplay> defectButtons = new ArrayList<ModColorDisplay>();
    private final ArrayList<ModColorDisplay> watcherButtons = new ArrayList<ModColorDisplay>();
*/

    Color _colorRed = new Color(171f,0f,0f,1f); //Red
    Color _colorPurple = new Color(171f,0f,196f,1f);//Purple
    Color _colorBlue = new Color(0f,117f,255f,1f); //Blue
    Color _colorGreen = new Color(0f,164f,0f,1f); //Green
    private float xPos = 350f, yPos = 700f;//, orgYPos = 750f; not sure what this one was for?
    public static SpireConfig config;

    private void ReadyModPanel(){

        try {
            config = new SpireConfig("MacdonaldMod", "config");

            if (!config.has(_multiverseEventEnabled))
                config.setBool(_multiverseEventEnabled, true);
            if (!config.has(_macdonaldModOverrideIronclad))
                config.setString(_macdonaldModOverrideIronclad, "no");
            if (!config.has(_macdonaldModOverrideSilent))
                config.setString(_macdonaldModOverrideSilent, "no");
            if (!config.has(_macdonaldModOverrideDefect))
                config.setString(_macdonaldModOverrideDefect, "no");
            if (!config.has(_macdonaldModOverrideWatcher))
                config.setString(_macdonaldModOverrideWatcher, "no");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Not currently using this, maybe just get rid of it?
        UIStrings configStrings = CardCrawlGame.languagePack.getUIString(makeID("ConfigMenuText"));

        Texture colorButton = new Texture(resourcesFolder + "/images/colorButton.png");
        Texture colorButtonOutline = new Texture(resourcesFolder + "/images/colorButtonOutline.png");

        settingsPanel = new ModPanel();

        //Set up the mod information displayed in the in-game mods menu.



        ModLabeledToggleButton b = new ModLabeledToggleButton("Include event?", xPos, yPos, Settings.CREAM_COLOR, FontHelper.charDescFont, config.getBool(_multiverseEventEnabled), settingsPanel, l -> {
        },  modToggleButton -> {
            config.setBool(_multiverseEventEnabled, modToggleButton.enabled);
            saveConfig();
        });

        settingsPanel.addUIElement(b);
        //let's see if this changes the color correctly...
        settingsPanel.addUIElement(new ModLabel("Ironclad Twist Color:", xPos, yPos-50, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, update->{}));

        //There is probably a cleaner way to handle this, but for now this is what I'm doing.

//REGION: Ironclad Buttons
        List<Color> ironcladRemoveColors = new ArrayList<>();
        ironcladRemoveColors.add(_colorGreen); ironcladRemoveColors.add(_colorBlue); ironcladRemoveColors.add(_colorPurple);
        List<ModColorDisplay> ironcladRemoveColorsButtons = new ArrayList<ModColorDisplay>();
        //This sets up the 'Click' actions
        Consumer<ModColorDisplay> handleRemoveClick = getCharModColorDisplayConsumer(ironcladRemoveColorsButtons, _macdonaldModOverrideIronclad);

        //Then *actually* create the buttons.
        for (int i = 0; i < ironcladRemoveColors.size(); i++) {

            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + i * 84f, yPos - (10f * Settings.scale) -100, 0f, colorButton, colorButtonOutline, handleRemoveClick);
            Color color = ironcladRemoveColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            modColorDisplay.a = color.a;

            //this should handle getting the existing setting, and then well, making it selected
            ReflectExistingTwistSetting(color, modColorDisplay, config.getString(_macdonaldModOverrideIronclad));

            ironcladRemoveColorsButtons.add(modColorDisplay);
            //ironcladButtons.add(modColorDisplay);
            settingsPanel.addUIElement(modColorDisplay);
        }
//END_REGION: Ironclad Buttons.

//REGION: Silent Buttons
        //TODO, yeah, eh make sure measurements are right
        yPos = yPos - 100;
        settingsPanel.addUIElement(new ModLabel("Silent Twist Color:", xPos, yPos-50,Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, update->{}));

        //Beginning of Silent section:

        List<Color> silentRemoveColors = new ArrayList<>();
        silentRemoveColors.add(_colorRed); silentRemoveColors.add(_colorBlue); silentRemoveColors.add(_colorPurple);
        List<ModColorDisplay> silentRemoveColorsButtons = new ArrayList<ModColorDisplay>();
        //This sets up the 'Click' actions
        Consumer<ModColorDisplay> silentHandleRemoveClick = getCharModColorDisplayConsumer(silentRemoveColorsButtons, _macdonaldModOverrideSilent);

        //Then *actually* create the buttons.
        for (int i = 0; i < silentRemoveColors.size(); i++) {
            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + i * 84f, yPos - (10f * Settings.scale) -100, 0f, colorButton, colorButtonOutline, silentHandleRemoveClick);
            Color color = silentRemoveColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            modColorDisplay.a = color.a;

            //this should handle getting the existing setting, and then well, making it selected
            ReflectExistingTwistSetting(color, modColorDisplay, config.getString(_macdonaldModOverrideSilent));

            silentRemoveColorsButtons.add(modColorDisplay);
            //wait is this below actually doing... anything?
            //silentButtons.add(modColorDisplay);
            settingsPanel.addUIElement(modColorDisplay);
        }
//END_REGION: Silent Buttons.

//REGION: Defect Buttons
        yPos = yPos - 100;
        settingsPanel.addUIElement(new ModLabel("Defect Twist Color:", xPos, yPos-50,Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, update->{}));

        //Beginning of Silent section:

        List<Color> defectRemoveColors = new ArrayList<>();
        defectRemoveColors.add(_colorRed); defectRemoveColors.add(_colorGreen); defectRemoveColors.add(_colorPurple);
        List<ModColorDisplay> defectRemoveColorsButtons = new ArrayList<ModColorDisplay>();
        //This sets up the 'Click' actions
        Consumer<ModColorDisplay> defectHandleRemoveClick = getCharModColorDisplayConsumer(defectRemoveColorsButtons, _macdonaldModOverrideDefect);

        //Then *actually* create the buttons.
        for (int i = 0; i < defectRemoveColors.size(); i++) {
            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + i * 84f, yPos - (10f * Settings.scale) -100, 0f, colorButton, colorButtonOutline, defectHandleRemoveClick);
            Color color = defectRemoveColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            modColorDisplay.a = color.a;

            //this should handle getting the existing setting, and then well, making it selected
            ReflectExistingTwistSetting(color, modColorDisplay, config.getString(_macdonaldModOverrideDefect));

            defectRemoveColorsButtons.add(modColorDisplay);
            //wait, is this below actually doing... Anything?
            //defectButtons.add(modColorDisplay);
            settingsPanel.addUIElement(modColorDisplay);
        }
//END_REGION: defect Buttons.

//REGION: Watcher Buttons
        yPos = yPos - 100;
        settingsPanel.addUIElement(new ModLabel("Watcher Twist Color:", xPos, yPos-50,Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, update->{}));

        //Beginning of Silent section:

        List<Color> watcherRemoveColors = new ArrayList<>();
        watcherRemoveColors.add(_colorRed); watcherRemoveColors.add(_colorGreen); watcherRemoveColors.add(_colorBlue);
        List<ModColorDisplay> watcherRemoveColorsButtons = new ArrayList<ModColorDisplay>();
        //This sets up the 'Click' actions
        Consumer<ModColorDisplay> watcherHandleRemoveClick = getCharModColorDisplayConsumer(watcherRemoveColorsButtons, _macdonaldModOverrideWatcher);

        //Then *actually* create the buttons.
        for (int i = 0; i < watcherRemoveColors.size(); i++) {
            ModColorDisplay modColorDisplay = new ModColorDisplay(xPos + i * 84f, yPos - (10f * Settings.scale) -100, 0f, colorButton, colorButtonOutline, watcherHandleRemoveClick);
            Color color = watcherRemoveColors.get(i);
            modColorDisplay.r = color.r;
            modColorDisplay.g = color.g;
            modColorDisplay.b = color.b;
            modColorDisplay.a = color.a;

            //this should handle getting the existing setting, and then well, making it selected
            ReflectExistingTwistSetting(color, modColorDisplay, config.getString(_macdonaldModOverrideWatcher));

            defectRemoveColorsButtons.add(modColorDisplay);
            //wait, is this below actually doing... Anything?
            //watcherButtons.add(modColorDisplay);
            settingsPanel.addUIElement(modColorDisplay);
        }
//END_REGION: Watcher Buttons.

    }

    //I believe with these changes, it can handle any color of character required now...
    private Consumer<ModColorDisplay> getCharModColorDisplayConsumer(List<ModColorDisplay> charRemoveColorsButtons, String charSetting) {
        Consumer<ModColorDisplay> handleRemoveClick;
        handleRemoveClick = modColorDisplay -> {
            charRemoveColorsButtons.forEach(m -> {
                m.rOutline = Color.BLACK.r;
                m.gOutline = Color.BLACK.g;
                m.bOutline = Color.BLACK.b;
            });
            String val = config.getString(charSetting);

            if(modColorDisplay.r == _colorPurple.r &&
                    modColorDisplay.g == _colorPurple.g &&
                    modColorDisplay.b == _colorPurple.b)
            {
                if(val.equals("Purple")) { //IF we already have purple selected, we want to remove our selection and break out
                    config.setString(charSetting, "NO");
                    modColorDisplay.rOutline = Color.BLACK.r;
                    modColorDisplay.gOutline = Color.BLACK.g;
                    modColorDisplay.bOutline = Color.BLACK.b;
                    saveConfig();
                    return;
                }
                config.setString(charSetting,"Purple");
                modColorDisplay.rOutline = Color.WHITE.r;
                modColorDisplay.gOutline = Color.WHITE.g;
                modColorDisplay.bOutline = Color.WHITE.b;
            } else if(modColorDisplay.r == _colorBlue.r &&
                    modColorDisplay.g == _colorBlue.g &&
                    modColorDisplay.b == _colorBlue.b)
            {
                if(val.equals("Blue")) { //IF we already have Blue selected, we want to remove our selection and break out
                    config.setString(charSetting, "NO");
                    modColorDisplay.rOutline = Color.BLACK.r;
                    modColorDisplay.gOutline = Color.BLACK.g;
                    modColorDisplay.bOutline = Color.BLACK.b;
                    saveConfig();
                    return;
                }
                config.setString(charSetting,"Blue");

                modColorDisplay.rOutline = Color.WHITE.r;
                modColorDisplay.gOutline = Color.WHITE.g;
                modColorDisplay.bOutline = Color.WHITE.b;
            } else if(modColorDisplay.r == _colorGreen.r &&
                    modColorDisplay.g == _colorGreen.g &&
                    modColorDisplay.b == _colorGreen.b)
            {
                if(val.equals("Green")) {
                    config.setString(charSetting, "NO");
                    modColorDisplay.rOutline = Color.BLACK.r;
                    modColorDisplay.gOutline = Color.BLACK.g;
                    modColorDisplay.bOutline = Color.BLACK.b;
                    saveConfig();
                    return;
                }
                config.setString(charSetting,"Green");

                modColorDisplay.rOutline = Color.WHITE.r;
                modColorDisplay.gOutline = Color.WHITE.g;
                modColorDisplay.bOutline = Color.WHITE.b;
            }
        else if(modColorDisplay.r == _colorRed.r &&
                modColorDisplay.g == _colorRed.g &&
                modColorDisplay.b == _colorRed.b)
        {
            if(val.equals("Red")) {
                config.setString(charSetting, "NO");
                modColorDisplay.rOutline = Color.BLACK.r;
                modColorDisplay.gOutline = Color.BLACK.g;
                modColorDisplay.bOutline = Color.BLACK.b;
                saveConfig();
                return;
            }
            config.setString(charSetting,"Red");

            modColorDisplay.rOutline = Color.WHITE.r;
            modColorDisplay.gOutline = Color.WHITE.g;
            modColorDisplay.bOutline = Color.WHITE.b;
        }
            saveConfig();
        };
        return handleRemoveClick;
    }

    private void ReflectExistingTwistSetting(Color color, ModColorDisplay modColorDisplay, String twistColor)
    {
        if(twistColor.equals("Blue") && ColorsEqual(color, _colorBlue))
        {
            modColorDisplay.rOutline = Color.WHITE.r;
            modColorDisplay.gOutline = Color.WHITE.g;
            modColorDisplay.bOutline = Color.WHITE.b;
        }
        else if(twistColor.equals("Green") && ColorsEqual(color, _colorGreen))
        {
            modColorDisplay.rOutline = Color.WHITE.r;
            modColorDisplay.gOutline = Color.WHITE.g;
            modColorDisplay.bOutline = Color.WHITE.b;
        }
        else if(twistColor.equals("Purple") && ColorsEqual(color, _colorPurple))
        {
            modColorDisplay.rOutline = Color.WHITE.r;
            modColorDisplay.gOutline = Color.WHITE.g;
            modColorDisplay.bOutline = Color.WHITE.b;
        }
        else if(twistColor.equals("Red") && ColorsEqual(color, _colorRed))
        {
            modColorDisplay.rOutline = Color.WHITE.r;
            modColorDisplay.gOutline = Color.WHITE.g;
            modColorDisplay.bOutline = Color.WHITE.b;
        }
    }

    private Boolean ColorsEqual(Color color1, Color color2)
    {
        return (color1.r == color2.r && color1.g == color2.g && color1.b == color2.b);
    }

    private void saveConfig() {
        try {
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

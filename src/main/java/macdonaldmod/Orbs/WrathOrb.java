package macdonaldmod.Orbs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.watcher.ChangeStanceAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import macdonaldmod.cards.EvokeStanceOrb;
import macdonaldmod.util.CrossCharacterRelicUtility;
import macdonaldmod.util.TextureLoader;
import macdonaldmod.util.WrathOrbPassiveEffect;

import static macdonaldmod.LearningMacMod.*;

//Copied a lot of this code from, well, elsewhere. This is VERY MUCH a WIP.

public class WrathOrb extends AbstractOrb{
    private float vfxTimer = 1.0f;
    private float vfxIntervalMin = 0.1f;
    private float vfxIntervalMax = 0.4f;
    private static final float ORB_WAVY_DIST = 0.04f;
    private static final float PI_4 = 12.566371f;
    public static String NAME = "Wrath";
    public static final String ORB_ID = makeID(NAME);
    private static final OrbStrings orbString = CardCrawlGame.languagePack.getOrbString(ORB_ID);
    public static Color color = Color.FIREBRICK.cpy();
    public static Color color2 = Color.ORANGE.cpy();

    public WrathOrb() {
        this.ID = ORB_ID;
        img = TextureLoader.getTexture(orbsPath("Wrath.png"));
        this.name = NAME;
        baseEvokeAmount = 1;
        this.basePassiveAmount = 2;
        this.passiveAmount = this.basePassiveAmount;
        this.updateDescription();
        this.angle = MathUtils.random(360.0F);
        this.channelAnimTimer = 0.5F;
        scale = 1.5F;

        //we want to trigger the orb when you first channel it as well
        //which is what this is doing here
        onStartOfTurn();
    }


    public void updateDescription() {
        this.applyFocus();
        this.description = orbString.DESCRIPTION[0]+" "+ this.passiveAmount+" " + orbString.DESCRIPTION[1] + orbString.DESCRIPTION[2] +" "+this.evokeAmount+" "+orbString.DESCRIPTION[3];
    }

    public void onEvoke() {

        CrossCharacterRelicUtility.ActuallyChangeStance = true;
        AbstractDungeon.actionManager.addToBottom(new ChangeStanceAction("Wrath"));
    }

    @Override
    public void onStartOfTurn() {
        AbstractDungeon.player.hand.addToBottom(new EvokeStanceOrb());
    }

    public void triggerEvokeAnimation() {
        CardCrawlGame.sound.play("POWER_MANTRA", 0.05F);
    }

    public void updateAnimation() {
        super.updateAnimation();
        angle += Gdx.graphics.getDeltaTime() * 45.0f;
        vfxTimer -= Gdx.graphics.getDeltaTime();
        if (vfxTimer < 0.0f) {
            AbstractDungeon.effectList.add(new WrathOrbPassiveEffect(cX, cY)); // This is the purple-sparkles in the orb. You can change this to whatever fits your orb.
            vfxTimer = MathUtils.random(vfxIntervalMin, vfxIntervalMax);
        }

    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setColor(new Color(1.0f, 1.0f, 1.0f, c.a / 2.0f));
        sb.draw(img, cX - 48.0f, cY - 48.0f + bobEffect.y, 48.0f, 48.0f, 96.0f, 96.0f, scale + MathUtils.sin(angle / PI_4) * ORB_WAVY_DIST * Settings.scale, scale, angle, 0, 0, 96, 96, false, false);
        sb.setColor(new Color(1.0f, 1.0f, 1.0f, this.c.a / 2.0f));
        sb.setBlendFunction(770, 1);
        sb.draw(img, cX - 48.0f, cY - 48.0f + bobEffect.y, 48.0f, 48.0f, 96.0f, 96.0f, scale, scale + MathUtils.sin(angle / PI_4) * ORB_WAVY_DIST * Settings.scale, -angle, 0, 0, 96, 96, false, false);
        sb.setBlendFunction(770, 771);
        renderText(sb);
        hb.render(sb);
    }

    public void playChannelSFX() {
        CardCrawlGame.sound.play("POWER_MANTRA", 0.05F);
    }

    public AbstractOrb makeCopy() {
        return new WrathOrb();
    }

}

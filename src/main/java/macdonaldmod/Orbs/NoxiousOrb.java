package macdonaldmod.Orbs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.defect.LightningOrbEvokeAction;
import com.megacrit.cardcrawl.actions.defect.LightningOrbPassiveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.*;


import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import macdonaldmod.util.HellfireOrbPassiveEffect;
import macdonaldmod.util.TextureLoader;

import java.nio.file.attribute.PosixFileAttributes;

import static macdonaldmod.LearningMacMod.makeID;
import static macdonaldmod.LearningMacMod.orbsPath;

public class NoxiousOrb extends AbstractOrb {

    private float vfxTimer = 1.0f;
    private float vfxIntervalMin = 0.1f;
    private float vfxIntervalMax = 0.4f;
    private static final float ORB_WAVY_DIST = 0.04f;
    private static final float PI_4 = 12.566371f;
    public static String NAME = "Noxious";
    public static final String ORB_ID = makeID(NAME);
    private static final OrbStrings orbString = CardCrawlGame.languagePack.getOrbString(ORB_ID);
    public static com.badlogic.gdx.graphics.Color color = Color.GREEN.cpy();
    public static com.badlogic.gdx.graphics.Color color2 = Color.YELLOW.cpy(); //TODO maybe different color, I don't know

    public NoxiousOrb() {
        this.ID = ORB_ID;
        img = TextureLoader.getTexture(orbsPath("HellfireOrb.png")); //Change this eventually
        this.name = NAME;
        baseEvokeAmount = 5;
        this.basePassiveAmount = 2;
        this.passiveAmount = this.basePassiveAmount;
        this.updateDescription();
        this.angle = MathUtils.random(360.0F);
        this.channelAnimTimer = 0.5F;
        scale = 1.5F;

    }

    public void updateDescription() {
        this.applyFocus();
        this.description = orbString.DESCRIPTION[0]+" "+ this.passiveAmount+" " + orbString.DESCRIPTION[1] + orbString.DESCRIPTION[2] +" "+this.evokeAmount+" "+orbString.DESCRIPTION[3];
    }

    public void onEndOfTurn() {
//        if (AbstractDungeon.player.hasPower("Electro")) {
//            float speedTime = 0.2F / (float)AbstractDungeon.player.orbs.size();
//            if (Settings.FAST_MODE) {
//                speedTime = 0.0F;
//            }
//
//            AbstractDungeon.actionManager.addToBottom(new VFXAction(new OrbFlareEffect(this, OrbFlareEffect.OrbFlareColor.LIGHTNING), speedTime));
//            AbstractDungeon.actionManager.addToBottom(new LightningOrbEvokeAction(new DamageInfo(AbstractDungeon.player, this.passiveAmount, DamageInfo.DamageType.THORNS), true));
//        } else {
//            AbstractDungeon.actionManager.addToBottom(new LightningOrbPassiveAction(new DamageInfo(AbstractDungeon.player, this.passiveAmount, DamageInfo.DamageType.THORNS), this, false));

//    }
        //^^ Original end of turn for Lightning Orb.


        AbstractMonster randomMonster = AbstractDungeon.getMonsters().getRandomMonster((AbstractMonster)null, true, AbstractDungeon.cardRandomRng);
        if (randomMonster != null) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(randomMonster, AbstractDungeon.player, new PoisonPower(randomMonster, AbstractDungeon.player, passiveAmount)));
        }
    }

    public void onEvoke() {

        AbstractMonster randomMonster = AbstractDungeon.getMonsters().getRandomMonster((AbstractMonster)null, true, AbstractDungeon.cardRandomRng);
        if (randomMonster != null) {
            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(randomMonster, AbstractDungeon.player, new PoisonPower(randomMonster, AbstractDungeon.player, this.evokeAmount)));
        }
    }

    public void triggerEvokeAnimation() {
        CardCrawlGame.sound.play("POWER_MANTRA", 0.05F);
    }

    public void updateAnimation() {
        super.updateAnimation();
        angle += Gdx.graphics.getDeltaTime() * 45.0f;
        vfxTimer -= Gdx.graphics.getDeltaTime();
        if (vfxTimer < 0.0f) {
            AbstractDungeon.effectList.add(new HellfireOrbPassiveEffect(cX, cY)); // TODO I've got to change this, obviously...
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
        return new NoxiousOrb();
    }
}

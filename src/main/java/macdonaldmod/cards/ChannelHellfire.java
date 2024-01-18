package macdonaldmod.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.Lightning;
import com.megacrit.cardcrawl.powers.PoisonPower;
import macdonaldmod.Orbs.HellfireOrb;
import macdonaldmod.util.CardStats;

public class ChannelHellfire extends BaseCard{

    //Custom Cards are not yet being used, so are a WIP.

    public static final String ID = makeID("Channel Hellfire");
    private static final CardStats info = new CardStats(
            CardColor.BLUE, //Eventually change this to be its own Red/Blue color
            CardType.SKILL, //The type. ATTACK/SKILL/POWER/CURSE/STATUS
            CardRarity.UNCOMMON, //Rarity. BASIC is for starting cards, then there's COMMON/UNCOMMON/RARE, and then SPECIAL and CURSE. SPECIAL is for cards you only get from events. Curse is for curses, except for special curses like Curse of the Bell and Necronomicurse.
            CardTarget.SELF, //The target. Single target is ENEMY, all enemies is ALL_ENEMY. Look at cards similar to what you want to see what to use.
            1 //The card's base cost. -1 is X cost, -2 is the cost for unplayable cards like curses, or Reflex.
    );

    public ChannelHellfire() {
        super(ID, info);
    }


    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {
        this.addToBot(new ChannelAction(new HellfireOrb()));
    }

    @Override
    public AbstractCard makeCopy() { //Optional
        return new ChannelHellfire();
    }


}

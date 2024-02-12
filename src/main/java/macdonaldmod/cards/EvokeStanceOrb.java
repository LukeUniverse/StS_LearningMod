package macdonaldmod.cards;

import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.actions.defect.EvokeOrbAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import macdonaldmod.Orbs.WrathOrb;
import macdonaldmod.Orbs.CalmOrb;
import macdonaldmod.util.CardStats;

public class EvokeStanceOrb extends BaseCard{

    //Custom Cards are not yet being used, so are a WIP.

    public static final String ID = makeID("Evoke Stance");
    private static final CardStats info = new CardStats(
            CardColor.BLUE, //Eventually change this to be its own Red/Blue color
            CardType.SKILL, //The type. ATTACK/SKILL/POWER/CURSE/STATUS
            CardRarity.SPECIAL, //Rarity. BASIC is for starting cards, then there's COMMON/UNCOMMON/RARE, and then SPECIAL and CURSE. SPECIAL is for cards you only get from events. Curse is for curses, except for special curses like Curse of the Bell and Necronomicurse.
            CardTarget.SELF, //The target. Single target is ENEMY, all enemies is ALL_ENEMY. Look at cards similar to what you want to see what to use.
            0 //The card's base cost. -1 is X cost, -2 is the cost for unplayable cards like curses, or Reflex.
    );

    public EvokeStanceOrb() {
        super(ID, info);
        FleetingField.fleeting.set(this, true);
    }


    @Override
    public void use(AbstractPlayer abstractPlayer, AbstractMonster abstractMonster) {

        if (!AbstractDungeon.player.orbs.isEmpty()) {
            AbstractOrb orb = (AbstractOrb)AbstractDungeon.player.orbs.get(0);
            if ((orb instanceof WrathOrb)||(orb instanceof CalmOrb)) {
                this.addToTop(new EvokeOrbAction(1));
                this.exhaust = true;
            }
        }

    }

    @Override
    public AbstractCard makeCopy() { //Optional
        return new EvokeStanceOrb();
    }


}

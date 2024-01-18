package macdonaldmod.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PoisonPower;
import macdonaldmod.util.CardStats;

import static macdonaldmod.Patches.AbstractCardEnum.MACDONALDYELLOW;

public class PoisonedStrike extends BaseCard {

    //Custom Cards are not yet being used, so are a WIP.


    public static final String ID = makeID("Poisoned Strike");
    private static final CardStats info = new CardStats(
            MACDONALDYELLOW, //Custom color I used to explore how to, well, add a custom color
            CardType.ATTACK, //The type. ATTACK/SKILL/POWER/CURSE/STATUS
            CardRarity.COMMON, //Rarity. BASIC is for starting cards, then there's COMMON/UNCOMMON/RARE, and then SPECIAL and CURSE. SPECIAL is for cards you only get from events. Curse is for curses, except for special curses like Curse of the Bell and Necronomicurse.
            CardTarget.ENEMY, //The target. Single target is ENEMY, all enemies is ALL_ENEMY. Look at cards similar to what you want to see what to use.
            1 //The card's base cost. -1 is X cost, -2 is the cost for unplayable cards like curses, or Reflex.
    );

    private static final int DAMAGE = 6;
    private static final int UPG_DAMAGE = 2;
    private static final int POISON = 3;
    private static final int UPG_POISON = 1;


    public PoisonedStrike() {
        super(ID, info);

        setDamage(DAMAGE, UPG_DAMAGE);
        setMagic(POISON, UPG_POISON);

        this.tags.add(CardTags.STRIKE); //Should this be elsewhere?

    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
        this.addToBot(new ApplyPowerAction(m, p, new PoisonPower(m, p, this.magicNumber), this.magicNumber));
    }

    @Override
    public AbstractCard makeCopy() { //Optional
        return new PoisonedStrike();
    }

}
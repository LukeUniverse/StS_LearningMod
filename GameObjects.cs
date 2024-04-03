using CGTestng;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Net.Security;
using System.Reflection.Metadata.Ecma335;
using System.Runtime.InteropServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Channels;
using System.Threading.Tasks;
using static System.Collections.Specialized.BitVector32;
using static System.Net.Mime.MediaTypeNames;

namespace CGTesting
{

	#region Interfaces

	public interface iCreature
	{
		public int Health { get; set; }
		public int CurrentBlock { get; set; }

		public List<GameStatusCondition> Conditions { get; set; }
	}

	public interface iTriggerable
	{
		public bool TriggersOn(Trigger trigger);
	}

	public interface iActionSet : iTriggerable
	{
		public List<Trigger>? Triggers { get; set; }
		public List<GameAction>? Actions { get; set; }

	}


	#endregion Intefaces

	#region Enemy stuff:

	public class GameEnemy : iCreature
	{
		public GameEnemy(string name, int health, List<EnemyActionSet> actions, IntentSystem intents, List<StartingConditionDTO> startingConditions)
		{
			EnemyName = name;
			EnemyHealth = health;
			EnemyActionSets = new List<EnemyActionSet>();
			foreach (EnemyActionSet action in actions) { EnemyActionSets.Add(action.Copy()); }
			Intents = intents.Copy();
			StartingConditions = new List<StartingConditionDTO>();
			foreach(StartingConditionDTO startingCondition in startingConditions) StartingConditions.Add(startingCondition.Copy());

			Conditions = new List<GameStatusCondition>();
			CurrentBlock = 0;
		}

		public GameEnemy(string name, int healthMin, int healthMax, List<EnemyActionSet> actions, IntentSystem intents, List<StartingConditionDTO> startingConditions)
		{
			EnemyName = name;
			EnemyHealth = GameManager.RandomNumberGenerator.Next(healthMin, healthMax);
			EnemyMinHealth = healthMin;
			EnemyMaxHealth = healthMax;
			EnemyActionSets = new List<EnemyActionSet>();
			foreach (EnemyActionSet action in actions) { EnemyActionSets.Add(action.Copy()); }
			Intents = intents.Copy();
            StartingConditions = new List<StartingConditionDTO>();
            foreach (StartingConditionDTO startingCondition in startingConditions) StartingConditions.Add(startingCondition.Copy());

            Conditions = new List<GameStatusCondition>();
			CurrentBlock = 0;
		}



		public List<EnemyActionSet> EnemyActionSets { get; set; }
		public List<GameStatusCondition> Conditions { get; set; }
		public EnemyActionSet? NextActionSet { get; set; }
        List<StartingConditionDTO> StartingConditions { get; set; }

        private IntentSystem Intents { get; set; }



		public void PrepNextAction()
		{
			//Rewritten logic has shoved all prep work into the Enemy's 'IntentSystem' object.
			//Now to write the parsing for this... Yuck. lol.
			string nextActionName = Intents.GetNextAction();

            EnemyActionSet actionSet = EnemyActionSets.Where(x => x.SetName == nextActionName).First();

			NextActionSet = actionSet;
		}

		public int Health { get => EnemyHealth; set => EnemyHealth = value; }
		public int CurrentBlock { get; set; }

		private int EnemyHealth;
		private int EnemyMinHealth;
		private int EnemyMaxHealth;
		public string EnemyName;

		public GameEnemy Copy()
		{
			//If they have a min and max health, then when we copy (aka grab a new one) then we want that reflected here.
			if (EnemyMinHealth != 0 && EnemyMaxHealth != 0)
				EnemyMaxHealth = GameManager.RandomNumberGenerator.Next(EnemyMinHealth, EnemyMaxHealth);

			GameEnemy enemyCopy = new GameEnemy(EnemyName, EnemyHealth, EnemyActionSets, Intents, StartingConditions);

			//pass this along too, yeah? and now I have another variable thing here with Curl Up starting status condition. Time to revist this logic I guess?
			enemyCopy.EnemyMaxHealth = EnemyMaxHealth;
			enemyCopy.EnemyMinHealth = EnemyMinHealth;
            // This bit(well and the bit right above need refactoring for sure...!)
            HandleStartingCondtions();


            foreach (GameStatusCondition condition in Conditions) { enemyCopy.Conditions.Add(condition.Copy(enemyCopy)); }

			enemyCopy.CurrentBlock = CurrentBlock;

			enemyCopy.PrepNextAction();
			return enemyCopy;
		}

		private void HandleStartingCondtions()
		{
            foreach (StartingConditionDTO startingConditions in StartingConditions)
                {
                    List<GameStatusCondition> conditionsToAdd = startingConditions.GetConditions();

                    foreach (GameStatusCondition statusCondition in conditionsToAdd)
                    {

                        if (Conditions.Where(x => x.StatusConditionName == statusCondition.StatusConditionName).FirstOrDefault() != null)
                        {
                            //if the target already has the condition...
                            Conditions.Where(x => x.StatusConditionName == statusCondition.StatusConditionName).First().Count++;
                            //then just bump the count up by one.
                        }
                        else //else it needs it's first instance of it.
                            Conditions.Add(statusCondition);
                    }
                }
        }

	}


	public class EnemyActionSet : iActionSet
	{
		public EnemyActionSet(string setName, string setDescription, int chance)
		{
			SetName = setName;
			_setDescription = setDescription;
			Chance = chance;

			Actions = new List<GameAction>();
		}

		//Not sure if this will be needed but... Yep, here it is.
		public EnemyActionSet(string setName, string setDescription, int chance, List<GameAction> actions)
		{
			SetName = setName;
			_setDescription = setDescription;
			Chance = chance;

			Actions = new List<GameAction>();
			foreach (GameAction action in actions)
				Actions.Add(action.Copy());
		}

		public List<GameAction>? Actions { get; set; }
		public string SetName { get; set; }
		public string SetDescription
		{
			get
			{
				string rv = _setDescription;
				try
				{
					if (Actions == null) return "Error Parsing Description...";
					rv = Regex.Replace(rv, @"\{.*?\}", Actions.Where(x => x.Action_Type == ActionType.Damage).First().ActionStrength.ToString());
					rv = Regex.Replace(rv, @"\s{2,}", " "); // let's exclude trivial replaces 
				}
				catch { rv = _setDescription; }
				return rv;
			}
			set { _setDescription = value; }
		}
		public int Chance { get; set; }

		private string _setDescription { get; set; }
		public List<Trigger>? Triggers { get; set; }

		//Method that takes in a trigger maybe?
		public bool TriggersOn(Trigger trigger)
		{

			bool rv = false;

			if (Triggers != null && Triggers.Contains(trigger))
				rv = true;
			return rv;

		}

		public EnemyActionSet Copy()
		{
			EnemyActionSet copy = new EnemyActionSet(SetName, _setDescription, Chance);
			copy.Actions = new List<GameAction>();

			if (Actions != null)
				foreach (GameAction action in Actions)
					copy.Actions.Add(action.Copy());

			return copy;
		}

	}


	#endregion

	#region Player

	public class Player : iCreature
	{
		public int Health { get; set; }
		public int CurrentBlock { get; set; }
		public int MaxEnergy { get; set; }
		public int CurrentEnergy { get; set; }
		public List<GameStatusCondition> Conditions { get; set; } = new List<GameStatusCondition>();
		//For now, make this a constant.
		public const int HandSize = 5;
		//Should perhaps have some sort of 'GameCardCollection' object possibly. We'll see.
		public List<GameCard> Hand { get; set; } = new List<GameCard>();
		public List<GameCard> Deck { get; set; } = new List<GameCard>();
		public List<GameCard> Exile { get; set; } = new List<GameCard>();
		public List<GameCard> Discard { get; set; } = new List<GameCard>();

		public List<GameRelic> Relics { get; set; } = new List<GameRelic>();
	}

	#endregion

	#region GameAction stuff:
	public class GameAction
	{
		public GameAction(ActionType actionType, ActionTarget target, int multiTargetNum, int actionStrength, iCreature? targetCreature = null)
		{
			Action_Type = actionType;
			Action_Target = target;
			ActionStrength = actionStrength;
			MultiTargetNum = multiTargetNum;
			//I don't want this to HAVE to get set
			if (targetCreature != null)
				Target = targetCreature;

		}

		public GameAction(ActionType actionType, ActionTarget target, int multiTargetNum, int actionStrengthMin, int actionStrengthMax, iCreature? targetCreature = null)
		{
			Action_Type = actionType;
			Action_Target = target;
			ActionStrengthMin = actionStrengthMin;
			ActionStrengthMax = actionStrengthMax;

			MultiTargetNum = multiTargetNum;
			//I don't want this to HAVE to get set
			if (targetCreature != null)
				Target = targetCreature;

		}

		public ActionType Action_Type { get; set; }
		public ActionTarget Action_Target { get; set; }

		public int MultiTargetNum { get; set; }
		public int ActionStrength;

		public int ActionStrengthMin;
		public int ActionStrengthMax;

		public string? Condition { get; set; }
		public Zone ActionZoneTarget { get; set; }


		public iCreature? Target { get; set; }
		public iCreature? Source { get; set; }

		public GameAction Copy()
		{


			//if it's been defined with a min and max, then get a new value for the main instance of the number when making the copy //Is this actually giving me ranom values....
			if (ActionStrengthMin != 0 && ActionStrengthMax != 0)
				ActionStrength = GameManager.RandomNumberGenerator.Next(ActionStrengthMin, ActionStrengthMax); //Not sure I'm getting the results I want via just having this here... lol

			GameAction copy = new GameAction(Action_Type, Action_Target, MultiTargetNum, ActionStrength, Target);
			copy.Condition = Condition;
			copy.Source = Source;
			copy.ActionZoneTarget = ActionZoneTarget;
			return copy;

		}

	}

	public class GameCard
	{

		public GameCard(string cardName, string cardDescription, int cardCost)
		{
			CardName = cardName;
			CardDescription = cardDescription;
			CardCost = cardCost;
			CardActionSets = new List<CardActionSet>();
			CardTags = new List<string>();

		}

		//Things that get set by parsing
		public string CardName { get; set; }
		public string CardDescription { get; set; }
		public int CardCost { get; set; }
		public List<CardActionSet> CardActionSets { get; set; }
		public PlayRequirement? Requirement { get; set; }
		public List<string> CardTags { get; set; }

		//Non Parsed things:
		public bool IsTempCopy { get; set; }

		public GameCard Copy()
		{
			GameCard copiedCard = new GameCard(CardName, CardDescription, CardCost);

			foreach (CardActionSet actionSet in CardActionSets)
				copiedCard.CardActionSets.Add(actionSet.Copy());

			foreach (string tag in CardTags)
				copiedCard.CardTags.Add(tag);//Don't think I need any sort of Copy if it's just a string value...

			if (Requirement != null)
				copiedCard.Requirement = Requirement.Copy();

			return copiedCard;
		}

		public GameCard TempCopy()
		{
			GameCard copy = Copy();
			copy.IsTempCopy = true;

			return copy;
		}
	}

	//Card actions don't need a name, or description, the card construct contains that info.
	public class CardActionSet : iActionSet
	{

		public CardActionSet()
		{
			Actions = new List<GameAction>();
			Triggers = new List<Trigger>();
		}

		public List<Trigger>? Triggers { get; set; }
		public List<GameAction>? Actions { get; set; }

		public bool TriggersOn(Trigger trigger)
		{
			bool rv = false;
			if (Triggers != null && Triggers.Contains(trigger))
				rv = true;
			return rv;
		}

		public CardActionSet Copy()
		{
			CardActionSet copy = new CardActionSet();
			copy.Actions = new List<GameAction>();
			copy.Triggers = new List<Trigger>();

			if (Actions != null)
				foreach (GameAction action in Actions)
					copy.Actions.Add(action.Copy()); //The default constructor sets these to new Lists, I KNOW they're not null! I guess someone could set them to null by the time we call this? and that's why it's warning me?

			if (Triggers != null)
				foreach (Trigger trigger in Triggers)
					copy.Triggers.Add(trigger); //The default constructor sets these to new Lists, I KNOW they're not null!

			return copy;
		}
	}

	public class PlayRequirement
	{
		public PlayRequirement(string relevantZone, string cardTag, string amount)
		{
			RelevantZone = (Zone)(Enum.Parse(typeof(Zone), relevantZone));
			CardTag = cardTag;
			Amount = amount;
		}

		public PlayRequirement(Zone relevantZone, string cardTag, string amount)
		{
			RelevantZone = relevantZone;
			CardTag = cardTag;
			Amount = amount;
		}

		public Zone RelevantZone { get; set; }
		public string CardTag { get; set; }
		public string Amount { get; set; } //set to string because we want to allow ALL and NONE and ONLY in addition to straight up numbers.

		public PlayRequirement Copy()
		{
			PlayRequirement copy = new PlayRequirement(RelevantZone, CardTag, Amount);
			return copy;
		}

	}


	#endregion

	#region Room Stuff

	public class GameRoom
	{
		public bool IsEmpty { get { return (Enemies.Count == 0); } set { throw new Exception("Cannot set GameRoom.IsEmpty"); } }

		public GameRoom()
		{
			RoomName = "";
			Enemies = new List<GameEnemy>();
		}

		public GameRoom(List<GameEnemy> enemies)
		{
			RoomName = "";
			Enemies = enemies;
		}

		public List<GameEnemy> Enemies { get; set; }
		public string RoomName { get; set; }

		public GameRoom Copy()
		{
			GameRoom roomCopy = new GameRoom();
			roomCopy.RoomName = RoomName;
			foreach (GameEnemy enemy in Enemies)
				roomCopy.Enemies.Add(enemy.Copy());

			return roomCopy;
		}
	}


	#endregion


	public class GameStatusCondition
	{
		public string StatusConditionName
		{
			get { return _statusConditionName; }
			set { _statusConditionName = value; }
		}
		public List<string> Triggers
		{
			get { return _triggers; }
			set { _triggers = value; }
		}
		public bool Stacks
		{
			get { return _stacks; }
			set { _stacks = value; }
		}
		public int DecayRate
		{
			get { return _decayRate; }
			set { _decayRate = value; }
		}
		public List<string> AppliedOn
		{
			get { return _appliedOn; }
			set { _appliedOn = value; }
		}
		public string ApplicationFormula
		{
			get { return _applicationFormula; }
			set { _applicationFormula = value; }
		}

		//This isn't one we're going to parse from the data, rather if we go to add a condition to an enemy, and they already have it, we'll check to see if it can stack and if so then we'll bump the count up.
		public int Count
		{
			get { return _count; }
			set { _count = value; }
		}

		private string _statusConditionName = "";
		private List<string> _triggers = new List<string>();
		private bool _stacks = false;
		private int _decayRate = 0;
		private List<string> _appliedOn = new List<string>();
		private string _applicationFormula = "";
		private int _count = 1;

		public List<CardActionSet> ActionSets = new List<CardActionSet>();//right now using the same action sets as cards.

        public GameStatusCondition Copy(iCreature owner)
		{
			GameStatusCondition conditionCopy = new GameStatusCondition();

			conditionCopy.StatusConditionName = _statusConditionName;
			conditionCopy.Triggers = _triggers;
			conditionCopy.Stacks = _stacks;
			conditionCopy.DecayRate = _decayRate;
			conditionCopy.AppliedOn = _appliedOn;
			conditionCopy.ApplicationFormula = _applicationFormula;
			conditionCopy.Count = Count;

            foreach (CardActionSet actionSet in ActionSets)
            {

                CardActionSet copiedActionSet = actionSet.Copy();
                //this is definitely a hacky way of getting it done, but eh, I can address that later. hah

                if (copiedActionSet.Actions != null)
                {
					foreach (GameAction action in copiedActionSet.Actions)
					{
						action.Source = owner;
					}
                }
                conditionCopy.ActionSets.Add(copiedActionSet);
            }

            return conditionCopy;
		}

	}

	public class GameRelic : iTriggerable //Still need to refactor this to use ActionSets most likely.
	{
		public string RelicName;
		public string RelicDescription;
		public string RelicFlavor;
		public List<CardActionSet> ActionSets = new List<CardActionSet>(); //right now using the same action sets as cards.


		public GameRelic(string relicName, string description, string relicFlavor)
		{
			RelicName = relicName;
			RelicDescription = description;
			RelicFlavor = relicFlavor;
		}
		public bool TriggersOn(Trigger trigger)
		{
			bool rv = ActionSets.Where(x => x.TriggersOn(trigger)).Any();
			return rv;

		}

		public GameRelic Copy()
		{
			GameRelic newGameRelic = new GameRelic(RelicName, RelicDescription, RelicFlavor);

			newGameRelic.ActionSets = new List<CardActionSet>();
			foreach (CardActionSet actionSet in ActionSets)
			{

				CardActionSet copiedActionSet = actionSet.Copy();
				//this is definitely a hacky way of getting it done, but eh, I can address that later. hah

				if (copiedActionSet.Actions != null)
				{
					foreach (GameAction action in copiedActionSet.Actions)
						action.Source = GameManager.Player;
				}
				newGameRelic.ActionSets.Add(copiedActionSet);
			}


			return newGameRelic;
		}



	}

	//WIP IntentSystem

	public class IntentSystem
	{
		//Ugh, I should just make more DTO objects. This is a weird mesh of things.

		Dictionary<string, int> FirstTurnActionOptions = new Dictionary<string, int>();
		public int? MaxRepeats = null;
		Tuple<string, int>? LoopedAction;
		List<IntentActionDTO> NonLoopedActions = new List<IntentActionDTO>();
		Dictionary<String, List<Trigger>> TriggerActions = new Dictionary<String, List<Trigger>>();

		public IntentSystem(Dictionary<string, int> firstTurnActionOptions, int? maxRepeats, Tuple<string, int>? loopedAction, List<IntentActionDTO> nonLoopedActions, Dictionary<string, List<Trigger>> triggers)
		{
			FirstTurnActionOptions = firstTurnActionOptions;
			MaxRepeats = maxRepeats;
			LoopedAction = loopedAction;
			NonLoopedActions = nonLoopedActions;
			TriggerActions = triggers;
		}

		private bool FirstTurn = true;
		private Dictionary<int, string> previousActionsOrdered = new Dictionary<int, string>();
		int currentActionCount { get { return previousActionsOrdered.Count + 1; } }

		public string GetNextAction()
		{
			//covers first turn...
			if (FirstTurn)
			{
				List<Tuple<string, int, int>> actionSetTuples = new List<Tuple<string, int, int>>();

				int loopingVariable = 0;
				foreach (KeyValuePair<string, int> firstTurnActionOption in FirstTurnActionOptions)
				{
					int startingVal = loopingVariable;
					loopingVariable += firstTurnActionOption.Value;
					int endingVal = loopingVariable;
					actionSetTuples.Add(Tuple.Create<string, int, int>(firstTurnActionOption.Key, startingVal, endingVal));
				}

				int randomNumb = GameManager.RandomNumberGenerator.Next(0, loopingVariable);
				Tuple<string, int, int>? action = null;

                if (actionSetTuples != null && actionSetTuples.Count > 0)
					action = actionSetTuples.Where(x => x.Item2 <= randomNumb && x.Item3 >= randomNumb).First();
				//No many how many action sets we shove in this, we can get the odds right in theory.
				if (action != null)
				{
                    previousActionsOrdered.Add(currentActionCount, action.Item1);
					return action.Item1;
				}

				FirstTurn = false;
			}

			//Then let's check our trigger actions...
			if (TriggerActions.Count > 0)
			{
				//Not sure any creature will have more than one of these, if they DO we might have to figure
				//out a way of RANKing them to see which is more importent to trigger first...
				foreach (KeyValuePair<string, List<Trigger>> triggedAction in TriggerActions)
				{
					foreach (Trigger trig in triggedAction.Value)
					{
						if (GameManager.CheckTrigger(trig))
                        {
                            previousActionsOrdered.Add(currentActionCount, triggedAction.Key);
                            return triggedAction.Key; //IF there are more than one TriggeredActions, well, see above
						}
					}
				}
			}

			//If we still haven't hit anything, let's check our LOOPED Action (hopefully there is only 1 looped action?).
			if (LoopedAction != null)
			{
				int actionsTakenSince = 0;
				//Loop through previous actions backwards until we get to the last time we did the looped action...
				for (int actionNumber = previousActionsOrdered.Count(); actionNumber > 0; actionNumber--)
				{
					if (previousActionsOrdered.GetValueOrDefault(actionNumber) != LoopedAction.Item1)
					{
						actionsTakenSince++;
					}
					else
					{
						break;
					}
				}
				//if the count of how many actions we have taken since our last looped action equals
				//the LoopEvery int of of our Looped action, then we need to do the looped action.
				if (actionsTakenSince == LoopedAction.Item2)
				{
                    previousActionsOrdered.Add(currentActionCount, LoopedAction.Item1);
                    return LoopedAction.Item1;
				}
			}

			//If we still haven't hit an action, we need to then look at our regular selection of actions (aka our NonLoopedActions)

			//something like:
			List<Tuple<string, int, int>> nonloopedactionSetTuples = new List<Tuple<string, int, int>>();

			int trackingVar = 0;
			foreach (IntentActionDTO actionOption in NonLoopedActions)
			{
				if (previousActionsOrdered.Where(x => x.Value == actionOption.Name).ToList().Count() >= actionOption.CanBeUsedMaxOf)
				{
					//If we've used this option the max number of times... don't use it again.
					//Should we be adding the chances for this option to a different one? Or... For now jsut break.
					//breaking here is esentially evenly distributing the amount across all of the options.
					break;
				}

				int startingVal = trackingVar;
				trackingVar += actionOption.Chance;
				int endingVal = trackingVar;
				nonloopedactionSetTuples.Add(Tuple.Create<string, int, int>(actionOption.Name, startingVal, endingVal));
			}

			int randomNumbNonLooped = GameManager.RandomNumberGenerator.Next(0, trackingVar);
			Tuple<string, int, int> nonloopedaction = nonloopedactionSetTuples.Where(x => x.Item2 <= randomNumbNonLooped && x.Item3 >= randomNumbNonLooped).First();
			//No many how many action sets we shove in this, we can get the odds right in theory.
			if (nonloopedaction != null)
			{
				IntentActionDTO actionDTO = NonLoopedActions.FirstOrDefault(x => x.Name == nonloopedaction.Item1);
				//Now, check here if this has been repeated too many times...
				int repititionCount = 0;
				//Loop through previous actions backwards until we get to the last time we did the looped action...
				for (int actionNumber = previousActionsOrdered.Count(); actionNumber > 0; actionNumber--)
				{
					if (previousActionsOrdered.GetValueOrDefault(actionNumber) == actionDTO.Name)
					{
						repititionCount++;
					}
					else
					{
						break;
					}
				}
				if (repititionCount == MaxRepeats)
				{
                    previousActionsOrdered.Add(currentActionCount, actionDTO.NextInLine);
                    return actionDTO.NextInLine;
				}

				previousActionsOrdered.Add(currentActionCount, actionDTO.Name);
				return actionDTO.Name;
			}

			//We shouldn't hit all the way down here...
			throw new NotImplementedException();
		}

		public IntentSystem Copy()
		{
			Dictionary<string, int> firstTurnActionOptions = new Dictionary<string, int>();
			int? maxRepeats = null;
			Tuple<string, int>? loopedAction;
			List<IntentActionDTO> nonLoopedActions = new List<IntentActionDTO>();
			Dictionary<String, List<Trigger>> triggerActions = new Dictionary<String, List<Trigger>>();

			foreach (KeyValuePair<string, int> ftActionOption in firstTurnActionOptions)
				firstTurnActionOptions.Add(ftActionOption.Key, ftActionOption.Value);

			maxRepeats = MaxRepeats;

			loopedAction = LoopedAction != null ? new Tuple<string, int>(LoopedAction.Item1, LoopedAction.Item2) : null;

			foreach (IntentActionDTO intentAction in NonLoopedActions)
				nonLoopedActions.Add(intentAction.Copy());

			foreach (KeyValuePair<string, List<Trigger>> trigAction in TriggerActions)
				triggerActions.Add(trigAction.Key, trigAction.Value);

			return new IntentSystem(firstTurnActionOptions, maxRepeats, loopedAction, nonLoopedActions, triggerActions);
		}
	}

	


	public class IntentActionDTO
	{
		public IntentActionDTO(string name, int chance, string? next, int? maxof)
		{
			Name = name;
			Chance = chance;
			NextInLine = next;
			CanBeUsedMaxOf = maxof;
		}

		public string Name;
        public int Chance;
		public string? NextInLine;
		public int? CanBeUsedMaxOf;

		public IntentActionDTO Copy()
		{
			return new IntentActionDTO(Name, Chance, NextInLine, CanBeUsedMaxOf);
		}
	}

	public class StartingConditionDTO
	{
        public StartingConditionDTO(string name, int min, int max, iCreature owner)
        {
			//If a min or max cannot be found in the json, these end up getting passed 1.
            Name = name;
            Min = min;
            Max = max;
			Owner = owner; //This can be null. Which Can be a Problem.
        }

        string Name;
		int Min;
		int Max;
		iCreature Owner;

		public List<GameStatusCondition> GetConditions()
		{
			List<GameStatusCondition> conditions = new List<GameStatusCondition>();
			int count = GameManager.RandomNumberGenerator.Next(Min, Max);

			for (int i = count; i > 0; i--)
				conditions.Add(GameManager.AllConditions.Where(x => x.StatusConditionName == Name).First().Copy(Owner));
			
			return conditions;
		}

		public StartingConditionDTO Copy()
		{
			return new StartingConditionDTO(Name, Min, Max, Owner);
		}
	}

	public class PhaseSystem
	{
		//IntentSystem Intents = new IntentSystem();
		//List<Trigger> Triggers = new List<Trigger>();
		//probs some other thing too
	}


    #region Enums

    public enum ActionType
	{
		Unknown = 0,
		Damage = 1,
		Block = 2,
		Heal = 3,
		ApplyCondition = 4,
		Kill = 5,
		CreateCard = 6
	}

	public enum ActionTarget
	{
		Unknown = 0,
		Player = 1,
		Enemy = 2,
		Any = 3,
		RandomEnemy = 4,
		AllEnemies = 5,
		Self = 6, //May want to re go through Player Action stuff and change block cards to have this as an action target, instead of just Player.
		SelfCard = 7
	}

	public enum Trigger
	{
		CombatDamage,
		TakeCombatDamage,
		EndOfCombat,
		EndOfTurn,
		NotAlone
	}


	public enum Zone //for now, have a Zone Enum, I might later want to support custom zones defined the JSON though...
	{
		Unknown = 0,
		Deck = 1,
		Discard = 2,
		Exile = 3,
		Hand = 4,
		Custom = 5
	}

	public enum ActionSetType
	{
		Unknown = 0,
		Card = 1,
		Enemy = 2
	}

	#endregion

}

using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text;
using CGTesting;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using static System.Collections.Specialized.BitVector32;
using static System.Net.Mime.MediaTypeNames;

namespace CGTestng
{


	internal partial class Program
	{
        /*
			Targeting bug truly smashed this time (for realsies)
			
			Refactor Card Descriptions so that they can get a number from a specific action of theirs...

			Okay, I really need to work on Enemies and their action queue system...
			First thing first: Give each EnemyActionSet an ID number

			Withering Mass
			The Champ
			possibly look at Shelled Parasite as well for it's Plated Armor stun effect type of thing.
			Centurion & Mystic
			Gremlin Leader

			are two good example enemies to look at for complex decision trees regarding intent systems.

			Alright, I think I just did a SHIT ton of coding, with zero testing. ugh.
			Time to give the enemies some intentsystems, I've gone down to just three enemies, so it should be an easier process...
			But yeah, real worried I did something dumb and something is going to explode at some point soon.


			Hmmmm... Maybe I should Redefine block as simply a GameStatusCondition... this would take away some of it's abstraction from the rest of the JSON definined things allowing them play with it a bit easier 
			
			Or one of the things listed down below.
			Work more on 'Room' Support?
			Add 'death' of player. Make it easily disabled though for testing purposes.S
			

			This can be a fun little thing to do down here, choose a random relic / card and make that be the 'next card/relic' to add. Slowly gets more content while also forcing me to considering what the code needs
			for each random card / relic.
			--Next thing to add... 
		
		NEXT THING:

		(1) BODY SLAM
		Damage equal to your defense.
		
		(Ah, because the damage cares about a 'magic number', I see that will be a unique thing to code in.)

		Relics:
		BURNING BLOOD <- Done~ish, still needs some refactoring.
		
		COMMON:
		Clash <- Doneish, some refactoring might be nice.

		Uncommon
		ANGER <-DONE

		Curses:
		Shame <- DONE.
		*/

        //This Main method should basically never change. If I am ever sitting down thinking of changing it, stop myself and ask myself "Do I really need to do that here?"
        //I want to be doing EVERYTHING in the GameManager, and occasionally touching the Display Manager when new things that get displayed need to be handled.
        static void Main(string[] args)
		{
			GameManager.SetUp();// 1/2 places we call into the GameManager.
			bool keepGoing = true;
			
			while (keepGoing)
			{
				DisplayMethods.RefreshDisplay(true);
				Console.WriteLine("Options: R,F,E, Action Strings (like '2' or '1,1')"); //this can be removed probsm since I'm the only one testing it right now.

				string? line = Console.ReadLine();
				line = (line != null) ? line : "";
				if (line == "r") //Refresh the Display
					keepGoing = true;
				else if (line =="f") //End the 'Game'
					keepGoing = false;
				else //Everything else.
				{
					GameManager.DoAction(line); // 2/2 of places we call into the GameManager. 
				}
			}
		}
	}
	
	public static class GameManager
	{
		#region Fields

		public static List<string> ErrorMessages = new List<string>();
		
		//Made these public for now, solely to make moving the Parsing methods over to a utility class EASIER. Come back and refactor this / the parsing utility later.
		public static List<GameCard> AllCards = new List<GameCard>();
		public static List<GameRoom> AllRooms = new List<GameRoom>();
		public static List<GameEnemy> AllEnemies = new List<GameEnemy>();
		public static List<GameStatusCondition> AllConditions = new List<GameStatusCondition>();
		public static List<GameRelic> AllRelics = new List<GameRelic>();


		public static GameRoom CurrentRoom = new GameRoom();
		public static Player Player = new Player();

		#endregion
		
		#region Public methods x2
		//These are the only two things the console should ever be interacting with. If they're interacting with something else, stop yourself and ask youself "Why?"

		public static void SetUp() //I know this is alphabetically out of order, but it makes sense to my mind to have the SetUp method first.
		{
			ParsingUtility.ParseEnemyJSON();
			//move this bit here to allow support of on the fly Card parsing too (for enemies, perhaps they should no longer be called cards?)

			string jsonString = File.ReadAllText("JSON/cards.json"); // Should print True") 
				//File.ReadAllText(@"C:\Random personal stuff\Random personal dev Junk\cgtjson\cards.json");
			JObject json = JObject.Parse(jsonString);
			JToken? jCards = json["cards"];
			JArray cards = jCards != null ? (JArray)jCards : new JArray();
			ParsingUtility.ParseCardsJSON(cards);
			ParsingUtility.ParseConditionsJSON();
			ParsingUtility.ParseRelicsJSON();
			ParsingUtility.ParseRoomsJSON();
			FillRoom();

			//eventually make this bit more dynamic
			Player = new Player()
			{
				Health = 70,//There should be a max health somewhere too ideally

				//MaxEnergy = 3,
				//CurrentEnergy =3

				//for debugging, obviously..
				MaxEnergy = 100,
				CurrentEnergy =100
			};

			//source for relic actions should now be getting set to be the Player via the Copy method.
			GameRelic relicBB = AllRelics.Where(x => x.RelicName == "Burning Blood").First().Copy();
			
			//Go ahead and start us with burning blood.
			Player.Relics.Add(relicBB);

			//Strike x5
			Player.Deck.Add(AllCards.Where(x=>x.CardName == "Strike").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Strike").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Bash").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Bash").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Bash").First().Copy());
			//Defend x4
			//Player.Deck.Add(AllCards.Where(x => x.CardName == "Defend").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Defend").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Defend").First().Copy());
			Player.Deck.Add(AllCards.Where(x => x.CardName == "Defend").First().Copy());
			//Bash x1
			Player.Deck.Add(AllCards.Where(c => c.CardName == "Bash").First().Copy());


			Player.Deck.Add(AllCards.Where(c => c.CardName == "Clash").First().Copy());
			//Player.Deck.Add(AllCards.Where(c => c.CardName == "Anger").First().Copy());
			//Player.Deck.Add(AllCards.Where(c => c.CardName == "Shame").First().Copy());



			ShuffleDeck();
			DrawToHandSize();
		}

		//This is getting moved here specifically to be set in the weird little processsubaction method for now.
		//I need to think through this more though.
        static GameEnemy? overArchingEnemyTarget = null;

        public static void DoAction(string input)
		{
			try
			{
				ErrorMessages = new List<string>();// Clear this out at the start of each action please.

				if (input == "+")
				{//Purely debug testing logic
					Player.CurrentEnergy++;
					return;
				}

				if (input == "s")
				{//Purely debug testing logic
					Player.Conditions.Add(AllConditions.Where(x => x.StatusConditionName == "Strength").First().Copy(Player));
					return;
				}

				if (input == "k")//Smidge of useful testing logic.
				{
					//Create, then set the Subaction, then return so we can loop back here and process the subaction.
					GameAction killAction = new GameAction(ActionType.Kill, ActionTarget.Enemy, 0, 0, null);
					ProcessSubAction(killAction);
					return;
				}

				if (input == "e") //End Turn is just baked into this rn
				{
					EndTurn();
					return;//end turn, so don't finish the rest of this.
				}

				GameCard thisCard = Player.Hand[int.Parse(input) - 1];

				if (thisCard.CardTags.Contains("UNPLAYABLE"))
				{
					ErrorMessages.Add($"Card: {thisCard.CardName} is unplayable.");
					return;
				}

				if (thisCard.Requirement != null)
				{
					//Right now Clash is the only card with a Requirement we have so let's code for that one just right here... Refactor this out to somewhere else though.
					PlayRequirement playRequirement = thisCard.Requirement;

					List<GameCard> relevantZone = new List<GameCard>();

					switch (playRequirement.RelevantZone)
					{
						case (Zone.Discard):
							relevantZone = Player.Discard;
							break;
						case (Zone.Hand):
							relevantZone = Player.Hand;
							break;
						case Zone.Unknown:
							relevantZone = new List<GameCard>();
							break;
					}

					if (playRequirement.Amount == "ALL" || playRequirement.Amount == "ONLY")
					{
						if (playRequirement.Amount == "ALL")
						{
							//They ALL need to have the tag, and thus the count of things that have the tag should be equal to the overall count.
							if (relevantZone.Where(x => x.CardTags.Contains(playRequirement.CardTag)).ToList().Count != relevantZone.Count)
							{
								ErrorMessages.Add("Requirement not met.");
								return;
							}
						}
						else
						{

						}
					}
					else
					{
						//If it is not one of the above then it should be a number.
						int requiredAmount = int.Parse(playRequirement.Amount);
					}
					//If we make it past everything, then woo, no issues with requirements and nothing special needs to happen. Just move on.
				}

				//first check energy...
				if (thisCard.CardCost > Player.CurrentEnergy)
				{
					ErrorMessages.Add("Not enough energy to play card.");
					return;
				}

				//Subtract our energy before we do the actions? Yeah, before.
				Player.CurrentEnergy -= thisCard.CardCost;

				foreach (CardActionSet actionSet in thisCard.CardActionSets)
				{
					if (actionSet.Actions == null)
						continue;

					//Right now each card only has one action set, right?, that will change those, and this here should only be with OnPlay triggers
					foreach (GameAction action in actionSet.Actions)
					{
						action.Source = Player;
						//If we've gathered up a target via previous subaction, then use that target again here.
						if (action.Action_Target == ActionTarget.Enemy && (overArchingEnemyTarget != null))
						    action.Target = overArchingEnemyTarget;
						

						ProcessAction(action);

                        
                        //Overarching Enemy is now getting set as part of the ProcessSubAction, and is a variable whose scope it the entire GameManager.
						//Not sure I really like that, but since I switched to zeroing out a target at the end of the process action step I kinda needed to do it
						//that way?
						//if (action.Target != null && action.Action_Target == ActionTarget.Enemy)
						//{

                        //    Console.WriteLine($"We are setting the action's Overarching enemy to: {((GameEnemy)action.Target).EnemyName}");
                        //    overArchingEnemyTarget = (GameEnemy)action.Target;
						//}


						//The CardSelf Action can fire off here... Where it has access to the card still!!!
						//This little snippet here should be enough for now at least for the Anger card... I think.
						//This should probably actually be in the processaction section though... hah.
						if (action.Action_Target == ActionTarget.SelfCard)
						{
							//Right now all CreateCard actions will create a temp card that is gone on room end.
							if (action.Action_Type == ActionType.CreateCard)
							{
								switch (action.ActionZoneTarget)
								{
									case Zone.Discard:
										Player.Discard.Add(AllCards.Where(x => x.CardName == thisCard.CardName).First().TempCopy());
										break;
								}
							}
						}
					}
				}

                //Reset the overArching target, I don't think I was doing this anywhere, but I should have been.
                overArchingEnemyTarget = null;

				ChangeCardZone(thisCard, Player.Hand, Player.Discard);
			}
			catch (Exception ex)
			{
				ErrorMessages.Add(ex.ToString());
			}
		}

		//Breaking my rule and adding another public thing... At least it's just being called from my GameObjects though...?
		public static Random RandomNumberGenerator { get { if (_random == null) { _random = new Random(); return _random; } else return _random; } set { throw new Exception("Cannot set GetRandom"); } }
		private static Random _random = new Random(); //now each Random.Next should be using THIS random, so it won't be lots of randoms being made at roughly the same time, but one Random being reused.
		//okay that doesn't seem to matter lol.
		#endregion


		private static void FillRoom()
		{
			//Should I be nulling out CurrentRoom at some point maybe? Or make a clear method? Or something?

			//for now just make a copy of a random room.
			//CurrentRoom = AllRooms[RandomNumberGenerator.Next(AllRooms.Count)].Copy();

			// If you want to test a specific room, here you go, force it everytime.
			CurrentRoom = AllRooms.Where(x=> x.RoomName == "Louse Room").First().Copy();

			//LMM TODO, this might require revisiting...
			//for now setting the enemies action sources to themselves right here, which definitely feels like the wrong place to have it, but here we are. //Okay at least this is here.
			foreach (GameEnemy enemy in CurrentRoom.Enemies)
			{
				
				List<GameAction> enemyActionSets = new List<GameAction>();

				//Get all of the possible actions from all the possible cards for this enemy.
				foreach (EnemyActionSet enemyActionSet in enemy.EnemyActionSets)
				{
					if (enemyActionSet.Actions == null)
						continue;
					enemyActionSets.AddRange(enemyActionSet.Actions);
				}
				//Then set their source.
				foreach (GameAction action in enemyActionSets)
					action.Source = enemy;

				//Maybe have this here for the set up???
				//enemy.PrepNextAction();
			}
		}

		private static void EndTurn()
		{
			//Decay condtions on player needs to happen BEFORE basically everything else.
			for (int i = GameManager.Player.Conditions.Count; i > 0; i--)
			{
				GameStatusCondition focusedPlayerCondition = GameManager.Player.Conditions[i -1];

				if (focusedPlayerCondition.DecayRate > 0)
				{
					for (int decayCount = 0; decayCount < focusedPlayerCondition.DecayRate; decayCount++)
					{
						if (focusedPlayerCondition.Count > 1)
							focusedPlayerCondition.Count--;
						else Player.Conditions.Remove(focusedPlayerCondition);

					}
				}
			}

			//Discard hand.
			for (int cardNumb = Player.Hand.Count; cardNumb > 0; cardNumb--)
			{
				GameCard focusedCard = Player.Hand[cardNumb-1];
				foreach (CardActionSet actionSet in focusedCard.CardActionSets.Where(x => x.TriggersOn(Trigger.EndOfTurn)).ToList())
				{
					if (actionSet.Actions == null)
						continue;

					//Foreach ActionSet that triggers on EoT for cards in our hand, process all of their actions.
					foreach (GameAction action in actionSet.Actions)
						ProcessAction(action);
				}
				ChangeCardZone(focusedCard, Player.Hand, Player.Discard);
			}

			//Draw up to hand.
			DrawToHandSize();

			//Enemies peform their action.
			foreach (GameEnemy enemy in CurrentRoom.Enemies)
			{
				PerformEnemyActions(enemy);
			}

			if (CurrentRoom.IsEmpty)
			{
				//Put hand, discard, and exile back into your deck...then remove any temp copies from deck.
				for (int i = Player.Hand.Count; i > 0; i--)
				{
					ChangeCardZone(Player.Hand[i-1], Player.Hand, Player.Deck);
				}
				for (int i = Player.Discard.Count; i > 0; i--)
				{
					ChangeCardZone(Player.Discard[i - 1], Player.Discard, Player.Deck);
				}
				for (int i = Player.Exile.Count; i > 0; i--)
				{
					ChangeCardZone(Player.Exile[i - 1], Player.Exile, Player.Deck);
				}

				for (int i = Player.Deck.Count; i > 0; i--)
				{
					GameCard card = Player.Deck[i - 1];
					if (card.IsTempCopy)
						Player.Deck.Remove(card);
					//Is there some way I should be disposing of the card? Maybe.
				}

				ShuffleDeck();
				DrawToHandSize();

				//Currently we just have one room, go ahead and refill it if it is empty for now.
				FillRoom();

				//I suppose this is the end of combat for now...
				//So do some EoT stuff...
				foreach (GameRelic relic in Player.Relics)
				{
					if (relic.TriggersOn(Trigger.EndOfCombat))
					{
						foreach (CardActionSet actionSet in relic.ActionSets.Where(x => x.TriggersOn(Trigger.EndOfCombat)))
						{
							if (actionSet.Actions == null) continue;

							foreach(GameAction action in actionSet.Actions)
								ProcessAction(action);
						}
					}
				}
			}

			//, and then on Enemy logic is here. Probably refactor down the line.

			foreach (GameEnemy enemy in GameManager.CurrentRoom.Enemies)
			{
				for(int i = enemy.Conditions.Count; i > 0; i--)
				{
					GameStatusCondition focusedEnemyCondition = enemy.Conditions[i -1];

					if (focusedEnemyCondition.DecayRate > 0)
					{
						for (int decayCount = 0; decayCount < focusedEnemyCondition.DecayRate; decayCount++)
						{
							if (focusedEnemyCondition.Count > 1)
								focusedEnemyCondition.Count--;
							else enemy.Conditions.Remove(focusedEnemyCondition);

						}
					}
				}
			}

			//This is currently only being called at End Turn, so let's refresh energy after this...
			Player.CurrentEnergy = Player.MaxEnergy;
		}


		#region Action Performance

		private static void ApplyAction(GameAction action)
		{
			if (action.Action_Type == ActionType.Damage)
				ApplyDamage(action);
			else if (action.Action_Type == ActionType.ApplyCondition)
				ApplyCondition(action);
			else if (action.Action_Type == ActionType.Heal)
				ApplyHeal(action);
			else if (action.Action_Type == ActionType.Block)
				ApplyBlock(action);
			//action.Target.CurrentBlock += action.ActionStrength;

			//After the action is done firing off, we should remove the target. It was causing bugs.
			//Ideally the creatures would dispose somehow, I need to look into that...
			action.Target = null;
		}

		//Parses the 'Formula' string on conditions to modify Damage for triggers related to damage.
		private static float ApplyConditionFormulasToNumber(List<GameStatusCondition> conditions, float currentNumber)
		{
			float returnDamage = currentNumber;

			//addition
			foreach (GameStatusCondition condition in conditions.Where(x => x.ApplicationFormula.Split(',')[0] == "+").ToList())
			{
				string[] formulaBits = condition.ApplicationFormula.Split(',');
				//Do this for now

				string operation = formulaBits[0];
				string amount = formulaBits[1];
				returnDamage = returnDamage + int.Parse(formulaBits[1]);
			}

			//Multiplication
			foreach (GameStatusCondition condition in conditions.Where(x => x.ApplicationFormula.Split(',')[0] == "*").ToList())
			{
				//For now have the formula be a simple little A, B thing.
				string[] formulaBits = condition.ApplicationFormula.Split(',');
				//Do this for now

				string operation = formulaBits[0];
				string amount = formulaBits[1];

				returnDamage = (returnDamage * float.Parse(amount));
			}

			return returnDamage;
		}

		private static void PerformEnemyActions(GameEnemy enemy)
		{
			if (enemy.NextActionSet == null || enemy.NextActionSet.Actions == null)
				return;

			foreach (GameAction action in enemy.NextActionSet.Actions)
			{
					ProcessAction(action);
			}
			enemy.PrepNextAction();


		}

		private static void ProcessAction(GameAction action)
		{
			//(action.Action_Type == ActionType.Damage) || (action.Action_Type == ActionType.Damage)
			if (action.Action_Target == ActionTarget.Enemy)
			{
				if (action.Target == null)
				{
					//This bit should basically result in us setting a target for the action, and then calling this method again. Hence why we don't continue down the path.
					ProcessSubAction(action);
					return;
				}
				ApplyAction(action);

			}
			else if (action.Action_Target == ActionTarget.RandomEnemy)
			{ 
				GameEnemy enemy = CurrentRoom.Enemies[new Random().Next(CurrentRoom.Enemies.Count)];
				action.Target = enemy;

				ApplyAction(action);

			}
			else if (action.Action_Target == ActionTarget.Player)
			{
				action.Target = Player;

				ApplyAction(action);
			}
			else if (action.Action_Target == ActionTarget.AllEnemies)
			{
				//Currently, this one here is the only reason I cannot just have an ApplyAction() at the very bottom of this message. Because this targeting 
				//style needs to be ran through multiple times unless I want to allow a list of enemies to be the target.
				for (int i = 0; i < CurrentRoom.Enemies.Count; i++)
				{
					GameEnemy enemy = CurrentRoom.Enemies[i];
					//This bit is a smidge weird, because it is reseting the target on the action each time it loops through, but it is how it is (for now at least).
					action.Target = enemy;
					ApplyAction(action);
				}
			}
			else if (action.Action_Target == ActionTarget.Self)
			{
				action.Target = action.Source;
				ApplyAction(action);
			}
		}

		//This is used when an action requires you to select a target.
		private static void ProcessSubAction(GameAction subAction)
		{

			int target = GetTarget(); //currently this only cares about targeting enemies. Eventually we will also have to target cards in hand tho, keep that in mind.

			GameEnemy currentEnemy = CurrentRoom.Enemies[target];
			overArchingEnemyTarget = currentEnemy;
			subAction.Target = currentEnemy;

			if (subAction.Action_Type == ActionType.Kill)
			{
				subAction.Source = Player;
				subAction.ActionStrength = currentEnemy.Health+currentEnemy.CurrentBlock; //Should in theory be enough to kill it.
				subAction.Action_Type = ActionType.Damage; //Don't want to code in a Kill yet, but we should be able to kill just via the normal damage stuff.
				ProcessAction(subAction);
			}
			else if ((subAction.Action_Type == ActionType.Damage)|| (subAction.Action_Type == ActionType.ApplyCondition))
			{
				ProcessAction(subAction);
			}
		}

		#region Subregion: Actually DO the actions

		private static void ApplyBlock(GameAction action)
		{
			float blockPostConditions = action.ActionStrength;

			//Get any Source Conditions the creature has related to combat damage.

			List<GameStatusCondition> sourceConditions = new List<GameStatusCondition>(); 
			if(action.Source != null && action.Source.Conditions != null)
                sourceConditions = action.Source.Conditions.Where(x => x.AppliedOn.Contains("OnGiveBlock")).ToList();

			List<GameStatusCondition> targetConditions = new List<GameStatusCondition>();
            if (action.Target != null && action.Target.Conditions != null)
				targetConditions = action.Target.Conditions.Where(x => x.Triggers.Contains("OnGainBlock")).ToList();


            blockPostConditions = ApplyConditionsToNumber(blockPostConditions, targetConditions, sourceConditions);

			//Instead of using action number, change this to damagePostConditions, I believe casting to an int will round down, but that is something to check later.
			for (int blockToGain = (int)blockPostConditions; blockToGain >0; blockToGain--)
			{
				if(action.Target != null)
					action.Target.CurrentBlock++;
			}

		}
		private static void ApplyCondition(GameAction action)
		{
			//just converts the action's string value into the actual condition object that contains the information about
			//how the condition works.
			//just placing this here to deal with possible dereference of null reference
			if (action.Target == null)
				return;

			for (int i = 0; i < action.ActionStrength; i++)
			{

				//If we cannot find the condition, that is a problem and I don't mind a hard crash here to let me know.
				GameStatusCondition condition = AllConditions.First(c => c.StatusConditionName == action.Condition).Copy(action.Target);

				if (action.Target.Conditions.Where(x => x.StatusConditionName == condition.StatusConditionName).FirstOrDefault() != null)
				{
					//if the target already has the condition...
					action.Target.Conditions.Where(x => x.StatusConditionName == condition.StatusConditionName).First().Count++;
					//then just bump the count up by one.
				}
				else //else it needs it's first instance of it.
					action.Target.Conditions.Add(condition);

			}
		}

		private static void ApplyDamage(GameAction action)
		{
			float damagePostConditions = action.ActionStrength;

			//Get any Source Conditions the creature has related to combat damage.
            List<GameStatusCondition> sourceConditions = new List<GameStatusCondition>();
            if (action.Source != null && action.Source.Conditions != null)
                sourceConditions = action.Source.Conditions.Where(x => x.AppliedOn.Contains("CombatDamage")).ToList();

            List<GameStatusCondition> targetConditions = new List<GameStatusCondition>();
            if (action.Target != null && action.Target.Conditions != null)
                targetConditions = action.Target.Conditions.Where(x => x.Triggers.Contains("TakeCombatDamage")).ToList();



            damagePostConditions = ApplyConditionsToNumber(damagePostConditions, targetConditions, sourceConditions);

			//Instead of using action number, change this to damagePostConditions, I believe casting to an int will round down, but that is something to check later.
			for (int damage = (int)damagePostConditions; damage >0; damage--)
			{
				if (action.Target == null)
					continue; 

				if (action.Target.CurrentBlock > 0)
					action.Target.CurrentBlock--;
				else
					action.Target.Health--;
			}

            foreach (GameStatusCondition condition in targetConditions.Where(x => x.ApplicationFormula == string.Empty).ToList())
            {
                //this bit is new, because well Curl Up is a status on a target taking damage, that doesn't modify the damage at all... so yup. This should definitely be cleaned up somewhere else
				foreach(CardActionSet actionSet in condition.ActionSets.Where(x=>x.Triggers.Contains(Trigger.TakeCombatDamage)).ToList())
				{
					//Welp, we've gotten it to apply a single block.. hah.
					foreach(GameAction x in actionSet.Actions)
					{
						//THIS IS WORKING BUT IS WRONG
						//THIS IS HARD CODING THINGS IN
						//AND I CANNOT HAVE THAT.
						//THINK ABOUT THIS MORE LUKE.
						x.ActionStrength = condition.Count;
						ProcessAction(x);
						action.Target.Conditions.Remove(action.Target.Conditions.Where(x => x.StatusConditionName == condition.StatusConditionName).First());
					}
				}
            }

            if (action.Target != null && action.Target.Health <= 0 && action.Target.GetType() != typeof(Player))//If the action is palyer, we cannot remove that, hehe
			{
				//Should a dispose method exist?
				CurrentRoom.Enemies.Remove((GameEnemy)action.Target);
			}

		}

		private static void ApplyHeal(GameAction action)
		{
			float healPostConditions = action.ActionStrength;

            //TODO determine good name for healing related triggers? Right now no formula's to modify healing will be found.
            List<GameStatusCondition> sourceConditions = new List<GameStatusCondition>();
            if (action.Source != null && action.Source.Conditions != null)
                sourceConditions = action.Source.Conditions.Where(x => x.AppliedOn.Contains("Healing")).ToList();

            List<GameStatusCondition> targetConditions = new List<GameStatusCondition>();
            if (action.Target != null && action.Target.Conditions != null)
                targetConditions = action.Target.Conditions.Where(x => x.Triggers.Contains("Healing")).ToList();


            healPostConditions = ApplyConditionsToNumber(healPostConditions, targetConditions, sourceConditions);

			for (int heal = (int)healPostConditions; heal >0; heal--)
			{
				if (action.Target == null)
					continue;
				//We need to look into like 'max health' or something in the near future for this.
				action.Target.Health++;
			}
		}

		private static float ApplyConditionsToNumber(float actionNumber, List<GameStatusCondition> targetConditions, List<GameStatusCondition> sourceConditions)
		{
			float returnVariable = actionNumber;
			if (sourceConditions != null)
			{
				returnVariable = ApplyConditionFormulasToNumber(sourceConditions, returnVariable);
			}
			if (targetConditions != null)
			{
				returnVariable = ApplyConditionFormulasToNumber(targetConditions, returnVariable);
			}

			return returnVariable;
		}
		
		#endregion
		#endregion

		#region Card Zone Related

		private static void DrawACard()
		{
			if (Player.Deck.Count == 0)
			{
				for (int discardedCardNumber = Player.Discard.Count; discardedCardNumber > 0; discardedCardNumber--)
				{
					ChangeCardZone(Player.Discard[discardedCardNumber-1], Player.Discard, Player.Deck);
				}
				ShuffleDeck();
			}

			GameCard card = Player.Deck.First();
			ChangeCardZone(card, Player.Deck, Player.Hand);
		}

		private static void DrawToHandSize()
		{
			for (int i = 0; i < Player.HandSize; i++)
			{
				DrawACard();
			}
		}

		private static void ChangeCardZone(GameCard cardToDiscard, List<GameCard> removalZone, List<GameCard> additionZone)
		{
			removalZone.Remove(cardToDiscard);
			additionZone.Add(cardToDiscard);
		}

		private static void ShuffleDeck()
		{
			int deckCount = Player.Deck.Count;
			for (int cardNumber = Player.Deck.Count - 1; cardNumber > 1; cardNumber--)
			{
				int rnd = RandomNumberGenerator.Next(cardNumber + 1);

				GameCard value = Player.Deck[rnd];
				Player.Deck[rnd] = Player.Deck[cardNumber];
				Player.Deck[cardNumber] = value;
			}
		}

		#endregion

		//Yucky yucky, gotta interact with the console here in the GameManager? I don't like that, so am putting this way down here.
		//yucky yucky, but easy to change down the line at least.
		private static int GetTarget()
		{
			Console.WriteLine("Select target");
			string? input = Console.ReadLine();
			input = (input != null) ? input : "";

			return int.Parse(input)-1;
		}

        public static bool CheckTrigger(Trigger trig)
        {
			//Triggers here such as:

			//Number of Enemies
			//Player Health threshholds
			//check health of current dude?

			//This will need more thought process I feel like.
            throw new NotImplementedException();
        }
    }
}
using CGTestng;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace CGTesting
{
	static class ParsingUtility
	{
		public static List<iActionSet> ParseActionSetJSON(JArray ActionSets, ActionSetType actionSetType)
		{
			List<iActionSet> actionSets = new List<iActionSet>();

			foreach (JObject jcard in ActionSets)
			{

				JToken? setNameJT = jcard["ActionSetName"];
				string cardName = setNameJT != null ? setNameJT.ToString() : "";

				JToken? descriptionJT = jcard["ActionSetDescription"];
				string cardDescription = descriptionJT != null ? descriptionJT.ToString() : "";

				JToken? chanceJT = jcard["Chance"];
				int chance = 0; int.TryParse(chanceJT != null ? chanceJT.ToString() : "", out chance);


				JToken? jTriggers = jcard["triggers"];
				JArray jtriggers = (jTriggers != null) ? (JArray)jTriggers : new JArray();

				iActionSet actionSet;



				if (actionSetType == ActionSetType.Enemy)
				{
					actionSet = new EnemyActionSet(cardName, cardDescription, chance);
				}
				else //if (actionSetType == ActionSetType.Card)
				{
					actionSet = new CardActionSet();
				}

				foreach (JValue value in jtriggers)
				{
					if (actionSet.Triggers == null)
						continue;
					actionSet.Triggers.Add((Trigger)Enum.Parse(typeof(Trigger), value.ToString()));
				}

				JToken? ca = jcard["actions"];
				if (ca != null)
					ParseActionJSON((JArray)ca, actionSet.Actions);

				actionSets.Add(actionSet);

			}
			return actionSets;
		}

		public static void ParseCardsJSON(JArray cards, List<GameCard>? overrideAddLocation = null)
		{
			foreach (JObject jcard in cards)
			{

				JToken? cn = jcard["CardName"];
				string cardName = cn != null ? cn.ToString() : "";

				JToken? cd = jcard["CardDescription"];
				string cardDescription = cd != null ? cd.ToString() : "";

				if (cardDescription.Length > 20)
					cardDescription = cardDescription.Substring(0, 17) + "..."; //Just putting this here for now because I'm tired of weirdly long cards but don't want to go through the hassle of making another line of text.

				JToken? cc = jcard["CardCost"];
				int cardCost = cc != null ? (int)cc : 0;

				GameCard card = new GameCard(cardName, cardDescription, cardCost);

				JToken? ca = jcard["actionsets"];
				if (ca != null)
					ParseCardActionSetJSON((JArray)ca, card.CardActionSets);

				JToken? jTags = jcard["CardTags"];
				JArray jtags = (jTags != null) ? (JArray)jTags : new JArray();

				JToken? jtPR = jcard["PlayRequirement"];
				JObject? jPlayRequirement = jtPR != null ? (JObject)jtPR : null;

				if (jPlayRequirement != null)
				{
					string prZone, prCardTag, prAmount;

					JToken? zone = jPlayRequirement["Zone"];
					prZone = zone != null ? zone.ToString() : "";

					JToken? cardTag = jPlayRequirement["CardTag"];
					prCardTag = cardTag != null ? cardTag.ToString() : "";

					JToken? amount = jPlayRequirement["Amount"];
					prAmount = amount != null ? amount.ToString() : "";

					card.Requirement = new PlayRequirement(prZone, prCardTag, prAmount);
				}

				foreach (JValue value in jtags)
				{
					card.CardTags.Add(value.ToString());
				}

				if (overrideAddLocation != null)
				{
					overrideAddLocation.Add(card);
				}
				else
					GameManager.AllCards.Add(card);
			}
		}

		private static void ParseCardActionSetJSON(JArray ActionSets, List<CardActionSet> overrideAddLocation)
		{
			List<iActionSet> list = ParseActionSetJSON(ActionSets, ActionSetType.Card);

			foreach (iActionSet actionSet in list)
			{
				overrideAddLocation.Add((CardActionSet)actionSet);
			}
		}

		private static void ParseEnemyActionSetJSON(JArray ActionSets, List<EnemyActionSet> overrideAddLocation)
		{
			List<iActionSet> list = ParseActionSetJSON(ActionSets, ActionSetType.Enemy);

			foreach (iActionSet actionSet in list)
			{
				overrideAddLocation.Add((EnemyActionSet)actionSet);
			}
		}

		public static void ParseConditionsJSON()
		{
			string jsonString = File.ReadAllText("JSON/StatusConditions.json");
			JObject json = JObject.Parse(jsonString);

			//Doing this JToken? stuff to get rid of derefence of possible null reference, it's annoying but it is what it is. (though I think an actual crash would be more informative?)
			JToken? jConditions = json["StatusConditions"];
			JArray conditions = jConditions != null ? (JArray)jConditions : new JArray();

			foreach (JObject jCondition in conditions)
			{
				GameStatusCondition gameCondition = new GameStatusCondition();

				JToken? cn = jCondition["name"];
				gameCondition.StatusConditionName = cn != null ? cn.ToString() : "";

				JToken? jTriggers = jCondition["triggers"];
				JArray jtriggers = (jTriggers != null) ? (JArray)jTriggers : new JArray();

				foreach (JValue value in jtriggers)
				{
					gameCondition.Triggers.Add(value.ToString());
				}


				JToken? stacks = jCondition["stacks"];
				gameCondition.Stacks = stacks != null ? ((bool)stacks) : false;

				JToken? decayRate = jCondition["decay"];
				gameCondition.DecayRate = decayRate != null ? int.Parse(decayRate.ToString()) : 1;

				JToken? appliedOnArray = jCondition["appliedon"];
				JArray appliedOn = (appliedOnArray != null) ? (JArray)appliedOnArray : new JArray();

				foreach (JValue appliedOnValue in appliedOn)
					gameCondition.AppliedOn.Add(appliedOnValue.ToString());

				JToken? formula = jCondition["applicationFormula"];
				gameCondition.ApplicationFormula = formula != null ? formula.ToString() : "";


                JToken? jActionSets = jCondition["actionsets"];
                JArray aArray = (jActionSets != null) ? (JArray)jActionSets : new JArray();

                ParseCardActionSetJSON(aArray, gameCondition.ActionSets);


                GameManager.AllConditions.Add(gameCondition);
			}
		}

		public static void ParseEnemyJSON()
		{
			//Here is where we will define how we parse enemy JSON, and then construct said Enemies, okay, the simpliest json parsing ever is happening here, but that's something at least.
			string jsonString = File.ReadAllText("JSON/enemy.json");

			JObject json = JObject.Parse(jsonString);
			JToken? jEnemies = json["enemies"];
			JArray enemies = jEnemies != null ? (JArray)jEnemies : new JArray();

			foreach (JObject child in enemies.Children<JObject>())
			{
				JToken? jName = child["name"];
				string name = jName != null ? jName.ToString() : "";
				//Health Refactor begin
				JToken? jHealth = child["health"];

				string health = jHealth != null ? jHealth.ToString() : "";
				string[] healthA = health.Contains(',') ? health.Split(',') : new string[] { health };

				int healthMin = int.Parse(healthA[0]);
				int healthMax = healthA.Length > 1 ? int.Parse(healthA[1]) : healthMin;


				JToken? jActions = child["actionsets"];
				JArray aArray = (jActions != null) ? (JArray)jActions : new JArray();
				//this will fill out the enemy actions...

				//refactored a bit so I could included actions in constructor...
				List<EnemyActionSet> actions = new List<EnemyActionSet>();
				ParseEnemyActionSetJSON(aArray, actions);


                JToken? startingConditionsToken = child["StartingConditions"];
				JArray? startingConditionsJArray= startingConditionsToken != null ? (JArray)startingConditionsToken : null;

				List<StartingConditionDTO> conditionDTOs = new List<StartingConditionDTO>();

				if (startingConditionsJArray != null)
                    conditionDTOs = ParseStartingConditionsArray(startingConditionsJArray);

                JToken? jIntentSystem = child["IntentSystem"];
				JObject oIntentSystem = (JObject)jIntentSystem; //yeah yeah yeah, omfg, you might be null here, how scary.

				IntentSystem intent = ParseEnemyIntentSystem(oIntentSystem);

				GameEnemy gameEnemyToAdd = new GameEnemy(name, healthMin, healthMax, actions, intent, conditionDTOs);


				GameManager.AllEnemies.Add(gameEnemyToAdd);

			}
		}

		public static void ParseRelicsJSON()
		{
			string jsonString = File.ReadAllText("JSON/relics.json");
			JObject json = JObject.Parse(jsonString);

			JToken? jRelics = json["relics"];
			JArray relics = jRelics != null ? (JArray)jRelics : new JArray();

			foreach (JObject jRelic in relics)
			{

				JToken? rn = jRelic["name"];
				string relicName = rn != null ? rn.ToString() : "";

				JToken? rd = jRelic["description"];
				string relicDescription = rd != null ? rd.ToString() : "";

				JToken? rf = jRelic["flavor"];
				string relicFlavor = rf != null ? rf.ToString() : "";

				GameRelic gameRelic = new GameRelic(relicName, relicDescription, relicFlavor);

				JToken? jActionSets = jRelic["actionsets"];
				JArray aArray = (jActionSets != null) ? (JArray)jActionSets : new JArray();

				ParseCardActionSetJSON(aArray, gameRelic.ActionSets);


				GameManager.AllRelics.Add(gameRelic);
			}
		}

		public static void ParseRoomsJSON()
		{
			string jsonString = File.ReadAllText("JSON/rooms.json");
			JObject json = JObject.Parse(jsonString);
			JToken? jRooms = json["rooms"];
			JArray rooms = jRooms != null ? (JArray)jRooms : new JArray();

			foreach (JObject child in rooms.Children<JObject>())
			{
				GameRoom newRoom = new GameRoom();

				JToken? jName = child["RoomName"];
				newRoom.RoomName = jName != null ? jName.ToString() : "";
				JToken? jHealth = child["health"];

				JToken? jActions = child["EnemyIDs"];
				JArray aArray = (jActions != null) ? (JArray)jActions : new JArray();

				foreach (JValue value in aArray)
				{
					//Should error if it cannot find an enemy the room wants, which will let me know the code is fudged up. Eventually add code to handle an error though.
					newRoom.Enemies.Add(GameManager.AllEnemies.Where(x => x.EnemyName == value.ToString()).First().Copy());
				}
				GameManager.AllRooms.Add(newRoom);
			}
		}

		public static IntentSystem ParseEnemyIntentSystem(JObject jIntentSystem)
		{
			//Okay, so I do need to make sure pretty much all of these things are, well, are not null.
			//Because for the simple intent systems, most of them WILL be null...
			Dictionary<string, int> firstTurnActionOptions = new Dictionary<string, int>();
			int? maxRepeats = 0;
			Tuple<string, int>? loopedAction = null;
			List<IntentActionDTO> nonLoopedActions = new List<IntentActionDTO>();
			Dictionary<string, List<Trigger>> triggerActions = new Dictionary<string, List<Trigger>>();

			//First Turn Action Options
			JArray firstTurnActionOptionsJSONARRAY = (JArray)jIntentSystem["FirstTurnActionOptions"];
			if (firstTurnActionOptionsJSONARRAY != null)
			{
				foreach (JObject firstTurnActionOption in firstTurnActionOptionsJSONARRAY)
				{
					string name = firstTurnActionOption["Name"].ToString();
					int chance = int.Parse(firstTurnActionOption["Chance"].ToString());
					firstTurnActionOptions.Add(name, chance);
				}
			}
			JToken? jMaxRepeat = jIntentSystem["MaxRepeats"];
			//MaxRepeats
			maxRepeats = jMaxRepeat != null ? int.Parse(jMaxRepeat.ToString()) : null;

			//Looped Actions
			JObject loopedActionJ = (JObject)jIntentSystem["LoopedAction"];
			if (loopedActionJ != null)
			{
				string loopedActionName = loopedActionJ["LoopedActionName"].ToString();
				int loopEvery = int.Parse(loopedActionJ["LoopEvery"].ToString());
				loopedAction = new Tuple<string, int>(loopedActionName, loopEvery);
			}

			//Non Looped Actions
			JArray nonloopedActionsJSONARRAY = (JArray)jIntentSystem["NonLoopedActions"];
			if (nonloopedActionsJSONARRAY != null)
			{
				foreach (JObject nonLoopedAction in nonloopedActionsJSONARRAY)
				{
					string name = nonLoopedAction["Name"].ToString();
					int chance = int.Parse(nonLoopedAction["Chance"].ToString());

					JToken? nextInLineToken = nonLoopedAction["NextInLine"];
					string? nextInLine = (nextInLineToken != null) ? nextInLineToken.ToString() : null;
					JToken? maxToken = nonLoopedAction["CanBeUsedMaxOf"];

					int? canBeUsedMaxOf = maxToken != null ? int.Parse(maxToken.ToString()) : null;

					nonLoopedActions.Add(new IntentActionDTO(name, chance, nextInLine, canBeUsedMaxOf));
				}
			}
			//Triggered Actions
			JArray triggerActionsJSONARRAY = (JArray)jIntentSystem["TriggerActions"];
			if (triggerActionsJSONARRAY != null)
			{
				foreach (JObject triggerActionOptionOBJECT in triggerActionsJSONARRAY)
				{
					string name = triggerActionOptionOBJECT["Name"].ToString();
					List<Trigger> trigs = new List<Trigger>();
					JArray triggerArray = (JArray)triggerActionOptionOBJECT["TriggersOn"];
					foreach (JValue value in triggerArray)
					{
						trigs.Add((Trigger)Enum.Parse(typeof(Trigger), value.ToString()));
					}
					triggerActions.Add(name, trigs);
				}
			}

			return new IntentSystem(firstTurnActionOptions, maxRepeats, loopedAction, nonLoopedActions, triggerActions);

		}


		//This needs to be able to parse actions from multiple places, and spit them out to multiple places, it's going to be probably be the most verstile of the JSON parsing?
		//It's the 'crux' of how the game will ultimately function. This will need to be able to parse many many possibilities I feel like?
		private static void ParseActionJSON(JArray actions, List<GameAction>? addLocation)
		{
			if (addLocation == null)
				addLocation = new List<GameAction>();
			foreach (JObject child in actions.Children<JObject>())
			{
				//Action's ultimately should not have names. Names should be a card thing.
				//string name = child["ActionName"].ToString();
				JToken? jType = child["ActionType"];
				int type = (jType != null) ? int.Parse(jType.ToString()) : 0;
				JToken? jActionTarget = child["ActionTarget"];
				int target = (jActionTarget != null) ? int.Parse(jActionTarget.ToString()) : 0;
				JToken? jMultiTargetNum = child["MultiTargetNum"];
				int targetnum = (jMultiTargetNum != null) ? int.Parse(jMultiTargetNum.ToString()) : 0;

				//Need to mimic the enemy health parse logic here...
				JToken? jActionStrength = child["ActionStrength"];
				//int actionstrength = (jActionStrength!=null) ? int.Parse(jActionStrength.ToString()) : 0;

				string actionStrength = jActionStrength != null ? jActionStrength.ToString() : "";
				string[] actionStrengthArray = actionStrength.Contains(',') ? actionStrength.Split(',') : new string[] { actionStrength };

				int strengthMin = int.Parse(actionStrengthArray[0]);
				int strengthMax = actionStrengthArray.Length > 1 ? int.Parse(actionStrengthArray[1]) : strengthMin;
				///

				//					"ActionZoneTarger": 2,
				JToken? actionZoneTriggerJT = child["ActionZoneTarget"];
				int actionZoneTrigger = (actionZoneTriggerJT != null) ? (int)actionZoneTriggerJT : 0;

				JToken? JCondition = child["Condition"];
				string condition = (JCondition != null) ? JCondition.ToString() : "";

				GameAction gameAction = new GameAction((ActionType)type, (ActionTarget)target, targetnum, strengthMin, strengthMax);
				gameAction.Condition = condition;
				gameAction.ActionZoneTarget = (Zone)actionZoneTrigger;

				addLocation.Add(gameAction);
			}
		}

		private static List<StartingConditionDTO> ParseStartingConditionsArray(JArray startingConditionsArray)
		{
			List<StartingConditionDTO> startingConditions = new List<StartingConditionDTO>();
			if (startingConditionsArray != null)
			{
				foreach (JObject startingConditionObject in startingConditionsArray)
				{

					JToken? nameToken = startingConditionObject["Name"];
					string name = nameToken != null ? nameToken.ToString() : "ERROR";

					JToken? minToken = startingConditionObject["Min"];
					int min = minToken != null ? int.Parse(minToken.ToString()) : 1;


					JToken? maxToken = startingConditionObject["Max"];
					int max = maxToken != null ? int.Parse(maxToken.ToString()) : 1;

					startingConditions.Add(new StartingConditionDTO(name, min, max, null));
				}
			}
			return startingConditions;
		}
	
	}
}


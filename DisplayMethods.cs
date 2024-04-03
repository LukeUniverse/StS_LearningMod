using CGTestng;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Mime;
using System.Text;
using System.Threading.Tasks;

namespace CGTesting
{
	public static class DisplayMethods
	{
		//This will be useful for box drawing: https://en.wikipedia.org/wiki/List_of_Unicode_characters#Box_Drawing
		public static char BOX_TOP = '─';
		public static char BOX_TOP_LEFTCORNER = '┌';
		public static char BOX_TOP_RIGHTCORNER = '┐';

		public static char BOX_BOTTOM = '─';
		public static char BOX_BOTTOM_LEFTCORNER = '└';
		public static char BOX_BOTTOM_RIGHTCORNER= '┘';

		public static char BOX_WALL = '│';

		public static char TAB_Character = '\t';
		public static char SPACE_Character = ' ';

		static public void RefreshDisplay(bool ClearDisplay)
		{
			if(ClearDisplay)
				Console.Clear();

			Console.WriteLine("Enemies:");
			DisplayEnemies();
			Console.WriteLine("Player Information:");
			DisplayPlayerInformation();
			Console.WriteLine("Actions:");
			DisplayHand(); // will eventually be display hand or something like that.

			if (GameManager.ErrorMessages != null && GameManager.ErrorMessages.Count >0)
				foreach(string errorMessage in GameManager.ErrorMessages) Console.WriteLine("Error Message: "+errorMessage);
		}

		static public void DisplayHand()
		{
			//Come back to refact for longer text strings at some point.

			StringBuilder TOPLINE = new StringBuilder();
			StringBuilder NAMEANDENERGYLINE = new StringBuilder();
			StringBuilder DESCRIPTIONLINE = new StringBuilder();
			StringBuilder BOTLINE = new StringBuilder();

			int boxLength = 15; //for now just use a hardcoedvalue here

			foreach (GameCard card in GameManager.Player.Hand)
			{
				//I'm assuming here CardDescription will always be the 'longest' section. Might want to do a breakdown of this into more than one line though?
				boxLength =card.CardDescription.Length + 5;

				TOPLINE.Append(BOX_TOP_LEFTCORNER);
				for (int space = 0; space < boxLength; space++)
					TOPLINE.Append(BOX_TOP);
				TOPLINE.Append($"{BOX_TOP_RIGHTCORNER}{TAB_Character}");

				int spaceNeededForName = boxLength - card.CardName.Length - card.CardCost.ToString().Length - 3;
				NAMEANDENERGYLINE.Append(BOX_WALL+ $"({card.CardCost}) " +card.CardName);
				for (int space = 0; space < spaceNeededForName; space++)
					NAMEANDENERGYLINE.Append(SPACE_Character);
				NAMEANDENERGYLINE.Append($"{BOX_WALL}{TAB_Character}");

				
					int spaceNeededForDescription = boxLength - card.CardDescription.Length;
					DESCRIPTIONLINE.Append($"{BOX_WALL}{card.CardDescription}");
					for (int space = 0; space < spaceNeededForDescription; space++)
						DESCRIPTIONLINE.Append(SPACE_Character);
					DESCRIPTIONLINE.Append($"{BOX_WALL}{TAB_Character}");
				
				BOTLINE.Append(BOX_BOTTOM_LEFTCORNER);
				for (int space = 0; space < boxLength; space++)
					BOTLINE.Append(BOX_BOTTOM);
				BOTLINE.Append($"{BOX_BOTTOM_RIGHTCORNER}{TAB_Character}");
			}

			Console.WriteLine(TOPLINE);
			Console.WriteLine(NAMEANDENERGYLINE);
			Console.WriteLine(DESCRIPTIONLINE);
			Console.WriteLine(BOTLINE);
		}


		//I feel like at some point I should condense Display Enemies and Display Player Information down to a set of shared things... Heck, maybe all the displays? I don't know.

		static public void DisplayEnemies()
		{
			StringBuilder TOPLINE = new StringBuilder();
			StringBuilder NAMELINE = new StringBuilder();
			StringBuilder HEALTHLINE = new StringBuilder();
			StringBuilder BLOCKLINE = new StringBuilder();
			StringBuilder CONDITIONSLINE = new StringBuilder();
			StringBuilder ACTIONLINE = new StringBuilder();
			StringBuilder BOTTOMLINE = new StringBuilder();


			//Okay, so I've made it a bit more complicated by doing a for loop for each line, but that should also open me up to use any length of string I want here on the lines...
			//long as this works how I think it will.
			int length;

			//currently there are 8 spaces between line and line, 3 between box and box
			foreach (GameEnemy enemy in GameManager.CurrentRoom.Enemies)
			{
				//hehehe even better...
				length = enemy.EnemyName.Length + 6;

				TOPLINE.Append(BOX_TOP_LEFTCORNER);
				for (int i = 0; i < length; i++)
				{
					TOPLINE.Append(BOX_TOP);
				}
				TOPLINE.Append($"{BOX_TOP_RIGHTCORNER}{TAB_Character}");

				NAMELINE.Append($"{BOX_WALL}Name: {enemy.EnemyName}");
				int spaceNeededForName = length - 6 - enemy.EnemyName.Length; //(the 6 is for "Name: ")
				for (int i = 0; i < spaceNeededForName; i++)
				{
					NAMELINE.Append(SPACE_Character);
				}
				NAMELINE.Append($"{BOX_WALL}{TAB_Character}");

				HEALTHLINE.Append($"{BOX_WALL}HP: {enemy.Health}");
				int spaceNeededForHealth = length -4 -enemy.Health.ToString().Length; //4 is for "HP: "
				for (int i = 0; i < spaceNeededForHealth; i++)
				{
					HEALTHLINE.Append(SPACE_Character);
				}
				HEALTHLINE.Append($"{BOX_WALL}{TAB_Character}");

				int spaceNeededForBlock = length - 3 - enemy.CurrentBlock.ToString().Length;
				BLOCKLINE.Append($"{BOX_WALL}B: {enemy.CurrentBlock}");
				for (int i = 0; i < spaceNeededForBlock; i++)
				{
					BLOCKLINE.Append(SPACE_Character);
				}
				BLOCKLINE.Append($"{BOX_WALL}{TAB_Character}");

				string conditions = BuildConditionsString(enemy.Conditions);
				CONDITIONSLINE.Append($"{BOX_WALL}C: {conditions}");
				int spaceNeededForConditions = length -3 -conditions.ToString().Length; //4 is for "HP: "
				for (int i = 0; i < spaceNeededForConditions; i++)
				{
					CONDITIONSLINE.Append(SPACE_Character);
				}
				CONDITIONSLINE.Append($"{BOX_WALL}{TAB_Character}");

				//This needs some sort of 'Upcoming Action' or something
				if (enemy.NextActionSet != null)
				{
					ACTIONLINE.Append($"{BOX_WALL}{enemy.NextActionSet.SetDescription}");
					int spaceNeededForDamage = length - enemy.NextActionSet.SetDescription.ToString().Length;
					for (int i = 0; i < spaceNeededForDamage; i++)
					{
						ACTIONLINE.Append(SPACE_Character);
					}
					ACTIONLINE.Append($"{BOX_WALL}{TAB_Character}");
				}
				BOTTOMLINE.Append(BOX_BOTTOM_LEFTCORNER);
				for (int i = 0; i < length; i++)
				{
					BOTTOMLINE.Append(BOX_BOTTOM);
				}
				BOTTOMLINE.Append($"{BOX_BOTTOM_RIGHTCORNER}{TAB_Character}");
			}

			Console.WriteLine(TOPLINE);
			Console.WriteLine(NAMELINE);
			Console.WriteLine(HEALTHLINE);
			Console.WriteLine(BLOCKLINE);
			Console.WriteLine(CONDITIONSLINE);
			Console.WriteLine(ACTIONLINE);
			Console.WriteLine(BOTTOMLINE);
		}

		static public void DisplayPlayerInformation()
		{
			StringBuilder topLine = new StringBuilder();
			StringBuilder healthLine = new StringBuilder();
			StringBuilder blockLine = new StringBuilder();
			StringBuilder CONDITIONSLINE = new StringBuilder();
			StringBuilder energyLine = new StringBuilder();
			StringBuilder deckLine = new StringBuilder();
			StringBuilder discardLine = new StringBuilder();
			StringBuilder bottomLine = new StringBuilder();

			int length = 15;

			topLine.Append(BOX_TOP_LEFTCORNER);
			for (int i = 0; i < length; i++)
			{
				topLine.Append(BOX_TOP);
			}
			topLine.Append($"{BOX_TOP_RIGHTCORNER}{TAB_Character}");


			int spaceNeededForHealth = length - 4 - GameManager.Player.Health.ToString().Length;
			healthLine.Append($"{BOX_WALL}HP: {GameManager.Player.Health}");
			for (int i = 0; i < spaceNeededForHealth; i++)
			{
				healthLine.Append(SPACE_Character);
			}
			healthLine.Append($"{BOX_WALL}{TAB_Character}");

			int spaceNeededForBlock = length - 3 - GameManager.Player.CurrentBlock.ToString().Length;
			blockLine.Append($"{BOX_WALL}B: {GameManager.Player.CurrentBlock}");
			for (int i = 0; i < spaceNeededForBlock; i++)
			{
				blockLine.Append(SPACE_Character);
			}
			blockLine.Append($"{BOX_WALL}{TAB_Character}");


			string conditions = BuildConditionsString(GameManager.Player.Conditions);
            CONDITIONSLINE.Append($"{BOX_WALL}C: {conditions}");
			int spaceNeededForConditions = length -3 - conditions.ToString().Length; //3 is for "C: "
			for (int i = 0; i < spaceNeededForConditions; i++)
			{
				CONDITIONSLINE.Append(SPACE_Character);
			}
			CONDITIONSLINE.Append($"{BOX_WALL}{TAB_Character}");


			int spaceNeededForEnergy = length - 5 - GameManager.Player.CurrentEnergy.ToString().Length;
			energyLine.Append($"{BOX_WALL}[E]: {GameManager.Player.CurrentEnergy}");
			for (int i = 0; i < spaceNeededForEnergy; i++)
			{
				energyLine.Append(SPACE_Character);
			}
			energyLine.Append($"{BOX_WALL}{TAB_Character}");

			int spaceNeededForDeck = length - 8 - GameManager.Player.Deck.Count().ToString().Length;
			deckLine.Append($"{BOX_WALL}[Deck]: {GameManager.Player.Deck.Count}");
			for (int i = 0; i < spaceNeededForDeck; i++)
			{
				deckLine.Append(SPACE_Character);
			}
			deckLine.Append($"{BOX_WALL}{TAB_Character}");

			int spaceNeededForDiscard = length - 11 - GameManager.Player.Discard.Count.ToString().Length;
			discardLine.Append($"{BOX_WALL}[Discard]: {GameManager.Player.Discard.Count}");
			for (int i = 0; i < spaceNeededForDiscard; i++)
			{
				discardLine.Append(SPACE_Character);
			}
			discardLine.Append($"{BOX_WALL}{TAB_Character}");

			bottomLine.Append(BOX_BOTTOM_LEFTCORNER);
			for (int i = 0; i < length; i++)
			{
				bottomLine.Append(BOX_BOTTOM);
			}
			bottomLine.Append($"{BOX_BOTTOM_RIGHTCORNER}{TAB_Character}");

			Console.WriteLine(topLine);
			Console.WriteLine(healthLine);
			Console.WriteLine(blockLine); 
			Console.WriteLine(CONDITIONSLINE); 
			Console.WriteLine(energyLine);
			Console.WriteLine(deckLine);
			Console.WriteLine(discardLine);
			Console.WriteLine(bottomLine);
		}

		private static string BuildConditionsString(List<GameStatusCondition> conditionsList)
		{
            string conditions = "";
            if (conditionsList.Count > 0)
            {
                foreach (GameStatusCondition condition in conditionsList)
                {
                    if (condition.StatusConditionName == "Weak") //Right now we're hard coding this in, probably want to change that down the line?
                        conditions += "[W";                      //Well, I mean, this is just the display method, which ultimately shouldn't exist
                    else if (condition.StatusConditionName == "Frail")// in the final version of this at all. so maybe this is okay.
                        conditions += "[F";
                    else if (condition.StatusConditionName == "Vulnerable")
                        conditions += "[V";
                    else if (condition.StatusConditionName == "Strength")
                        conditions += "[S";
                    else if (condition.StatusConditionName == "Curl Up")
                        conditions += "[CU";
					else
                        conditions += "[???";

                    if (condition.Count > 1)
                        conditions += condition.Count;
                    conditions += "]";

                }
            }
			return conditions;
        }
	}
}

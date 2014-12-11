package eitan.belote

class CliStrategy implements Strategy
{
  Player player

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    player.showHand()

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

    def card = null

    while (!validCards.contains(card))
    {
      if (card != null) {
        println "${card} is not a valid card, try again.."
      }
      println "${player}: play a card (enter a number): "
      def input = br.readLine()
      def cardIndex = input.toInteger() - 1
      card = player.hand[cardIndex]
    }

    println "${player} plays ${card}"
    card
  }
}

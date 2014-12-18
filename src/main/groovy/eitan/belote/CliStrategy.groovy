package eitan.belote

class CliStrategy implements Strategy
{
  Player player

  @Override
  boolean envoi(Card candidate)
  {
    def response = prompt("Would you like to go for ${candidate.suit} (y/n) ?")
    response?.toLowerCase()?.startsWith("y")
  }

  @Override
  Suit envoi()
  {
    def response = ''
    while (!Suit.isValidAcronym(response)) {
      response = prompt("Any other suit you'd like to go for? (h=heart,d=diamond,s=spade,c=clubs, or p for pass)")
      if (response?.toLowerCase() == 'p') {
        return null
      }
    }
    Suit.interpretSuitFromAcronym(response)
  }

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    player.showHand()

    def card = null

    while (!validCards.contains(card))
    {
      if (card != null) {
        println "${card} is not a valid card, try again.."
      }

      def input = prompt("Play a card (pick a number):")
      def cardIndex = input.toInteger() - 1
      card = player.hand[cardIndex]
    }

    println "${player} plays ${card}"
    card
  }

  // TODO: improve this implementation to use a closure;  figure out how to return response
  private String prompt(String caption) {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
    println "${player}: ${caption} "
    br.readLine()
  }
}

package eitan.belote

class CliStrategy implements Strategy
{
  Player player

  @Override
  void offer(Game game, Card candidate)
  {
    def response = prompt("Would you like to go for ${candidate.suit} (y/n) ?")
    boolean envoi = response?.toLowerCase()?.startsWith("y")
    if (envoi)
    {
      game.envoi()
    }
    else
    {
      game.pass()
    }
  }

  @Override
  void offer(Game game)
  {
    def response = ''
    while (!Suit.isValidAcronym(response)) {
      response = prompt("Any other suit you'd like to go for? (h=heart,d=diamond,s=spade,c=clubs, or p for pass)")
      if (response?.toLowerCase() == 'p') {
        game.passDeuxFois()
        return
      }
    }
    def suit = Suit.interpretSuitFromAcronym(response)
    game.envoi(suit)
  }

  @Override
  void play(Game game, Set<Card> validCards, Round round)
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

    game.playerChooses(card)
  }

  private String prompt(String caption) {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
    println "${player}: ${caption} "
    br.readLine()
  }
}

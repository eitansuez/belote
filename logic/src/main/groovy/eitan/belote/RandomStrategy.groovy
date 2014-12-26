package eitan.belote

class RandomStrategy implements Strategy
{
  Player player

  @Override
  void offer(Game game, Card candidate)
  {
    game.pass()  // alternately game.envoi()
  }

  @Override
  void offer(Game game)
  {
    game.passDeuxFois()  // alternately game.envoi(suit)
  }

  @Override
  void play(Game game, Set<Card> validCards, Round round)
  {
    game.playerChooses(validCards.first())
  }

}

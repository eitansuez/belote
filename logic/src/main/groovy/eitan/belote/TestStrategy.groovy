package eitan.belote

class TestStrategy implements Strategy
{
  Player player

  @Override
  void offer(Game game, Card candidate)
  {
    game.pass()
  }

  @Override
  void offer(Game game)
  {
    game.passDeuxFois()
  }

  @Override
  void play(Game game, Set<Card> validCards, Round round)
  {
    game.playerChooses(round.highest(validCards))
  }

}

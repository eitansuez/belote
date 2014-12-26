package eitan.belote

interface Strategy
{
  void offer(Game game, Card candidate)
  void offer(Game game)
  void play(Game game, Set<Card> validCards, Round round)
}

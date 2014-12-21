package eitan.belote

interface UI
{
  void turnUpCard(Card card)
  void receiveCard(Player player, Card card)

  void playCard(Player player, Card card)
  Card chooseCard(Player player, Set<Card> validCards)
  void clearHand(Player player)

  boolean envoi(Player player, Suit candidateSuit)
  Suit envoi(Player player)

  void roundEnds(Player winner, int points)
  void gameUpdate(Team team1, int score1, Team team2, int score2)
  void gameEnds(Team team1, int score1, Team team2, int score2)
}

package eitan.belote

interface UI
{
  void turnUpCard(Card card)
  void receiveCard(Player player, Card card)

  void playerDecision(Player player, boolean envoi, Suit suit)

  void playCard(Player player, Card card)
  void clearHand(Player player)

  void roundEnds(Player winner, int points)
  void gameUpdate(Team team1, int score1, Team team2, int score2)
  void gameEnds(Team team1, int score1, Team team2, int score2)
}

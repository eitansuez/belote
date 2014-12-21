package eitan.belote

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate

class StompUI implements UI
{
  @Autowired
  private SimpMessagingTemplate template;

  @Override
  void turnUpCard(Card card)
  {
  }

  @Override
  void receiveCard(Player player, Card card)
  {
    // card -> name
  }

  @Override
  void playCard(Player player, Card card)
  {

  }

  @Override
  Card chooseCard(Player player, Set<Card> validCards)
  {
    return null
  }

  @Override
  void clearHand(Player player)
  {

  }

  @Override
  boolean envoi(Player player, Suit candidateSuit)
  {
    return false
  }

  @Override
  Suit envoi(Player player)
  {
    return null
  }

  @Override
  void roundEnds(Player winner, int points)
  {

  }

  @Override
  void gameUpdate(Team team1, int score1, Team team2, int score2)
  {

  }

  @Override
  void gameEnds(Team team1, int score1, Team team2, int score2)
  {

  }
}

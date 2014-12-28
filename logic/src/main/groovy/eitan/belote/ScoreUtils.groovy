package eitan.belote

class ScoreUtils
{
  static int roundScore(int score) {
    Math.round(score/10) * 10
  }
}

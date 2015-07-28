package eitan.belote

import static java.lang.Math.round

class ScoreUtils
{
  static int roundScore(int score) {
    round(score/10) * 10
  }
}

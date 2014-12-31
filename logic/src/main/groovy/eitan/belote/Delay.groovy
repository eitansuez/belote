package eitan.belote

enum Delay {
  Short(100), Standard(250), Long(1000), ExtraLong(3000)

  int delayValue
  Delay(int value)
  {
    delayValue = value
  }
}

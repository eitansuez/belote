package eitan.belote

trait Emitter
{
  UI ui = new TextUI()

  // todo: should be async
  void emit(String event, args) {
    ui.invokeMethod(event, args)
  }
}
package eitan.belote

import akka.actor.UntypedActor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.messaging.simp.SimpMessagingTemplate

import javax.inject.Named

@Named("StompActor")
@Scope("prototype")
class StompActor extends UntypedActor
{
  @Override
  void onReceive(Object message) throws Exception
  {
    if (message instanceof BeloteEvent) {
      sleep(1000)
      BeloteEvent event = (BeloteEvent) message
      sendCmd(event.name, event.args)
    }
  }

  @Autowired
  private final SimpMessagingTemplate template

  private void sendCmd(cmd, args) {
    template.convertAndSend('/topic/belote', [cmd: cmd, args: args.collect { arg -> marshal(arg) }])
  }

  public static marshal(arg) {
    if (arg instanceof Player)
    {
      ((Player) arg).name
    }
    else if (arg instanceof Team)
    {
      ((Team) arg).toString()
    }
    else if (arg instanceof Card)
    {
      ((Card) arg).toString()
    }
    else if (arg instanceof Enum)
    {
      ((Enum) arg).name()
    }
    else if (arg instanceof Collection)
    {
      arg.collect { item -> marshal(item) }
    }
    else {
      arg
    }
  }
}
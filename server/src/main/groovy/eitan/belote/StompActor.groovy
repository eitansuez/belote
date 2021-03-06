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
      BeloteEvent event = (BeloteEvent) message
      if (event.to)
      {
        sendCmdToUser(event.to, event.name, event.args)
      }
      else
      {
        sendCmd(event.name, event.args)
      }
      sleep(event.subsequentDelayMs)
    }
  }

  @Autowired
  private final SimpMessagingTemplate template

  private void sendCmd(cmd, args) {
    template.convertAndSend('/topic/belote', [cmd: cmd, args: args.collect { arg -> marshal(arg) }])
  }

  private void sendCmdToUser(user, cmd, args) {
    template.convertAndSendToUser(user, '/queue/belote', [cmd: cmd, args: args.collect { arg -> marshal(arg) }])
  }

  static marshal(arg) {
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
      ((Card) arg).toString().replaceAll(' ', '_')
    }
    else if (arg instanceof Enum)
    {
      ((Enum) arg).name()
    }
    else if (arg instanceof Collection)
    {
      arg.collect { item -> marshal(item) }
    }
    else if (arg instanceof GString)
    {
      arg.toString()
    }
    else
    {
      arg
    }
  }
}
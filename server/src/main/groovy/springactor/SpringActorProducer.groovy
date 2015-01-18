package springactor

import akka.actor.Actor
import akka.actor.IndirectActorProducer
import org.springframework.context.ApplicationContext

class SpringActorProducer implements IndirectActorProducer
{
  final ApplicationContext applicationContext
  final String actorBeanName

  SpringActorProducer(ApplicationContext applicationContext, String actorBeanName)
  {
    this.applicationContext = applicationContext
    this.actorBeanName = actorBeanName
  }

  @Override
  Actor produce()
  {
    applicationContext.getBean(actorBeanName)
  }

  @Override
  Class<? extends Actor> actorClass()
  {
    applicationContext.getType(actorBeanName)
  }
}
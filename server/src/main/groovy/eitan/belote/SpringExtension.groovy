package eitan.belote

import akka.actor.AbstractExtensionId
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.Props
import org.springframework.context.ApplicationContext

class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt>
{
  static SpringExtension SpringExtProvider = new SpringExtension()

  @Override
  SpringExt createExtension(ExtendedActorSystem system) {
    new SpringExt()
  }

  static class SpringExt implements Extension {
    private volatile ApplicationContext applicationContext

    void initialize(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext
    }

    Props props(String actorBeanName) {
      Props.create(SpringActorProducer.class, applicationContext, actorBeanName)
    }
  }
}
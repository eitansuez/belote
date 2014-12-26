package eitan.belote

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import static eitan.belote.SpringExtension.SpringExtProvider

@SpringBootApplication
@EnableWebSocketMessageBroker
class Application extends AbstractWebSocketMessageBrokerConfigurer
{
  @Override
  void configureMessageBroker(MessageBrokerRegistry registry)
  {
    registry.enableSimpleBroker("/topic")
    registry.setApplicationDestinationPrefixes("/app")
  }

  @Override
  void registerStompEndpoints(StompEndpointRegistry registry)
  {
    registry.addEndpoint("/newPartie").withSockJS()
  }

  @Autowired
  private ApplicationContext applicationContext

  @Bean
  ActorSystem actorSystem()
  {
    ActorSystem system = ActorSystem.create("BeloteWithAkka")
    SpringExtProvider.get(system).initialize(applicationContext)
    system
  }

  static void main(String[] args) {
    SpringApplication.run(Application.class, args)
  }

}

package eitan.belote

import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
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
    registry.enableSimpleBroker("/topic", "/queue")
    registry.setApplicationDestinationPrefixes("/app")
  }

  @Override
  void registerStompEndpoints(StompEndpointRegistry registry)
  {
    registry.addEndpoint("/belote").withSockJS()
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

  @Configuration
  @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
  protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Autowired
    private SecurityProperties security

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {

      auth.authenticationProvider(new AuthenticationProvider() {

        boolean supports(Class<?> authentication) {
          UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
        }

        Authentication authenticate(Authentication authentication) throws AuthenticationException {
          UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication

          new UsernamePasswordAuthenticationToken(token.name, token.credentials, null)
        }
      })


    }

  }

}

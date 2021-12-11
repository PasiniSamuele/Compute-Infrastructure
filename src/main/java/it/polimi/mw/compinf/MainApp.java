package it.polimi.mw.compinf;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import akka.routing.FromConfig;
import akka.actor.OneForOneStrategy;
import akka.japi.pf.DeciderBuilder;
import it.polimi.mw.compinf.http.TaskRegistry;
import it.polimi.mw.compinf.http.TaskRoutes;
import scala.concurrent.ExecutionContextExecutor;

import java.net.InetSocketAddress;
import scala.concurrent.duration.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class MainApp {
	public static int SUPERVISOR_RETRIES = 10;
	public static int SUPERVISOR_PERIOD = 1;

	static void startHttpServer(Route route, akka.actor.typed.ActorSystem<?> system) {
		CompletionStage<ServerBinding> futureBinding =
				Http.get(system).newServerAt("localhost", 8080).bind(route);

		futureBinding.whenComplete((binding, exception) -> {
			if (binding != null) {
				InetSocketAddress address = binding.localAddress();
				system.log().info("Server online at http://{}:{}/",
						address.getHostString(),
						address.getPort());
			} else {
				system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
				system.terminate();
			}
		});
	}

	public static void main(String[] args) {
		ActorSystem sys = ActorSystem.create("system");

		Duration duration = Duration.create(SUPERVISOR_PERIOD, TimeUnit.MINUTES);

		SupervisorStrategy strategy = new OneForOneStrategy(SUPERVISOR_RETRIES, duration, false,
				DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()).build());

		ActorRef router = sys.actorOf(
				FromConfig.getInstance().withSupervisorStrategy(strategy).props(Props.create(Actor.class)), "router");

		//#server-bootstrapping
		Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
			akka.actor.typed.ActorRef<TaskRegistry.Command> userRegistryActor =
					context.spawn(TaskRegistry.create(), "UserRegistry");

			TaskRoutes taskRoutes = new TaskRoutes(context.getSystem(), userRegistryActor);
			startHttpServer(taskRoutes.taskRoutes(), context.getSystem());

			return Behaviors.empty();
		});

		// boot up server using the route as defined below
		akka.actor.typed.ActorSystem<NotUsed> system = akka.actor.typed.ActorSystem.create(rootBehavior, "HelloAkkaHttpServer");
		final ExecutionContextExecutor dispatcher = system.dispatchers().lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"));

		//#server-bootstrapping


		/*for (int i = 0; i < 20; i++) {
			router.tell(new TaskMessage(i), ActorRef.noSender());
		}*/
	}
}

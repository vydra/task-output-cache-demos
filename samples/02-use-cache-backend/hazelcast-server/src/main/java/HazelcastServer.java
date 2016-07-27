import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import io.airlift.airline.Cli;
import io.airlift.airline.Cli.CliBuilder;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;

import java.util.logging.Level;

@SuppressWarnings("WeakerAccess")
public class HazelcastServer {
	public static void main(String... args) {
		@SuppressWarnings("unchecked")
		CliBuilder<Runnable> builder = Cli.<Runnable>builder("hazelcast-server")
				.withDescription("hazelcast server")
				.withDefaultCommand(Help.class)
				.withCommands(Help.class, Run.class);

		Cli<Runnable> parser = builder.build();

		parser.parse(args).run();
	}

	@Command(name = "run", description = "run a Hazelcast server")
	public static class Run implements Runnable {
		@Option(name = {"-p", "--port"}, description = "port to start the server on, defaults to 5701")
		private int port = 5701;

		@Option(name = {"-M", "--enable-multicast"}, description = "enable multicast discovery")
		private boolean multicast = false;

		@Option(name = {"-d", "--debug"}, description = "debug mode")
		private boolean debug = false;

		@Option(name = {"-v", "--verbose"}, description = "verbose mode")
		private boolean verbose = false;

		@Option(name = {"-q", "--quiet"}, description = "quiet mode, only print errors")
		private boolean quiet = false;

		@Override
		public void run() {
			Level logLevel = debug ? Level.FINEST : verbose ? Level.INFO : quiet ? Level.SEVERE : Level.WARNING;
			java.util.logging.Logger.getLogger("com.hazelcast").setLevel(logLevel);
			Config config = new Config();
			config
					.getNetworkConfig()
					.setPort(port)
					.getJoin()
					.getMulticastConfig()
					.setEnabled(multicast);
			Hazelcast.newHazelcastInstance(config);
		}
	}
}

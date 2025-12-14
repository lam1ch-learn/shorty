import cliService.CliCommands;
import cliService.TtlCleanup;
import cliService.UserService;
import config.AppConfig;
import models.User;
import request.RedirectService;
import storage.RedisStore;
import storage.Saveable;

public class Main {
	public static void main(String[] args) {
		Saveable storage = new RedisStore();
		RedirectService redirectService = new RedirectService(storage);

		UserService userService = new UserService();
		User user = userService.getCurrentUser();

		TtlCleanup ttlCleanupService = new TtlCleanup(storage, AppConfig.CLEANUP_INTERVAL_MINUTES);
		ttlCleanupService.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ttlCleanupService.stop();
			userService.saveUser();
		}));

		CliCommands cli = new CliCommands(storage, redirectService, user);
		cli.run();
	}
}

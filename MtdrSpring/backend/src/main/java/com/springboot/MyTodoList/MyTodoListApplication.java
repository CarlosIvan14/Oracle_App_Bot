package com.springboot.MyTodoList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.springboot.MyTodoList.controller.ToDoItemBotController;
import com.springboot.MyTodoList.service.ProjectsServiceBot;
import com.springboot.MyTodoList.service.SprintsServiceBot;
import com.springboot.MyTodoList.service.TaskCreationServiceBot;
import com.springboot.MyTodoList.service.TaskServiceBot;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.UserRoleServiceBot;
import com.springboot.MyTodoList.util.BotMessages;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MyTodoListApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

	@Autowired
	private ProjectsServiceBot projectsServiceBot;

	@Autowired
	private SprintsServiceBot sprintsServiceBot;

	@Autowired
	private TaskServiceBot taskServiceBot;

	@Autowired
	private TaskCreationServiceBot taskCreationServiceBot;

	@Autowired
	private UserRoleServiceBot userRoleServiceBot;

	@Value("${telegram.bot.token}")
	private String telegramBotToken;

	@Value("${telegram.bot.name}")
	private String botName;

	public static void main(String[] args) {
		// Cargar variables de entorno desde el archivo .env ubicado en la raíz (misma carpeta que pom.xml)
        Dotenv dotenv = Dotenv.configure()
                              .directory("./")
                              .load();
        // Configurar la API key como propiedad del sistema para que Spring Boot la inyecte con @Value
        System.setProperty("openai.api.key", dotenv.get("OPENAI_API_KEY"));
        
		SpringApplication.run(MyTodoListApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(new ToDoItemBotController(telegramBotToken, botName, projectsServiceBot, sprintsServiceBot, taskServiceBot, taskCreationServiceBot, userRoleServiceBot));
			logger.info(BotMessages.BOT_REGISTERED_STARTED.getMessage());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
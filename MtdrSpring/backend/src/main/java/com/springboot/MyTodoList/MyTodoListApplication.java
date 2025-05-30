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
import com.springboot.MyTodoList.service.UserRoleServiceBot;
import com.springboot.MyTodoList.util.BotMessages;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class MyTodoListApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(MyTodoListApplication.class);

	/* ======== SERVICES ======== */
	@Autowired
	private ProjectsServiceBot projectsServiceBot;

	@Autowired
	private SprintsServiceBot sprintsServiceBot;

	/* -> NUEVO: URL del backend que usará el bot */
	/* (se lee de application.properties o variable de entorno) */

	private TaskServiceBot taskServiceBot;


	@Autowired
	private TaskCreationServiceBot taskCreationServiceBot;

	@Autowired
	private UserRoleServiceBot userRoleServiceBot;

	/* ======== CONFIG ======== */
	@Value("${telegram.bot.token}")
	private String telegramBotToken;

	@Value("${telegram.bot.name}")
	private String botName;

	/* -> NUEVO: URL del backend que usará el bot */
	/* (se lee de application.properties o variable de entorno) */
	@Value("${backend.base-url:http://localhost:8081}")
	private String backendBaseUrl;

	public static void main(String[] args) {

		/* 1) Cargar variables del archivo .env si existe */
		Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMalformed().ignoreIfMissing().load();

		/*
		 * 2) Pasar la API‑key de OpenAI como propiedad de sistema para que Spring la
		 * recoja con @Value
		 */
		String openAIKey = dotenv.get("OPENAI_API_KEY");
		if (openAIKey != null) {
			System.setProperty("openai.api.key", openAIKey);
		}

		SpringApplication.run(MyTodoListApplication.class, args);
	}

	@Override
	public void run(String... args) {

		try {
			/* Instanciar la API de Telegram */
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

			/* Registrar nuestro bot: <<—— SE AÑADE backendBaseUrl */
			botsApi.registerBot(new ToDoItemBotController(telegramBotToken, botName, backendBaseUrl, // <--
																										// nuevo
																										// parámetro
					projectsServiceBot, sprintsServiceBot, taskServiceBot, taskCreationServiceBot, userRoleServiceBot));

			logger.info(BotMessages.BOT_REGISTERED_STARTED.getMessage());

		}
		catch (TelegramApiException e) {
			logger.error("Error registrando el botdfsdf:", e);
		}
	}

}

// TasksService.java
package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TasksService {

	@Autowired
	private TasksRepository tasksRepository;

	public Tasks addTask(Tasks task) {
		return tasksRepository.save(task);
	}

	public List<Tasks> findAllTasks() {
		return tasksRepository.findAll();
	}

	public Optional<Tasks> getTaskById(int id) {
		return tasksRepository.findById(id);
	}

	public Tasks updateTask(int id, Tasks taskDetails) {
		return tasksRepository.findById(id).map(task -> {
			// Actualización total (PUT)
			task.setCreationTs(taskDetails.getCreationTs());
			task.setName(taskDetails.getName());
			task.setStatus(taskDetails.getStatus());
			task.setDescription(taskDetails.getDescription());
			task.setStoryPoints(taskDetails.getStoryPoints());
			task.setSprint(taskDetails.getSprint());
			task.setDeadline(taskDetails.getDeadline());
			task.setRealHours(taskDetails.getRealHours());
			task.setEstimatedHours(taskDetails.getEstimatedHours());
			return tasksRepository.save(task);
		}).orElse(null);
	}

	// Método para patch: actualiza sólo los campos enviados en el JSON
	public Tasks patchTask(int id, JsonNode taskUpdates) {
		return tasksRepository.findById(id).map(task -> {
			ObjectMapper mapper = new ObjectMapper();
			// Para evitar fallos si se envían propiedades desconocidas
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				// Fusiona (merge) los campos enviados en taskUpdates sobre el objeto task
				// existente
				mapper.readerForUpdating(task).readValue(taskUpdates);
			}
			catch (IOException e) {
				throw new RuntimeException("Error al actualizar la tarea", e);
			}
			return tasksRepository.save(task);
		}).orElse(null);
	}

	public boolean deleteTask(int id) {
		if (tasksRepository.existsById(id)) {
			tasksRepository.deleteById(id);
			return true;
		}
		return false;
	}

	// Método para obtener todas las tareas de un Sprint dado su id_sprint
	public List<Tasks> getTasksBySprint(int sprintId) {
		return tasksRepository.findBySprintId(sprintId);
	}

	// Get all unassigned tasks for a sprint
	public List<Tasks> getUnassignedTasksBySprint(int sprintId) {
		return tasksRepository.findUnassignedTasksBySprint(sprintId);
	}

}
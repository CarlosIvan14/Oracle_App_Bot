package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.Tasks;
import com.springboot.MyTodoList.repository.TaskAssigneesRepository;
import com.springboot.MyTodoList.repository.TasksRepository;
import com.springboot.MyTodoList.repository.ProjectUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TaskAssigneesService {

    @Autowired
    private TaskAssigneesRepository taskAssigneesRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    // Método para agregar una nueva asignación (valida que el Task y el ProjectUser existan)
    public TaskAssignees addTaskAssignee(TaskAssignees taskAssignee) {
        // Validar Task
        if (taskAssignee.getTask() != null && taskAssignee.getTask().getId() != 0) {
            Optional<Tasks> existingTask = tasksRepository.findById(taskAssignee.getTask().getId());
            if (existingTask.isPresent()) {
                taskAssignee.setTask(existingTask.get());
            } else {
                throw new IllegalArgumentException("Task with id " + taskAssignee.getTask().getId() + " not found.");
            }
        } else {
            throw new IllegalArgumentException("Task must be provided with a valid id.");
        }

        // Validar ProjectUser
        if (taskAssignee.getProjectUser() != null && taskAssignee.getProjectUser().getIdProjectUser() != 0) {
            Optional<ProjectUser> existingProjectUser = projectUserRepository.findById(taskAssignee.getProjectUser().getIdProjectUser());
            if (existingProjectUser.isPresent()) {
                taskAssignee.setProjectUser(existingProjectUser.get());
            } else {
                throw new IllegalArgumentException("ProjectUser with id " + taskAssignee.getProjectUser().getIdProjectUser() + " not found.");
            }
        } else {
            throw new IllegalArgumentException("ProjectUser must be provided with a valid id.");
        }

        return taskAssigneesRepository.save(taskAssignee);
    }

    public List<TaskAssignees> getAllTaskAssignees() {
        return taskAssigneesRepository.findAll();
    }

    public Optional<TaskAssignees> getTaskAssigneeById(int id) {
        return taskAssigneesRepository.findById(id);
    }

    public TaskAssignees updateTaskAssignee(int id, TaskAssignees taskAssigneeDetails) {
        return taskAssigneesRepository.findById(id).map(taskAssignee -> {
            // Actualizar ProjectUser si se envía uno nuevo
            if (taskAssigneeDetails.getProjectUser() != null && taskAssigneeDetails.getProjectUser().getIdProjectUser() != 0) {
                Optional<ProjectUser> existingProjectUser = projectUserRepository.findById(taskAssigneeDetails.getProjectUser().getIdProjectUser());
                existingProjectUser.ifPresent(taskAssignee::setProjectUser);
            }
            // Actualizar Task si se envía uno nuevo
            if (taskAssigneeDetails.getTask() != null && taskAssigneeDetails.getTask().getId() != 0) {
                Optional<Tasks> existingTask = tasksRepository.findById(taskAssigneeDetails.getTask().getId());
                existingTask.ifPresent(taskAssignee::setTask);
            }
            return taskAssigneesRepository.save(taskAssignee);
        }).orElse(null);
    }

    public boolean deleteTaskAssignee(int id) {
        if (taskAssigneesRepository.existsById(id)) {
            taskAssigneesRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Obtener todas las asignaciones para un usuario (OracleUser) dado su idUser
    public List<TaskAssignees> getTaskAssigneesByUser(int idUser) {
        return taskAssigneesRepository.findByProjectUserUserIdUser(idUser);
    }
    
    // Obtener asignaciones para un ProjectUser y un Sprint específico
    public List<TaskAssignees> getTaskAssigneesByUserAndSprint(int projectUserId, int sprintId) {
        return taskAssigneesRepository.findByProjectUserIdAndSprintId(projectUserId, sprintId);
    }
}
 
package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.TaskAssignees;
import com.springboot.MyTodoList.repository.TaskAssigneesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskAssigneesService {

    @Autowired
    private TaskAssigneesRepository taskAssigneesRepository;

    public TaskAssignees addTaskAssignee(TaskAssignees taskAssignee) {
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
            taskAssignee.setProjectUser(taskAssigneeDetails.getProjectUser());
            taskAssignee.setTask(taskAssigneeDetails.getTask());
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
}

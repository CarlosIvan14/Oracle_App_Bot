package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Sprint;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    @Autowired
    private SprintRepository sprintRepository;

    public Sprint addSprint(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public List<Sprint> findAllSprints() {
        return sprintRepository.findAll();
    }

    public Optional<Sprint> getSprintById(int id) {
        return sprintRepository.findById(id);
    }

    public Sprint updateSprint(int id, Sprint sprintDetails) {
        return sprintRepository.findById(id).map(sprint -> {
            sprint.setCreationTs(sprintDetails.getCreationTs());
            sprint.setDescription(sprintDetails.getDescription());
            sprint.setName(sprintDetails.getName());
            sprint.setProject(sprintDetails.getProject());
            sprint.setTasks(sprintDetails.getTasks());
            return sprintRepository.save(sprint);
        }).orElse(null);
    }

    public boolean deleteSprint(int id) {
        if (sprintRepository.existsById(id)) {
            sprintRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

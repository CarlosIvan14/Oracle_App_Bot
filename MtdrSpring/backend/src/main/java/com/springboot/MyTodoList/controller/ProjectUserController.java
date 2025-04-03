package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.service.ProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/project-users")
public class ProjectUserController {

    @Autowired
    private ProjectUserService projectUserService;

    // Crear un nuevo ProjectUser
    @PostMapping
    public ResponseEntity<ProjectUser> createProjectUser(@RequestBody ProjectUser projectUser) {
        ProjectUser createdProjectUser = projectUserService.addProjectUser(projectUser);
        return new ResponseEntity<>(createdProjectUser, HttpStatus.CREATED);
    }

    // Obtener todos los ProjectUser
    @GetMapping
    public ResponseEntity<List<ProjectUser>> getAllProjectUsers() {
        List<ProjectUser> projectUsers = projectUserService.getAllProjectUsers();
        return new ResponseEntity<>(projectUsers, HttpStatus.OK);
    }

    // Obtener un ProjectUser por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectUser> getProjectUserById(@PathVariable int id) {
        Optional<ProjectUser> projectUserOpt = projectUserService.getProjectUserById(id);
        return projectUserOpt.map(projectUser -> new ResponseEntity<>(projectUser, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar un ProjectUser
    @PutMapping("/{id}")
    public ResponseEntity<ProjectUser> updateProjectUser(@PathVariable int id, @RequestBody ProjectUser projectUserDetails) {
        ProjectUser updatedProjectUser = projectUserService.updateProjectUser(id, projectUserDetails);
        if (updatedProjectUser != null) {
            return new ResponseEntity<>(updatedProjectUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar un ProjectUser
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectUser(@PathVariable int id) {
        boolean deleted = projectUserService.deleteProjectUser(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/project/{projectId}/users")
    public ResponseEntity<List<OracleUser>> getUsersByProjectId(@PathVariable int projectId) {
        List<OracleUser> users = projectUserService.getUsersByProjectId(projectId);
        return ResponseEntity.ok(users);
    }
}

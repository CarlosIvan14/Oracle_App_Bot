package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.service.ProjectUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
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
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectUser> patchProjectUser(@PathVariable int id, @RequestBody Map<String, Object> updates) {
        ProjectUser patchedProjectUser = projectUserService.patchProjectUser(id, updates);
        if (patchedProjectUser != null) {
            return new ResponseEntity<>(patchedProjectUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

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
 // Obtener todos los usuarios que pertenecen de  un proyecto
    @GetMapping("/project/{projectId}/users")
    public ResponseEntity<List<OracleUser>> getUsersByProjectId(@PathVariable int projectId) {
        List<OracleUser> users = projectUserService.getUsersByProjectId(projectId);
        return ResponseEntity.ok(users);
    }
//Obtener todos los proyectos a los que pertenece un usuario
    @GetMapping("/user/{userId}/projects")
    public ResponseEntity<List<Projects>> getProjectsByUserId(@PathVariable int userId) {
        List<Projects> projects = projectUserService.getProjectsByUserId(userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("project-user-id/project-id/{projectId}/user-id/{userId}")
    public ResponseEntity<Integer> getProjectUserId(@PathVariable int userId, @PathVariable int projectId) {
        Integer id = projectUserService.getProjectUserIdByUserIdAndProjectId(userId, projectId);
        return id != null ? ResponseEntity.ok(id) : ResponseEntity.notFound().build();
    }

    @GetMapping("/role-user/project-id/{projectId}/user-id/{userId}")
    public ResponseEntity<String> getRoleUser(@PathVariable int userId, @PathVariable int projectId) {
        String roleUser = projectUserService.getRoleUserByUserIdAndProjectId(userId, projectId);
        return roleUser != null ? ResponseEntity.ok(roleUser) : ResponseEntity.notFound().build();
    }
}

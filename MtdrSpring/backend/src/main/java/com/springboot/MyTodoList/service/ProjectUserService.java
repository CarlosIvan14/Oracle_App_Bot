package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.OracleUser;
import com.springboot.MyTodoList.model.ProjectUser;
import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.repository.ProjectUserRepository;
import com.springboot.MyTodoList.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectUserService {

	@Autowired
	private ProjectUserRepository projectUserRepository;

	@Autowired
	private ProjectsRepository projectsRepository;

	// Obtener todos los ProjectUser
	public List<ProjectUser> getAllProjectUsers() {
		return projectUserRepository.findAll();
	}

	// Obtener un ProjectUser por ID
	public Optional<ProjectUser> getProjectUserById(int id) {
		return projectUserRepository.findById(id);
	}

	// Crear un nuevo ProjectUser
	public ProjectUser addProjectUser(ProjectUser projectUser) {
		// Verifica que la entidad Projects ya exista
		Projects proj = projectUser.getProject();
		if (proj != null && proj.getIdProject() != 0) {
			Projects managedProject = projectsRepository.findById(proj.getIdProject()).orElseThrow(
					() -> new IllegalArgumentException("Proyecto con id " + proj.getIdProject() + " no existe"));
			projectUser.setProject(managedProject);
		}
		else {
			throw new IllegalArgumentException("El proyecto es nulo o no tiene un ID válido");
		}
		return projectUserRepository.save(projectUser);
	}

	// Actualizar un ProjectUser existente
	public ProjectUser updateProjectUser(int id, ProjectUser projectUserDetails) {
		Optional<ProjectUser> projectUserOpt = projectUserRepository.findById(id);
		if (projectUserOpt.isPresent()) {
			ProjectUser projectUser = projectUserOpt.get();
			if (projectUserDetails.getUser() != null) {
				projectUser.setUser(projectUserDetails.getUser());
			}
			if (projectUserDetails.getProject() != null && projectUserDetails.getProject().getIdProject() != 0) {
				Projects managedProject = projectsRepository.findById(projectUserDetails.getProject().getIdProject())
						.orElseThrow(() -> new IllegalArgumentException(
								"Proyecto con id " + projectUserDetails.getProject().getIdProject() + " no existe"));
				projectUser.setProject(managedProject);
			}
			if (projectUserDetails.getRoleUser() != null) {
				projectUser.setRoleUser(projectUserDetails.getRoleUser());
			}
			if (projectUserDetails.getStatus() != null) {
				projectUser.setStatus(projectUserDetails.getStatus());
			}
			return projectUserRepository.save(projectUser);
		}
		return null;
	}

	public ProjectUser patchProjectUser(int id, Map<String, Object> updates) {
		Optional<ProjectUser> projectUserOpt = projectUserRepository.findById(id);
		if (projectUserOpt.isPresent()) {
			ProjectUser projectUser = projectUserOpt.get();

			// Actualizar el usuario si viene en el payload
			if (updates.containsKey("user")) {
				Map<String, Object> userMap = (Map<String, Object>) updates.get("user");
				if (userMap.containsKey("idUser")) {
					int userId = (Integer) userMap.get("idUser");
					// Si se requiere validar que el usuario existe, se puede hacer una
					// consulta adicional
					OracleUser user = new OracleUser();
					user.setIdUser(userId);
					projectUser.setUser(user);
				}
			}

			// Actualizar el proyecto si viene en el payload
			if (updates.containsKey("project")) {
				Map<String, Object> projectMap = (Map<String, Object>) updates.get("project");
				if (projectMap.containsKey("idProject")) {
					int projectId = (Integer) projectMap.get("idProject");
					Projects managedProject = projectsRepository.findById(projectId).orElseThrow(
							() -> new IllegalArgumentException("Proyecto con id " + projectId + " no existe"));
					projectUser.setProject(managedProject);
				}
			}

			// Actualizar roleUser
			if (updates.containsKey("roleUser")) {
				projectUser.setRoleUser((String) updates.get("roleUser"));
			}

			// Actualizar status
			if (updates.containsKey("status")) {
				projectUser.setStatus((String) updates.get("status"));
			}

			return projectUserRepository.save(projectUser);
		}
		return null;
	}

	// Eliminar un ProjectUser
	public boolean deleteProjectUser(int id) {
		Optional<ProjectUser> projectUserOpt = projectUserRepository.findById(id);
		if (projectUserOpt.isPresent()) {
			projectUserRepository.deleteById(id);
			return true;
		}
		return false;
	}

	public List<OracleUser> getUsersByProjectId(int projectId) {
		return projectUserRepository.findUsersByProjectId(projectId);
	}

	public List<Projects> getProjectsByUserId(int userId) {
		return projectUserRepository.findProjectsByUserId(userId);
	}

	public Integer getProjectUserIdByUserIdAndProjectId(int userId, int projectId) {
		return projectUserRepository.findProjectUserIdByUserIdAndProjectId(userId, projectId);
	}

	public String getRoleUserByUserIdAndProjectId(int userId, int projectId) {
		return projectUserRepository.findRoleUserByUserIdAndProjectId(userId, projectId);
	}

}

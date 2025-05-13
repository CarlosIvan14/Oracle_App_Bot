package com.springboot.MyTodoList.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.springboot.MyTodoList.model.Projects;
import com.springboot.MyTodoList.repository.ProjectsRepository;

public class ProjectServiceTest {

    @Mock
    private ProjectsRepository projectsRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddProject() {
        // Arrange
        Projects newProject = new Projects();
        newProject.setName("New Project");
        when(projectsRepository.save(any(Projects.class))).thenReturn(newProject);

        // Act
        Projects result = projectService.addProject(newProject);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "New Project");
        verify(projectsRepository).save(newProject);
    }

    @Test
    public void testFindAllProjects() {
        // Arrange
        Projects project1 = new Projects();
        project1.setName("Project 1");
        Projects project2 = new Projects();
        project2.setName("Project 2");
        List<Projects> expectedProjects = Arrays.asList(project1, project2);
        when(projectsRepository.findAll()).thenReturn(expectedProjects);

        // Act
        List<Projects> result = projectService.findAllProjects();

        // Assert
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getName(), "Project 1");
        assertEquals(result.get(1).getName(), "Project 2");
        verify(projectsRepository).findAll();
    }

    @Test
    public void testGetProjectByIdFound() {
        // Arrange
        Projects project = new Projects();
        project.setIdProject(1);
        project.setName("Existing Project");
        when(projectsRepository.findById(1)).thenReturn(Optional.of(project));

        // Act
        Optional<Projects> result = projectService.getProjectById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(result.get().getName(), "Existing Project");
        verify(projectsRepository).findById(1);
    }

    @Test
    public void testGetProjectByIdNotFound() {
        // Arrange
        when(projectsRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Optional<Projects> result = projectService.getProjectById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(projectsRepository).findById(999);
    }

    @Test
    public void testUpdateProjectSuccess() {
        // Arrange
        Projects existingProject = new Projects();
        existingProject.setIdProject(1);
        existingProject.setName("Old Name");
        existingProject.setDescription("Old Description");

        Projects projectDetails = new Projects();
        projectDetails.setName("New Name");
        projectDetails.setDescription("New Description");
        projectDetails.setCreationTs(LocalDateTime.now());
        projectDetails.setDeletedTs(LocalDateTime.now());

        when(projectsRepository.findById(1)).thenReturn(Optional.of(existingProject));
        when(projectsRepository.save(any(Projects.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Projects result = projectService.updateProject(1, projectDetails);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "New Name");
        assertEquals(result.getDescription(), "New Description");
        assertEquals(result.getCreationTs(), projectDetails.getCreationTs());
        assertEquals(result.getDeletedTs(), projectDetails.getDeletedTs());
        verify(projectsRepository).findById(1);
        verify(projectsRepository).save(existingProject);
    }

    @Test
    public void testUpdateProjectNotFound() {
        // Arrange
        when(projectsRepository.findById(999)).thenReturn(Optional.empty());
        Projects projectDetails = new Projects();

        // Act
        Projects result = projectService.updateProject(999, projectDetails);

        // Assert
        assertNull(result);
        verify(projectsRepository).findById(999);
    }

    @Test
    public void testDeleteProjectSuccess() {
        // Arrange
        when(projectsRepository.existsById(1)).thenReturn(true);

        // Act
        boolean result = projectService.deleteProject(1);

        // Assert
        assertTrue(result);
        verify(projectsRepository).deleteById(1);
    }

    @Test
    public void testDeleteProjectNotFound() {
        // Arrange
        when(projectsRepository.existsById(999)).thenReturn(false);

        // Act
        boolean result = projectService.deleteProject(999);

        // Assert
        assertFalse(result);
        verify(projectsRepository).existsById(999);
    }
}
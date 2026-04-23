package com.bezkoder.spring.thymeleaf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;
import com.bezkoder.spring.thymeleaf.repository.TutorialRepository;

@WebMvcTest(TutorialController.class)
class TutorialControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TutorialRepository tutorialRepository;

  @Test
  void getAll_withoutKeyword_loadsAllTutorials() throws Exception {
    Tutorial java = new Tutorial("Java", "Core Java", 1, true);
    Tutorial spring = new Tutorial("Spring", "Spring Boot", 2, false);

    when(tutorialRepository.findAll()).thenReturn(Arrays.asList(java, spring));

    mockMvc.perform(get("/tutorials"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutorials"))
        .andExpect(model().attributeExists("tutorials"));

    verify(tutorialRepository).findAll();
  }

  @Test
  void getAll_withKeyword_filtersTutorials() throws Exception {
    Tutorial spring = new Tutorial("Spring", "Spring Boot", 2, false);

    when(tutorialRepository.findByTitleContainingIgnoreCase("spring"))
        .thenReturn(Collections.singletonList(spring));

    mockMvc.perform(get("/tutorials").param("keyword", "spring"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutorials"))
        .andExpect(model().attribute("keyword", "spring"))
        .andExpect(model().attributeExists("tutorials"));

    verify(tutorialRepository).findByTitleContainingIgnoreCase("spring");
  }

  @Test
  void getAll_whenRepositoryFails_setsMessageOnModel() throws Exception {
    when(tutorialRepository.findAll()).thenThrow(new RuntimeException("db error"));

    mockMvc.perform(get("/tutorials"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutorials"))
        .andExpect(model().attributeExists("message"));
  }

  @Test
  void addTutorial_returnsFormWithPublishedDefaultTrue() throws Exception {
    mockMvc.perform(get("/tutorials/new"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutorial_form"))
        .andExpect(model().attributeExists("tutorial"))
        .andExpect(model().attribute("pageTitle", "Create new Tutorial"));
  }

  @Test
  void saveTutorial_persistsAndRedirectsWithSuccessFlashMessage() throws Exception {
    when(tutorialRepository.save(any(Tutorial.class))).thenAnswer(invocation -> invocation.getArgument(0));

    mockMvc.perform(post("/tutorials/save")
        .param("title", "Mockito")
        .param("description", "Testing")
        .param("level", "1")
        .param("published", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attributeExists("message"));

    verify(tutorialRepository).save(any(Tutorial.class));
  }

  @Test
  void saveTutorial_whenRepositoryFails_redirectsWithMessageAttribute() throws Exception {
    when(tutorialRepository.save(any(Tutorial.class))).thenThrow(new RuntimeException("save failed"));

    mockMvc.perform(post("/tutorials/save")
        .param("title", "Mockito")
        .param("description", "Testing")
        .param("level", "1")
        .param("published", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("/tutorials?message=*"));
  }

  @Test
  void editTutorial_withExistingId_returnsEditForm() throws Exception {
    Tutorial tutorial = new Tutorial("JUnit", "Testing", 2, false);
    tutorial.setId(100);

    when(tutorialRepository.findById(100)).thenReturn(Optional.of(tutorial));

    mockMvc.perform(get("/tutorials/100"))
        .andExpect(status().isOk())
        .andExpect(view().name("tutorial_form"))
        .andExpect(model().attributeExists("tutorial"))
        .andExpect(model().attribute("pageTitle", "Edit Tutorial (ID: 100)"));
  }

  @Test
  void editTutorial_withMissingId_redirectsAndSetsFlashMessage() throws Exception {
    when(tutorialRepository.findById(999)).thenReturn(Optional.empty());

    mockMvc.perform(get("/tutorials/999"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attributeExists("message"));
  }

  @Test
  void deleteTutorial_success_redirectsWithSuccessMessage() throws Exception {
    mockMvc.perform(get("/tutorials/delete/8"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attribute("message", "The Tutorial with id=8 has been deleted successfully!"));

    verify(tutorialRepository).deleteById(8);
  }

  @Test
  void deleteTutorial_whenRepositoryFails_redirectsWithFlashMessage() throws Exception {
    doThrow(new RuntimeException("delete failed")).when(tutorialRepository).deleteById(5);

    mockMvc.perform(get("/tutorials/delete/5"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attributeExists("message"));

    verify(tutorialRepository).deleteById(5);
  }

  @Test
  void updateTutorialPublishedStatus_true_redirectsWithPublishedMessage() throws Exception {
    mockMvc.perform(get("/tutorials/7/published/true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attribute("message", "The Tutorial id=7 has been published"));

    verify(tutorialRepository).updatePublishedStatus(7, true);
  }

  @Test
  void updateTutorialPublishedStatus_false_redirectsWithDisabledMessage() throws Exception {
    mockMvc.perform(get("/tutorials/7/published/false"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attribute("message", "The Tutorial id=7 has been disabled"));

    verify(tutorialRepository).updatePublishedStatus(7, false);
  }

  @Test
  void updateTutorialPublishedStatus_whenRepositoryFails_setsErrorFlashMessage() throws Exception {
    doThrow(new RuntimeException("update failed")).when(tutorialRepository).updatePublishedStatus(11, true);

    mockMvc.perform(get("/tutorials/11/published/true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tutorials"))
        .andExpect(flash().attributeExists("message"));
  }
}

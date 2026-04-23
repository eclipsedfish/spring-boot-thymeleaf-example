package com.bezkoder.spring.thymeleaf.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;

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
}

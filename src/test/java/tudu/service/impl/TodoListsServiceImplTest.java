package tudu.service.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import tudu.domain.Todo;
import tudu.domain.TodoList;

import javax.persistence.EntityManager;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoListsServiceImplTest {

    @Mock private EntityManager em;

    @Spy @InjectMocks private TodoListsServiceImpl todoListsService = new TodoListsServiceImpl();

    @Captor ArgumentCaptor<String> idCaptor;

    @Before
    public void init_partial_mock_for_unit_test___REMEMBER_dont_design_code_like_that() throws Exception {
        // mock partiel, toujours mettre le willReturn/thenReturn avant le given/when
        willReturn(todoListWithTodoIds("a", "b", "c", "d")).given(todoListsService).findTodoList(anyString());
    }

    private TodoList todoListWithTodoIds(String... todoIds) {
        TodoList todoList = new TodoList();
        for (String todoId : todoIds) {
            todoList.getTodos().add(todoWithId(todoId));
        }
        return todoList;
    }

    private Todo todoWithId(String todoId) {
        Todo todo = new Todo();
        todo.setTodoId(todoId);
        return todo;
    }

    @Test
    public void just_make_things_work_for_deleteTodoList() throws Exception {
        todoListsService.deleteTodoList("some list");
    }

    @Test
    public void deleteTodoList_should_remove_each_Todo() throws Exception {
        // given
        willReturn(todoListWithTodoIds("mint", "rum", "sugar", "lime juice", "club soda")).given(todoListsService).findTodoList(anyString());

        // when
        todoListsService.deleteTodoList("groceries");

        // then
        verify(em).remove("mint");
        verify(em).remove("rum");
        verify(em).remove("sugar");
        verify(em).remove("lime juice");
        verify(em).remove("club soda");
        verify(em).remove("groceries");

        // ou avec un captor et fest-assert
        verify(em, times(6)).remove(idCaptor.capture());
        assertThat(idCaptor.getAllValues()).contains("groceries", "rum", "sugar", "mint", "lime juice", "club soda");
    }

    @Test
    public void entityManager_raises_some_exception() throws Exception {
        // given
        willReturn(todoListWithTodoIds("mint", "rum", "sugar", "lime juice", "club soda")).given(todoListsService).findTodoList(anyString());
        willDoNothing().willNothing().willThrow(new NotPossible()).given(em).remove(Matchers.<Object>any());

        // when
        try {
            todoListsService.deleteTodoList("groceries");
        } catch (NotPossible np) {
            // then
            verify(em, times(3)).remove(any());
            verifyNoMoreInteractions(em);
        }
    }

    private class NotPossible extends RuntimeException {
    }
}

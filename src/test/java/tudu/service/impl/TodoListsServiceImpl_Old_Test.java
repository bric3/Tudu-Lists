package tudu.service.impl;

import org.easymock.EasyMock;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tudu.domain.Todo;
import tudu.domain.TodoList;
import tudu.domain.User;
import tudu.security.PermissionDeniedException;
import tudu.service.UserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class TodoListsServiceImpl_Old_Test {

    static String todoListBackup = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<todolist>" + " <title>test list</title>" + " <rss>true</rss>"
            + " <users>" + "  <user>test</user>" + " </users>" + " <todos>"
            + "  <todo id=\"0001\">"
            + "   <creationDate>1127860040000</creationDate>"
            + "   <description>test todo</description>"
            + "   <priority>10</priority>" + "   <completed>false</completed>"
            + "  </todo>" + " </todos>" + "</todolist>";

    TodoList todoList = new TodoList();
    User user = new User();

    UserService userService = null;

    TodoListsServiceImpl todoListsService = new TodoListsServiceImpl();

    @Before
    public void before() {
        todoList.setListId("001");
        todoList.setName("Test Todo List");
        todoList.setRssAllowed(false);

        user.setLogin("test_user");
        user.setFirstName("First name");
        user.setLastName("Last name");

        userService = createMock(UserService.class);

        ReflectionTestUtils.setField(todoListsService, "userService", userService);
    }

    @After
    public void after() {
        verify(userService);
    }

    private void replay() {
        EasyMock.replay(userService);
    }

    @Test
    public void testCreateTodoList() {

        expect(userService.getCurrentUser()).andReturn(user);

        replay();

        todoListsService.createTodoList(todoList);

        assertTrue(user.getTodoLists().contains(todoList));
    }

    @Test
    public void testFindTodoList() {
        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);

        expect(userService.getCurrentUser()).andReturn(user);

        replay();
        try {
            TodoList testTodoList = todoListsService.findTodoList("001");
            assertEquals(todoList, testTodoList);
        } catch (PermissionDeniedException pde) {
            fail("Permission denied when looking for Todo.");
        }
    }

    @Test
    public void testFailedFindTodoList() {
        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);

        expect(userService.getCurrentUser()).andReturn(user);

        replay();

        try {
            todoListsService.findTodoList("001");
            fail("A PermissionDeniedException should have been thrown");
        } catch (PermissionDeniedException pde) {

        }
    }

    @Test
    public void testUpdateTodoList() {
        //todoListDAO.updateTodoList(todoList);

        replay();

        todoListsService.updateTodoList(todoList);
    }

    @Test
    public void testDeleteTodoList() {
        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        expect(userService.getCurrentUser()).andReturn(user);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);

        userService.updateUser(user);

        //todoListDAO.removeTodoList("001");

        replay();

        todoListsService.deleteTodoList("001");

        assertFalse(user.getTodoLists().contains(todoList));
    }

    @Test
    public void testAddTodoListUser() {
        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        expect(userService.getCurrentUser()).andReturn(user);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);

        User user2 = new User();
        user2.setLogin("another_user");
        expect(userService.findUser("another_user")).andReturn(user2);

        //todoListDAO.updateTodoList(todoList);

        replay();

        todoListsService.addTodoListUser("001", "another_user");

        assertTrue(todoList.getUsers().contains(user2));
        assertTrue(user2.getTodoLists().contains(todoList));
    }

    @Test
    public void testDeleteTodoListUser() {
        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        User user2 = new User();
        user2.setLogin("another_user");
        user2.getTodoLists().add(todoList);
        todoList.getUsers().add(user2);

        expect(userService.getCurrentUser()).andReturn(user);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);

        expect(userService.findUser("another_user")).andReturn(user2);

        //todoListDAO.updateTodoList(todoList);

        replay();

        todoListsService.deleteTodoListUser("001", "another_user");

        assertFalse(todoList.getUsers().contains(user2));
        assertFalse(user2.getTodoLists().contains(todoList));
    }

    @Test
    public void testBackupTodoList() {
        todoList.getUsers().add(user);

        Todo todo = new Todo();
        todo.setTodoId("0001");
        Calendar creationCal = Calendar.getInstance();
        creationCal.clear();
        creationCal.set(Calendar.YEAR, 2005);
        todo.setCreationDate(creationCal.getTime());
        todo.setDescription("Backup Test description");
        todo.setPriority(0);
        todo.setCompleted(false);

        todoList.getTodos().add(todo);

        replay();

        Document doc = todoListsService.backupTodoList(todoList);

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlContent = outputter.outputString(doc);

        assertTrue(xmlContent.indexOf("<title>Test Todo List</title>") > 0);
        assertTrue(xmlContent.indexOf("<todo id=\"0001\">") > 0);
        assertTrue(xmlContent.indexOf("<creationDate>"
                + creationCal.getTimeInMillis() + "</creationDate>") > 0);
        assertTrue(xmlContent
                .indexOf("<description>Backup Test description</description>") > 0);
        assertTrue(xmlContent.indexOf("<priority>0</priority>") > 0);
        assertTrue(xmlContent.indexOf("<completed>false</completed>") > 0);
    }

    @Test
    public void testRestoreTodoListCreate() throws Exception {
        InputStream content = new ByteArrayInputStream(todoListBackup
                .getBytes());

        expect(userService.getCurrentUser()).andReturn(user);
        TodoList todoList = new TodoList();
        //todoListDAO.saveTodoList(todoList);
        userService.updateUser(user);
        Todo todo = new Todo();
        //todoDAO.saveTodo(todo);

        replay();

        todoListsService.restoreTodoList("create", "001", content);
    }

    @Test
    public void testRestoreTodoListReplace() throws Exception {
        InputStream content = new ByteArrayInputStream(todoListBackup
                .getBytes());

        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        Todo todo = new Todo();
        todo.setTodoId("0001");
        todo.setTodoList(todoList);
        todoList.getTodos().add(todo);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);
        expect(userService.getCurrentUser()).andReturn(user);
        //todoDAO.removeTodo("0001");
        //todoListDAO.updateTodoList(todoList);
        Todo createdTodo = new Todo();
        //todoDAO.saveTodo(createdTodo);

        replay();

        todoListsService.restoreTodoList("replace", "001", content);
    }

    @Test
    public void testRestoreTodoListMerge() throws Exception {
        InputStream content = new ByteArrayInputStream(todoListBackup
                .getBytes());

        todoList.getUsers().add(user);
        user.getTodoLists().add(todoList);

        //expect(todoListDAO.getTodoList("001")).andReturn(todoList);
        expect(userService.getCurrentUser()).andReturn(user);

        Todo createdTodo = new Todo();
        //todoDAO.saveTodo(createdTodo);

        //todoListDAO.updateTodoList(todoList);

        replay();

        todoListsService.restoreTodoList("merge", "001", content);

        assertNotNull(todoList.getLastUpdate());
    }
}

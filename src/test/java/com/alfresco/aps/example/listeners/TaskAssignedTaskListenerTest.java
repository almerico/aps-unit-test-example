package com.alfresco.aps.example.listeners;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TaskAssignedTaskListenerTest {

	@Configuration
	static class ContextConfiguration {
		@Bean
		public TaskAssignedTaskListener taskAssignedTaskListener() {
			return new TaskAssignedTaskListener();
		}
	}

	@InjectMocks
	@Spy
	private static TaskAssignedTaskListener taskAssignedTaskListener;

	@Mock
	private DelegateTask task;

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/*
	 * Testing TaskAssignedTaskListener.notify(DelegateTask task) method using a
	 * mock DelegateTask created using Mockito library
	 */
	@Test
	public void test() throws Exception {

		/*
		 * Creating a map which will be used during the
		 * DelegateTask.getVariable() & DelegateTask.setVariable() calls from
		 * TaskAssignedTaskListener as well as from this test
		 */
		Map<String, Object> variableMap = new HashMap<String, Object>();

		/*
		 * Stub a DelegateTask.setVariable() call
		 */
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arg = invocation.getArguments();
				variableMap.put((String) arg[0], arg[1]);
				return null;
			}
		}).when(task).setVariable(anyString(), any());

		/*
		 * Stub a DelegateTask.getVariable() call
		 */
		when(task.getVariable(anyString())).thenAnswer(new Answer<String>() {
			public String answer(InvocationOnMock invocation) {
				return (String) variableMap.get(invocation.getArguments()[0]);
			}
		});
		
		/*
		 * Start the test by invoking the method on task listener
		 */
		taskAssignedTaskListener.notify(task);
		
		/*
		 * sample assertion to make sure that the java code is setting correct
		 * value
		 */
		assertThat(task.getVariable("oddOrEven")).isNotNull().isIn("ODDDATE", "EVENDATE");
	}

}
